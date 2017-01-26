package journal

import org.reactivestreams.Publisher

import scala.concurrent.Future

/**
  * Created by inakov on 19.01.17.
  */
trait JournalRepository {

  def save(events: Seq[EventRecord]): Future[Option[Int]]

  def loadHighestSequenceNr(persistenceKey: String, fromSeqNr: Long): Future[Option[Long]]

  def delete(persistenceKey: String, toSequenceNr: Long): Future[Int]

  def eventStream(persistenceKey: String, fromSeqNr: Long, toSeqNr: Long, maxSize: Long): Publisher[EventRecord]

}