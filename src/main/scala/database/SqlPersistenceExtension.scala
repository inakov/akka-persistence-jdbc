package database

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import journal.{JournalRepository, JournalRepositoryImpl, PersistenceKeyRepository, PersistenceKeyRepositoryImpl}
import snapshot.{SnapshotRepository, SnapshotRepositoryImpl}

/**
  * Created by inakov on 24.01.17.
  */
object SqlPersistenceExtension extends ExtensionId[SqlPersistenceExtension] with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem): SqlPersistenceExtension = {
    new SqlPersistenceExtension(system)
  }

  override def lookup(): ExtensionId[_ <: Extension] = SqlPersistenceExtension
}

class SqlPersistenceExtension(val system: ExtendedActorSystem) extends Extension{
  val dbConfiguration = new DbConfiguration {}

  val journalRepository: JournalRepository = new JournalRepositoryImpl(dbConfiguration.config)
  val persistenceKeyRepository: PersistenceKeyRepository = new PersistenceKeyRepositoryImpl(dbConfiguration.config)
  val snapshotRepository: SnapshotRepository = new SnapshotRepositoryImpl(dbConfiguration.config)

}
