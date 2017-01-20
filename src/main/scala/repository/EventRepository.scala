package repository

import scala.concurrent.Future

/**
  * Created by inakov on 19.01.17.
  */
class EventRepository extends PersistenceKeyTable {
  this: DatabaseComponent =>

  import profile.api._

  def create(persistenceKey: PersistenceKey): Future[Long] = db.run{ persistenceKeysAutoInc += persistenceKey }

  def list(): Future[List[PersistenceKey]] = db.run{ persistenceKeys.to[List].result }

}

private[repository] trait PersistenceKeyTable {
  this: DatabaseComponent =>

  import profile.api._

  private[PersistenceKeyTable] class PersistenceKeysTable(tag: Tag) extends Table[PersistenceKey](tag, "persistence_keys") {
    def persistenceKey = column[Long]("persistence_key", O.PrimaryKey, O.AutoInc)
    def persistenceId = column[String]("persistence_id")
    def * = (persistenceKey.?, persistenceId) <> (PersistenceKey.tupled, PersistenceKey.unapply)
  }

  protected val persistenceKeys = TableQuery[PersistenceKeysTable]

  protected def persistenceKeysAutoInc = persistenceKeys returning persistenceKeys.map(_.persistenceKey)

}

case class PersistenceKey(persistenceKey: Option[Long] = None, persistenceId: String)