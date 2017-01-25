package snapshot

import akka.persistence.snapshot.SnapshotStoreSpec
import com.typesafe.config.ConfigFactory

/**
  * Created by inakov on 25.01.17.
  */
class SqlSnapshotStoreSpec extends SnapshotStoreSpec(ConfigFactory.load){

}
