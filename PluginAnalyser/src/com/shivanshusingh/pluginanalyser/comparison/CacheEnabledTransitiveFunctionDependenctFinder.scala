/*package com.shivanshusingh.pluginanalyser.comparison

import collection.mutable

object LinkDemoCache extends App {


  case class Plugin(name: String, imports: Set[(String, String)], exports: Set[(String, String)], inheritance: Map[String, Set[String]]) {
    override def toString = name
  }

  val plugins: Map[String, Plugin] = Map(
    "P1" -> Plugin("P1", Set(("A", "foo")), Set(("A", "")), Map("A" -> Set("B"))),
    "P2" -> Plugin("P2", Set(), Set(("B", "")), Map("B" -> Set("C"))),
    "P3" -> Plugin("P3", Set(), Set(("C", ""), ("D", ""), ("D", "foo")), Map("C" -> Set("D"))),
    "P4" -> Plugin("P4", Set(), Set(("B", "")), Map("B" -> Set("X"))),
    "P5" -> Plugin("P5", Set(), Set(("X", ""), ("X", "foo")), Map()),
    "P6" -> Plugin("P6", Set(), Set(("B", ""), ("R", "")), Map("B" -> Set("R"), "R" -> Set("C")))
  )

 this is just constructing a map of types to plugins exporting those types
   this is equivalent to the DependencyFinder.types and DependencyFinder.plugins combo. 
  val typesDefined: Map[String, Set[Plugin]] = {
    var result = Map[String, Set[Plugin]]()
    for (plugin <- plugins.values;
         (classname, funname) <- plugin.exports;
         if funname == "")
      result = result + (classname -> (result.getOrElse(classname, Set()) + plugin))
    result
  }

  println(typesDefined)


  var cache: mutable.Map[(String, String), Set[Set[Plugin]]] = mutable.Map()

  //returns a list of plug-in combinations that have fitting exports
  def findExports(imp: (String, String)): Set[Set[Plugin]] =
    cache.getOrElseUpdate(imp, {
      var result = Set[Set[Plugin]]()

      val (classname, funname) = imp

      val targetPlugins: Set[Plugin] = typesDefined.getOrElse(classname, Set())

      for (targetPlugin <- targetPlugins) {
        if (targetPlugin.exports contains imp)
          result = result + Set(targetPlugin)
        else {
          val superclasses = targetPlugin.inheritance.getOrElse(classname, Set())

          for (superclass <- superclasses)
            result = result ++ findExports((superclass, funname)).map(_ + targetPlugin)
        }
      }
      result
    })


  val p1 = plugins("P1")


  for (imp <- p1.imports) {
    println("Import: " + imp)
    println("Export: " + findExports(imp))
  }


  println("Cache: "+cache)

}
*/