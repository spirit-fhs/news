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
package rest

import org.unsane.spirit.news
import model._

import net.liftweb.json.Serialization.write

import net.liftweb.json.JsonDSL._
import net.liftweb.common.Box
import net.liftweb.json.{JArray}
import net.liftweb.common.Loggable


import java.util.{GregorianCalendar,Date, Locale}

import java.text.SimpleDateFormat
import net.liftweb.json.JsonAST.{JField, JValue, JObject, JString}

object Response extends Loggable {

  implicit val formats = net.liftweb.json.DefaultFormats

  private[this] def afterDate(date1Param: String, date2Param: String): Boolean = {

    val sdf1 = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US)
    val date1 = sdf1.parse(date1Param)

    val sdf2 = new SimpleDateFormat("yyyyMMdd", Locale.US)
    val date2 = sdf2.parse(date2Param)

    date1.after(date2)
  }
  
  private[this] def makeJValue(entry: EntryPreview)  : JValue = {

    ("nr", entry.nr) ~ ("date", entry.date) ~ ("writer", entry.writer) ~ ("subject", entry.subject) ~ ("semester", entry.semester)
  }

  private[this] def makePreview(entry: Entry) : EntryPreview = {
    new EntryPreview(
      entry.nr.value,
      entry.date.value,
      entry.writer.value,
      entry.subject.value,
      entry.semester.value)
  }

  private[this] def filterNews(entries: List[Entry]) : List[EntryPreview] = {
    entries.map(makePreview _)
  }

  private[this] def filterSemester(entries: List[Entry], key: String): List[Entry] = {
    entries.filter{entry => entry.semester.value.split(" ") contains key
    }
  }

  private[this] def filterDateEntryPreview(entries: List[EntryPreview], key: String): List[EntryPreview] = {
    entries.filter{entry => afterDate(entry.date,key)}
  }

  private[this] def filterDate(entries: List[Entry], key: String): List[Entry] = {
    entries.filter{entry => afterDate(entry.date.value,key)}
  }

  def getPreviewNews(params: Map[String, List[String]]): JArray = {

    val news = Entry.findAll

    val newsPreview = params.get("date") match {
      case Some(x) => filterDateEntryPreview(filterNews(news), x.head)
      case _ => filterNews(news)
    }

    JArray(newsPreview.map(makeJValue _))
  }
  
  def getAllNews(params: Map[String, List[String]]) : JArray = {

    if(params.isEmpty) {
      logger info  "entry"
      JArray(Entry.findAll.map(_.asJValue))
    }
    else {
      logger info "Hello World!"

      var news = Entry.findAll
      
      news = params.get("semester") match {
        case Some(x) => filterSemester(news, x.head)
        case _ => news
      }

      news = params.get("date") match {
        case Some(x) => filterDate(news, x.head)
        case _ => news
      }

      JArray(news.map(_.asJValue))
    }
  }

  def getOneNews(id: String): Box[JValue] = {
    Entry.find("nr" -> id).map{x => x.asJValue}
  }

  def getSchedule(className: String, week: String) : JArray = {
    val classSchedule = ScheduleRecord.findAll.filter {
        x => x.className.value.toLowerCase == className }

    val in = classSchedule.filterNot( x =>
        x.appointment.get.week.toLowerCase == week )

    in.map(_.asJValue)
  }

}
