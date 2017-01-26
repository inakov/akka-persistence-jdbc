package journal

import akka.actor.ActorSystem
import akka.persistence.{AtomicWrite, PersistentRepr}
import akka.persistence.journal.AsyncWriteJournal
import akka.serialization.{Serialization, SerializationExtension}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Source
import database.SqlPersistenceExtension

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * Created by inakov on 22.01.17.
  */
class SqlWriteJournal extends AsyncWriteJournal{

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val mat: Materializer = ActorMaterializer()

  private val sqlPersistenceExtension = SqlPersistenceExtension(context.system)

  private val journalRepository: JournalRepository = sqlPersistenceExtension.journalRepository
  private val persistenceKeyRepository: PersistenceKeyRepository = sqlPersistenceExtension.persistenceKeyRepository

  private val serialization: Serialization = SerializationExtension(context.system)

  override def asyncWriteMessages(messages: Seq[AtomicWrite]): Future[Seq[Try[Unit]]] = {
    val batches = for(atomicWrite <- messages) yield writeAtomicBatch(atomicWrite)
    Future.sequence(batches)
  }

  def writeAtomicBatch(atomicWrite: AtomicWrite): Future[Try[Unit]] = {
    val transformedEvents = Try(atomicWrite.payload.map(repr => createEventRecord(atomicWrite.persistenceId, repr)))
    transformedEvents match {
      case Success(events) => persistBatch(events)
      case Failure(e) => Future.successful(Failure(e))
    }
  }

  private def createEventRecord(persistenceKey: String, persistentRepr: PersistentRepr): EventRecord = {
    val content = serialization.serialize(persistentRepr).get
    EventRecord(persistenceKey, persistentRepr.sequenceNr, content, None)
  }

  private def persistBatch(events: Seq[EventRecord]): Future[Try[Unit]] = {
    val persistenceResult = journalRepository.save(events).map(_ => ())
    val promise = Promise[Try[Unit]]()
    persistenceResult.onComplete(promise.success)
    promise.future
  }

  override def asyncDeleteMessagesTo(persistenceId: String, toSequenceNr: Long): Future[Unit] = {
    journalRepository.delete(persistenceId, toSequenceNr).map(_ => ())
  }

  override def asyncReplayMessages(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long,
                                   max: Long)(recoveryCallback: (PersistentRepr) => Unit): Future[Unit] = {
      Source.fromPublisher(journalRepository.eventStream(persistenceId, fromSequenceNr, toSequenceNr, max))
        .map(event => serialization.deserialize(event.content, classOf[PersistentRepr]).get)
        .runFold(0){ (count, event) =>
          recoveryCallback(event)
          count + 1
        }.map(_ => ())
  }


  override def asyncReadHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Future[Long] = {
    for{
      seqNr <- journalRepository.loadHighestSequenceNr(persistenceId, fromSequenceNr)
    } yield seqNr.getOrElse(0L)
  }
}
