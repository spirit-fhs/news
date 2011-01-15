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
  private val useLDAPAuth =
    loadProps("Productive") == "yes" ||
      loadProps("UseLDAPAuth") == "yes"
  private val env = new Hashtable[String,String]
  private val userhome = System.getProperty("user.dir")


  def tryLogin(userName: String, passWord: String) =
    if (useLDAPAuth) tryLoginLDAP(userName, passWord)
    else {
      S.setSessionAttribute("fullname", userName)
      S.setSessionAttribute("email", "testuser@nonvalid")
      true
    }

  /**
   * Trying to get auth from the LDAP
   * @param userName the login
   * @param passWord the password
   * @param ldapServer the ldapServer, should be ldap1 (default param) or zefi
   * @return Boolean
   */
  def tryLoginLDAP(
    userName: String,
     passWord: String,
     ldapServer: String = "ldap1"
  ): Boolean = {
    logger info userName + " is trying to log into "+ldapServer+"!"
    val (ldapURL, dn) =
      if (ldapServer == "ldap1") {
        ("ldaps://ldap1.fh-schmalkalden.de:636"
        ,"uid=" + userName + "," +
         (if (userName.equals("denison")) "ou=students,dc=fh-sm,dc=de"
          else "ou=people,dc=fh-sm,dc=de"))
      } else if (ldapServer == "zefi") {
        ("ldaps://zefi.fh-schmalkalden.de:636"
         ,"uid="+userName+",ou=people,ou=in,dc=fh-schmalkalden,dc=de")
      } else {
        return false
      }

    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    env.put(Context.PROVIDER_URL, ldapURL)
    env.put(Context.SECURITY_AUTHENTICATION, "simple")
    env.put(Context.SECURITY_PRINCIPAL, dn)
    env.put(Context.SECURITY_CREDENTIALS, passWord)
    env.put(Context.SECURITY_PROTOCOL, "SSL")

    try {
      System.setProperty("javax.net.ssl.trustStore", userhome + "/sslstore")
      val ctx: DirContext = new InitialDirContext(env)
      if (ldapServer == "zefi") {
        val attrs: Attributes = ctx.getAttributes(dn)
        val gidNumber = attrs.get("gidNumber").get(0)
        // only staff can log in
        if (gidNumber != "1001") return false
      }
      val attrs: Attributes = ctx.getAttributes(dn)
      S.setSessionAttribute("fullname", getFullname(attrs, ldapServer))
      S.setSessionAttribute("email", emailValidator(getEmail(attrs), userName))
      logger info userName + " logged in successfully!"
      true
    } catch {
      case e: AuthenticationException =>
        if (ldapServer == "ldap1") {
          tryLoginLDAP(userName, passWord, "zefi")
        }
        else {
          logger error e.printStackTrace.toString
          S error "Error: Bitte richtige FHS-ID und Passwort angeben"
          S redirectTo "/user_mgt/login"
          false
        }
      case b: NamingException =>
        logger error b.printStackTrace.toString
        logger error b.getExplanation
        logger error b.getRootCause.getMessage
        S error "Error: I can't see LDAP, please contact a SPIRIT-Admin"
        S redirectTo "/user_mgt/login"
        false
      case c: TimeLimitExceededException =>
        logger error c.printStackTrace.toString
        S error "Error: LDAP is taking long, please contact a SPIRIT-Admin"
        S redirectTo "/user_mgt/login"
        false
      case _ =>
        false
    }
  }

  /**
   * Checks if we have to build "fhsid@fh-sm.de" or if there was found an _.____@fh-sm.de
   */
  private def emailValidator(email: String, userName:String): String = email match {
    case "" => userName+"@fh-sm.de" // does not work for students
    case _ => email
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
   * @TODO Employees don't have a personalTitle in the LDAP Directory
   */
  private def getFullname(attrs: Attributes, ldapServer: String): String = {
    val ids = attrs.getIDs.toList
    def getAttrVal(id: String) =
      if (ids contains id) attrs.get(id).get(0).toString else ""
    if (ldapServer == "ldap1")
      (getAttrVal("personalTitle") + " " + getAttrVal("sn")).trim
    else // zefi
      getAttrVal("displayName")
  }

}
