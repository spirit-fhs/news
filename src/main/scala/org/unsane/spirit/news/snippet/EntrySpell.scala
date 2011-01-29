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
package org.unsane.spirit.news.snippet

import org.unsane.spirit.news.model.Entry
import java.util._
import java.text._

/*
 * @author Marcus Denison
 */
class EntrySpell(firstEntry: Entry) extends Ordered[Entry] {

  /**
   * Comparing the Entry numbers.
   * */
  def compare(that: Entry) =
    this.firstEntry.nr.value.toInt compare that.nr.value.toInt

  /**
   * Making it easier to compare the Dates from an Entry.
   * @param secondEntry Entry to be compared.
   * @return Boolean
   * */
  def before(secondEntry: Entry): Boolean = {
    df.parse(firstEntry.date.value.toString) before df.parse(secondEntry.date.value.toString)
  }

  /**
   * Making it easier to compare the Dates from an Entry.
   * @param secondEntry Entry to be compared.
   * @return Boolean
   * */
  def after(secondEntry: Entry): Boolean = {
    df.parse(firstEntry.date.value.toString) after df.parse(secondEntry.date.value.toString)
  }

  /**
   * Comparing Entry Authors, making live once easier here!
   * @param secondEntry
   * @return Boolean
   * */
  def hasSameAuthor(secondEntry: Entry): Boolean = {
    firstEntry.name.value.toString == secondEntry.name.value.toString
  }

  /**
   * Making it easier if we want to compare a name from an Entry from a User we Allready know.
   * */
  def hasSameAuthor(user: String): Boolean = {
    firstEntry.name.value.toString == user
  }

  /**
   * Making it easier to check if a given Entry contains a specific semester.
   * @param semester
   * @return Boolean
   * */
  def contains(semester: String): Boolean = {
    firstEntry.semester.toString.split(" ").contains(semester)
  }

  private val df = new SimpleDateFormat ("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US)
}
