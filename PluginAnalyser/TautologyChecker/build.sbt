scalaVersion := "2.10.1"

javaOptions += "-Xmx4G -Xms2G -Xss4096m"

libraryDependencies += "de.fosd.typechef" % "featureexprlib_2.10" % "0.3.4"

TaskKey[File]("mkrun") <<= (baseDirectory, fullClasspath in Runtime, mainClass in Runtime) map { (base, cp, main) =>
  val template = """#!/bin/sh
java -ea -Xmx4G -Xms2G -Xss4G -classpath "%s" %s "$@"
"""
  val mainStr = ""
  val contents = template.format(cp.files.absString, mainStr)
  val out = base / "run.sh"
  IO.write(out, contents)
  out.setExecutable(true)
  out
}
