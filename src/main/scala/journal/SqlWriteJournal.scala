package journal

import akka.persistence.{AtomicWrite, PersistentRepr}
import akka.persistence.journal.AsyncWriteJournal
import akka.serialization.{Serialization, SerializationExtension}
import akka.stream.scaladsl.Source

import scala.collection.immutable.Seq
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * Created by inakov on 22.01.17.
  */
class SqlWriteJournal extends AsyncWriteJournal{

  private val repository: JournalRepository = _
  private val serialization: Serialization = SerializationExtension(context.system)

  override def asyncWriteMessages(messages: Seq[AtomicWrite]): Future[Seq[Try[Unit]]] = {
    val batches = for(atomicWrite <- messages) yield writeAtomicBatch(atomicWrite)
    Future.sequence(batches)
  }

  def writeAtomicBatch(atomicWrite: AtomicWrite): Future[Try[Unit]] = {
    //TODO: get persistence key
    val persistenceKey = atomicWrite.persistenceId.toLong
    val serializedEventsBatch =
      Try(atomicWrite.payload.map(repr => createEventRecord(persistenceKey, repr)).map(_.get))

    serializedEventsBatch match {
      case Success(events) => persistBatch(events)
      case Failure(cause) => Future.successful(Failure(cause))
    }
  }

  private def createEventRecord(persistenceKey: Long, persistentRepr: PersistentRepr): Try[EventRecord] = {
    serialization.serialize(persistentRepr).map{ content =>
      EventRecord(persistenceKey, persistentRepr.sequenceNr, content, None)
    }
  }

  private def persistBatch(events: Seq[EventRecord]): Future[Try[Unit]] = {
    val persistenceResult = repository.save(events).map(_ => ())
    val result = Promise[Try[Unit]]()
    persistenceResult.onComplete(result.success)
    result.future
  }

  override def asyncDeleteMessagesTo(persistenceId: String, toSequenceNr: Long): Future[Unit] = {
    //TODO: get persistence key
    val persistenceKey = persistenceId.toLong
    repository.delete(persistenceKey, toSequenceNr).map(_ => ())
  }

  override def asyncReplayMessages(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long,
                                   max: Long)(recoveryCallback: (PersistentRepr) => Unit): Future[Unit] = {
    //TODO: get persistence key
    val persistenceKey = persistenceId.toLong
    Source.fromPublisher(repository.eventStream(persistenceKey, fromSequenceNr, toSequenceNr, max))
      .map(event => serialization.deserialize(event.content, classOf[PersistentRepr]).get)
      .runFold(0){ (count, event) =>
        recoveryCallback(event)
        count + 1
      }.map(count => ())
  }


  override def asyncReadHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Future[Long] = {
    //TODO: get persistence key
    val persistenceKey = persistenceId.toLong
    repository.loadHighestSequenceNr(persistenceKey, fromSequenceNr).map(_.getOrElse(0L))
  }
}
