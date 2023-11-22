val scala3Version = "3.3.1"
val V = new {
  val quill = "4.8.0"
  val zio = "2.0.18"
  val sttp = "4.0.0-M6"
}

lazy val root = project
  .in(file("."))
  .settings(
    name := "fp-zio",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    // Compile scope
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client4" %% "zio-json" % V.sttp,

      // Syncronous JDBC Modules
      "io.getquill" %% "quill-jdbc-zio" % V.quill
        // Exclusion to resolve conflict with scala 3 version of geny:
        // [error] Modules were resolved with conflicting cross-version suffixes in ProjectRef(uri("file:/.../importer/"), "root"):
        // [error]    com.lihaoyi:geny _3, _2.13
        // [error] (update) Conflicting cross-version suffixes in: com.lihaoyi:geny
        exclude("com.lihaoyi", "geny_2.13"),
      "org.postgresql" % "postgresql" % "42.6.0",

      "ch.qos.logback" % "logback-classic" % "1.4.7",
    ),

    // Test scope
    libraryDependencies += "dev.zio" %% "zio-test" % V.zio % Test
  )
