import de.fosd.typechef.featureexpr.{ FeatureExprParser, FeatureExprFactory }

object CheckConstraints extends App {

  val allowedConstrLen = 1000000;

  FeatureExprFactory.setDefault(FeatureExprFactory.bdd)

  val rawfmFile = "fodder/dependencies.fm"
  val fmFile = "fodder/dependencies.dimacs"
  val unfiltered_constraintsFile = "fodder/constraints.fm"
  val constraintsFile = "fodder/filtered_constraints.fm"
  Util.translateToDimacs(rawfmFile, fmFile)
  Util.removeUnknowns(unfiltered_constraintsFile, constraintsFile)

  val constraints = io.Source.fromFile(constraintsFile).getLines()

  val parser = new FeatureExprParser(FeatureExprFactory.bdd)

  // val fm = FeatureExprFactory.dflt.featureModelFactory.create(parser.parseFile(fmFile))
  val fm = FeatureExprFactory.dflt.featureModelFactory.createFromDimacsFile(fmFile, "")

  println("==Max Constraint Length allowed, set to " + allowedConstrLen);

  var maxConstrLenDetected = 0
  var minConstrLenDetected = allowedConstrLen
  var constrCount = 0
  var avgConstrLen = 0
  var ignoredConstrCount = 0
  var notTautologyCount = 0
  var constrLens = List[Int]()
  for (constraint <- constraints if !constraint.trim.isEmpty) {
    constrCount += 1
    println("checking " + constraint)

    val c = constraint.split("//").head
    val constrLen = c.length()
    constrLens = constrLen :: constrLens
    avgConstrLen += constrLen

    if (constrLen > maxConstrLenDetected)
      maxConstrLenDetected = constrLen;
    if (minConstrLenDetected > constrLen)
      minConstrLenDetected = constrLen;
    if (constrLen <= allowedConstrLen) {
      val expr = parser.parse(c)

      if (!expr.isTautology(fm)) {
        notTautologyCount += 1
        println("  not a tautology!")
      }

    } else {
      println("  expr ignored for being too long , >" + allowedConstrLen)
      ignoredConstrCount += 1
    }

  }

  avgConstrLen /= constrCount;
  constrLens = constrLens.sortWith(_ < _);

  var lenMap: Map[Int, Int] = Map()
  for (entry <- constrLens) {
    var currentSum = 1;
    if (lenMap.contains(entry))
      currentSum += lenMap(entry)
    lenMap += (entry -> currentSum)
  }

  println("==Constraints ignored = " + ignoredConstrCount)
  println("==Constraints ! Tautology = " + notTautologyCount)
  println("=========")
  println("==Min constraint length  detected = " + minConstrLenDetected)
  println("==Max constraint length  detected = " + maxConstrLenDetected)
  println("==Avg constraint length  detected = " + avgConstrLen)
  println("==Med constraint length  detected = " + constrLens((constrLens.length / 2)))
  // println(constrLens)
  println("=========")
  // println(lenMap)

}
