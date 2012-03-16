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
package model

import java.util.Properties
import java.io._
import collection.mutable.ArrayBuffer

/**
 * Could have used Lift's Props, but using java.util.Properties fitted the solution in a better way.
 * @author Marcus Denison
 */
trait Config {

  /**
   * loadSemesters loads the current semesters
   * @return List[String]
   */
  def loadSemesters(major: String): List[String] = {
    props load new FileInputStream(configFile)
    props.getProperty(props.getProperty("Semester") + "_" + major).split(";").toList
  }

  lazy val allSemesterAsList4News = {
    loadSemesters("BaI").take(3) :::
    loadSemesters("BaWI").take(3) :::
    loadSemesters("BaITS") :::
    loadSemesters("BaMuMa") :::
    loadSemesters("Ma")
  }

  /**
   * loadEmails loads the mailings lists
   * takes an Array full with semesters and gehts the mailing lists for them
   * @param sem Array[String]
   * @return Array[String]
   */
  def loadEmails(sem: Array[String]): Array[String] = {
    props load new FileInputStream(configFile)
    var studentEmails = ArrayBuffer[String]()
    for(s <- sem) studentEmails += props getProperty s
    studentEmails toArray
  }

  def loadSchedule(major: String): Array[String] = {
    props load new FileInputStream(configFile)
    props.getProperty(props.getProperty("Semester") + "_" + major).split(";")
  }

  /**
   * loadProps
   * @param prop Name Property that shall be loaded as a String
   * @return String Property that was loaded
   */
  def loadProps(prop: String): String = {
    props load new FileInputStream(configFile)
    props getProperty prop
  }

   // taken from http://code-redefined.blogspot.com/2009/05/md5-sum-in-scala.html
  def md5SumString(str: String) : String = {
    import java.security.MessageDigest
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(str.getBytes)
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }

  /**
   * @return List[String] All valid classnames for the given semester.
   */
  lazy val allClassNamesAsLowercase: List[String] = {
    loadSchedule("I").map(_.toLowerCase).toList :::
    loadSchedule("WI").map(_.toLowerCase).toList :::
    loadSchedule("ITS").map(_.toLowerCase).toList :::
    loadSchedule("MUMA").map(_.toLowerCase).toList :::
    loadSchedule("MA").map(_.toLowerCase).toList ::: Nil
  }

  /**
   * Saving to changeable.properties!
   * Only for schedule panic button!
   */
  def saveProps(prop: String, value: String) {
    val wProps = new Properties
    wProps.load(new FileInputStream(changeableFile))
    wProps.setProperty(prop, value)
    wProps.store(new FileOutputStream(changeableFile), "Changed by: " + User.currentUserId.openOr("John Doe") + " at " + new java.util.Date)
  }

  /**
   * Reading from changeable.properties.
   * Only for schedule panic button!
   */
  def loadChangeableProps(props: String): String = {
    val wProps = new Properties
    wProps.load(new FileInputStream(changeableFile))
    wProps.getProperty("schedule")
  }

  private val props = new Properties
  private val changeableFile = System.getProperty("user.dir") + "/changeable.properties"
  private val configFile = System.getProperty("user.dir") + "/settings.properties"
}
