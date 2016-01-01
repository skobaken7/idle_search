package utils

import java.util.Date
import play.api.data.format._
import play.api.data.FormError
import play.api.data.Forms._
import play.api.data.Mapping

object Utils {
  type Range[A] = (Option[A], Option[A])  
  
  case class Param(
    name: Option[String],
    birth: Range[Date],
    height: Range[Int],
    weight: Range[Int],
    bust:   Range[Int],
    waist:  Range[Int],
    hip:    Range[Int],
    cup:    Range[Char]
  ) {
    def birth_sql = birth match {
      case (Some(from), Some(to)) => (Some(new java.sql.Date(from.getTime)), Some(new java.sql.Date(to.getTime)))
      case (None,       Some(to)) => (None,                                  Some(new java.sql.Date(to.getTime)))
      case (Some(from), None)     => (Some(new java.sql.Date(from.getTime)), None)
      case (None,       None)     => (None, None)
    }
  }

  private implicit def charFormat: Formatter[Char] = new Formatter[Char] {
    def bind(key: String, data: Map[String, String]) =
      data.get(key).filter(s => s.length == 1 && s != " ").map(s => Right(s.charAt(0))).getOrElse(
        Left(Seq(FormError(key, "error.required", Nil)))
      )
    def unbind(key: String, value: Char) = Map(key -> value.toString)
  }
  val char = of[Char]
}
