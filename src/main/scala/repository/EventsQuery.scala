package repository

import java.sql.Timestamp

/**
  * Created by inakov on 21.01.17.
  */
private[repository] trait EventsQuery {
  this: DBComponent =>

  import profile.api._

  private[EventsQuery] class EventsTable(tag: Tag) extends Table[Event](tag, "events_journal"){
    def persistenceKey = column[Long]("persistence_key")
    def sequenceNumber = column[Long]("sequence_nr")
    def content = column[Array[Byte]]("content")
    def created = column[Timestamp]("created")
    val pk = primaryKey("events_pk", (persistenceKey, sequenceNumber))

    def * = (persistenceKey, sequenceNumber, content, created.?) <> (Event.tupled, Event.unapply)
  }

  protected val eventsJournal = TableQuery[EventsTable]

  protected def insertEvents(events: Seq[Event]) = eventsJournal ++= events.sortBy(_.sequenceNumber)

  protected def deleteEvents(persistenceKey: Long, toSeqNum: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .filter(_.sequenceNumber <= toSeqNum).delete

  protected def loadEvents(persistenceKey: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .sortBy(_.sequenceNumber.desc)

  protected def loadEvents(persistenceKey: Long, fromSeqNum: Long, toSeqNum: Long, maxSize: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .filter(_.sequenceNumber >= fromSeqNum)
      .filter(_.sequenceNumber <= toSeqNum)
      .take(maxSize)

  protected def highestSeqNum(persistenceKey: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .sortBy(_.sequenceNumber.asc)
      .map(_.sequenceNumber).take(1)


}