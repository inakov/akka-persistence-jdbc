package snapshot

import akka.persistence.{SelectedSnapshot, SnapshotMetadata, SnapshotSelectionCriteria}
import akka.persistence.snapshot.SnapshotStore

import scala.concurrent.Future

/**
  * Created by inakov on 22.01.17.
  */
class SqlSnapshotStore extends SnapshotStore{
  override def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = ???

  override def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] = ???

  override def deleteAsync(metadata: SnapshotMetadata): Future[Unit] = ???

  override def deleteAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Unit] = ???
}
