package org.unsane.spirit.news
package snippet

import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import org.unsane.spirit.news.model.Config
import org.unsane.spirit.news.model.{ScheduleRecordQueue, ScheduleRecord}
import xml.NodeSeq
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE.{JsRaw, Call}
import net.liftweb.common.{Full, Box}
import net.liftweb.http.js.{JE, JsonCall, JsCmd}

/**
 * Rendering the Schedule for a given classname and week.
 *
 * @TODO This is a WIP, still work to be done!
 */
class Schedule extends Config {

  loadChangeableProps("schedule") match {
    case "new" =>
    case "old" => S.redirectTo("/stundenplan/index")
    case _ => S.redirectTo("/")
  }

  sealed abstract class period(time: String, schedule: List[ScheduleRecord]) {
    val periodSchedule = schedule.filter {
      x => x.appointment.get.time.trim().replaceAll(" ", "") == time
    }
    val Monday = periodSchedule.filter { x =>
      x.appointment.get.day.trim == "Montag"
    }
    val Tuesday = periodSchedule.filter { x =>
      x.appointment.get.day.trim == "Dienstag"
    }
    val Wednesday = periodSchedule.filter { x =>
      x.appointment.get.day.trim == "Mittwoch"
    }
    val Thursday = periodSchedule.filter { x =>
      x.appointment.get.day.trim == "Donnerstag"
    }
    val Friday = periodSchedule.filter { x =>
      x.appointment.get.day.trim == "Freitag"
    }
  }

  val className = S.param("classname").openOr("").toLowerCase

  className match {
    case s if (allClassNamesAsLowercase contains s) =>
    case "" => S.redirectTo("/schedule?classname=" + allClassNamesAsLowercase.headOr("bai1"))
    case _ => S.redirectTo("/404")
  }

  val week = S.param("week").openOr("").toLowerCase match {
    case "u" => "g"
    case "g" => "u"
    case "w" => "m"
    case _ => NewsSnippets.weekNr match {
      case even if (even % 2 == 0) => "u"
      case odd if (odd % 2 != 0) => "g"
      case _ => "m"
    }
  }

  val classSchedule = ScheduleRecord.findAll.filter { x =>
    x.className.value.toLowerCase == className }.filterNot { x =>
      x.appointment.get.week.toLowerCase == week
    }

  /**
   * Each object represents a given period from Monday - Friday.
   * Example: object one represents the first period from Monday - Friday.
   * Since there won't be any classes given after 2100 Hours it is
   * necessary to only have seven objects here.
   */
  object one extends period("08.15-09.45", classSchedule)
  object two extends period("10.00-11.30", classSchedule)
  object three extends period("11.45-13.15", classSchedule)
  object four extends period("14.15-15.45", classSchedule)
  object five extends period("16.00-17.30", classSchedule)
  object six extends period("17.45-19.15", classSchedule)
  object seven extends period("19.30-21.00", classSchedule)

  /**
   * @param List[ScheduleRecord] Should be one or more Events as a List.
   * @return NodeSeq The Events surrounded with Tags for prettiness.
   */
  def mkPrettyEvent(schedule: List[ScheduleRecord]): NodeSeq = {

    schedule map { x =>

      val cycle = x.appointment.get.week match {
        case "g" => "even"
        case "u" => "odd"
        case _ => "weekly"
      }

      val icon = x.eventType.get.toLowerCase match {
        case "uebung" => "tutorial"
        case _ => "lecture"
      }
      
      <div class={"event " + cycle}>
       <span class={"eventTitle "}>{x.titleShort.get}</span><div class={icon}></div>
       <div style="clear:both"></div>
       {(if (x.group.value.replaceAll("""\u00A0""", "") == "") ""
           else <div>{"Gruppe: " + x.group.value.replaceAll("""\u00A0""", "")}</div>)}
       <div style="float:left">{x.appointment.get.location.place.building + ":" + x.appointment.get.location.place.room}</div>
       <div style="float:right">{x.member.get.map(_.name.replaceAll("_","")).mkString(" ")}</div>
       <div style="clear:both"></div>
      </div>
    }
  }

