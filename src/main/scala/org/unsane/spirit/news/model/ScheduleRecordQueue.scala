package org.unsane.spirit.news.model

import net.liftweb.mongodb._
import record.{MongoRecord, MongoId, MongoMetaRecord}
import net.liftweb.mongodb.record.{ field => mongoField }
import net.liftweb.record.field.StringField

object ScheduleRecordQueue extends ScheduleRecordQueue with MongoMetaRecord[ScheduleRecordQueue] {

}

class ScheduleRecordQueue extends MongoRecord[ScheduleRecordQueue] with MongoId[ScheduleRecordQueue] {
  def meta = ScheduleRecordQueue

  object appointment extends mongoField.MongoCaseClassField[ScheduleRecordQueue, appointment](this)
  object className extends StringField(this, 100)
  object eventType extends StringField(this, 100)
  object member extends mongoField.MongoCaseClassListField[ScheduleRecordQueue, member](this)
  object titleLong extends StringField(this, 100)
  object titleShort extends StringField(this, 100)
  object group extends StringField(this,100)

}

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
    "group": "3",
    "titleLong": "Software....",
    "titleShort": "SWE V3"
}

 */