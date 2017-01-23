package snapshot

import scala.concurrent.Future

/**
  * Created by inakov on 23.01.17.
  */
trait SnapshotRepository {

  def save(snapshot: SnapshotRecord): Future[Int]

  def deleteSnapshot(persistenceId: String, seqNr: Long): Future[Int]

  def deleteSnapshot(persistenceId: String, maxSequenceNr: Option[Long], maxTimestamp:  Option[Long],
                     minSequenceNr: Option[Long], minTimestamp:  Option[Long]): Future[Int]

  def loadSnapshot(persistenceId: String, maxSeqNr: Option[Long], maxCreatedAt:  Option[Long],
                   minSeqNr: Option[Long], minCreatedAt:  Option[Long]): Future[Option[SnapshotRecord]]

}
