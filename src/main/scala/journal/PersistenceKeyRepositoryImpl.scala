package journal
import database.DbComponent
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

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
    db.run{selectPersistenceKey(persistenceId).take(1).result.headOption}

  private def insertKeyIfNotExists(persistenceId: String): Future[Int] =
    db.run{insertIfNotExists(persistenceId)}

  override def saveOrLoadKey(persistenceId: String)(implicit ec: ExecutionContext): Future[Long] = {
    loadPersistenceKey(persistenceId).flatMap{
      case Some(key) => Future.successful(key)
      case None =>{
        for{
          _ <- insertKeyIfNotExists(persistenceId)
          key <- loadPersistenceKey(persistenceId)
        } yield key.get
      }
    }
  }
}
