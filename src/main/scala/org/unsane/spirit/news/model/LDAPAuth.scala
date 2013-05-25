/**
 * Copyright (c) 2010 spirit-fhs
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of his contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHORS ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.unsane.spirit.news
package model

import javax.naming._
import javax.naming.directory._
import java.util._
import net.liftweb.http.S
import net.liftweb.common.Loggable
import scala.collection.JavaConversions._

trait LDAPAuth extends Loggable with Config {
  private val useLDAPAuth = (loadProps("Productive") == "yes" || loadProps("UseLDAPAuth") == "yes")
  private val env = new Hashtable[String,String]

  def tryLogin(userName: String, passWord: String): Boolean = {
    // allow additional users in setting.properties
    val additionalUsers = loadProps("users").split(';').map{_.trim}
    if (additionalUsers contains userName) {
      logger info "found "+userName+" in additional users."
      val userInfo = loadProps(userName).split(';').map{_.trim}
      if (userInfo.length >= 3) {
        if (userInfo(2) == md5SumString(passWord)) {
          S.setSessionAttribute("fullname", userInfo(0))
          S.setSessionAttribute("email", userInfo(1))
          logger info "password is fine for "+userInfo(0)+
                " <"+userInfo(1)+">"
          return true
        }
      }
    }
    if (useLDAPAuth) {
      tryLoginLDAP(userName, passWord)
    }
    else {
      S.setSessionAttribute("fullname", userName)
      S.setSessionAttribute("email", "testuser@nonvalid")
      true
    }
  }

  /**
   * Trying to get auth from the LDAP
   * @param userName the login
   * @param passWord the password
   * @return Boolean
   */
  def tryLoginLDAP(
    userName: String,
     passWord: String
  ): Boolean = {
    logger warn userName + " is trying to log into zefi!"
    val ldapURL = "ldaps://zefi.fh-schmalkalden.de:636"
    val dn =  "uid="+userName+",ou=people,ou=in,dc=fh-schmalkalden,dc=de"

    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    env.put(Context.PROVIDER_URL, ldapURL)
    env.put(Context.SECURITY_AUTHENTICATION, "simple")
    env.put(Context.SECURITY_PRINCIPAL, dn)
    env.put(Context.SECURITY_CREDENTIALS, passWord)
    env.put(Context.SECURITY_PROTOCOL, "SSL")

    try {

      val ctx: DirContext = new InitialDirContext(env)
      val attrs: Attributes = ctx.getAttributes(dn)
      val gidNumber = attrs.get("gidNumber").get(0)
        // only staff can log in
      if (gidNumber != "1001" && !allowedStudents.contains(userName)) {
        S error "Students may not log in. Sorry!"
        S redirectTo "/user_mgt/login"
        return false
      }
      S.setSessionAttribute("fullname", getFullname(attrs))
      S.setSessionAttribute("email", emailValidator(getEmail(attrs), userName, gidNumber.toString))
      logger info userName + " logged in successfully!"
      true
    } catch {
      case e: AuthenticationException =>
          logger error e.printStackTrace.toString
          S error "Error: Bitte richtige FHS-ID und Passwort angeben"
          S redirectTo "/user_mgt/login"
          false
      case b: NamingException =>
        logger error b.printStackTrace.toString
        logger error b.getExplanation
        logger error b.getRootCause.getMessage
        S error "Oops, no LDAP? Something went wrong. Please tell someone in charge."
        S redirectTo "/user_mgt/login"
        false
      case c: TimeLimitExceededException =>
        logger error c.printStackTrace.toString
        S error "Waiting for LDAP to long! Please tell someone in charge."
        S redirectTo "/user_mgt/login"
        false
      case _ =>
        false
    }
  }

  /**
   * Checks if we have to build "fhsid@fh-sm.de" or if there was found an _.____@fh-sm.de
   * @todo If the String is emtpy, we need to return an not-valid String in order to disable
   * the email function. How should this be solved?
   * This is only a quickfix, should be fixed a more proper way in the future!!!!!
   */
  private def emailValidator(email: String, userName:String, gidNumber: String): String = (email, gidNumber) match {
    case ("", "1002") => userName + "@stud.fh-sm.de"
    case ("", "1001") => userName + "@fh-sm.de"
    case ("", _)      => "not-valid"
    case _            => email
  }

  /**
   * looks up the email from the given user, there is no email in zefi
   * @param attrs the attributes
   * @return String either email or empty String
   */
  private def getEmail(attrs: Attributes): String = {
    val ids = attrs.getIDs.toList
    def getAttrValList(id: String): List[String] =
      if (ids contains id)
        for (i <- 0 to attrs.get(id).size - 1)
          yield attrs.get(id).get(i).toString
      else
        Nil: List[String]
    val emails = getAttrValList("mail") map { _.toString } filter {
      email => email.matches("[a-zA-Z][.].\\w.*@fh-sm.de") ||
               email.matches("[a-zA-Z][.].\\w.*@stud.fh-sm.de")
    }
    if (emails isEmpty) "" else emails.head
  }

  /**
   * @param attrs the attributes
   * @return String built with title and last name
   */
  private def getFullname(attrs: Attributes): String = {
    val ids = attrs.getIDs.toList
    def getAttrVal(id: String) = if (ids contains id) attrs.get(id).get(0).toString else ""
    getAttrVal("displayName")
  }
}
