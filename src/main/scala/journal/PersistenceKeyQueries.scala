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

  def insertIfNotExists(persistenceId: String) = persistenceKeys.map(_.persistenceId).forceInsertQuery {
    val exists = persistenceKeys.filter(_.persistenceId === persistenceId.bind).exists
    Query(persistenceId.bind).filter(_ => !exists)
  }

}

case class PersistenceKey(persistenceKey: Option[Long] = None, persistenceId: String)