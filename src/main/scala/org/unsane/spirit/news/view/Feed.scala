package org.unsane.spirit.news
package snippet

import scala.xml._
import net.liftweb.util.Helpers._
import net.liftweb.textile._
import model.Entry

import net.liftweb._
import net.liftweb.common.{Box, Full, Empty, EmptyBox, Failure}
import http.{S, LiftView}
import net.liftweb.mapper._
import java.lang.String

/** View-Klasse für die Erstellung von Feeds über die eingetragenen News
	*/
class Feed extends LiftView {

	def news = Entry.findAll.reverse

  def semesterChanger(input: String): String = {
    if(input.startsWith("semester ")) { "Alle" }
    else { input }
  }

	override def dispatch = {
    case s:String => createRss2Feed
  } 

/**Erstellt RSS2-Feed über die letzten Nachrichten.
* @return XML für den Feed */
	def createRss2Feed(): NodeSeq = {
		<rss version="2.0">
			<channel>
				<title>Spirit @ FH-Schmalkalden - RSS Feed</title>
				<link>http://spirit.fh-schmalkalden.de</link>
				<description>RSS Feed für die aktuellen Meldungen am Fachbereich Informatik</description>
				<language>de-de</language>
				<image>
				  <url>http://spirit.fh-schmalkalden.de/classpath/images/logo_spirit.jpg</url>
				  <title>Spirit</title>
				  <link>http://spirit.fh-schmalkalden.de</link>
				</image>
				<ttl>60</ttl>
				{news.map{entry =>
					(<item>
						<title>{entry.subject.value.toString} ({semesterChanger(entry.semester.value.toString)})</title>
						<description>{entry.news.value.toString.substring(0, 128) + "..."}</description>
						<link>http://spirit.fh-schmalkalden.de/entry/{entry.nr.value.toString}</link>
						<author>{entry.writer.value.toString}</author>
						<guid>{entry.nr.value.toString}</guid>
						<pubDate>{entry.date.value.toString}</pubDate>
					</item>)
				}}
			</channel>
		</rss>
	}

}
