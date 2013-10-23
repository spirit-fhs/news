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
import net.liftmodules.textile._
import net.liftweb.json.JsonDSL._
import model.{Config, Entry}
import net.liftweb.common.{Empty, Box, Full, Loggable}
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.js.{JE,JsExp}

/**
 * @author Marcus Denison
 */
class ViewNews extends SpiritHelpers with Loggable with Config {

  /**
   * Pattern Matching if a search has begun.
   * If not, all News will be returned.
   */
  lazy val news: List[Entry] = S.param("search") match {

    case Full(s) =>
      logger info ("Searching for " + s + "!")
      val validSearch =
        loadSemesters("BaI") :: loadSemesters("BaWI") ::
        loadSemesters("BaITS") :: loadSemesters("BaMuMa") :: loadSemesters("BaMC") ::
        loadSemesters("Ma") :: loadSemesters("Other") :: Nil

      if (validSearch.flatten.contains(s)) {
        Entry.findAll.filter { entry =>
          entry.contains(s) || entry.contains("semester")
        }.sortWith(
          (entry1, entry2) => (entry1 > entry2)
        )
      } else {
          Entry.find("nr" -> s) match {
            case Full(x) => List(x)
            case _ => Entry.findAll.sortWith(
                        (entry1, entry2) => (entry1 > entry2)
                      )
          }
        }

    case _ =>
      Entry.findAll.sortWith(
        (entry1, entry2) => (entry1 > entry2)
      )
  }

  /**
   * Adding a dropdown menu to the ViewNews in order
   * to achieve a better user experience when searching
   * for news.
   */
  def classNameChooser() = {

    val classNames =
      "alle" :: allSemesterAsList4News zip  "Alle" :: allClassNamesAsLowercase

    val (name2, js) = SHtml.ajaxCall(JE.JsRaw("this.value"),
                                     s => (S.redirectTo("/semsearch/" + s))): (String, JsExp)

    SHtml.select(classNames.toSeq, Full(S.param("search").openOr("Alle")), x => x, "onchange" -> js.toJsCmd)

  }

  def render = {

   ".entry" #> news.map( entry =>
     ".writer"    #> entry.writer.value.toString &
     ".subject"   #> <a href={"/entry/"+entry.nr.value.toString}>
                     {entry.subject.value.toString}</a> &
     ".nr"        #> entry.nr.value.toString &
     ".lifecycle" #> entry.lifecycle.value.toString &
     ".date"      #> Text(entry.date.value.toString.substring(4, 11) + ". " +
                          entry.date.value.toString.substring(17, 22)) &
     ".semester"  #> sem2link(semesterChanger(entry.semester.value.toString).split(" ")) &
     ".news"      #> TextileParser.toHtml(entry.news.value.toString))

  }

}
