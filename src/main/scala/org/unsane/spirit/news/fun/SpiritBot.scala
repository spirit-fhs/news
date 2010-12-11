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
package fun

import java.net._
import java.io._
import java.util._
import scala.actors.Actor
import scala.actors.Actor._
import org.unsane.spirit.news.snippet._
import scala.xml._
import org.unsane.spirit.news.model._

/**
  * SpiritBot is a IRC Bot that sends the News to a Channel on the IRC Network
  * This is a Geeky/Nerdy Addon ;)! But not using it in production mode!
  * @author Marcus Denison
  */
object SpiritBot extends Actor with Config {

  def connect {
    try{
      socket = new Socket(server, port)
      out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
      in = new BufferedReader(new InputStreamReader(socket.getInputStream))
      start
      actor {
        while(connected && in != null)
          SpiritBot.this ! in.readLine
          print(in.readLine)
      }
      say(msg = "NICK " + botName)
      say(msg = "USER " + botName + " hostname " + server + " :Hallo" )
      say(msg = "JOIN " + channel)
    } catch {
      case a: UnknownHostException => a
        println(errMsg + "Ups, can't see " + server)
      case b: NoRouteToHostException => b
        println(errMsg + "Ups, can't see " + server)
    }
  }


  def act {
    loop {
      react {
        case msg: String if(msg startsWith "PING") => ping(msg)
        case msg: String if(msg startsWith "NEWS") => say(channel, msg)
        case msg: String if((msg contains "PRIVMSG " + channel) && (msg contains botName)) => say(channel,"I am only here for the NEWS, so leave me alone!")
        case msg: String if((msg contains "PRIVMSG " + channel) && (msg contains "NEWS on Spirit?")) => say(channel,getNews)
        case s: String => println(s)
      }
    }
  }

  private def say(chan: String = "", msg: String) {
    if (chan == "") out.write("PRIVMSG " + chan + " :" + msg)
    else out.write(msg)
    out.newLine
    out.flush
  }

  private def ping(msg: String) =
    say(msg = "PONG :" + (msg split ":")(0))

  private def getNews() : String = {
    val news = Entry.findAll
    if(news.nonEmpty)
      news(0).subject + " from " + news(0).writer + " is the latest news i have!"
    else
      "Nothing new"
  }
  private def connected = socket != null

  private var socket:Socket = _
  private var out: BufferedWriter = _
  private var in: BufferedReader = _
  private val server = loadProps("server")
  private val port: Int = loadProps("port").toInt
  private val channel = loadProps("channel")
  private val botName = loadProps("botName")
  private val welcomeMsg = loadProps("welcomeMsg")
  private val errMsg = "[SpiritBot] -- "
}
