package snapshot

import database.DBComponent

/**
  * Created by inakov on 21.01.17.
  */
trait SnapshotQueries {
  this: DBComponent =>

  import profile.api._

  private[SnapshotQueries] class SnapshotTable(tag: Tag) extends Table[SnapshotRecord](tag, "snapshots"){
    def persistenceId = column[String]("persistence_id")
    def sequenceNumber = column[Long]("sequence_nr")
    def createdAt = column[Long]("created_at")
    def snapshot = column[Array[Byte]]("snapshot")

    def * = (persistenceId, sequenceNumber, createdAt, snapshot) <> (SnapshotRecord.tupled, SnapshotRecord.unapply)
  }

  private val snapshots = TableQuery[SnapshotTable]

  protected def insertSnapshot(snapshot: SnapshotRecord) =
    snapshots += snapshot

  protected def selectSnapshots(persistenceId: String) =
    snapshots
      .filter(_.persistenceId === persistenceId)
      .sortBy(_.sequenceNumber.desc)

  protected def selectByIdAndSeqNr(persistenceId: String, seqNr: Long) =
    snapshots
      .filter(_.persistenceId === persistenceId)
      .filter(_.sequenceNumber === seqNr)


  protected def selectSnapshotByCriteria(persistenceId: String, maxSeqNr: Option[Long], maxCreatedAt:  Option[Long],
                                         minSeqNr: Option[Long], minCreatedAt:  Option[Long]) =
    snapshots
      .filter(_.persistenceId === persistenceId)
      .filter{ snapshot =>
        List(
          maxSeqNr.map(snapshot.sequenceNumber <= _),
          maxCreatedAt.map(snapshot.createdAt <= _),
          minSeqNr.map(snapshot.sequenceNumber >= _),
          minCreatedAt.map(snapshot.createdAt >= _)
        ).collect({case Some(criteria)  => criteria}).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])
      }.sortBy(_.sequenceNumber.desc)

}

case class SnapshotRecord(persistenceId: String, sequenceNumber: Long, createdAt: Long, snapshot: Array[Byte])
