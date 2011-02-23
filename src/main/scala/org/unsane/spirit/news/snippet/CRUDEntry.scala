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
import java.util.Date
import java.util.Locale
import net.liftweb.json.JsonDSL._
import model._

class CRUDEntry extends Loggable with SpiritHelpers with Config with EntryPreview {

  /**
   * CrudEntry is either a new Entry or a an existing Entry to be Updated!
   */
  lazy val CrudEntry =
    CurrentEntry.get match {
      case Full(entry) =>
        logger info "Entry was found: " + entry.nr.value + "!"
        newEntry = false
        entry
      case Empty =>
        logger info "Creating new Entry to save later!"
        newEntry = true
        Entry.createRecord
      case _ =>
        logger info "This should not have happend, but why did it?!"
        newEntry = true
        Entry.createRecord
    }

  /**
   * Lists all Entries for the logged in User!
   */
  def viewUserEntries(xhtml: NodeSeq): NodeSeq = {
    Entry.findAll("name" -> User.currentUserId.open_!.toString).sortWith(
      (entry1, entry2) => (entry1 > entry2)
    ).flatMap(v =>
      <tr><td>{v.nr.value.toString}</td><td>{v.writer.value.toString}</td>
      <td>{v.subject.value.toString}</td><td>{v.date.value.toString.substring(4, 11) + ". " +
                                              v.date.value.toString.substring(17, 22)}</td>
      <td>{link("/edit/edit", () => CurrentEntry(Full(v)), Text("Edit"))}</td>
      <td>{link("/edit/delete", () => CurrentEntry(Full(v)), Text("Delete"))}</td></tr> )
  }

  /**
   * If CrudEntry is a new Entry, create() does the rest of setting values,
   * saving, spreading it via email and twitter.
   */
  def create() {
    lazy val nr = if(EntryCounter.findAll.isEmpty) "1" else EntryCounter.findAll.head.counter.toString

    CrudEntry.date.set( date )
    CrudEntry.name.set( User.currentUserId.openOr("Oops!") )
    CrudEntry.semester.set ( changedSemester )
    CrudEntry.writer.set ( S.getSessionAttribute("fullname").openOr("Oops!") )
    CrudEntry.nr.set ( nr )
    CrudEntry.save

    val count = if(EntryCounter.findAll.isEmpty) EntryCounter.createRecord else EntryCounter.findAll.head
      count.counter.set( (nr.toInt + 1).toString )
      count.save

    logger info "Entry was created by " + User.currentUserId.openOr("")
    if(sendEmail && changedSemester.nonEmpty){
      logger info "News should be sent via eMail!"
      MailHandler.send(TextileParser.toHtml(CrudEntry.news.value.toString).toString, CrudEntry.subject.value, loadEmails(changedSemester.split(" ")))
    }
    if (tweet) {
      logger info "News should be spread via Twitter!"
      Spreader ! Tweet(CrudEntry.subject.value, changedSemester.split(" ").map(" #"+_).mkString , nr)
    }
  }

  /**
   * If CrudEntry is not a new Entry, update() does the rest of setting values,
   * updating, spreading it via email and twitter.
   */
  def update() {
    val oldNr = CrudEntry.nr.value
    val newNr =
      if(tweetUpdate)
        if(EntryCounter.findAll.isEmpty) "1"
        else EntryCounter.findAll.head.counter.toString
      else oldNr

    CrudEntry.date.set( date )
    CrudEntry.semester.set ( changedSemester )
    CrudEntry.nr.set ( newNr )
    CrudEntry.save

    if (newNr != oldNr) {
      val count =
        if(EntryCounter.findAll.isEmpty) EntryCounter.createRecord
        else EntryCounter.findAll.head
      count.counter.set( (newNr.toInt + 1).toString )
      count.save
    }

    logger info "Semesters: " + changedSemester
    logger info "Entry was updated by " + User.currentUserId.openOr("")
    if (sendEmail) MailHandler.send(TextileParser.toHtml(CrudEntry.news.value).toString,
      "[Update] " + CrudEntry.subject.value, loadEmails(changedSemester split (" ")))
    if (tweet && tweetUpdate) Spreader ! Tweet("[Update] " + CrudEntry.subject.value, changedSemester.split(" ").map(" #"+_).mkString , newNr)
    S notice "Ihr update wurde gespeichert"
  }

  /**
   * This Views a Confirmscreen, if a User really wants to delete his Entry.
   */
  def delete(xhtml: Node): NodeSeq = {
    try {
      bind("del", xhtml,
        "textarea" -> CrudEntry.news.value,
        "subject" -> CrudEntry.subject.value,
        "verfasser" -> CrudEntry.writer.value,
        "Nr" -> CrudEntry.nr.value,
        "submit" -> submit("Löschen", () => {
          logger info "Entry Nr: " + CrudEntry.nr.value +
                      " was deleted by " + User.currentUserId.openOr("")
          CrudEntry.delete_!
          S notice "Ihr Eintrag wurde gelöscht"
          S redirectTo "/index" }),
        "cancel" -> submit("Abbrechen", () => S.redirectTo("/edit/editieren"))

      )
    } catch {
      case e =>
        logger info e.printStackTrace.toString
        S notice "You shouldn't do this!"
        S redirectTo "/index"
    }
  }

  /**
   * Building the input Forms, with either empty forms or getting the values from an existing Entry.
   */
  def view(xhtml: NodeSeq): NodeSeq = {

    bind("CRUDView", xhtml,
      "date"  -> text(if(CrudEntry.lifecycle.value == "") lifecycleFormat.format(new Date)
                      else CrudEntry.lifecycle.value,
                      date => CrudEntry.lifecycle.set( dateValidator(date) ),
                      "cols" -> "80", "id" -> "datepicker"),
      "textarea" -> textarea(CrudEntry.news.value.toString,
                             CrudEntry.news.set(_),
                             "rows" -> "12", "cols" -> "80",
                             "style" -> "width:100%", "id" -> "entry"),
      "subject" -> text(CrudEntry.subject.value, CrudEntry.subject.set(_)),
      "verfasser" -> S.getSessionAttribute("fullname").open_!.toString,
      "email" -> checkbox(false, if(_) sendEmail = true),
      if(newEntry) "twitter" -> ""
      else "twitter" -> checkbox(true, if(_) tweetUpdate = true),
      if(newEntry) "submit" -> submit("Senden", () => {
        create()
        S.redirectTo("/index")
      })
      else "submit" -> submit("Update", () => {
        update()
        S.redirectTo("/index")
      })
    )
  }

  /**
   * Building the Checkboxes for the Mailinglists.
   * If Crudentry contains any semester, the checkbox is set to true.
   */
  def makeCheckboxList(xhtml: NodeSeq): NodeSeq = {
    loadSemesters(S.attr("semester").open_!) flatMap { sem =>
      bind("List", xhtml,
        "label" -> sem,
        if(CrudEntry contains sem) "checkbox" -> checkbox(true, if (_) changedSemester += sem + " ")
        else "checkbox" -> checkbox(false, if (_) changedSemester += sem + " ")
      )
    }
  }

  var tweetUpdate = false
  object CurrentEntry extends RequestVar[Box[Entry]](Empty)

  private lazy val date = df.format(new Date)
  private val lifecycleFormat = new SimpleDateFormat ("dd.MM.yyyy")
  private var sendEmail = false
  private val tweet = loadProps("Tweet") == "yes"
  private var newEntry = false
  private val df = new SimpleDateFormat ("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US)
  private var changedSemester = ""
}
