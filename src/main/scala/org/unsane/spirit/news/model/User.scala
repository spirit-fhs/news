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

import net.liftweb.mapper._
import net.liftweb.sitemap._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.sitemap.Loc.Hidden

class User extends MegaProtoUser[User] {
  def getSingleton = User
}

object User extends User with MetaMegaProtoUser[User] with LDAPAuth {

  import SiteMap._

 /**
  * Setting enforceUniqueLinks to false will let use set multiple links.
  * We need this to force Users from http -> https login Page.
  *
  */
  enforceUniqueLinks = false

  override def loginMenuLoc: Box[Menu] =
    Full(Menu(Loc("Login", loginPath, S.??("login"), Hidden :: loginMenuLocParams)))

  override def menus: List[Menu] = sitemap
  override lazy val sitemap: List[Menu] = List(loginMenuLoc, logoutMenuLoc).flatten(a => a)
  override def loginXhtml = {
    (<lift:surround with="default" at ="content">

      <h3>{"Derzeit ist der Login nur für Mitarbeiter!"}</h3>
      <form method="post" action={S.uri}>
      <table>
        <tr><td colspan="2">{S.??("log.in")}</td></tr>
        <tr><td>{S.??("FHS-ID")}</td><td><user:user /></td></tr>
        <tr><td>{S.??("password")}</td><td><user:password /></td></tr>
            <td><user:submit /></td>
      </table>
      </form>
     </lift:surround>)
  }
  /**
   * Overriding login here is necessary because we need to Auth against fHS LDAP.
   * @todo Is this a proper solution?
   */
  override def login = {
    if (S.post_?) {
      if (S.param("username").open_!.equals("") || S.param("password").open_!.equals("")) {
        S.error("Errorcode: Bitte User und Pass angeben")
        S.redirectTo("/user_mgt/login")
      }
      if (tryLogin(S.param("username").open_!,S.param("password").open_!)) {
        User.logUserIdIn(S.param("username").open_!)
        S notice "Login Successful as " + User.currentUserId.open_!
        S redirectTo "/index"
      } else { }
    }

    bind("user", loginXhtml,
      "user" -> ((<input type="text" name="username"/>)),
      "password" -> (<input type="password" name="password"/>),
      "submit" -> (<input type="submit" value={S.??("log.in")}/>))
  }
}
