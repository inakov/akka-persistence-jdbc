package journal
import database.DbComponent
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

/**
  * Created by inakov on 24.01.17.
  */
class PersistenceKeyRepositoryImpl(val config: DatabaseConfig[JdbcProfile])
  extends PersistenceKeyRepository with PersistenceKeyQueries with DbComponent{

  import config.profile.api._

  private[this] val persistenceIds: TrieMap[String, Long] = TrieMap.empty

  override def savePersistenceKey(persistenceKey: PersistenceKey): Future[Long] = {
    db.run{persistenceKeysAutoInc += persistenceKey}
  }

  override def loadPersistenceKey(persistenceId: String): Future[Option[Long]] =
    db.run{selectPersistenceKey(persistenceId).result.headOption}

  override def saveOrLoadKey(persistenceId: String): Future[Long] = {
    db.run(insertIfNotExists(persistenceId))
  }
}
