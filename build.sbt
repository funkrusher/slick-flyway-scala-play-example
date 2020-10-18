import com.typesafe.config.ConfigFactory

name := """slick-flyway-scala-play-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(FlywayPlugin)

libraryDependencies ++= Seq(
    jdbc,
    ehcache,
    ws,
    guice,
    "org.flywaydb" %% "flyway-play" % "5.2.0",
    "org.mariadb.jdbc" % "mariadb-java-client" % "2.4.3",
    "com.typesafe.play" %% "play-slick" % "3.0.0",
    specs2 % Test
)

//
// FLYWAY
//
// flyway can be forced not to use the compile-time classpath via "filesystem:"-syntax.
lazy val flywayDbConf = settingKey[(String, String, String)]("Typesafe config file with slick settings")
flywayDbConf := {
    val cfg = ConfigFactory.parseFile((resourceDirectory in Compile).value / "application.conf")
    val prefix = s"db.default"
    (cfg.getString(s"$prefix.url"), cfg.getString(s"$prefix.username"), cfg.getString(s"$prefix.password"))
}
flywayUrl := flywayDbConf.value._1
flywayUser := flywayDbConf.value._2
flywayPassword := flywayDbConf.value._3
flywayLocations := Seq("filesystem:conf/db/migration")
