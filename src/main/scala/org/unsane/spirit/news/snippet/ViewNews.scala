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
import net.liftweb.textile._
import model.Entry

/**
 * @author Marcus Denison
 */
object ViewNews {
  
  /**
   * semesterChanger is only changing the output from semester -> Alle, cause it looks nicer on the webpage! 
   */
  def semesterChanger(input: String): String = {
    if(input.startsWith("semester ")) { "Alle" }
    else { input }
  }
}

class ViewNews extends SpiritHelpers {

  /**
   * Binds all entries!
   */
  def view (xhtml : NodeSeq) : NodeSeq = {
    val news2 = Entry.findAll.reverse
    
    news2.flatMap(entry =>
      bind("entry", xhtml,
	      "writer" -> entry.writer.value.toString,
        "subject" -> entry.subject.value.toString,
        "nr" -> entry.nr.value.toString,
        "lifecycle" -> entry.lifecycle.value.toString,
        "date" -> Text(entry.date.value.toString.substring(4, 11) + ". " + entry.date.value.toString.substring(17, 22)),
        "semester" -> sem2link(ViewNews.semesterChanger(entry.semester.value.toString).split(" ")),
        "news" -> TextileParser.toHtml(entry.news.value.toString)))
  }
}
