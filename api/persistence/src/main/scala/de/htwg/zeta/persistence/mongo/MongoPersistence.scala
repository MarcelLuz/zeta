package de.htwg.zeta.persistence.mongo

import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import de.htwg.zeta.persistence.general.Persistence
import de.htwg.zeta.persistence.mongo.MongoHandler.IdOnlyEntity
import models.Entity
import reactivemongo.api.Cursor
import reactivemongo.api.MongoConnection
import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.bson.BSONDocumentWriter

class MongoPersistence[E <: Entity](
    uri: String,
    dbName: String,
    implicit val entityHandler: BSONDocumentWriter[E] with BSONDocumentReader[E])(
    implicit manifest: Manifest[E])
  extends Persistence[E] {


  private val sId = "_id"

  private val connection: Future[MongoConnection] = Future.fromTry {
    MongoDriver().connection(uri)
  }

  private val idProjection = BSONDocument(sId -> 1)

  private def doDatabaseAction[T](f: BSONCollection => Future[T]): Future[T] = {
    connection.flatMap(_.database(dbName)).map(_.collection(entityTypeName)).flatMap(f)

  }

  /** Create a new entity.
   *
   * @param entity the entity to save
   * @return Future, with the created entity
   */
  override def create(entity: E): Future[E] = {
    doDatabaseAction { collection =>
      collection.insert(entity).map(_ => entity)
    }
  }

  /** Get a single entity.
   *
   * @param id The id of the entity
   * @return Future containing the read entity
   */
  override def read(id: UUID): Future[E] = {
    doDatabaseAction { collection =>
      collection.find(BSONDocument(sId -> id.toString)).requireOne[E]
    }
  }

  /** Update a entity.
   *
   * @param entity The updated entity
   * @return Future containing the updated entity
   */
  override private[persistence] def update(entity: E): Future[E] = {
    doDatabaseAction { collection =>
      collection.findAndUpdate(BSONDocument(sId -> entity), entity).map(_ =>
        entity
      )
    }
  }

  /** Delete a entity.
   *
   * @param id The id of the entity to delete
   * @return Future
   */
  override def delete(id: UUID): Future[Unit] = {
    doDatabaseAction { collection =>
      collection.remove(BSONDocument(sId -> id.toString)).flatMap(_ =>
        Future.successful(())
      )
    }
  }

  /** Get the id's of all entity.
   *
   * @return Future containing all id's of the entity type
   */
  override def readAllIds(): Future[Set[UUID]] = {
    doDatabaseAction { collection =>
      collection.find(BSONDocument.empty, idProjection).cursor[IdOnlyEntity]().
        collect(-1, Cursor.FailOnError[Set[IdOnlyEntity]]())
    }.map(_.map(_.id))
  }


}