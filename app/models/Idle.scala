package models

import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.db.slick.HasDatabaseConfig
import scala.concurrent.Future
import java.sql.Date
import utils.Utils._
import slick.ast.BaseTypedType
import scala.collection.immutable.Set

case class Idle(id: String, name: String, kana: Option[String], birth: Option[Date], height: Option[Int], weight: Option[Int], bust: Option[Int], waist: Option[Int], hip: Option[Int], cup: Option[Char])

trait IdleTable extends HasDatabaseConfig[JdbcProfile] {
  import dbConfig.driver.api._
  
  protected val IdleQuery : TableQuery[Idles]
  
  class Idles(tag: Tag) extends Table[Idle](tag, "idles"){
    def id     = column[String]("id", O.PrimaryKey)
    def name   = column[String]("name")
    def kana   = column[String]("kana")
    def birth  = column[Date]("birth")
    def height = column[Int]("height")
    def weight = column[Int]("weight")
    def bust   = column[Int]("bust")
    def waist  = column[Int]("waist")
    def hip    = column[Int]("hip")
    def cup    = column[Char]("cup")
    
    def * = (id, name, kana.?, birth.?, height.?, weight.?, bust.?, waist.?, hip.?, cup.?) <> (Idle.tupled, Idle.unapply)
  }
  
  object Idles {
    type IdleQuery = Query[Idles, Idle, Seq]
    
    def find(params: Param) = {
      val name_q = params.name.map { name => 
        val name_like = "%" + name + "%"
        IdleQuery.filter(t => t.name.like(name_like) && t.kana.like(name_like))
      }.getOrElse(IdleQuery)
      
      val filter_q = filterByRange(params.birth_sql, _.birth).
        andThen(filterByRange(params.height, _.height)).
        andThen(filterByRange(params.weight, _.weight)).
        andThen(filterByRange(params.bust, _.bust)).
        andThen(filterByRange(params.waist, _.waist)).
        andThen(filterByRange(params.hip, _.hip)).
        andThen(filterByRange(params.cup, _.cup))(name_q)

      val q = filter_q.sortBy(_.kana)

      db.run(q.result)
    }
    
    def filterByRange[A : BaseTypedType](range: Range[A], col: Idles => slick.lifted.Rep[A]) : IdleQuery => IdleQuery = q => {
      val q1 = range._1.map(min => q.filter(t => col(t) >= min)).getOrElse(q)
      val q2 = range._2.map(max => q1.filter(t => col(t) <= max)).getOrElse(q1)
      q2
    }

    def grepNotExists(ids: Set[String]) = {
      val q = IdleQuery.filter(t => t.id inSet ids).map(_.id)
      db.run(q.result).map(exists => ids -- exists.toSet)
    }

    def insertIdle(idle: Idle) = db.run(IdleQuery += idle)
  }
}
