name := "hygienic-transformations"

version := "1.0"

scalaVersion := "2.11.4"

scalacOptions += "-deprecation"

//libraryDependencies += "org.eclipse.jdt" % "core" % "3.3.0-v_771"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2"

val JavaTools = List[Option[String]] (
  // manual
  sys.env.get("JDK_HOME"),
  sys.env.get("JAVA_HOME"),
  // osx
  try Some("/usr/libexec/java_home".!!.trim)
  catch {
    case _: Throwable => None
  },
  // fallback
  sys.props.get("java.home").map(new File(_).getParent),
  sys.props.get("java.home")
).flatten.map { n =>
  new File(n + "/lib/tools.jar")
}.find(_.exists).getOrElse (
  throw new java.io.FileNotFoundException (
    """Could not automatically find the JDK/lib/tools.jar.
      |You must explicitly set JDK_HOME or JAVA_HOME.""".stripMargin
  )
)

unmanagedJars in Compile += JavaTools
