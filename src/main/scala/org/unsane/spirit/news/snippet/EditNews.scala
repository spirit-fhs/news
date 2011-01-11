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

import scala.xml._
import net.liftweb.util.Helpers._
import net.liftweb.http.{S}
import net.liftweb.http.SHtml._
import net.liftweb.http._
import net.liftweb.common._
import net.liftweb.textile._
import java.text._
import java.util.Locale
import model._
import net.liftweb.json.JsonDSL._

/**
 * With EditNews one can either edit his entries or may delete them.
 * Using different Methods to talk to the MongoDB, to see what is possible!
 * @author Marcus Denison
 */
class EditNews extends Loggable with SpiritHelpers with Config with EntryPreview {

  private val tweet = loadProps("Tweet") == "yes"

  /** 
   * Views the list of entries from the current user for editing or deleting them.
   */
  def view (xhtml : NodeSeq) : NodeSeq = {
    val news = Entry.findAll("name" -> User.currentUserId.open_!.toString)
    
    news.flatMap(v =>
      <tr><td>{v.nr.value.toString}</td><td>{v.writer.value.toString}</td>
      <td>{v.subject.value.toString}</td><td>{v.date.value.toString.substring(4, 11) + ". " + v.date.value.toString.substring(17, 22)}</td>
      <td>{link("/edit/edit", () => CurrentEntry(Full(v)), Text("Edit"))}</td>
      <td>{link("/edit/delete", () => CurrentEntry(Full(v)), Text("Delete"))}</td></tr> )
  }

  /**
   * Takes the given parameters, searches for the entry with the given number and saves the new entry with new number.
   * Would like to use old number, but we would have problem tweeting this with Twitter! 
   * @param post the changed post
   * @param nr the entry number
   * @param subject the post subject
   * @param semester the post semesters
   * @param lifecycle the post day of deletion
   * @todo change all post names to entry
   */
  def changeEntry(oldEntry: Entry,
                    oldNr: String,
                    post: String,
                    subject: String,
                    semester: String,
                    lifecycle: String) {
    val date = df.format(new java.util.Date)
    val newNr = if(EntryCounter.findAll.isEmpty) "1" else EntryCounter.findAll.head.counter.toString
    Entry.find(oldEntry.asDBObject).open_!.delete_!
    logger info "Entry was deleted by " + User.currentUserId.openOr("")
    val entry = Entry.createRecord
        entry.name.set( User.currentUserId.open_!.toString )
        entry.subject.set( subject )
        entry.news.set ( post )
        entry.semester.set ( semester )
        entry.writer.set ( S.getSessionAttribute("fullname").open_!.toString )
        entry.date.set ( date )
        entry.nr.set ( newNr )
        entry.lifecycle.set ( lifecycle )
        entry.save

    val count = if(EntryCounter.findAll.isEmpty) EntryCounter.createRecord else EntryCounter.findAll.head
        count.counter.set( (newNr.toInt + 1).toString )
        count.save

    logger debug "Semesters: " + semester
    logger info "Entry was created by " + User.currentUserId.openOr("")
    logger info "Entry was updated by " + User.currentUserId.openOr("")
    if (sendEmail) MailHandler.send(TextileParser.toHtml(post).toString, subject, semester split (" "))
    if (tweet && tweetUpdate) Spreader ! Tweet("[Update] " + subject, semester.split(" ").map(" #"+_).mkString , newNr)
    S notice "Ihr update wurde gespeichert"
    S redirectTo "/index"
  }
  
  /**
   * Searches for the entry with the given number an deletes the post
   * @param Entry
   */
  def delete(delEntry: Entry){
    Entry.find(delEntry.asDBObject).open_!.delete_!
    logger info "Entry was deleted by " + User.currentUserId.openOr("")
    S notice "Ihr Eintrag wurde gelöscht"
    S redirectTo "/index"
  }

  /**
   * Creates the view for editing the user entries
   */
  def edit(xhtml: Node): NodeSeq = {
    try {
      val oldEntry = CurrentEntry.open_!
      val nr = oldEntry.nr.value.toString
      var subject = oldEntry.subject.value.toString

      var textNote = ""
      var lifecycle = oldEntry.lifecycle.value.toString
      //val dateArray = oldEntry.lifecycle.value.toString.split("\\.")
      //val day = dateArray(0)
      //val month = dateArray(1)
      //val year = dateArray(2)

      bind("test", xhtml,
        "date"  -> text(lifecycle, lifecycle = _, "cols" -> "80", "id" -> "datepicker"),
        //"day" -> text(day, lc => lifecycle += lc + ".", "size" -> "2"),
        //"month" -> text(month, lc => lifecycle += lc + ".", "size" -> "2"),
        //"year" -> text(year, lc => lifecycle += lc, "size" -> "4"),
        "email" -> checkbox(false, if(_) sendEmail = true),
        "textarea" -> textarea(oldEntry.news.value.toString, tn => textNote = tn, "rows" -> "12", "cols" -> "80", "style" -> "width:100%", "id" -> "entry"),
        "twitter" -> checkbox(true, if(_) tweetUpdate = true),
        "subject" -> text(subject, subject = _),
        "semester" -> oldEntry.semester.value.toString,
        "verfasser" -> oldEntry.writer.value.toString,
        "Nr" -> oldEntry.nr.value.toString,
        "back" -> submit("Zurück", () => S redirectTo "/edit/editieren"),
        "submit" -> submit("Senden", () => changeEntry(oldEntry, nr, textNote, subject, changedSemester, dateValidator(lifecycle))))
    } catch {
      case e: NullPointerException => e
        logger info e.printStackTrace.toString
        S notice "You shouldn't do this!"
        S redirectTo "/index"
    }
  }

