package org.unsane.spirit.news

import model.Entry
import snippet.EntrySpell

package object snippet {

  /**
   * This is where the Magic happens :)!
   * */
  implicit def convertEntry2EntrySpell(entry: Entry) = new EntrySpell(entry: Entry)
}
