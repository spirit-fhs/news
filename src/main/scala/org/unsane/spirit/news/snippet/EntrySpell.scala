package org.unsane.spirit.news.snippet

import org.unsane.spirit.news.model.Entry
import java.util._
import java.text._

/*
 * @author Marcus Denison
 */
class EntrySpell(firstEntry: Entry) {

  /**
   * Compares the Numbers of two given Entrys.
   * @param secondEntry The Entry to be compared.
   * @return Boolean
   */
  def >(secondEntry: Entry): Boolean = {
    firstEntry.nr.value.toInt > secondEntry.nr.value.toInt
  }

  /**
   * Compares the Numbers of two given Entrys.
   * @param secondEntry The Entry to be compared.
   * @return Boolean
   */
  def <(secondEntry: Entry): Boolean = {
    firstEntry.nr.value.toInt < secondEntry.nr.value.toInt
  }

  /**
   * Compares the Numbers of two given Entrys.
   * @param secondEntry The Entry to be compared.
   * @return Boolean
   */
  def !=(secondEntry: Entry): Boolean = {
    firstEntry.nr.value.toInt != secondEntry.nr.value.toInt
  }

  /**
   * Making it easier to compare the Dates from an Entry.
   * @param secondEntry Entry to be compared.
   * @return Boolean
   * */
  def before(secondEntry: Entry): Boolean = {
    df.parse(firstEntry.date.value.toString) before df.parse(secondEntry.date.value.toString)
  }

  /**
   * Making it easier to compare the Dates from an Entry.
   * @param secondEntry Entry to be compared.
   * @return Boolean
   * */
  def after(secondEntry: Entry): Boolean = {
    df.parse(firstEntry.date.value.toString) after df.parse(secondEntry.date.value.toString)
  }

  /**
   * Comparing Entry Authors, making live once easier here!
   * @param secondEntry
   * @return Boolean
   * */
  def hasSameAuthor(secondEntry: Entry): Boolean = {
    firstEntry.name.value.toString == secondEntry.name.value.toString
  }

  /**
   * Making it easier if we want to compare a name from an Entry from a User we Allready know.
   * */
  def hasSameAuthor(user: String): Boolean = {
    firstEntry.name.value.toString == user
  }

  /**
   * Making it easier to check if a given Entry contains a specific semester.
   * @param semester
   * @return Boolean
   * */
  def contains(semester: String): Boolean = {
    firstEntry.semester.toString.split(" ").contains(semester)
  }

  private val df = new SimpleDateFormat ("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US)
}
