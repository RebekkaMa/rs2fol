import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import parser.RDFSurfacesParser
import java.io.File

class RDFSurfacesParserTest : ShouldSpec(
    {

        afterTest {
            RDFSurfacesParser.resetAll()
        }

        should("transform example2.n3 without exception") {
            val file = File("src/test/resources/turtle/example2.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example2.p")
            println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText())))
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }

        should("transform example3.n3 without exception") {
            val file = File("src/test/resources/turtle/example3.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example3.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example4.n3 without exception") {
            val file = File("src/test/resources/turtle/example4.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example4.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example5.n3 without exception") {
            val file = File("src/test/resources/turtle/example5.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example5.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example6.n3 without exception") {
            val file = File("src/test/resources/turtle/example6.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example6.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example7.n3 without exception") {
            val file = File("src/test/resources/turtle/example7.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example7.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example8.n3 without exception") {
            val file = File("src/test/resources/turtle/example8.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example8.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example9.n3 without exception") {
            val file = File("src/test/resources/turtle/example9.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example9.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example10.n3 without exception") {
            val file = File("src/test/resources/turtle/example10.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example10.p")
            println((Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))))
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        //TODO("Multiline Support, multiple ' in TPTP Format")
        should("transform example11.n3 without exception") {
            val file = File("src/test/resources/turtle/example11.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example11.p")
            println((Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))))
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example12.n3 without exception") {
            val file = File("src/test/resources/turtle/example12.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example12.p")
            println((Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))))
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example13.n3 without exception") {
            val file = File("src/test/resources/turtle/example13.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example13.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example14.n3 without exception") {
            val file = File("src/test/resources/turtle/example14.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example14.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example15.n3 without exception") {
            val file = File("src/test/resources/turtle/example15.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example15.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example16.n3 without exception") {
            val file = File("src/test/resources/turtle/example16.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example16.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()

        }
        should("transform example17.n3 without exception") {
            val file = File("src/test/resources/turtle/example17.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example17.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example18.n3 without exception") {
            val file = File("src/test/resources/turtle/example18.n3")
            //val solutionFile = File("src/test/resources/turtle-fol/example18.p")
            println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText())))
        }
        should("transform example19.n3 without exception") {
            val file = File("src/test/resources/turtle/example19.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example19.p")
            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example20.n3 without exception") {
            val file = File("src/test/resources/turtle/example20.n3")
            //val solutionFile = File("src/test/resources/turtle-fol/example20.p")
            println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText())))
        }
        should("transform example21.n3 without exception") {
            val file = File("src/test/resources/turtle/example21.n3")
            //(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()

            println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText())))
        }
        should("transform example22.n3 without exception") {
            val file = File("src/test/resources/turtle/example22.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example22.p")

            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example23.n3 without exception") {
            val file = File("src/test/resources/turtle/example23.n3")
            //val solutionFile = File("src/test/resources/turtle-fol/example23.p")

            println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText())))
        }
        should("transform example24.n3 without exception") {
            val file = File("src/test/resources/turtle/example24.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example24.p")

            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example25.n3 without exception") {
            val file = File("src/test/resources/turtle/example25.n3")
            //val solutionFile = File("src/test/resources/turtle-fol/example25.p")

            println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText())))
        }
        should("transform example26.n3 without exception") {
            val file = File("src/test/resources/turtle/example26.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example26.p")

            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example27.n3 without exception") {
            val file = File("src/test/resources/turtle/example27.n3")
            val solutionFile = File("src/test/resources/turtle-fol/example27.p")

            (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()))) shouldBeEqualComparingTo solutionFile.readText()
        }
        should("transform example28.n3 without exception") {
            val file = File("src/test/resources/turtle/example28.n3")
            println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText())))
        }

        context("ownExamples") {
            val file = File("src/test/resources/ownExamples/lists.n3")

            println(
                (Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText())))
            )

        }

        context("blogic") {
            should("transform blogic abc.n3") {
                val file = File("src/test/resources/blogic/abc.n3")
                val solutionFile = File("src/test/resources/blogic-fol/abc-fol.p")

                println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()),true))
                Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()),true) shouldBeEqualComparingTo solutionFile.readText()

            }
            should("transform blogic abcd.n3") {
                val file = File("src/test/resources/blogic/abcd.n3")
                val solutionFile = File("src/test/resources/blogic-fol/abcd-fol.p")

                println(Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()), true))
                Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()),true) shouldBeEqualComparingTo solutionFile.readText()

            }

        }
        context("lists"){
            should("transform blogic lists.n3") {
                val file = File("src/test/resources/lists/lists.n3")
                val solutionFile = File("src/test/resources/lists/lists.p")

                Transformer().transformToFOL(RDFSurfacesParser.parseToEnd(file.readText()),true) shouldBeEqualComparingTo solutionFile.readText()
            }
        }
    })
