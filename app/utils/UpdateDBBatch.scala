package utils

import java.net.URL
import java.sql.Date
import scala.io.Source
import scala.xml._
import models.DAO
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.i18n._
import models._
import scala.annotation.tailrec

class UpdateDBBatch(messages: Messages) extends DAO {
  val LIST_ENTRY_POINT = 
    "https://ja.wikipedia.org/w/api.php?action=query&list=categorymembers&cmlimit=500&cmtitle=Category:グラビアアイドル&format=xml"

  val DETAIL_ENTRY_POINT = 
    "https://ja.wikipedia.org/w/api.php?format=xml&action=query&prop=revisions&rvprop=parsetree&pageids=%s"

  val DETAIL_API_MAX_COUNT = 5

  def exec(token: String = "") : Unit = {
    val url = LIST_ENTRY_POINT + (if(token.isEmpty) "" else ("&cmcontinue=" + token))
    val xml = XML.load(new URL(url))
    val cmcontinue = xml \\ "continue" \\ "@cmcontinue"
    val ids = (xml \\ "cm" \\ "@pageid") map (_.toString)

    val notExists = Idles.grepNotExists(ids.toSet)
    notExists.onSuccess{ case notExists => insertNews(notExists) }

    cmcontinue.headOption match {
      case Some(cmcontinue) => exec(cmcontinue.toString)
      case None => ()
    }
  }

  @tailrec
  final def insertNews(newids: Set[String]) : Unit = newids.splitAt(DETAIL_API_MAX_COUNT) match {
    case (hd, tl) => {
      if (hd.size > 0) {
        insertNewsSub(hd)
      }

      if (tl.size > 0) {
        insertNews(tl)
      }
    }
  }

  def insertNewsSub(newids_subset: Set[String]) = {
    val url = DETAIL_ENTRY_POINT.format(newids_subset.mkString("|"))

    val xml = XML.load(new URL(url))

    for(page <- xml \\ "page") {
      val id = page \\ "@pageid"

      if (!id.toString.isEmpty) {
        val content = page.toString.replaceAll("\\s", "").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
        insert(id.toString, content)
      }
    }
  }

  def insert(id: String, content: String) = grepString(content, messages("wikipedia.name")) match {
    case Some(name) => {
      Idles.insertIdle(Idle(
        id, name,
        grepString(content, messages("wikipedia.kana")),
        grepBirth(content),
        grepInt(content, messages("wikipedia.height")),
        grepInt(content, messages("wikipedia.weight")),
        grepInt(content, messages("wikipedia.bust")),
        grepInt(content, messages("wikipedia.waist")),
        grepInt(content, messages("wikipedia.hip")),
        grepString(content, messages("wikipedia.cup")).filter(_.size > 0).map(_(0))
      ))
    }

    case None => ()
  }
  
  def grepString(content: String, name: String) = 
    s"<part><name>${name}</name>=<value>(.*?)</value></part>".r.findFirstMatchIn(content).map(_.group(1))

  def grepInt(content: String, name: String) = grepString(content, name) match {
    case Some(str) => try { Some(str.toInt) } catch { case _: Throwable => None }
    case None      => None
  }
  
  def grepBirth(content: String) = {
    val year  = grepString(content, messages("wikipedia.birth_year"))
    val month = grepString(content, messages("wikipedia.birth_month"))
    val day   = grepString(content, messages("wikipedia.birth_day"))
  
    (year, month, day) match {
      case (Some(y), Some(m), Some(d)) => try {
        Some(Date.valueOf("%s-%s-%s".format(y, m, d)))
      } catch {
        case e: Throwable => None
      }
      case _ => None
    }
  }
}

object UpdateDBBatch {
  def run(implicit messages: Messages) = new UpdateDBBatch(messages).exec()
}
