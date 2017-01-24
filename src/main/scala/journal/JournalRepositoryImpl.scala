package journal

import database.DbComponent
import org.reactivestreams.Publisher
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by inakov on 23.01.17.
  */
class JournalRepositoryImpl(val config: DatabaseConfig[JdbcProfile])
  extends JournalRepository with PersistenceKeyQueries with EventsQueries with DbComponent{

  import config.profile.api._

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
