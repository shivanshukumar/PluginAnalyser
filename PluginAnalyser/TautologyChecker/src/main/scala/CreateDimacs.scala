//package de.fosd.typechef.busybox

import de.fosd.typechef.featureexpr.{FeatureExprFactory, FeatureExprParser}
import de.fosd.typechef.featureexpr.sat.{SATFeatureModel, SATFeatureExpr}
import java.io.{OutputStreamWriter, PrintStream, File, FileWriter}
import FeatureExprFactory.sat._

/**
 * reads a feature expression from a file (parameter 1) and creates a dimacs file (parameter 2)
 */

 object CreateDimacs {
  def translateToDimacs(inputFilename:String, outputFilename:String)
  {
    if (null==inputFilename || null==outputFilename) {println("expect input file as parameter");}
    else {
        //test
//        val tmpExpr=createDefinedExternal("CONFIG_FEATURE_TELNETD_INETD_WAIT") implies createDefinedExternal("CONFIG_FEATURE_TELNETD_STANDALONE")

      //  val inputFilename = args(0)
        assert(new File(inputFilename).exists(), "File " + inputFilename + " does not exist")
        val fexpr = new FeatureExprParser(FeatureExprFactory.sat).parseFile(inputFilename).asInstanceOf[SATFeatureExpr]


        val fm = SATFeatureModel.create(fexpr).asInstanceOf[SATFeatureModel]

        //test
//        assert(tmpExpr.isTautology(fm))

      //  val outputFilename = if (args.length < 2) "fm.dimacs" else args(1)
        val out = //new OutputStreamWriter())
            new FileWriter(outputFilename)

        for ((v, i) <- fm.variables)
            out.write("c " + i + " " + v + "\n")

        out.write("p cnf " + fm.variables.size + " " + fm.clauses.size() + "\n")

        var i = 0
        while (i < fm.clauses.size) {
            val c = fm.clauses.get(i)
            val vi = c.iterator()
            while (vi.hasNext)
                out.write(vi.next + " ")
            out.write("0\n")
            i=i+1
        }

        out.close()

        println("wrote .dimacs.")

//        println(fexpr)

//        val fm2=SATFeatureModel.createFromDimacsFile_2Var(outputFilename)
//        assert(tmpExpr.isTautology(fm2))

    }
    }
}