  /**
   * Creates the view for validating the deletion of an entry 
   */
  def confirmDelete(xhtml: Node): NodeSeq = {
    try {
      val delEntry = CurrentEntry.open_!

      bind("test", xhtml,
        "textarea" -> CurrentEntry.open_!.news.value.toString,
        "subject" -> CurrentEntry.open_!.subject.value.toString,
        "verfasser" -> CurrentEntry.open_!.writer.value.toString,
        "Nr" -> CurrentEntry.open_!.nr.value.toString,
        "submit" -> submit("Delete!", () => delete(delEntry)))
    } catch {
      case e: NullPointerException => e
        logger info e.printStackTrace.toString
        S notice "You shouldn't do this!"
        S redirectTo "/index"
    }
  }

/**
   * Builds the CheckboxList
   * @todo need to build it a little different so the View is more flexible
   */
  def makeBaICheckboxList(xhtml: NodeSeq): NodeSeq = {
    val semester = CurrentEntry.open_!.semester.value.toString.split(" ")
    loadSemesters("BaI") flatMap { sem =>
      println(semester.mkString(" ") + " : " + sem)
      bind("BaI", xhtml,
        "label" -> sem,
        if(semester.contains(sem)) "checkbox" -> checkbox(true, if (_) changedSemester += sem + " ") else "checkbox" -> checkbox(false, if (_) changedSemester += sem + " ")
      )
    }
  }

  def makeBaWICheckboxList (xhtml: NodeSeq): NodeSeq = {
    val semester = CurrentEntry.open_!.semester.value.toString.split(" ")
    loadSemesters("BaWI") flatMap { sem =>
      bind("BaWI", xhtml,
        "label" -> sem,
        if(semester.contains(sem)) "checkbox" -> checkbox(true, if (_) changedSemester += sem + " ") else "checkbox" -> checkbox(false, if (_) changedSemester += sem + " ")
      )
    }
  }

  def makeMuMaCheckboxList(xhtml: NodeSeq): NodeSeq = {
    val semester = CurrentEntry.open_!.semester.value.toString.split(" ")
    loadSemesters("BaMuMa") flatMap { sem =>
      bind("MuMa", xhtml,
        "label" -> sem,
        if(semester.contains(sem)) "checkbox" -> checkbox(true, if (_) changedSemester += sem + " ") else "checkbox" -> checkbox(false, if (_) changedSemester += sem + " ")
      )
    }
  }

  def makeITSCheckboxList(xhtml: NodeSeq): NodeSeq = {
    val semester = CurrentEntry.open_!.semester.value.toString.split(" ")
    loadSemesters("BaITS") flatMap { sem =>
      bind("ITS", xhtml,
        "label" -> sem,
        if(semester.contains(sem)) "checkbox" -> checkbox(true, if (_) changedSemester += sem + " ") else "checkbox" -> checkbox(false, if (_) changedSemester += sem + " ")
      )
    }
  }

  def makeMaCheckboxList(xhtml: NodeSeq): NodeSeq = {
    val semester = CurrentEntry.open_!.semester.value.toString.split(" ")
    loadSemesters("Ma") flatMap { sem =>
      bind("Ma", xhtml,
        "label" -> sem,
        if(semester.contains(sem)) "checkbox" -> checkbox(true, if (_) changedSemester += sem + " ") else "checkbox" -> checkbox(false, if (_) changedSemester += sem + " ")
      )
    }
  }

  def makeOtherCheckboxList(xhtml: NodeSeq): NodeSeq = {
    val semester = CurrentEntry.open_!.semester.value.toString.split(" ")
    loadSemesters("Other") flatMap { sem =>
      bind("Other", xhtml,
        "label" -> sem,
        if(semester.contains(sem)) "checkbox" -> checkbox(true, if (_) changedSemester += sem + " ") else "checkbox" -> checkbox(false, if (_) changedSemester += sem + " ")
      )
    }
  }

  private var changedSemester = ""
  private object CurrentEntry extends RequestVar[Box[Entry]](Empty)
  private val userhome = System.getProperty("user.dir")
  private val df = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US)
  private var sendEmail = false
  private var tweetUpdate = false
}
