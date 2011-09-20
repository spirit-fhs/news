package org.unsane.spirit.news.snippet

import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import org.unsane.spirit.news.model.Config
import org.unsane.spirit.news.model.{ScheduleRecordQueue, ScheduleRecord}
import xml.NodeSeq
import net.liftweb.http.S
import net.liftweb.common.Full


/**
 * @TODO This is a WIP, still work to be done!
 */
class Schedule extends Config {

  sealed abstract class period(time: String, schedule: List[ScheduleRecord]) {
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

  val className = S.param("classname").openOr("").toLowerCase

  val week = S.param("week").get.toLowerCase match {
    case "u" => "g"
    case "g" => "u"
    case _ => ""
  }

  val classSchedule = ScheduleRecord.findAll.filter {
    x => x.className.value.toLowerCase == className }

  val in = classSchedule.filterNot( x =>
    x.appointment.get.week.toLowerCase == week )

  def mkPrettyEvent(schedule: List[ScheduleRecord]): NodeSeq = {
    schedule map { x =>
      <pre>{x.titleShort.get + " " +
      x.eventType.get + "\n" +
      x.appointment.get.location.place.building + ":" + x.appointment.get.location.place.room + "\n" +
      (if (x.group.value.replaceAll("""\u00A0""", "") == "") ""
       else "Gruppe: " + x.group.value.replaceAll("""\u00A0""", "") + "\n" ) +
       x.member.get.map(_.name).mkString(" ")}</pre>
    }
  }

  object one extends period("08.15-09.45", in)
  object two extends period("10.00-11.30", in)
  object three extends period("11.45-13.15", in)
  object four extends period("14.15-15.45", in)
  object five extends period("16.00-17.30", in)
  object six extends period("17.45-19.15", in)
  object seven extends period("19.30-21.00", in)

  def render = {

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
