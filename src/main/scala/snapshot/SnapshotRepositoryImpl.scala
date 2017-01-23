package snapshot

import database.DBComponent
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.Future

/**
  * Created by inakov on 23.01.17.
  */
class SnapshotRepositoryImpl(val profile: JdbcProfile, val db: JdbcBackend#Database)
  extends SnapshotRepository with DBComponent with SnapshotQueries{

  import  profile.api._

  override def save(snapshot: SnapshotRecord): Future[Unit] = db.run(insertSnapshot(snapshot)).map(_ => ())

  override def deleteSnapshot(persistenceId: String, seqNr: Long): Future[Unit] =
    db.run(selectByIdAndSeqNr(persistenceId, seqNr).delete).map(_ => ())

  override def deleteSnapshot(persistenceId: String, maxSequenceNr: Option[Long], maxTimestamp: Option[Long],
                              minSequenceNr: Option[Long], minTimestamp: Option[Long]): Future[Unit] =
    db.run{
      selectSnapshotByCriteria(persistenceId, maxSequenceNr, maxTimestamp, minSequenceNr, minTimestamp).delete
    }.map(_ => ())

  override def loadSnapshot(persistenceId: String, maxSeqNr: Option[Long], maxCreatedAt: Option[Long],
                            minSeqNr: Option[Long], minCreatedAt: Option[Long]): Future[Option[SnapshotRecord]] =
    db.run{
      selectSnapshotByCriteria(persistenceId, maxSeqNr, maxCreatedAt, minSeqNr, minCreatedAt).take(1).result.headOption
    }

}
