package journal

import org.reactivestreams.Publisher

import scala.concurrent.Future

/**
  * Created by inakov on 19.01.17.
  */
trait JournalRepository {

  def savePersistenceKey(persistenceKey: PersistenceKey): Future[Long]

  def loadPersistenceKey(persistenceId: String): Future[Option[Long]]

  def save(events: Seq[EventRecord]): Future[Option[Int]]

  def loadHighestSequenceNr(persistenceKey: Long, fromSeqNr: Long): Future[Option[Long]]

  def delete(persistenceKey: Long, toSequenceNr: Long): Future[Int]

  def eventStream(persistenceKey: Long, fromSeqNr: Long, toSeqNr: Long, maxSize: Long): Publisher[EventRecord]

}