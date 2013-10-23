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

import dispatch._
import dispatch.classic._
import oauth._
import scala.actors._
import Http._
import net.liftweb.common.Loggable

import twitter4j._
import auth.AccessToken

/* This is one cool feature!
 * Spreader takes the Entry number, gets a TinyURL for this.
 * And it will be TWittered instantly.
 * If Twitter is down or not reachable, the Actor will throw the exception!
 */

case class Tweet(subject: String, semester: String, number: String)

object Spreader extends Actor with Config with Loggable {

  private val twitter = new TwitterFactory().getInstance()
  twitter.setOAuthConsumer(loadProps("Consumer"), loadProps("ConsumerSecret"))
  twitter.setOAuthAccessToken(new AccessToken(loadProps("Token"), loadProps("TokenSecret")))

  private def mkTweet(subject: String, tinyurl: String, semester: String) = {
    val tailWithoutSemester = " " + tinyurl
    val tailSemester = tailWithoutSemester + " " + semester
    val tail =
      if (tailSemester.length > 130) tailWithoutSemester
      else tailSemester
    val maxlen = 140 - tail.length
    val shortSubject =
      if (subject.length <= maxlen) subject
      else subject.slice(0, maxlen-3)+"..."
    shortSubject + tail
  }

  def act {
    loop {
      react {
        case Tweet(subject,semester,nr) =>
          try {
            val http = new Http
            val longUrl = url("http://is.gd/api.php?longurl=http://spirit.fh-schmalkalden.de/entry/" + nr)
            val tinyurl = http(longUrl as_str)
            twitter.updateStatus(mkTweet(subject, tinyurl, semester))
          } catch {
            case e =>
              logger error e.toString
          }
      }
    }
  }
}
