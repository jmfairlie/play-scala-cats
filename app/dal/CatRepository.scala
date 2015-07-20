package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import models.Cat

import scala.concurrent.{ Future, ExecutionContext }
import play.api.Logger

/**
 * A repository for cats.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class CatRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table.
   */
  private class ClowderTable(tag: Tag) extends Table[Cat](tag, "clowder") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The color column */
    def color = column[String]("color")
    
    /** The breed column */
    def breed = column[Int]("breed")
    
    /** The gender column */
    def gender = column[Int]("gender")
    
    /** The name column */
    def filename = column[String]("filename")

   
    def * = (id, name, color, breed, gender, filename) <> ((Cat.apply _).tupled, Cat.unapply)
  }

 
  private val clowder = TableQuery[ClowderTable]

 
  def create(name: String, color: String, breed: Int, gender: Int, filename: String): Future[Cat] = db.run {
    Logger.info(s"repo.create($name, $color, $breed, $gender, $filename)")
    (
      clowder.map(c => (c.name, c.color, c.breed, c.gender, c.filename))
     
      returning clowder.map(_.id)
   
      into ((attr, id) => Cat(id, attr._1, attr._2, attr._3, attr._4, attr._5))
    ) += (name, color, breed, gender, filename)
  }
  
  /*
   * Update cat
   */
  def update(id: Long, name: String, color: String, breed: Int, gender: Int, filename: String) = db.run {
    Logger.info(s"repo.update($id, $name, $color, $breed, $gender, $filename)")
    if (filename.isEmpty())
    {
        clowder.filter(_.id === id).map( c => (c.name, c.color, c.breed, c.gender)).update((name, color, breed, gender))
    }
    else
    {
        clowder.filter(_.id === id).map( c => (c.name, c.color, c.breed, c.gender, c.filename)).update((name, color, breed, gender, filename))
    }
  }
  
  
  /**
   * Delete one cat in the database. Not async cause I couldn't figure out how
   */
  def delete(id: Long) {
    Logger.info(s"delete: $id")
    val q  = clowder.filter(_.id === id)
    val action = q.delete
    val affectedRowsCount: Future[Int] = db.run(action)
  }
  
  /**
   * get cat by id
   */
  def edit(id: Long): Future[Cat] = db.run 
  {
    clowder.filter(_.id === id).result.head
  }
  

  /**
   * List all the cats in the database.
   */
  def list(): Future[Seq[Cat]] = db.run {
    clowder.result
  }
}
