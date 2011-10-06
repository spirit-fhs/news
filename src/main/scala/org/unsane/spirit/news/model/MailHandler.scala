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

import javax.mail._
import javax.mail.internet._
import java.util.Properties
import net.liftweb.http.S
import net.liftweb.common.Loggable

/**
 * Sending an email to the Array that will be given to send()
 * and sends an email to the actual sender!
 */
object MailHandler extends Loggable {

  lazy val footer =
    """<br><br><br>
      |----------------------------------------------------------<br>
      |Sent over FhS_Spirit!<br>
      |<a href="http://spirit.fh-schmalkalden.de/">
      |  http://spirit.fh-schmalkalden.de/</a><br>
      |Follow @<a href="http://www.twitter.com/fhs_spirit">
      |  FhS_Spirit</a> on Twitter<br>
      |Visit <a href=http://www.facebook.com/fhs.spirit>
      |  FhS Spirit</a> on Facebook<br>""".stripMargin

  /**
   * send takes the post and sends it to the receipients via email
   * @param context the actual post that was written
   * @param subject the subject of that post
   */
  def send(context: String, subject: String, adresses: Array[String]) = {
    logger info User.currentUserId.openOr("") + " is using the email function!"

    val props = new Properties
    props.setProperty("mail.transport.protocol", "smtp")
    props.setProperty("mail.host", "smtp.fh-schmalkalden.de")

    val mailSession = Session.getDefaultInstance(props, null)
    val transport = mailSession.getTransport

    val msg = new MimeMessage(mailSession)
    msg.setSubject(subjectMatcher(subject), "UTF-8")
    msg.setContent(context + footer, "text/html; charset=UTF-8")

    msg.addRecipients(Message.RecipientType.TO,
       adresses map { email =>
         logger info "Sending to " + email
         new InternetAddress(email).asInstanceOf[javax.mail.Address]
       })

    msg.addRecipient(Message.RecipientType.CC,
      new InternetAddress(S.getSessionAttribute("email").openOr("")))

    msg.setFrom(new InternetAddress(S.getSessionAttribute("email").openOr("").toString,
                 S.getSessionAttribute("fullname").openOr("").toString))

    try {
      transport.connect
      transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO))
      transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.CC))
      transport.close
      logger info "Mail was sent successfully!"
      S notice "eMail wurde gesendet!"
    } catch {
      case e =>
        logger warn "Mail was not sent correctly!"
        logger warn e.printStackTrace.toString
        S error "eMail konnte nicht versendet werden! "
    }

  }

  /**
   * if there is not subject given, subjectMatcher will put in a standard one
   * @param subject
   * @return String
   */
  private def subjectMatcher(subject: String): String = subject match {
    case "" => "Neuigkeiten auf SPIRIT!"
    case _ => subject
  }
}
