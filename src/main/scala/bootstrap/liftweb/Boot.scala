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
import fun._
import snippet._

import net.liftweb._
import util._
import http._
import sitemap._
import Loc._

import common._
import util.Helpers._
import mongodb._

class Boot extends Loggable with Config {
  def boot {

    val productive = loadProps("Productive") == "yes"

    // Opens connection to MongoDB with user/pass "spirit_news"
    MongoDB.defineDbAuth(DefaultMongoIdentifier,
      MongoAddress(MongoHost("127.0.0.1", 27017), "spirit_news"),
      "spirit_news",
      "spirit_news")

    // Need this for the resource stuff !!
    ResourceServer.allow {
      case "jquery" :: _ => true
      case "images" :: _ => true
      case "blueprint" :: _ => true
    }

    LiftRules.noticesAutoFadeOut.default.set((noticeType: NoticeType.Value) => Full((4 seconds, 4 seconds)))
    LiftRules.addToPackages("org.unsane.spirit.news")
    LiftRules.dispatch.prepend(NamedPF("Login Validation") {
      case Req("login_required" :: page , extension, _)
        if (!LoginUtil.isLogged) =>
          () => Full(RedirectResponse("success"))
    })

    val loggedIn = If(() => User.loggedIn_?, () => RedirectResponse("/index"))
    val loggedOut = If(() => User.notLoggedIn_?, () => RedirectResponse("/index"))

    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
          case (req,failure) =>
            NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })

    LiftRules.statefulRewrite.append {
      case RewriteRequest(ParsePath("entry" :: entrynr :: Nil,_,_,_),_,_) =>
        RewriteResponse("entry" :: Nil, Map("entrynr" -> entrynr))
    }

    LiftRules.statefulRewrite.append {
      case RewriteRequest(ParsePath("semsearch" :: semsearch :: Nil,_,_,_),_,_) =>
        RewriteResponse("semsearch" :: Nil, Map("semsearch" -> semsearch))
    }

    val schedule_i = loadSchedule("I")
    val schedule_wi = loadSchedule("WI")
    val schedule_its = loadSchedule("ITS")
    val schedule_muma = loadSchedule("MUMA")
    val schedule_ma = loadSchedule("MA")

    // TODO Build Menu some different way, a cleaner way!?
    val entries: List[Menu] = Menu(Loc("Home", List("index"), "Home")) ::
            Menu(Loc("Entry", List("entry"), "entry", Hidden )) ::
            Menu(Loc("SemSearch", List("semsearch"), "semsearch", Hidden )) ::
            Menu(Loc("Stundenplan", List("stundenplan", "index"), "Stundenplan" ),
              Menu(Loc("Informatik", List("stundenplan", "BaI"), "BaI"),
                Menu(Loc("Informatik_1", List("stundenplan", schedule_i(0)), schedule_i(0))),
                Menu(Loc("Informatik_3", List("stundenplan", schedule_i(1)), schedule_i(1))),
                Menu(Loc("Informatik_5", List("stundenplan", schedule_i(2)), schedule_i(2)))),
              Menu(Loc("WInformatik", List("stundenplan", "BaWI"), "BaWI"),
                Menu(Loc("WInformatik_1", List("stundenplan", schedule_wi(0)), schedule_wi(0))),
                Menu(Loc("WInformatik_3", List("stundenplan", schedule_wi(1)), schedule_wi(1))),
                Menu(Loc("WInformatik_5", List("stundenplan", schedule_wi(2)), schedule_wi(2)))),
              Menu(Loc("Muma", List("stundenplan", "BaMM"), "BaMM"),
                Menu(Loc("Muma_1", List("stundenplan", schedule_muma(0)), schedule_muma(0))),
                Menu(Loc("Muma_3", List("stundenplan", schedule_muma(1)), schedule_muma(1))),
                Menu(Loc("Muma_5", List("stundenplan", schedule_muma(2)), schedule_muma(2)))),
              Menu(Loc("ITS", List("stundenplan", "BaIS"), "BaIS"),
                Menu(Loc("ITS_1", List("stundenplan", schedule_its(0)), schedule_its(0))),
                Menu(Loc("ITS_3", List("stundenplan", schedule_its(1)), schedule_its(1))),
                Menu(Loc("ITS_5", List("stundenplan", schedule_its(2)), schedule_its(2)))),
              Menu(Loc("Master", List("stundenplan", "MaI"), "MaI"),
                Menu(Loc("MA_1", List("stundenplan", schedule_ma(0)), schedule_ma(0))),
                Menu(Loc("MA_3", List("stundenplan", schedule_ma(1)), schedule_ma(1))))) ::
            Menu(Loc("404", List("404"), "404", Hidden)) ::
            Menu(Loc("Groups", List("groups"), "Gruppen")) ::
            Menu(Loc("Blocks", List("blocks"), "Blöcke")) ::
            Menu(Loc("Verfassen", List("writenews"), "Verfassen", loggedIn)) ::
            Menu(Loc("editieren", Link(List("edit"), true, "/edit/editieren"), "Editieren", loggedIn)) ::
            Menu(Loc("Bugs und Anregungen", ExtLink("https://pads.fh-schmalkalden.de/trac/newticket") , "Bugs und Anregungen")) ::
            (if (productive)
              Menu(Loc("SSLLogin", ExtLink("https://spirit.fh-schmalkalden.de/user_mgt/login") , "Anmelden", loggedOut)) ::
              User.sitemap
            else
              User.sitemap)

    LiftRules.setSiteMap(SiteMap(entries:_*))
    DayChecker.start()
    // Spreader.start()
    if (loadProps("ircConnect").equals("true")){
      SpiritBot.connect
    }
  }
}
