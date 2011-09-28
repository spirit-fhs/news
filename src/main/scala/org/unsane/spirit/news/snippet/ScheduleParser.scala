package org.unsane.spirit.news.snippet

import net.liftweb.util.BindHelpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.{JsonCall, JsCmd}
import net.liftweb.http.js.JE.{JsRaw, Call}
import net.liftweb.http.{SHtml, S}
import org.unsane.spirit.news.lib.ScheduleParsingHelper

/**
 * Rendering view for importing Data via timetable2db.
 */
class ScheduleParser {

    lazy val sph = ScheduleParsingHelper()

    def render = {

      "type=submit" #>
        SHtml.submit("Parser Starten!", () => sph.runParser(""),
                     "onClick" -> (JsShowId("ajax-loader") & JsHideId("hint") &
                                   JsRaw("$('#do_submit').attr('hidden', 'true')")).toJsCmd,
                     "disabled" -> "true")

    }


}