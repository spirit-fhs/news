package org.unsane.spirit.news.snippet

import net.liftweb.util.BindHelpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.{JsRaw, Call}
import net.liftweb.http.{SHtml, S}
import org.unsane.spirit.news.lib.ScheduleParsingHelper
import net.liftweb.common.Full
import net.liftweb.http.js.{JE, JsonCall, JsCmd,JsExp}

/**
 * Rendering view for importing Data via timetable2db.
 */
class ScheduleParser {

    lazy val sph = ScheduleParsingHelper()
    val schedules = Seq(("new", "Neuer Stundenplan"), ("old", "Alter Stundenplan"))
    val scheduleType = sph.loadChangeableProps("schedule")

    val (name2, js) = SHtml.ajaxCall(JE.JsRaw("this.value"),
                                     s => (sph.saveProps("schedule", s))): (String, JsExp)

    val classNames = "alle" :: sph.allClassNamesAsLowercase

    def render = {
      "name=scheduleSwitch" #> SHtml.select(schedules, Full(scheduleType), x => x, "onchange" -> js.toJsCmd)
    }

}
