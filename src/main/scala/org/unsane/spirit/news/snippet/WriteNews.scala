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

import scala.xml.{XML, NodeSeq}
import net.liftweb.util.Helpers._
import net.liftweb.http._
import S._
import js._
import JsCmds._
import JE._

import net.liftweb.http.SHtml._
import net.liftweb.textile._
import net.liftweb.common.Loggable
import net.liftweb.textile
import java.util._
import java.text._
import model._
import scala.xml._



/**
 * WriteNews lets you write entries and send them directly to the FhS mailinglists via SPIRIT.
 * @author Marcus Denison
 */
class WriteNews extends Loggable with Config with SpiritHelpers with EntryPreview {
  
  private val tweet = loadProps("Tweet") == "yes"

  /**
   * Creates the view for writing entries.
   */
  def writeNews(xhtml : NodeSeq) : NodeSeq = {

    def submitNode(name: String, subject: String, post: String, lifecycle: String, semester: String) {
      val date = df format today
      lazy val nr = if(EntryCounter.findAll.isEmpty) "1" else EntryCounter.findAll.head.counter.toString
      val entry = Entry.createRecord
        entry.name.set( name )
        entry.subject.set( subject )
        entry.news.set ( post )
        entry.semester.set ( semester )
        entry.writer.set ( getSessionAttribute("fullname").open_!.toString )
        entry.date.set ( date )
        entry.nr.set ( nr )
        entry.lifecycle.set ( lifecycle )
        entry.save
      val count = if(EntryCounter.findAll.isEmpty) EntryCounter.createRecord else EntryCounter.findAll.head
        count.counter.set( (nr.toInt + 1).toString ) 
        count.save
      
      logger info "Entry was created by " + User.currentUserId.openOr("")
      if(sendEmail && semester.nonEmpty){
        MailHandler.send(TextileParser.toHtml(post).toString, subject, loadEmails(semester.split(" ")))
      }
      if (tweet) Spreader ! Tweet(subject, semester.split(" ").map(" #"+_).mkString , nr)
      redirectTo("/index")
    }

  bind("form", xhtml,
    "subject" -> text("", subject = _, "cols" -> "80"),
    "date" -> text(todayText format today, lifecycle = _, "cols" -> "80", "id" -> "datepicker"),
    // Next three lines might be depreceated!!!!
    //"day" -> text(day format today, lifecycle += _ + ".", "size" -> "2"),
    //"month" -> text(month format today, lifecycle += _ + ".", "size" -> "2"),
    //"year" -> text(year format today, lifecycle += _, "size" -> "4"),
    "email" -> checkbox(false, if (_) sendEmail = true),
    "textarea" -> textarea("", news = _, "rows" -> "12", "cols" -> "80", "style" -> "width:100%", "id" -> "entry"),
    "submit" -> submit("Senden",
      () => submitNode(name,
      subject,
      news,
      dateValidator(lifecycle),
      semester)))
  }

  /**
   * Builds the CheckboxList.
   * @todo need to build it a little different so the View is more flexible
   */
  def makeBaICheckboxList(xhtml: NodeSeq): NodeSeq = {
    loadSemesters("BaI") flatMap { sem =>
      bind("BaI", xhtml,
        "label" -> sem,
        "checkbox" -> checkbox(false, if (_) semester += sem + " ")
      )
    }
  }

  def makeBaWICheckboxList (xhtml: NodeSeq): NodeSeq = {
    loadSemesters("BaWI") flatMap { sem =>
      bind("BaWI", xhtml,
        "label" -> sem,
        "checkbox" -> checkbox(false, if (_) semester += sem + " ")
      )
    }
  }

  def makeMuMaCheckboxList(xhtml: NodeSeq): NodeSeq = {
    loadSemesters("BaMuMa") flatMap { sem =>
      bind("MuMa", xhtml,
        "label" -> sem,
        "checkbox" -> checkbox(false, if (_) semester += sem + " ")
      )
    }
  }

  def makeITSCheckboxList(xhtml: NodeSeq): NodeSeq = {
    loadSemesters("BaITS") flatMap { sem =>
      bind("ITS", xhtml,
        "label" -> sem,
        "checkbox" -> checkbox(false, if (_) semester += sem + " ")
      )
    }
  }

  def makeMaCheckboxList(xhtml: NodeSeq): NodeSeq = {
    loadSemesters("Ma") flatMap { sem =>
      bind("Ma", xhtml,
        "label" -> sem,
        "checkbox" -> checkbox(false, if (_) semester += sem + " ")
      )
    }
  }

  def makeOtherCheckboxList(xhtml: NodeSeq): NodeSeq = {
    loadSemesters("Other") flatMap { sem =>
      bind("Other", xhtml,
        "label" -> sem,
        "checkbox" -> checkbox(false, if (_) semester += sem + " ")
      )
    }
  }

  private val df = new SimpleDateFormat ("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US)
  private val todayText = new SimpleDateFormat ("dd.MM.yyyy")
  private val day = new SimpleDateFormat ("dd")
  private val month = new SimpleDateFormat ("MM")
  private val year = new SimpleDateFormat ("yyyy")
  private val name = User.currentUserId.open_!
  private val userhome = System.getProperty("user.dir")
  private val today = new Date
  private var subject = ""
  private var news = ""
  private var semester = ""
  private var lifecycle = ""
  private var sendEmail = false
}
