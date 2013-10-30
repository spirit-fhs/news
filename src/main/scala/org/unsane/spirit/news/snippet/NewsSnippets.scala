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
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of his contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
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

import java.text.SimpleDateFormat
import net.liftweb.http.S
import java.util.{GregorianCalendar, Date, Calendar, Locale}
import org.unsane.spirit.news.model.BuildInfo

object NewsSnippets {

  private def gc = new GregorianCalendar()

  private def getGregorianTime() = gc.getTime

  private val simpleFormatWeek = new SimpleDateFormat
  private val simpleFormatDay = new SimpleDateFormat("EEEE", Locale.GERMAN)
  private val simpleFormatDate = new SimpleDateFormat

  simpleFormatWeek.applyPattern("ww")
  simpleFormatDate.applyPattern("dd.MM.yyyy")

  def weekString() = simpleFormatWeek.format(getGregorianTime)

  def dayString() = simpleFormatDay.format(getGregorianTime)

  def weekStart() = {
    val gcStart = gc
    gcStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    simpleFormatDate.format(gcStart.getTime)
  }

  def weekEnd() = {
    val gcEnd = gc
    gcEnd.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
    simpleFormatDate.format(gcEnd.getTime)
  }

  def weekNr() = {
    try {
      weekString.toInt
    } catch {
      case _: Throwable => 0
    }
  }

  /**
   * Creating a nice String for Students to see what day it is
   * and what kind of week.
   */
  def week = weekNr match {
    case 0 =>
      <span></span>
    case a if (weekNr % 2 == 0) =>
      <h3>Es ist
        {dayString}
        und eine gerade Woche (KW
        {weekNr}
        - Vom
        {weekStart}
        bis
        {weekEnd}
        ).</h3>
    case b if (weekNr % 2 != 0) =>
      <h3>Es ist
        {dayString}
        und eine ungerade Woche (KW
        {weekNr}
        - Vom
        {weekStart}
        bis
        {weekEnd}
        ).</h3>
    case _ =>
      <span></span>
  }

  /**
   * time creates the current date
   */
  def time = <span>
    {new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date)}
  </span>

  /**
   * hello creates the current fullname (title + lastname). Can only be used with logged in Users!
   */
  def hello = <span>
    {S.getSessionAttribute("fullname").openOr("").toString}
  </span>

  /**
   * code creates the tooltip that pops up next to the editing window
   */
  def code = {
    <span>
      <table>
        <tr>
          <td style="border:0">
            <strong>Formatierung:</strong>
            <br/>
            **bold text**
            <br/>
            __italic text__
            <br/>
            *_ bold italic text _*
            <br/>{"%{color:red}Text in red %"}<br/> <strong>Aufz
            &auml;
            hlung:</strong>
            <br/>
            * bulleted list
            <br/>
            <br/>
            * bulleted list
            <br/>
            ** 2-level
            <br/> <strong>Links:</strong>
            <br/>
            "Link to FhS": http://www.fh-schmalkalden.de</td>
        </tr>
      </table>
    </span>
  }

  /**
   * displays the version of spirit
   */

  def spiritVersion = {
    <h4 class="alt">Spirit Version:
      {BuildInfo.version}
    </h4>
  }

}
