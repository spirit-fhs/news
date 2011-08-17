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

import org.specs._
import model._
import snippet._
import net.liftweb.http.{S, LiftSession}
import net.liftweb.mapper.BaseMetaMapper
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.mongodb._
import specification.Contexts

import net.liftweb.record.field.StringField
import net.liftweb.json.JsonAST._
import net.liftweb.json.Extraction._
import net.liftweb.json.Printer._



object NewsSpec extends Specification with Config with Contexts {

  //implicit val formats = net.liftweb.json.DefaultFormats

  /* Defining our Database for Testing */
  MongoDB.defineDbAuth(DefaultMongoIdentifier,
    MongoAddress(MongoHost("127.0.0.1", 27017), "spirit_news"),
    "spirit_news",
    "spirit_news")

  /* Line 54 - 81 is from the Lift Wiki! https://www.assembla.com/wiki/show/liftweb/Unit_Testing_Snippets_With_A_Logged_In_User! THANKS!*/
  val session = new LiftSession("", StringHelpers.randomString(20), Empty)

  def inSession(a: => Any) = {
    S.initIfUninitted(session) { a }
  }

  def loginUser = inSession {
    User.logUserIdIn("SpecUser")
  }

  /* Defining what we'll do before and after an example.*/
  new SpecContext {
    beforeExample {
      val count = if(EntryCounter.findAll.isEmpty) EntryCounter.createRecord else EntryCounter.findAll.head
      count.counter.set( "0" )
      count.save
      Entry.findAll.foreach(_.delete_!)
      loginUser
    }
    afterExample {
     val count = if(EntryCounter.findAll.isEmpty) EntryCounter.createRecord else EntryCounter.findAll.head
     count.counter.set( "0" )
     count.save
     Entry.findAll.foreach(_.delete_!)
    }
    aroundExpectations(inSession(_))
  }

  "CrudEntry" should {
    val CrudCreate = new CRUDEntry
    CrudCreate.CrudEntry.lifecycle.set("10.10.2012")
    CrudCreate.CrudEntry.news.set("Test News")
    CrudCreate.CrudEntry.subject.set("NewsSpec")

    val CrudUpdate = new CRUDEntry

    "create and store one Entry." in {
      CrudCreate.create()
      Entry.findAll.filter(e =>
        e.nr.value.toInt == (EntryCounter.findAll.head.counter.value.toInt - 1)
      ).size mustEqual 1
    }

    "toJSON one Entry." in {

    }

    "read all entries for the SpecsUser." in {
      Entry.findAll.filter(e =>
        e hasSameAuthor User.currentUserId.open_!
      ).forall(_.name.value mustEqual User.currentUserId.open_!) mustBe true
    }

    "delete all entries from the SpecUser." in {
      Entry.findAll.filter(e =>
        e hasSameAuthor User.currentUserId.open_!
      ).foreach(e => e.delete_!)

      Entry.findAll.filter(e =>
        e hasSameAuthor User.currentUserId.open_!
      ).size mustEqual 0
    }

    "update an existing Entry with inc its number (Needed for Twitter)." in {
      CrudCreate.create()

      val oldEntry = Entry.findAll.head
      val oldNr = oldEntry.nr.value.toInt + 1

      CrudUpdate.CurrentEntry(Full(oldEntry))
      CrudUpdate.tweetUpdate = true
      CrudUpdate.update()

      Entry.findAll.head.nr.value mustEqual oldNr.toString
    }

    "update an existing Entry without inc its number (If not Tweeting Update)." in {
      CrudCreate.create()

      val oldEntry = Entry.findAll.head
      val oldNr = oldEntry.nr.value.toInt

      CrudUpdate.CurrentEntry(Full(oldEntry))
      CrudUpdate.tweetUpdate = false
      CrudUpdate.update()

      Entry.findAll.head.nr.value mustEqual oldNr.toString
    }
  }
}
