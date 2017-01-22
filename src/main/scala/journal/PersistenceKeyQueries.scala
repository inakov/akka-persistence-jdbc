package journal

import database.DBComponent

/**
  * Created by inakov on 21.01.17.
  */
private[journal] trait PersistenceKeyQueries {
  this: DBComponent =>

  import profile.api._

  private[PersistenceKeyTable] class PersistenceKeysTable(tag: Tag) extends Table[PersistenceKey](tag, "persistence_keys") {
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

  protected def selectPersistenceKeys() = persistenceKeys.to[List]
}