package journal

import database.DbComponent

import scala.concurrent.ExecutionContext

/**
  * Created by inakov on 21.01.17.
  */
trait PersistenceKeyQueries {
  this: DbComponent =>

  import config.profile.api._

  private[PersistenceKeyQueries] class PersistenceKeysTable(tag: Tag) extends Table[PersistenceKey](tag, "persistence_keys") {
    def persistenceKey = column[Long]("persistence_key", O.PrimaryKey, O.AutoInc)
    def persistenceId = column[String]("persistence_id")
    def * = (persistenceKey.?, persistenceId) <> (PersistenceKey.tupled, PersistenceKey.unapply)
  }

  private val persistenceKeys = TableQuery[PersistenceKeysTable]

  protected def persistenceKeysAutoInc = persistenceKeys returning persistenceKeys.map(_.persistenceKey)

  protected def insertPersistenceId(persistenceId: String) =
    persistenceKeysAutoInc += PersistenceKey(None, persistenceId)

  protected def selectPersistenceKey(persistenceId: String) =
    persistenceKeys.filter(_.persistenceId === persistenceId)
      .map(_.persistenceKey)

  protected def selectPersistenceIds() =
    persistenceKeys.map(_.persistenceId)

  protected def insertIfNotExists(persistenceId: String)(implicit ec: ExecutionContext) = {
    persistenceKeys.filter(_.persistenceId === persistenceId).take(1).result.headOption.flatMap {
      case Some(persistenceKey) =>
        DBIO.successful(persistenceKey.persistenceKey.get)
      case None =>
        persistenceKeysAutoInc += PersistenceKey(None, persistenceId)
    }
  }

}

case class PersistenceKey(persistenceKey: Option[Long] = None, persistenceId: String)