package org.unsane.spirit.news.snippet

import net.liftweb.util.BindHelpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.{JsRaw, Call}
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.js.{JE, JsonCall, JsCmd}
import net.liftweb.util.{ Mailer, Props }
import net.liftweb.util.Mailer._
import net.liftweb.common.Loggable

class Issues extends Loggable {

  private var name = ""
  private var email = ""
  private var subject = ""
  private var issue = ""

  private def process = {

    val mail = "Von: " + name + "\n" +
               "E-Mail: " + email + "\n" +
               "Betreff: " + subject + "\n" +
               "Nachricht: " + issue

    logger info "ISSUE: " + mail

    sendMail("no-reply@spirit.fh-schmalkalden.de",
      Props.get("bug.report.email").openOr(""),
      "Bug/Anregung/Kritik von Spirit", mail)

    def sendMail(from: String, to: String, subject: String, issue: String) {
      Mailer.sendMail(From(from), Subject(subject), (PlainMailBodyType(issue) :: To(to) :: Nil) : _*)
    }

    S.warning("Das Formular wurde erfolgreich abgesendet.")
    S.redirectTo("/")
  }

  def render = {

    "name=name" #> SHtml.text("", name = _) &
    "name=email" #> SHtml.text("", email = _ ) &
    "name=subject" #> SHtml.text("", subject = _) &
    "name=issue" #> SHtml.textarea("", issue = _) &
    "type=submit" #> SHtml.submit("Senden", () => process)
  }
}
