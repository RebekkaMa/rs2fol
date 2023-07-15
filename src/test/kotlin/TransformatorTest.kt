import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.ShouldSpec
import parser.RDFSurfacesParser
import java.io.File

class TransformatorTest
    : ShouldSpec(
    {

        val transformer = Transformer()

        should("transform example2.n3 without exception") {
            val file = File("src/test/resources/turtle/example2.n3")
            println(transformer.printRDFSurfaceGraphUsingNotation3(RDFSurfacesParser.parseToEnd(file.readText())))
        }

        should("transform example23.n3 without exception") {
            val file = File("src/test/resources/turtle/example24.n3")
            println(transformer.printRDFSurfaceGraphUsingNotation3(RDFSurfacesParser.parseToEnd(file.readText())))
        }

        should("transform example28.n3 without exception") {
            val file = File("src/test/resources/turtle/example28.n3")
            println(transformer.printRDFSurfaceGraphUsingNotation3(RDFSurfacesParser.parseToEnd(file.readText())))
        }

        should("transform lists.n3 without exception") {
            val file = File("/home/rebekka/Nextcloud/Studium/SS23/untitled1/src/test/resources/lists/ex01/lists.n3")
            println(transformer.printRDFSurfaceGraphUsingNotation3(RDFSurfacesParser.parseToEnd(file.readText())))
        }
    })