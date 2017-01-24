package journal

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by inakov on 24.01.17.
  */
trait PersistenceKeyRepository {

  def savePersistenceKey(persistenceKey: PersistenceKey): Future[Long]

  def loadPersistenceKey(persistenceId: String): Future[Option[Long]]

  def saveOrLoadKey(persistenceId: String)(implicit ec: ExecutionContext): Future[Long]

}
