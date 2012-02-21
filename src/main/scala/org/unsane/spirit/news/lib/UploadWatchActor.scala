package org.unsane.spirit.news
package lib

import java.nio.file._
import net.liftweb.common.Loggable
import actors.Actor
import scala.collection.JavaConversions._

class UploadWatchActor(watcher: WatchService) extends Actor with Loggable {

  def act {

    val sph = ScheduleParsingHelper()

    while(true) {
      try {
        val key = watcher.take()
        for (x <- key.pollEvents().toList) {
          logger info x.context() + " has changed. Sending to parser."
          sph.runParser(x.context().toString.stripPrefix("s_").stripSuffix(".html"))
        }
        key.reset()
      } catch {
        case s => logger warn s
      }
      Thread.sleep(1000)
    }
  }
}
