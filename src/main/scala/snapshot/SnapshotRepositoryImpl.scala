package snapshot

import database.DbComponent
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

/**
  * Created by inakov on 23.01.17.
  */
class SnapshotRepositoryImpl(val config: DatabaseConfig[JdbcProfile])
  extends SnapshotRepository with DbComponent with SnapshotQueries{

  import  config.profile.api._

  override def save(snapshot: SnapshotRecord): Future[Int] = db.run(insertSnapshot(snapshot))

  override def deleteSnapshot(persistenceId: String, seqNr: Long): Future[Int] =
    db.run(selectByIdAndSeqNr(persistenceId, seqNr).delete)

  override def deleteSnapshot(persistenceId: String, maxSequenceNr: Option[Long], maxTimestamp: Option[Long],
                              minSequenceNr: Option[Long], minTimestamp: Option[Long]): Future[Int] =
    db.run{
      selectSnapshotByCriteria(persistenceId, maxSequenceNr, maxTimestamp, minSequenceNr, minTimestamp).delete
    }

  override def loadSnapshot(persistenceId: String, maxSeqNr: Option[Long], maxCreatedAt: Option[Long],
                            minSeqNr: Option[Long], minCreatedAt: Option[Long]): Future[Option[SnapshotRecord]] =
    db.run{
      selectSnapshotByCriteria(persistenceId, maxSeqNr, maxCreatedAt, minSeqNr, minCreatedAt).take(1).result.headOption
    }

}
