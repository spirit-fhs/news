package org.unsane.spirit.news.snippet

import net.liftweb.http.SHtml
import net.liftweb.util.Helpers._
import net.liftweb.http
import http._
import js._
import JsCmds._
import JE._
import org.unsane.spirit.news.model.{Config, ScheduleRecord, ScheduleRecordQueue}
import xml.NodeSeq
import net.liftweb.json.JsonDSL._

class ScheduleQueue extends Config {

  sealed abstract class period(time: String, schedule: List[ScheduleRecordQueue]) {
    val tmp = schedule.filter {
      x => x.appointment.get.time.trim().replaceAll(" ", "") == time
    }
    val Monday = tmp.filter { x =>
      x.appointment.get.day.trim == "Montag"
    }
    val Tuesday = tmp.filter { x =>
      x.appointment.get.day.trim == "Dienstag"
    }
    val Wednesday = tmp.filter { x =>
      x.appointment.get.day.trim == "Mittwoch"
    }
    val Thursday = tmp.filter { x =>
      x.appointment.get.day.trim == "Donnerstag"
    }
    val Friday = tmp.filter { x =>
      x.appointment.get.day.trim == "Freitag"
    }
}

  val schedule_i = loadSchedule("I").map(_.toLowerCase)
  val schedule_wi = loadSchedule("WI").map(_.toLowerCase)
  val schedule_its = loadSchedule("ITS").map(_.toLowerCase)
  val schedule_muma = loadSchedule("MUMA").map(_.toLowerCase)
  val schedule_ma = loadSchedule("MA").map(_.toLowerCase)

  val classSchedule = ScheduleRecordQueue.findAll

  lazy val schedules_as_HTML = schedule_i.map(x =>
    renderSchedules(x, classSchedule.filter(
      y => y.className.get.toLowerCase == x)
    )
  ) ++ schedule_wi.map(x =>
    renderSchedules(x, classSchedule.filter(
      y => y.className.get.toLowerCase == x)
    )
  ) ++ schedule_muma.map(x =>
    renderSchedules(x, classSchedule.filter(
      y => y.className.get.toLowerCase == x)
    )
  ) ++ schedule_its.map(x =>
    renderSchedules(x, classSchedule.filter(
      y => y.className.get.toLowerCase == x)
    )
  ) ++ schedule_ma.map(x =>
    renderSchedules(x, classSchedule.filter(
      y => y.className.get.toLowerCase == x)
    )
  )

  def mkPrettyEvent(schedule: List[ScheduleRecordQueue]): NodeSeq = {

    schedule map { x =>

      val cycle = x.appointment.get.week match {
        case "g" => "even"
        case "u" => "odd"
        case _ => "weekly"
      }

      <div class={"event " + cycle}>
       <span class={"eventTitle"}>{x.titleShort.get + " " + x.eventType.get}</span>
       <div style="clear:both"></div>
       {(if (x.group.value.replaceAll("""\u00A0""", "") == "") ""
           else <div>{"Gruppe: " + x.group.value.replaceAll("""\u00A0""", "")}</div>)}
       <div style="float:left">{x.appointment.get.location.place.building + ":" + x.appointment.get.location.place.room}</div>
       <div style="float:right">{x.member.get.map(_.name).mkString(" ")}</div>
       <div style="clear:both"></div>
      </div>
    }
  }

  def renderSchedules(classname: String, in: List[ScheduleRecordQueue]) = {

    object one extends period("08.15-09.45", in)
    object two extends period("10.00-11.30", in)
    object three extends period("11.45-13.15", in)
    object four extends period("14.15-15.45", in)
    object five extends period("16.00-17.30", in)
    object six extends period("17.45-19.15", in)
    object seven extends period("19.30-21.00", in)

    in match {
      case Nil => <div></div>
      case _ => renderSchedule(classname, one, two, three, four, five, six, seven)
    }
  }

