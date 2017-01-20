package repository

import slick.jdbc.JdbcProfile

/**
  * Created by inakov on 20.01.17.
  */
trait DatabaseComponent {

  val profile: JdbcProfile

  import profile.api._

  val db: Database

}
