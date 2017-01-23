package journal

import java.sql.Timestamp

import database.DBComponent

/**
  * Created by inakov on 21.01.17.
  */
private[journal] trait EventsQueries {
  this: DBComponent =>

  import profile.api._

  private[EventsQueries] class EventsTable(tag: Tag) extends Table[EventRecord](tag, "events_journal"){
    def persistenceKey = column[Long]("persistence_key")
    def sequenceNumber = column[Long]("sequence_nr")
    def content = column[Array[Byte]]("content")
    def created = column[Timestamp]("created")
    val pk = primaryKey("events_pk", (persistenceKey, sequenceNumber))

    def * = (persistenceKey, sequenceNumber, content, created.?) <> (EventRecord.tupled, EventRecord.unapply)
  }

  private val eventsJournal = TableQuery[EventsTable]

  protected def insertEvents(events: Seq[EventRecord]) = eventsJournal ++= events.sortBy(_.sequenceNumber)

  protected def deleteEvents(persistenceKey: Long, toSeqNr: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .filter(_.sequenceNumber <= toSeqNr).delete

  protected def selectEvents(persistenceKey: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .sortBy(_.sequenceNumber.desc)

  protected def selectEvents(persistenceKey: Long, fromSeqNr: Long, toSeqNr: Long, maxSize: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .filter(_.sequenceNumber >= fromSeqNr)
      .filter(_.sequenceNumber <= toSeqNr)
      .take(maxSize)

  protected def highestSeqNum(persistenceKey: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .sortBy(_.sequenceNumber.asc)
      .map(_.sequenceNumber).take(1)


}

case class EventRecord(persistenceKey: Long, sequenceNumber: Long, content: Array[Byte], created: Option[Timestamp])