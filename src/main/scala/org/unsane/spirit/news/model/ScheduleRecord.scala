package org.unsane.spirit.news
package model

import net.liftweb.mongodb._
import record.{MongoRecord, MongoId, MongoMetaRecord}
import net.liftweb.mongodb.record.{ field => mongoField }
import net.liftweb.record.field.{IntField, StringField}

object ScheduleRecord extends ScheduleRecord with MongoMetaRecord[ScheduleRecord] {

}

class ScheduleRecord extends MongoRecord[ScheduleRecord] with MongoId[ScheduleRecord] {
  def meta = ScheduleRecord

  object appointment extends mongoField.MongoCaseClassField[ScheduleRecord, appointment](this)
  object className extends StringField(this, 100)
  object eventType extends StringField(this, 100)
  object member extends mongoField.MongoCaseClassListField[ScheduleRecord, member](this)
  object titleLong extends StringField(this, 100)
  object titleShort extends StringField(this, 100)
  object group extends StringField(this,100)
}

case class location(place: place, alternative: List[alternative])

case class place(building: String, room: String)

case class alternative(alterDay: String, alterTitleShort: String, altereventType: String,
                       alterWeek: String, hour: Int, alterLocation: alterLocation)

case class alterLocation(building: String, room: String)

case class member(fhs_id: String, name: String)

case class appointment(day: String, location: location,
                       time: String, week: String)


/**
 * The ScheduleRecord is based on following JSON.
 * {
    "appointment": {
        "day": "Dienstag",
        "location": {
            "alternative": [
                {
                    "alterDay": "Dienstag",
                    "alterLocation": {
                        "building": "B",
                        "room": "PC1"
                    },
                    "alterTitleShort": "SWE V3",
                    "alterWeek": "Gerade",
                    "altereventType": "Vorlesung",
                    "hour": 5
                }
            ],
            "place": {
                "building": "B",
                "room": "101"
            }
        },
        "time": "08.15-09.45",
        "week": "Gerade"
    },
    "className": "Bai6",
    "eventType": "Vorlesung",
    "member": [
        {
            "fhs_id": "braun",
            "name": "Braun"
        }
    ],
    "group": "4",
    "titleLong": "Software....",
    "titleShort": "SWE V3"
}

 */