package org.unsane.spirit.news
package snippet

import net.liftweb.util.Helpers._
import org.unsane.spirit.news.model.{ Config, ScheduleRecord}
import xml.NodeSeq
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.{JE, JsonCall, JsCmd}
import net.liftweb.http.{SessionVar, SHtml, S}
import net.liftweb.common.Full

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

  private object weekTypeVar extends SessionVar[String]("")
  private object classNameVar extends SessionVar[String](S.param("classname").openOr(""))

  sealed abstract case class period(time: String, schedule: List[ScheduleRecord]) {
    private val periodSchedule = schedule.filter {
      x => x.appointment.get.time.trim().replaceAll(" ", "") == time
    }

    val Monday = periodFilter("Montag")
    val Tuesday = periodFilter("Dienstag")
    val Wednesday = periodFilter("Mittwoch")
    val Thursday = periodFilter("Donnerstag")
    val Friday = periodFilter("Freitag")

    def periodFilter = (weekDay: String) => periodSchedule.filter { x =>
      x.appointment.get.day.trim == weekDay
    }

    def hasCourses = !((Monday ::: Tuesday ::: Wednesday ::: Thursday ::: Friday).isEmpty)

  }

  S.param("classname").openOr("") match {
    case "" =>
    case s if (!(allClassNamesAsLowercase contains s)) => S.redirectTo("/404")
    case s => classNameVar.set(s)
  }

  classNameVar.get match {
    case s if (allClassNamesAsLowercase contains s) =>
    case "" =>
      classNameVar(allClassNamesAsLowercase.head.toLowerCase)
    case _ => S.redirectTo("/404")
  }

  weekTypeVar.get match {
    case "u" =>
    case "g" =>
    case "w" =>
    case _ => weekTypeVar(NewsSnippets.weekNr match {
      case even if (even % 2 == 0) => "g"
      case odd if (odd % 2 != 0) => "u"
      case _ => "w"
    })}

  val classSchedule = ScheduleRecord.findAll.filter { x =>
    x.className.value.toLowerCase == classNameVar.get}.filterNot { x =>
      x.appointment.get.week.toLowerCase == (weekTypeVar.get match {
        case "g" => "u"
        case "u" => "g"
        case _ => "m"
  })}

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
  object eight extends period("21.15-22.45", classSchedule)

  private val periodList = List(one, two, three, four, five, six, seven, eight)

  /**
   * @param List[ScheduleRecord] Should be one or more Events as a List.
   * @return NodeSeq The Events surrounded with Tags for prettiness.
   */
  private def mkPrettyEvent(schedule: List[ScheduleRecord]): NodeSeq = {

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
                                     s => { classNameVar(s)
                                            S.redirectTo("/schedule?classname=" + s)})

    SHtml.select(allClassNamesAsLowercase.map(x => (x,x)), Full(classNameVar.get),
                 x => x, "onchange" -> js.toJsCmd)
  }

  def selectWeekBox = {

    val (name2, js) = SHtml.ajaxCall(JE.JsRaw("this.value"),
                                     s => { weekTypeVar(s)
                                            S.redirectTo("/schedule")})

    SHtml.select(Seq(("g", "Gerade"),("u", "Ungerade"),("w", "Alles")),
      (weekTypeVar.get match {
        case "u" => Full("u")
        case "g" => Full("g")
        case _ => Full("w")
      }), x => x, "onchange" -> js.toJsCmd)
  }

  /**
   * This method is needed in order to view or not view empty lines
   * a schedule.
   *
   * @param period Which period line shall be rendered.
   * @param renderMe If the given period shall be rendered.
   * If any higher period is rendered. This period will be rendered, too.
   * @return NodeSeq The rendered period as HTML or an empty DIV.
   *
   */
  def renderPeriod(in: period, renderMe: (Boolean, Boolean)): NodeSeq = renderMe match {

    case (false, false) => <div></div>

    case (_, _) =>
      <tr>
        <td class="first">{ in.time }</td>
        <td>{ mkPrettyEvent(in.Monday) }</td>
        <td>{ mkPrettyEvent(in.Tuesday) }</td>
        <td>{ mkPrettyEvent(in.Wednesday) }</td>
        <td>{ mkPrettyEvent(in.Thursday) }</td>
        <td>{ mkPrettyEvent(in.Friday) }</td>
      </tr>
  }

  def render = {
    <table>
      <caption>Stundenplan f&uuml;r das Semester: { classNameVar.get } </caption>
	  <thead>
    <tr>
      <th>Uhrzeit</th>
      <th>Montag</th>
      <th>Dienstag</th>
      <th>Mittwoch</th>
      <th>Donnerstag</th>
      <th>Freitag</th>
    </tr>
	  </thead>
	  <tbody> {
      periodList map { current =>
        renderPeriod(current, (current.hasCourses, periodList.dropWhile(_ != current).tail.map(_.hasCourses) contains true))
      }}
    </tbody>
    </table>
  }
}
