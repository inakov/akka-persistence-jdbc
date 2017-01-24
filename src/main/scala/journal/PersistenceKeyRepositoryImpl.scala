package journal
import database.DBComponent
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

/**
  * Created by inakov on 24.01.17.
  */
class PersistenceKeyRepositoryImpl(val profile: JdbcProfile, val db: JdbcBackend#Database)
  extends PersistenceKeyRepository with PersistenceKeyQueries with DBComponent{

  import profile.api._

  private[this] val persistenceIds: TrieMap[String, Long] = TrieMap.empty

  override def savePersistenceKey(persistenceKey: PersistenceKey): Future[Long] = {
    db.run{persistenceKeysAutoInc += persistenceKey}
  }

  override def loadPersistenceKey(persistenceId: String): Future[Option[Long]] =
    db.run{selectPersistenceKey(persistenceId).result.headOption}

  override def loadOrSaveKey(persistenceId: String): Future[Long] = {
    loadPersistenceKey(persistenceId).flatMap({
      case Some(key) => Future.successful(key)
      case None => savePersistenceKey(PersistenceKey(None, persistenceId))
    })
  }
}
