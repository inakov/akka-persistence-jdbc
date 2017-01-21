package snapshot

import database.DBComponent

/**
  * Created by inakov on 21.01.17.
  */
trait SnapshotQueries {
  this: DBComponent =>

  import profile.api._

  private[SnapshotQueries] class SnapshotTable(tag: Tag) extends Table[Snapshot](tag, "snapshots"){
    def persistenceKey = column[Long]("persistence_key")
    def sequenceNumber = column[Long]("sequence_nr")
    def createdAt = column[Long]("created_at")
    def snapshot = column[Array[Byte]]("snapshot")

    def * = (persistenceKey, sequenceNumber, createdAt, snapshot) <> (Snapshot.tupled, Snapshot.unapply)
  }

  private val snapshots = TableQuery[SnapshotTable]

  protected def insertSnapshot(snapshot: Snapshot) =
    snapshots += snapshot

  protected def loadSnapshots(persistenceKey: Long) =
    snapshots
      .filter(_.persistenceKey === persistenceKey)
      .sortBy(_.sequenceNumber.desc)

  protected def loadLatestSnapshot(persistenceKey: Long) =
    loadSnapshots(persistenceKey).take(1)

  protected def loadSnapshot(persistenceKey: Long, seqNr: Long) =
    loadSnapshots(persistenceKey).filter(_.sequenceNumber === seqNr)

  protected def snapshotsTo(persistenceKey: Long, toSeqNr: Long) =
    loadSnapshots(persistenceKey).filter(_.sequenceNumber <= toSeqNr)

  protected def snapshotsTo(persistenceKey: Long, toSeqNr: Long, maxCreated: Long) =
    snapshotsTo(persistenceKey, toSeqNr).filter(_.createdAt <= maxCreated)

}

case class Snapshot(persistenceKey: Long, sequenceNumber: Long, createdAt: Long, snapshot: Array[Byte])
