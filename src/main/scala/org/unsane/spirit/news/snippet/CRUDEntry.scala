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
import net.liftweb.http.{ S }
import net.liftweb.http.SHtml._
import net.liftweb.http._
import js.JsCmds._
import net.liftweb.common._
import net.liftmodules.textile._
import java.text._
import java.util.Date
import java.util.Locale
import net.liftweb.json.JsonDSL._
import model._
import net.liftweb.util.Props
import scala.concurrent.stm._

class CRUDEntry extends Loggable with SpiritHelpers with Config with EntryPreview {

  var tweetUpdate = false
  private lazy val date = df.format(new Date)
  private val lifecycleFormat = new SimpleDateFormat("dd.MM.yyyy")
  private var sendEmail = false
  private val tweet = loadProps("Tweet") == "yes"

  private var newEntry = Ref(true)

  private val df = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US)
  private var changedSemester = ""
  private val emailing = loadProps("Emailing") == "yes"

  object CurrentEntry extends RequestVar[Box[Entry]](Empty)

  /**
   * CrudEntry is either a new Entry or a an existing Entry to be Updated!
   */
   val CrudEntry =
    atomic {
      implicit txn =>
        CurrentEntry.get match {
          case Full(entry) =>
            logger info "Entry was found: " + entry.nr.value + "!"
            newEntry() = false
            entry
          case Empty =>
            logger info "Creating new Entry to save later!"
            newEntry() = true
            Entry.createRecord
          case _ =>
            logger info "This should not have happend, but why did it?!"
            newEntry() = true
            Entry.createRecord
        }
    }
  /**
   * Lists all Entries for the logged in User!
   */
  def viewUserEntries(xhtml: NodeSeq): NodeSeq = {
    <table>
      <tr>
        <th>Nr:</th>
        <th>Verfasser:</th>
        <th>Betreff:</th>
        <th>Vom:</th>
        <th>Optionen:</th>
      </tr>
      {
        Entry.findAll("name" -> User.currentUserId.openOr("").toString).sortWith(
          (entry1, entry2) => (entry1 > entry2)).flatMap(v =>
            <tr>
              <td style="border:0">{ v.nr.value.toString }</td>
              <td style="border:0">{ v.writer.value.toString }</td>
              <td style="border:0">{ v.subject.value.toString }</td>
              <td style="border:0">{ v.date.value.toString.substring(4, 11) + ". " + v.date.value.toString.substring(17, 22) }</td>
              <td style="border:0">{ link("/edit/edit", () => CurrentEntry(Full(v)), Text("Edit")) }</td>
              <td style="border:0">{ link("/edit/delete", () => CurrentEntry(Full(v)), Text("Delete")) }</td>
            </tr>)
      }
    </table>
  }

  /**
   * If CrudEntry is a new Entry, create() does the rest of setting values,
   * saving, spreading it via email and twitter.
   */
  def create() {
    lazy val nr = if (EntryCounter.findAll.isEmpty) "1" else EntryCounter.findAll.head.counter.toString

    CrudEntry.date.set(date)
    CrudEntry.name.set(User.currentUserId.openOr("Oops!"))
    CrudEntry.semester.set(changedSemester)
    CrudEntry.nr.set(nr)
    if (CrudEntry.subject.value.trim.isEmpty) {
      CrudEntry.subject.set((
        CrudEntry.news.value./:(("", 0)) { (o, i) =>
          if (o._2 > 20) o
          else (o._1 + i, o._2 + 1)
        }._1 + "...").replace("\n", " "))
      logger warn "Setting subject cause it was empty!"
    }
    CrudEntry.save

    val count = if (EntryCounter.findAll.isEmpty) EntryCounter.createRecord else EntryCounter.findAll.head
    count.counter.set((nr.toInt + 1).toString)
    count.save

    logger info "Entry was created by " + User.currentUserId.openOr("")
    if (sendEmail && changedSemester.nonEmpty) {
      logger info "News should be sent via eMail!"
      MailHandler.send(TextileParser.toHtml(CrudEntry.news.value.toString).toString, CrudEntry.subject.value, loadEmails(changedSemester.split(" ")))
    }
    if (tweet) {
      logger info "News should be spread via Twitter!"
      Spreader ! Tweet(CrudEntry.subject.value, changedSemester.split(" ").map(" #" + _).mkString, nr)
    }
  }

  /**
   * If CrudEntry is not a new Entry, update() does the rest of setting values,
   * updating, spreading it via email and twitter.
   */
  def update() {
    val oldNr = CrudEntry.nr.value
    val newNr =
      if (tweetUpdate)
        if (EntryCounter.findAll.isEmpty) "1"
        else EntryCounter.findAll.head.counter.toString
      else oldNr

    CrudEntry.date.set(date)
    CrudEntry.semester.set(changedSemester)
    CrudEntry.name.set(User.currentUserId.openOr("Oops!"))
    CrudEntry.nr.set(newNr)
    CrudEntry.save

    if (newNr != oldNr) {
      val count =
        if (EntryCounter.findAll.isEmpty) EntryCounter.createRecord
        else EntryCounter.findAll.head
      count.counter.set((newNr.toInt + 1).toString)
      count.save
    }

    logger info "Semesters: " + changedSemester
    logger info "Entry was updated by " + User.currentUserId.openOr("")
    if (sendEmail) MailHandler.send(TextileParser.toHtml(CrudEntry.news.value).toString,
      "[Update] " + CrudEntry.subject.value, loadEmails(changedSemester split (" ")))
    if (tweet && tweetUpdate) Spreader ! Tweet("[Update] " + CrudEntry.subject.value, changedSemester.split(" ").map(" #" + _).mkString, newNr)
    S notice "Ihr update wurde gespeichert"
  }

  /**
   * This Views a Confirmscreen, if a User really wants to delete his Entry.
   */
  def delete = {
    try {
      "name=textarea" #> CrudEntry.news.value &
        "name=subject" #> CrudEntry.subject.value &
        "name=verfasser" #> CrudEntry.writer.value &
        "name=nr" #> CrudEntry.nr.value &
        "type=submit" #> submit("Löschen", () => {
          logger info "Entry Nr: " + CrudEntry.nr.value +
            " was deleted by " + User.currentUserId.openOr("")
          CrudEntry.delete_!
          S notice "Ihr Eintrag wurde gelöscht"
          S redirectTo "/index"
        }) &
        "type=cancel" #> submit("Abbrechen", () => S.redirectTo("/edit/editieren"))

    } catch {
      case e: Throwable =>
        logger warn e.printStackTrace.toString
        S notice "You shouldn't do this!"
        S redirectTo "/index"
    }
  }

  /**
   * Building the input Forms, with either empty forms or getting the values from an existing Entry.
   */
  def view = {
    atomic {
      implicit txn =>
        emailing match {
          case true =>
          case false =>
            S.warning("E-Mailing ist deaktiviert.")
        }

        "name=date" #> text(if (CrudEntry.lifecycle.value == "") lifecycleFormat.format(new Date)
        else CrudEntry.lifecycle.value,
          date => CrudEntry.lifecycle.set(dateValidator(date)),
          "cols" -> "80", "id" -> "datepicker") &
          "name=textarea" #> textarea(CrudEntry.news.value.toString,
            CrudEntry.news.set(_),
            "rows" -> "12", "cols" -> "80",
            "style" -> "width:100%", "id" -> "entry") &
            "name=subject" #> text(CrudEntry.subject.value, CrudEntry.subject.set(_)) &
            "name=verfasser" #> text(if (CrudEntry.writer.value == "") S.getSessionAttribute("fullname").openOr("")
            else CrudEntry.writer.value, CrudEntry.writer.set(_)) &
            "name=email" #> checkbox(false,
              if (_) sendEmail = true,
              if (S.getSessionAttribute("email").openOr("") == "not-valid") "disabled" -> "disabled"
              else if (!emailing) "disabled" -> "disabled"
              else "enabled" -> "enabled") &
              "name=twitter" #> checkbox(true, if (_) tweetUpdate = true) &
              (if (newEntry()) "type=submit" #> submit("Senden", () => {
                create()
                S.redirectTo("/index")
              })
              else "type=submit" #> submit("Update", () => {
                update()
                S.redirectTo("/index")
              }))
    }
  }

  /**
   * Building the Checkboxes for the Mailinglists.
   * If Crudentry contains any semester, the checkbox is set to true.
   */
  def makecheckboxlist(in: NodeSeq): NodeSeq = {
    (".checkbox_row" #> loadSemesters(S.attr("semester").openOr("")).toList.map(sem =>
      ".title" #> sem &
        (if (CrudEntry contains sem) ".checkbox" #> checkbox(true, if (_) changedSemester += sem + " ")
        else ".checkbox" #> checkbox(false, if (_) changedSemester += sem + " ")))).apply(in)
  }

}
