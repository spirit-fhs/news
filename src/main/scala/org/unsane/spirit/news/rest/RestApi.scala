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

import org.unsane.spirit
import model.Entry

import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.json
import net.liftweb.common.{Full, Loggable}
import lib.Schedule

object RestApi extends RestHelper with Loggable {

  logger info "Rest is now online."

  serve {

    /**
     * Rest Interface in order to put the JSON schedule into the DB.
     */
    case "scheduleapi" :: "fileupload" ::  _ JsonPut jsonfile -> _ => {
      logger.warn("Fileupload is used. Please check if the Schedule is fine!")
      Schedule.import2DatabaseQueue(json.compact(json.render(jsonfile))) match {
        case true => JsonResponse(("upload" -> "true!"), Nil, Nil, 200)
        case false => JsonResponse(("upload" -> "false!"), Nil, Nil, 200)
      }

    }

    /**
     * this is a test, looking that REST-Api is doing his job
     */
    case "test" :: Nil Get req => {
      JsonResponse(("test" -> "test!"), Nil, Nil, 200)
    }

    /**
     * get all News
     */
    case "news" :: Nil Get req => {
      JsonResponse(Response.getAllNews(req.params), Nil, Nil, 200)
    }

    /**
     * get one News
      */
    case "news" :: AsLong(id) :: Nil Get req => {
      val response = Response.getOneNews(id.toString())

      response match {
        case Full(x) => JsonResponse(x, Nil, Nil, 200)
        case _ => JsonResponse("exception" -> "this id is not valid", Nil, Nil, 404)
      }

    }

  }

}