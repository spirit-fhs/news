package org.unsane.spirit.news
package lib

import net.liftweb.common.{Failure, Full}
import net.liftweb.json
import json.DefaultFormats
import json.JsonAST.{JValue, JArray}
import model.{ScheduleRecordQueue, ScheduleRecord}
import net.liftweb.json.JsonDSL._

/**
 * Object is used to import the complete schedule via JSON Put.
 * Data will be provided by a co-project called migrate.
 */
object Schedule {

  import net.liftweb.json.Formats
  implicit val formats = DefaultFormats

  def import2DatabaseQueue(in: String): Boolean = {

    val jsonStringAsSchedule = """{ "schedule": """ + in + """}"""

    val scheduleList = for {
      i <- (json.parse(jsonStringAsSchedule) \ "schedule" ).children
    } yield i

    val check = ScheduleRecordQueue.createRecord
    check.setFieldsFromJValue(scheduleList.head)
    ScheduleRecordQueue.findAll("className" ->
      check.className.value).map(_.delete_!)

    scheduleList map { x =>
      val newScheduleRecord = ScheduleRecordQueue.createRecord
      newScheduleRecord.setFieldsFromJValue(x)
      newScheduleRecord.save
    }

    true
  }
}
