package snapshot

import akka.persistence.serialization.Snapshot
import akka.persistence.{SelectedSnapshot, SnapshotMetadata, SnapshotSelectionCriteria}
import akka.persistence.snapshot.SnapshotStore
import akka.serialization.{Serialization, SerializationExtension}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by inakov on 22.01.17.
  */
class SqlSnapshotStore extends SnapshotStore{

  private val serialization: Serialization = SerializationExtension(context.system)
  private val snapshotRepository: SnapshotRepository = _


  override def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = {
    val (maxSeqNr, maxCreatedAt, minSeqNr, minCreatedAt) = extractSelectionCriteria(criteria)
    val snapshotRecordFuture =
      snapshotRepository.loadSnapshot(persistenceId, maxSeqNr, maxCreatedAt, minSeqNr, minCreatedAt)

    snapshotRecordFuture.map(snapshotOption =>
      snapshotOption.map{ snapshotRec =>
        val Snapshot(snapshot) = serialization.deserialize(snapshotRec.snapshot, classOf[Snapshot]).get
        val metadata = SnapshotMetadata(snapshotRec.persistenceId, snapshotRec.sequenceNumber, snapshotRec.createdAt)

        SelectedSnapshot(metadata, snapshot)
      })
  }

  override def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] = {
    val SnapshotMetadata(persistenceId, seqNr, createdAt) = metadata
    serialization.serialize(Snapshot(snapshot)) match {
      case Success(content) => saveSnapshot(persistenceId, seqNr, createdAt, content).map(_ => ())
      case Failure(exception) => Future.failed(exception)
    }
  }

  private def saveSnapshot(persistenceId: String, seqNr: Long, createdAt: Long, snapshot: Array[Byte]): Future[Unit] = {
    val snapshotRecord = SnapshotRecord(persistenceId, seqNr, createdAt, snapshot)
    snapshotRepository.save(snapshotRecord).map(_ => ())
  }

  override def deleteAsync(metadata: SnapshotMetadata): Future[Unit] = {
    val SnapshotMetadata(persistenceId, seqNr, _) = metadata
    snapshotRepository.deleteSnapshot(persistenceId, seqNr).map(_ => ())
  }

  override def deleteAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Unit] = {
    val (maxSeqNr, maxCreatedAt, minSeqNr, minCreatedAt) = extractSelectionCriteria(criteria)
    snapshotRepository.deleteSnapshot(persistenceId, maxSeqNr, maxCreatedAt, minSeqNr, minCreatedAt).map(_ => ())
  }

  private def extractSelectionCriteria(criteria: SnapshotSelectionCriteria) = {
    val maxSeqNrCriteria: Option[Long] =
      if(criteria.maxSequenceNr == Long.MaxValue) None else Some(criteria.maxSequenceNr)
    val maxCreatedAtCriteria: Option[Long] =
      if(criteria.maxTimestamp == Long.MaxValue) None else Some(criteria.maxTimestamp)
    val minSeqNrCriteria: Option[Long] =
      if(criteria.minSequenceNr == 0L) None else Some(criteria.minSequenceNr)
    val minCreatedAtCriteria: Option[Long] =
      if(criteria.minTimestamp == 0L) None else Some(criteria.minTimestamp)

    (maxSeqNrCriteria, maxCreatedAtCriteria, minSeqNrCriteria, minCreatedAtCriteria)
  }
}
