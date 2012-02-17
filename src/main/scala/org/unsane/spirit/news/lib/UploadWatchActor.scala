package org.unsane.spirit.news
package lib

import java.nio.file._
import net.liftweb.common.Loggable
import actors.Actor
import scala.collection.JavaConversions._

class UploadWatchActor(watcher: WatchService) extends Actor with Loggable {

  def act {

    val key = watcher.take
    val sph = ScheduleParsingHelper()

    while(true) {

      if (!key.pollEvents().toList.isEmpty) {
        logger info "Parsing new Schedules."
        sph.runParser("alle")
      }
      Thread.sleep(60000)
    }
  }
}
