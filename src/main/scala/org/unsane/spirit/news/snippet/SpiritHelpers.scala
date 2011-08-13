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
package snippet

import model.{ Entry, Config }
import scala.xml._
import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml._
import java.util._
import java.text._
import scala.collection._
import net.liftweb.common.Box
import net.liftweb.http.{LiftResponse, S}
import net.liftweb.common.Full
import net.liftweb.http.StreamingResponse

/**
 * @author Marcus Denison
 */
trait SpiritHelpers {

  /**
   * Generating the headers for the returnAsFeed method.
   */
  private def headers(length: String) = {
    ("Content-type" -> "text/rss+xml; charset=utf-8") ::
    ("Content-length" -> length) :: Nil
  }

  /**
   * For the creation of an RSS Feed
   * @param in The RSSFeed as an Array[Byte].
   * @return The LiftResponse which will be returned to the User.
   */
  def returnAsFeed(in: Array[Byte]): Box[LiftResponse] = {
    Full(StreamingResponse(
      new java.io.ByteArrayInputStream(in),
      () => {},
      in.length,
      headers(in.length.toString), Nil, 200)
    )
  }

  /**
   * Renders links for the semesters so each semester can be clicked in the index, semsearcher and entry.
   * User will be forwarded to /semsearch/<semester>. 
   */
  def sem2link(semesterArray: Array[String]): NodeSeq = {
    val renderedLinks = semesterArray map { currentSem =>
      link(currentSem, () => S redirectTo "/semsearch/" + currentSem, <span class="semester_space">{ currentSem }</span> )
    }
    renderedLinks.toSeq.flatten
  }

  /**
   * Checks all entries if they match to the current or older date, and deletes them.
   */
  def dayCheck() = {
    val formatter = new SimpleDateFormat("dd.MM.yyyy")
    val today = new Date
    Entry.findAll.filter(i => formatter.parse(i.lifecycle.value.toString).before(today))
            .foreach {
            o => o.delete_!
            }
  }

  /**
   * Checks the input if it is a Valid date or not, if the user won't put in a correct date. The lifecycle is set to 14 days
   * @param date the input date from a User
   * @return String a correct date
   */
  def dateValidator(date: String): String = {
    val checkDate = new SimpleDateFormat("dd.MM.yyyy")
    checkDate setLenient false
    val exceptionDate = new Date
    
    try {
      checkDate parse date
      if(checkDate.parse(date).after(exceptionDate)){
        date
      } else {
        val returnDate = checkDate.format(exceptionDate.getTime + 1209600000)
        S warning "Ihr Datum war vor dem Heutigen, der Beitrag wird in 14 Tagen gelöscht!"
        returnDate
      }
        
    } catch {
        case pe: java.text.ParseException => pe
          val returnDate = checkDate.format(exceptionDate.getTime + 1209600000)
          S warning "Ihr Beitrag verfaellt aumatisch in 14 Tagen, da das Datum nicht gültig war!"
          returnDate
      }
      
  }

  private val userhome = System.getProperty("user.dir")

}