  def selectClassnameBox = {

    val (name2, js) = SHtml.ajaxCall(JE.JsRaw("this.value"),
                                     s => (S.redirectTo("/schedule?classname=" + s)))

    SHtml.select(allClassNamesAsLowercase.map(x => (x,x)), Full(className),
                 x => x, "onchange" -> js.toJsCmd)
  }

  def selectWeekBox = {

    val (name2, js) = SHtml.ajaxCall(JE.JsRaw("this.value"),
                                     s => (S.redirectTo("/schedule?classname=" + className + "&week=" + s)))

    val weekSeq = Seq(("g", "Gerade"),("u", "Ungerade"),("w", "Alles"))

    SHtml.select(weekSeq,
      (week match {
        case "u" => Full("g")
        case "g" => Full("u")
        case _ => Full("w")
      }), x => x, "onchange" -> js.toJsCmd)
  }

  def render = {

    ".caption" #> className &
    ".oneMonday" #> {mkPrettyEvent(one.Monday)} &
    ".oneTuesday" #> {mkPrettyEvent(one.Tuesday)} &
    ".oneWednesday" #> {mkPrettyEvent(one.Wednesday)} &
    ".oneThursday" #> {mkPrettyEvent(one.Thursday)} &
    ".oneFriday" #> {mkPrettyEvent(one.Friday)} &
    ".twoMonday" #> {mkPrettyEvent(two.Monday)} &
    ".twoTuesday" #> {mkPrettyEvent(two.Tuesday)} &
    ".twoWednesday" #> {mkPrettyEvent(two.Wednesday)} &
    ".twoThursday" #> {mkPrettyEvent(two.Thursday)} &
    ".twoFriday" #> {mkPrettyEvent(two.Friday)} &
    ".threeMonday" #> {mkPrettyEvent(three.Monday)} &
    ".threeTuesday" #> {mkPrettyEvent(three.Tuesday)} &
    ".threeWednesday" #> {mkPrettyEvent(three.Wednesday)} &
    ".threeThursday" #> {mkPrettyEvent(three.Thursday)} &
    ".threeFriday" #> {mkPrettyEvent(three.Friday)} &
    ".fourMonday" #> {mkPrettyEvent(four.Monday)} &
    ".fourTuesday" #> {mkPrettyEvent(four.Tuesday)} &
    ".fourWednesday" #> {mkPrettyEvent(four.Wednesday)} &
    ".fourThursday" #> {mkPrettyEvent(four.Thursday)} &
    ".fourFriday" #> {mkPrettyEvent(four.Friday)} &
    ".fiveMonday" #> {mkPrettyEvent(five.Monday)} &
    ".fiveTuesday" #> {mkPrettyEvent(five.Tuesday)} &
    ".fiveWednesday" #> {mkPrettyEvent(five.Wednesday)} &
    ".fiveThursday" #> {mkPrettyEvent(five.Thursday)} &
    ".fiveFriday" #> {mkPrettyEvent(five.Friday)} &
    ".sixMonday" #> {mkPrettyEvent(six.Monday)} &
    ".sixTuesday" #> {mkPrettyEvent(six.Tuesday)} &
    ".sixWednesday" #> {mkPrettyEvent(six.Wednesday)} &
    ".sixThursday" #> {mkPrettyEvent(six.Thursday)} &
    ".sixFriday" #> {mkPrettyEvent(six.Friday)} &
    ".sevenMonday" #> {mkPrettyEvent(seven.Monday)} &
    ".sevenTuesday" #> {mkPrettyEvent(seven.Tuesday)} &
    ".sevenWednesday" #>{mkPrettyEvent(seven.Wednesday)} &
    ".sevenThursday" #> {mkPrettyEvent(seven.Thursday)} &
    ".sevenFriday" #> {mkPrettyEvent(seven.Friday)}

  }
}
