package journal

import akka.NotUsed
import akka.stream.scaladsl.Source
import database.DBComponent
import org.reactivestreams.Publisher
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

  override def loadPersistenceKey(persistenceId: String): Future[Option[Long]] =
    db.run{selectPersistenceKey(persistenceId).result.headOption}

  override def save(events: Seq[EventRecord]): Future[Option[Int]] = {
    db.run(insertEvents(events).transactionally)
  }

  override def loadHighestSequenceNr(persistenceKey: Long, fromSeqNr: Long): Future[Option[Long]] = {
    db.run(highestSeqNum(persistenceKey).result.headOption)
  }

  override def delete(persistenceKey: Long, toSequenceNr: Long): Future[Int] = {
    db.run(deleteEvents(persistenceKey, toSequenceNr))
  }

  override def eventStream(persistenceKey: Long, fromSeqNr: Long,
                           toSeqNr: Long, maxSize: Long): Publisher[EventRecord] = {
    db.stream(selectEvents(persistenceKey, fromSeqNr, toSeqNr, maxSize).result)
  }
}