  def renderSchedule(classname: String, one: period, two: period, three: period,
                     four: period, five: period, six: period,
                     seven: period) = {

  <table>
    <caption>{ "Stundenplan für: " + classname }</caption>
  <tr>
    <th class="first">Uhrzeit</th>
    <th>{"Montag"}</th>
    <th>{"Dienstag"}</th>
    <th>{"Mittwoch"}</th>
    <th>{"Donnerstag"}</th>
    <th>{"Freitag"}</th>
  </tr>
  <tr>
    <td class="first">08.15 - 09.45</td>
    <td>{mkPrettyEvent(one.Monday)}</td>
    <td>{mkPrettyEvent(one.Tuesday)}</td>
    <td>{mkPrettyEvent(one.Wednesday)}</td>
    <td>{mkPrettyEvent(one.Thursday)}</td>
    <td>{mkPrettyEvent(one.Friday)}</td>
  </tr>
  <tr>
    <td class="first">10.00 - 11.30</td>
    <td>{mkPrettyEvent(two.Monday)}</td>
    <td>{mkPrettyEvent(two.Tuesday)}</td>
    <td>{mkPrettyEvent(two.Wednesday)}</td>
    <td>{mkPrettyEvent(two.Thursday)}</td>
    <td>{mkPrettyEvent(two.Friday)}</td>
  </tr>
  <tr>
    <td class="first">11.45 - 13.15</td>
    <td>{mkPrettyEvent(three.Monday)}</td>
    <td>{mkPrettyEvent(three.Tuesday)}</td>
    <td>{mkPrettyEvent(three.Wednesday)}</td>
    <td>{mkPrettyEvent(three.Thursday)}</td>
    <td>{mkPrettyEvent(three.Friday)}</td>
  </tr>
  <tr>
    <td class="first">14.15 - 15.45</td>
    <td>{mkPrettyEvent(four.Monday)}</td>
    <td>{mkPrettyEvent(four.Tuesday)}</td>
    <td>{mkPrettyEvent(four.Wednesday)}</td>
    <td>{mkPrettyEvent(four.Thursday)}</td>
    <td>{mkPrettyEvent(four.Friday)}</td>
  </tr>
  <tr>
    <td class="first">16.00 - 17.30</td>
    <td>{mkPrettyEvent(five.Monday)}</td>
    <td>{mkPrettyEvent(five.Tuesday)}</td>
    <td>{mkPrettyEvent(five.Wednesday)}</td>
    <td>{mkPrettyEvent(five.Thursday)}</td>
    <td>{mkPrettyEvent(five.Friday)}</td>
  </tr>
  <tr>
    <td class="first">17.45 - 19.15</td>
    <td>{mkPrettyEvent(six.Monday)}</td>
    <td>{mkPrettyEvent(six.Tuesday)}</td>
    <td>{mkPrettyEvent(six.Wednesday)}</td>
    <td>{mkPrettyEvent(six.Thursday)}</td>
    <td>{mkPrettyEvent(six.Friday)}</td>
  </tr>
  <tr>
    <td class="first">19.30 - 21.00</td>
    <td>{mkPrettyEvent(seven.Monday)}</td>
    <td>{mkPrettyEvent(seven.Tuesday)}</td>
    <td>{mkPrettyEvent(seven.Wednesday)}</td>
    <td>{mkPrettyEvent(seven.Thursday)}</td>
    <td>{mkPrettyEvent(seven.Friday)}</td>
  </tr>
  </table>
  }

  def process() = {

    ScheduleRecordQueue.findAll.map( x => x.className.value ).distinct.map { x =>
      ScheduleRecord.findAll("className" -> x.toString()).map(_.delete_!)
    }

    val old = ScheduleRecordQueue.findAll
    old map { x =>
      val newSched = ScheduleRecord.createRecord
      newSched.setFieldsFromDBObject(x.asDBObject)
      newSched.save
    }
    old.map(_.delete_!)

    S.notice("Neuen Stundenpläne erfolgreich portiert!")
    S.redirectTo("/schedulepreview")
  }


  def render = {
    classSchedule.length match {
      case 0 => ".preview" #> "Keine Datensätze in der Queue!" &
                "type=submit" #> SHtml.submit("Veröffentlichen", () => (),"disabled" -> "disabled")
      case _ => ".preview" #> schedules_as_HTML.toList &
                "type=submit" #> SHtml.submit("Veröffentlichen", () => process)
    }
  }
}