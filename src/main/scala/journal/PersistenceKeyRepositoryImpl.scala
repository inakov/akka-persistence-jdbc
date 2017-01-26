package journal
import cache.{CacheWrapper, SimpleLurCacheWrapper}
import database.DbComponent
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by inakov on 24.01.17.
  */
class PersistenceKeyRepositoryImpl(val config: DatabaseConfig[JdbcProfile])
  extends PersistenceKeyRepository with PersistenceKeyQueries with DbComponent{

  import config.profile.api._

  private[this] val persistenceKeyCache: CacheWrapper[String, Long] =
    new SimpleLurCacheWrapper[String, Long](50, 500)

  override def savePersistenceKey(persistenceKey: PersistenceKey): Future[Long] = {
    db.run{persistenceKeysAutoInc += persistenceKey}
  }

  override def loadPersistenceKey(persistenceId: String): Future[Option[Long]] =
    db.run{selectPersistenceKey(persistenceId).take(1).result.headOption}

  private def insertKeyIfNotExists(persistenceId: String): Future[Int] =
    db.run{insertIfNotExists(persistenceId)}

  override def saveOrLoadKey(persistenceId: String)(implicit ec: ExecutionContext): Future[Long] = {
    val cacheResult = persistenceKeyCache.get(persistenceId)
    cacheResult match {
      case Some(key) => Future.successful(key)
      case None =>{
        loadPersistenceKey(persistenceId).flatMap{
          case Some(key) =>
            persistenceKeyCache.put(persistenceId, key)
            Future.successful(key)
          case None =>{
            for{
              _ <- insertKeyIfNotExists(persistenceId)
              key <- loadPersistenceKey(persistenceId)
            } yield {
              val persistenceKey = key.get
              persistenceKeyCache.put(persistenceId, persistenceKey)
              persistenceKey
            }
          }
        }
      }
    }
  }
}
