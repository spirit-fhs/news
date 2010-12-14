package org.unsane.spirit.news
package snippet

import net.liftweb.http._
import S._
import js._
import JsCmds._
import JE._
import net.liftweb.common._
import net.liftweb.util._
import Helpers._
import scala.xml._
import net.liftweb.textile._

trait EntryPreview {

  object jsonPreview extends JsonHandler {
    def apply(in: Any): JsCmd =
      SetHtml("entry_preview", in match {
        case JsonCmd("preview", _, p: String, _) =>
          <br /> +: <hr /> +: TextileParser.toHtml(p, Empty)
        case x => <b>Oops.... {x}</b>
      })
  }

  def mkPreview(in: NodeSeq): NodeSeq = {
    bind("json", in,
      "script" -> Script(jsonPreview.jsCmd),
      AttrBindParam("onclick", Text(jsonPreview.call("preview", ElemById("entry")~>Value).toJsCmd), "onclick"))
  }
}
