package database

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * Created by inakov on 24.01.17.
  */
trait DbConfiguration {
  lazy val config = DatabaseConfig.forConfig[JdbcProfile]("slick")
}
