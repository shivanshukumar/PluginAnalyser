import de.fosd.typechef.featureexpr.{FeatureExprParser, FeatureExprFactory}

object CheckConstraints extends App {

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  val fmFile="fodder/dependencies.fm"
  val constraintsFile="fodder/constraints.fm"

  val constraints = io.Source.fromFile(constraintsFile).getLines()


  val parser = new FeatureExprParser(FeatureExprFactory.bdd)

  val fm = FeatureExprFactory.dflt.featureModelFactory.create(parser.parseFile(fmFile))


  for (constraint <- constraints if !constraint.trim.isEmpty) {

    println("checking "+constraint)

    val c = constraint.split("//").head

    val expr=parser.parse(c)

    if (!expr.isTautology(fm))
      println("  not a tautology!")

  }


}
