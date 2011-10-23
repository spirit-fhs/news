package org.unsane.spirit.news.snippet

import net.liftweb.util.BindHelpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.{JsRaw, Call}
import net.liftweb.http.js.{JE, JsonCall, JsCmd}
import net.liftweb.util.Mailer._
import org.unsane.spirit.news.model.Config
import net.tanesha.recaptcha.{ReCaptchaFactory, ReCaptcha}
import net.liftweb.common._
import net.liftweb.util.{FieldIdentifier, FieldError, Mailer, Props}
import org.unsane.spirit.news.lib.{ ReCaptcha => RC }
import net.liftweb.http.{SessionVar, SHtml, S}


class Issues extends Loggable with RC with Config {

  case class issue(name: String, email: String, subject: String, mail: String) {

    override def toString = {
      "Von: " + name + "\n" +
      "E-Mail: " + email + "\n" +
      "Betreff: " + subject + "\n" +
      "Nachricht: " + mail
    }
  }

  object sessionIssue extends SessionVar[issue](issue("", "", "", ""))

  protected def reCaptchaPublicKey = loadProps("public.key")
  protected def reCaptchaPrivateKey = loadProps("private.key")

  var name = ""
  var email = ""
  var subject = ""
  var mailNews = ""

  private def process = {

    sessionIssue.set(issue(name, email, subject, mailNews))

    validateCaptcha() match {
      case List() =>
      case s =>
        S.error("Bitte validierung erneut eingeben!")
        S.redirectTo("/issues")
    }

    logger info "ISSUE: " + sessionIssue.get.toString

    val EmailParser = """([\w\d\-\_]+)(\+\d+)?@([\w\d\-\.]+)""".r

    val frommail = email match {
      case EmailParser(_,_,_) => email
      case _ => "no-reply@spirit.fh-schmalkalden.de"
    }

    sendMail(frommail,
      Props.get("bug.report.email").openOr(""),
      "Bug/Anregung/Kritik von Spirit", sessionIssue.get.toString)

    sessionIssue.remove()

    S.warning("Das Formular wurde erfolgreich abgesendet.")
    S.redirectTo("/")

  }

  def render = {

    "name=captcha" #> captchaXhtml() &
    "name=name" #> SHtml.text(sessionIssue.get.name, name = _) &
    "name=email" #> SHtml.text(sessionIssue.get.email, email = _ ) &
    "name=subject" #> SHtml.text(sessionIssue.get.subject, subject = _) &
    "name=issue" #> SHtml.textarea(sessionIssue.get.mail, mailNews = _) &
    "type=submit" #> SHtml.submit("Senden", () => process)
  }

  private def sendMail(from: String, to: String, subject: String, issue: String) {
    Mailer.sendMail(From(from), Subject(subject), (PlainMailBodyType(issue) :: To(to) :: Nil) : _*)
  }

}
