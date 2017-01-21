import repository.{DBComponent, EventRepository, PersistenceKey}
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
  * Created by inakov on 19.01.17.
  */
object Main extends App{
  // Use H2Driver to connect to an H2 database

  import scala.concurrent.ExecutionContext.Implicits.global

  val eventsRepo = new EventRepository with DBComponent{
    override val profile: JdbcProfile = H2Profile

    import profile.api._
    override val db: Database = Database.forConfig("h2mem1")

    val setup = DBIO.seq(
      persistenceKeys.schema.create,
      persistenceKeys += PersistenceKey(None, "TestId1")
    )
    db.run(setup)
  }

  eventsRepo.loadKeys().foreach(println)
}
