package journal

import scala.concurrent.Future

/**
  * Created by inakov on 19.01.17.
  */
trait JournalRepository {

  def savePersistenceKey(persistenceKey: PersistenceKey): Future[Long]

  def loadPersistenceIds(): Future[List[String]]

  def getKey(persistenceId: String): Future[Option[Long]]
//  = db.run{
//    selectPersistenceKey(persistenceId).result.headOption
//  }

  def save(events: Seq[EventRecord]): Future[Int]

  def loadHighestSequenceNr(persistenceKey: Long, fromSeqNr: Long): Future[Long]

  def delete(persistenceKey: Long, toSequenceNr: Long): Future[Int]

}