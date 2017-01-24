package database

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * Created by inakov on 20.01.17.
  */
trait DbComponent {

  val config: DatabaseConfig[JdbcProfile]
  val db: JdbcProfile#Backend#Database = config.db

}
