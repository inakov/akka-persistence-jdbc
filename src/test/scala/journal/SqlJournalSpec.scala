package journal

import akka.persistence.CapabilityFlag
import akka.persistence.journal.JournalSpec
import com.typesafe.config.ConfigFactory

/**
  * Created by inakov on 25.01.17.
  */
class SqlJournalSpec extends JournalSpec(ConfigFactory.load){

  override def supportsRejectingNonSerializableObjects: CapabilityFlag = false

}
