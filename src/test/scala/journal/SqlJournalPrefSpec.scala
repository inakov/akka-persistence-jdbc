package journal

import akka.persistence.CapabilityFlag
import akka.persistence.journal.JournalPerfSpec
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
  * Created by inakov on 26.01.17.
  */
class SqlJournalPrefSpec extends JournalPerfSpec(ConfigFactory.load){
  override protected def supportsRejectingNonSerializableObjects: CapabilityFlag = CapabilityFlag.on

  override def awaitDurationMillis: Long = 5.minute.toMillis
  override def eventsCount: Int = 1000
}
