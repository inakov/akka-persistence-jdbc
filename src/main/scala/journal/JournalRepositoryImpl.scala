package journal

import database.DBComponent
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.Future

/**
  * Created by inakov on 23.01.17.
  */
class JournalRepositoryImpl(val profile: JdbcProfile, val db: JdbcBackend#Database)
  extends JournalRepository with PersistenceKeyQueries with EventsQueries with DBComponent{

  import profile.api._

  override def savePersistenceKey(persistenceKey: PersistenceKey): Future[Long] = {
    db.run{persistenceKeysAutoInc += persistenceKey}
  }

  override def loadPersistenceIds(): Future[List[String]] = ???

  override def getKey(persistenceId: String): Future[Option[Long]] = ???

  override def save(events: Seq[EventRecord]): Future[Int] = ???

  override def loadHighestSequenceNr(persistenceKey: Long, fromSeqNr: Long): Future[Long] = ???

  override def delete(persistenceKey: Long, toSequenceNr: Long): Future[Int] = ???
}
