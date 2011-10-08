package org.unsane.spirit.news.lib

import actors.Actor
import net.liftweb.http.js.JsCmd
import java.io.{InputStreamReader, File, BufferedReader}
import net.liftweb.http.S
import net.liftweb.common.Loggable
import org.unsane.spirit.news.model.Config


object ScheduleParsingHelper {

  def apply() = {
    new ScheduleParsingHelper
  }
}

/**
 * Runs timetable2db, catches all output and errors from the ProcessBuilder
 * logs it and hopefully works.
 */
class ScheduleParsingHelper extends Loggable with Config {

  class ParsingLogger(in: BufferedReader) extends Actor {

    def act {
      loop {
        if (in.ready())
          logger info "Parsing Output: " + in.readLine()

      }
    }
  }

  def runParser(in: String): JsCmd = {

    in match {
      case "alle" => allClassNamesAsLowercase map parseSchedules _
      case s: String => parseSchedules(s)
      case _ =>
    }

    S.redirectTo("/scheduleAdmin/schedulepreview")
    S.notice("Schedules hopefully parsed!")
  }

  private def parseSchedules(course: String) = {

    val parser = loadProps("timetable2db.parser")
    val schedulePath = loadProps("timetable2db.schedulePath")
    val parsedPath = loadProps("timetable2db.parsedPath")
    val execPath = loadProps("timetable2db.execPath")

    // Full Path to the command we will be executing.
    val p = new ProcessBuilder(parser, schedulePath + "s_" + course + ".html",
                               course, parsedPath)

    // Full Path where the command shall be executed.
    p.directory(new File(execPath))

    val pr = p.start()

    val input = new BufferedReader(new InputStreamReader(pr.getInputStream))
    val error = new BufferedReader(new InputStreamReader(pr.getErrorStream))

    val inputActor = new ParsingLogger(input)
    val errorActor = new ParsingLogger(error)

    inputActor.start()
    errorActor.start()

    pr.waitFor()

  }

}