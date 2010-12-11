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

/**
 *  To much static text in here, move to props!!!!! 
 */
trait LDAPAuth extends Loggable {
  private val env = new Hashtable[String,String]
  private val userhome = System.getProperty("user.dir")
  System.setProperty("javax.net.ssl.trustStore", userhome + "/fhstore")
  
  /**
   * Trying to get auth from the LDAP
   * @param userName the login
   * @param passWord the password
   * @return Boolean
   */
  def tryLogin(userName: String, passWord: String): Boolean = {
    logger info userName + " is trying to log in!"
    var base = ""
    if (userName.equals("denison")) base = "ou=students,dc=fh-sm,dc=de"
    else base = "ou=people,dc=fh-sm,dc=de"
    val dn = "uid=" + userName + "," + base
        // ou=people benoetigt fuer professoren
        // ou=students nur studenten
        // val base ="ou=people,dc=fh-sm,dc=de"
    	  // since we want to use SSL we have to go to port 636 on the LDAP server
    val ldapURL = "ldaps://ldap1.fh-schmalkalden.de:636"

    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
    env.put(Context.PROVIDER_URL, ldapURL)
   	env.put(Context.SECURITY_AUTHENTICATION, "simple")
   	env.put(Context.SECURITY_PRINCIPAL, dn)
   	env.put(Context.SECURITY_CREDENTIALS, passWord)
    env.put(Context.SECURITY_PROTOCOL, "SSL")
        
    try {
      val ctx: DirContext = new InitialDirContext(env)
      S.setSessionAttribute("fullname", getFullname(ctx, dn))
      S.setSessionAttribute("email", emailValidator(getEmail(ctx, dn), userName))
      logger info userName + " logged in successfully!"
      true
    } catch {
      case e: AuthenticationException => e
        logger error e.printStackTrace.toString
        S error "Error: Bitte richtige FHS-ID und Passwort angeben"
        S redirectTo "/user_mgt/login"
        false
      case b: NamingException => b
        logger error b.printStackTrace.toString
        S error "Error: I can't see LDAP, please contact a SPIRIT-Admin"
        S redirectTo "/user_mgt/login"
        false
      case c: TimeLimitExceededException => c
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
    case "" => "spirit@informatik.fh-schmalkalden.de"
    case _ => email
  }

  /**
   * looks up the email from the given user
   * @param ctx the Directory Context
   * @param dn the username
   * @return String either email or empty String
   */
  private def getEmail(ctx: DirContext, dn: String): String = {
    val attrs: Attributes = ctx.getAttributes(dn)
    val idEnum = attrs.getIDs
    var email = ""
    idEnum foreach { x =>
      if(x == "mail" && attrs.get(x).get(0).toString.matches("[a-zA-Z][.].\\w.*@fh-sm.de") || attrs.get(x).get(0).toString.matches("[a-zA-Z][.].\\w.*@stud.fh-sm.de")) {
          email = attrs.get(x).get(0).toString
      }
    }
    email
  }

  /**
   * @param ctx the Directory Context
   * @param dn the username
   * @return String built with title and last name
   * @TODO Employees don't have a personalTitle in the LDAP Directory, this SUCKS !!!!! 
   */
  private def getFullname(ctx: DirContext, dn: String): String = {
    val attrs: Attributes = ctx.getAttributes(dn)
    val idEnum = attrs.getIDs
    var title_name = ""
    idEnum foreach { x =>
      if (x == "sn") title_name = attrs.get(x).get(0).toString
      if (x == "personalTitle" && attrs.get(x).get(0).toString != "") title_name = attrs.get(x).get(0).toString + " " + title_name
    }
    title_name
  }
}
