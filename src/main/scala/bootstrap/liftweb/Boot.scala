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
package bootstrap.liftweb

import org.unsane.spirit.news._
import model._
import lib._
import fun._
import rest.RestApi
import snippet.Feed

import net.liftweb._
import http._
import auth.{AuthRole, userRoles, HttpBasicAuthentication}
import sitemap._
import Loc._

import common._
import util._
import util.Helpers._
import mongodb._

/**
 * @todo Please refactor me!
 */
class Boot extends Loggable with Config {
  def boot {


    // Protecting file upload for schedule
    val userhome = System.getProperty("user.dir")
    System.setProperty("javax.net.ssl.trustStore", userhome + "/sslstore")
    val roles = AuthRole("Upload")

    LiftRules.httpAuthProtectedResource.append {
      case Req("scheduleapi" :: "fileupload" :: _, _, PutRequest) =>
        roles.getRoleByName("Upload")
    }

    val jsonUser = loadProps("jsonUser")
    val jsonPassword = loadProps("jsonPassword")
    logger info (jsonUser +":"+ jsonPassword)

    LiftRules.authentication = HttpBasicAuthentication("org.unsane.spirit") {
     case (user, pw, req) =>
      if (user == jsonUser && md5SumString(pw) == jsonPassword) {
        userRoles(AuthRole("Upload"))
        true
      } else {
        false
      }

    }

    val productive = loadProps("Productive") == "yes"
    val tweet = loadProps("Tweet") == "yes"
    val showschedule = loadProps("ShowSchedule") == "yes"
    val scheduleAdmins = loadProps("scheduleAdmins").split(";")

    // Opens connection to MongoDB with user/pass "spirit_news"
    MongoDB.defineDbAuth(DefaultMongoIdentifier,
      MongoAddress(MongoHost("127.0.0.1", 27017), "spirit_news"),
      "spirit_news",
      "spirit_news")

    LiftRules.addToPackages("org.unsane.spirit.news")

    LiftRules.dispatch.prepend(NamedPF("Login Validation") {
      case Req("login_required" :: page , extension, _)
        if (!LoginUtil.isLogged) =>
          () => Full(RedirectResponse("success"))
    })

    val loggedIn = If(() => User.loggedIn_?, () => RedirectResponse("/index"))
    val loggedOut = If(() => User.notLoggedIn_?, () => RedirectResponse("/index"))
    val onlyWS = If(() => loadProps("Semester") == "WS", () => RedirectResponse("/index"))
    val adminLoggedIn = If(() => scheduleAdmins contains User.currentUserId.openOr(""), () => RedirectResponse("/index"))

    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) =>
        NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })

    LiftRules.statefulRewrite.append {
      case RewriteRequest(ParsePath("entry" :: entrynr :: Nil,_,_,_),_,_) =>
        RewriteResponse("index" :: Nil, Map("search" -> entrynr))
    }

    LiftRules.statefulRewrite.append {
      case RewriteRequest(ParsePath("semsearch" :: semsearch :: Nil,_,_,_),_,_) =>
        RewriteResponse("index" :: Nil, Map("search" -> semsearch))
    }

    LiftRules.statelessDispatchTable.append(RestApi)

    val schedule_i = loadSchedule("I")
    val schedule_wi = loadSchedule("WI")
    val schedule_mc = loadSchedule("MC")
    val schedule_its = loadSchedule("ITS")
    val schedule_muma = loadSchedule("MUMA")
    val schedule_ma = loadSchedule("MA")

    // TODO Build Menu some different way, a cleaner way!?

    lazy val schedule: Menu = {
          if(showschedule) {
            Menu(Loc("StundenplanOld", List("stundenplan", "index"), "Stundenplan", Hidden),
              Menu(Loc("Informatik", List("stundenplan", "BaI" + loadProps("Semester")), "BaI"),
                Menu(Loc("Informatik_1", List("stundenplan", schedule_i(0)), schedule_i(0), LocGroup("Informatik"))),
                Menu(Loc("Informatik_2", List("stundenplan", schedule_i(1)), schedule_i(1), LocGroup("Informatik"))),
                Menu(Loc("Informatik_3", List("stundenplan", schedule_i(2)), schedule_i(2), LocGroup("Informatik")))),                
                Menu(Loc("MobileComp", List("stundenplan", "BaMC" + loadProps("Semester")), "BaMC"),
                Menu(Loc("MobileComp_1", List("stundenplan", schedule_mc(0)), schedule_mc(0), LocGroup("MobileComp"))),
                Menu(Loc("MobileComp_2", List("stundenplan", schedule_mc(1)), schedule_mc(1), LocGroup("MobileComp"))),
                Menu(Loc("MobileComp_3", List("stundenplan", schedule_mc(2)), schedule_mc(2), LocGroup("MobileComp")))),                
              Menu(Loc("WInformatik", List("stundenplan", "BaWI" + loadProps("Semester")), "BaWI"),
                Menu(Loc("WInformatik_1", List("stundenplan", schedule_wi(0)), schedule_wi(0), LocGroup("WInformatik"))),
                Menu(Loc("WInformatik_2", List("stundenplan", schedule_wi(1)), schedule_wi(1), LocGroup("WInformatik"))),
                Menu(Loc("WInformatik_3", List("stundenplan", schedule_wi(2)), schedule_wi(2), LocGroup("WInformatik")))),
              Menu(Loc("Muma", List("stundenplan", "BaMM" + loadProps("Semester")), "BaMM"),
                Menu(Loc("Muma_1", List("stundenplan", schedule_muma(0)), schedule_muma(0), LocGroup("Muma"))),
                Menu(Loc("Muma_2", List("stundenplan", schedule_muma(1)), schedule_muma(1), LocGroup("Muma"))),
                Menu(Loc("Muma_3", List("stundenplan", schedule_muma(2)), schedule_muma(2), LocGroup("Muma")))),
              Menu(Loc("ITS", List("stundenplan", "BaIS" + loadProps("Semester")), "BaIS"),
                Menu(Loc("ITS_1", List("stundenplan", schedule_its(0)), schedule_its(0), LocGroup("ITS"))),
                Menu(Loc("ITS_2", List("stundenplan", schedule_its(1)), schedule_its(1), LocGroup("ITS"))),
                Menu(Loc("ITS_3", List("stundenplan", schedule_its(2)), schedule_its(2), LocGroup("ITS")))),
              Menu(Loc("Master", List("stundenplan", "MaI" + loadProps("Semester")), "MaI"),
                Menu(Loc("MA_1", List("stundenplan", schedule_ma(0)), schedule_ma(0), LocGroup("Master"))),
                Menu(Loc("MA_2", List("stundenplan", schedule_ma(1)), schedule_ma(1), LocGroup("Master"), onlyWS))),
              Menu(Loc("Groups", List("groups"), "Gruppen")),
              Menu(Loc("Blocks", List("blocks"), "Blöcke", Hidden)),
              Menu(Loc("ExtBlocks", ExtLink("/blocks") , "Blöcke")),
              Menu(Loc("Abkuerzungen", List("stundenplan", "abkuerzungen"), "Abkuerzungen")),
              Menu(Loc("Hilfe", List("stundenplan", "hilfe"), "Hilfe"))
            )
            } else {
              Menu(Loc("Stundenplan", List("stundenplan", "na"), "Stundenplan" ))
            }
    }

    val entries: List[Menu] = Menu(Loc("Home", List("index"), "Home", Hidden)) ::
            Menu(Loc("404", List("404") , "404", Hidden)) ::
            Menu(Loc("StdPlanHilfe", List("help") , "Hilfe", Hidden)) ::
            Menu(Loc("ExtHome", ExtLink("/index") , "Home")) ::
            Menu(Loc("Entry", List("entry"), "entry", Hidden )) ::
            Menu(Loc("SemSearch", List("semsearch"), "semsearch", Hidden )) ::
            Menu(Loc("StundenplanDispatch", List("scheduleDispatch"), "Stundenplan")) ::
            schedule ::
            Menu(Loc("Verfassen", List("writenews"), "Verfassen", loggedIn)) ::
            Menu(Loc("editieren", Link(List("edit"), true, "/edit/editieren"), "Editieren", loggedIn)) ::
            Menu(Loc("ScheduleMgt", List("scheduleAdmin", "index"), "Std. Plan Verwaltung", adminLoggedIn)) ::
            Menu(Loc("schedule", List("schedule"), "schedule", Hidden),
              Menu(Loc("GroupsNew", List("schedule", "groups"), "Gruppen")),
              Menu(Loc("BlocksNew", List("schedule", "blocks"), "Blöcke", Hidden)),
              Menu(Loc("ExtBlocksNew", ExtLink("/schedule/blocks") , "Blöcke")),
              Menu(Loc("AbkuerzungenNew", List("schedule", "abkuerzungen"), "Abkuerzungen"))) ::
              Menu(Loc("Kontakt", List("issues") , "Kontakt")) ::
            Menu(Loc("Entwickler-Blog", ExtLink("http://padsblog.posterous.com/"), "Entwickler-Blog")) ::
            (if (productive)
              Menu(Loc("SSLLogin", ExtLink("https://spirit.fh-schmalkalden.de/user_mgt/login") , "Anmelden", loggedOut)) ::
              User.sitemap
            else
              User.sitemap)

    LiftRules.useXhtmlMimeType = false //required by ReCaptcha js lib
    LiftRules.resourceNames = "recaptcha" :: LiftRules.resourceNames

    //LiftRules.passNotFoundToChain = true
    LiftRules.liftRequest.append {
      case Req("staticschedule" :: _, _, _) => false
    }

    // This takes care of the RSS Feed.
    LiftRules.statelessDispatchTable.append(Feed)

    LiftRules.setSiteMap(SiteMap(entries:_*))

    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    UploadWatcher.run()
    DayChecker.start()
    if (tweet) Spreader.start()
    if (loadProps("ircConnect").equals("true")){
      SpiritBot.connect
    }


  }
}
