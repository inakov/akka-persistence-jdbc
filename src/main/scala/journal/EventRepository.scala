package journal

import java.sql.Timestamp

import database.DBComponent

import scala.concurrent.Future

/**
  * Created by inakov on 19.01.17.
  */
class EventRepository extends PersistenceKeyQueries with EventsQueries {
  this: DBComponent =>

  import profile.api._

  def addKey(persistenceKey: PersistenceKey): Future[Long] = db.run{ persistenceKeysAutoInc += persistenceKey }

  def loadKeys(): Future[List[PersistenceKey]] = db.run{ selectPersistenceKeys().result }

  def getKey(persistenceId: String): Future[Option[Long]] = db.run{
    selectPersistenceKey(persistenceId).result.headOption
  }

  def addEvent(events: Seq[Event]) = ???

}

case class PersistenceKey(persistenceKey: Option[Long] = None, persistenceId: String)
case class Event(persistenceKey: Long, sequenceNumber: Long, content: Array[Byte], created: Option[Timestamp])