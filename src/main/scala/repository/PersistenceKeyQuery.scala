package repository

/**
  * Created by inakov on 21.01.17.
  */
private[repository] trait PersistenceKeyQuery {
  this: DBComponent =>

  import profile.api._

  private[PersistenceKeyTable] class PersistenceKeysTable(tag: Tag) extends Table[PersistenceKey](tag, "persistence_keys") {
    def persistenceKey = column[Long]("persistence_key", O.PrimaryKey, O.AutoInc)
    def persistenceId = column[String]("persistence_id")
    def * = (persistenceKey.?, persistenceId) <> (PersistenceKey.tupled, PersistenceKey.unapply)
  }

  protected val persistenceKeys = TableQuery[PersistenceKeysTable]

  protected def persistenceKeysAutoInc = persistenceKeys returning persistenceKeys.map(_.persistenceKey)

  protected def findPersistenceKey(persistenceId: String) =
    persistenceKeys.filter(_.persistenceId === persistenceId)
      .map(_.persistenceKey)

  protected def loadPersistenceIds() =
    persistenceKeys.map(_.persistenceId)

}