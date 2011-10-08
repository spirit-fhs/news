package org.unsane.spirit.news.snippet

import net.liftweb.util.BindHelpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.{JsRaw, Call}
import net.liftweb.http.{SHtml, S}
import org.unsane.spirit.news.lib.ScheduleParsingHelper
import net.liftweb.common.Full
import net.liftweb.http.js.{JE, JsonCall, JsCmd}

/**
 * Rendering view for importing Data via timetable2db.
 */
class ScheduleParser {

    lazy val sph = ScheduleParsingHelper()
    val schedules = Seq(("new", "Neuer Stundenplan"), ("old", "Alter Stundenplan"))
    val scheduleType = sph.loadChangeableProps("schedule")

    val (name2, js) = SHtml.ajaxCall(JE.JsRaw("this.value"),
                                     s => (sph.saveProps("schedule", s)))

    val classNames = "alle" :: sph.allClassNamesAsLowercase

    def render = {

      "name=classNameChooser" #> SHtml.select(classNames.map(s => (s, s)), Full("alle"), sph.runParser(_)) &
      "type=submit" #>
        SHtml.submit("Parser Starten!", () => (),
                     "onClick" -> (JsShowId("ajax-loader") & JsHideId("hint") & JsHideId("hint2") & JsHideId("classNameChooser") &
                                   JsRaw("$('#do_submit').attr('hidden', 'true')") &
                                   JsRaw("$('#scheduleSwitch').attr('hidden', 'true')")).toJsCmd) &
      "name=scheduleSwitch" #> SHtml.select(schedules, Full(scheduleType), x => x, "onchange" -> js.toJsCmd)
    }

}
