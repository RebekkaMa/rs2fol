import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import parser.RDFSurfacesParser
import java.io.File

class TransformerTest
    : ShouldSpec(
    {

        val transformer = Transformer()

        should("transform example2.n3 without exception") {
            val file = File("src/test/resources/turtle/example2.n3")
            println(transformer.toNotation3Sublanguage(RDFSurfacesParser.parseToEnd(file.readText())))
        }

        should("transform example23.n3 without exception") {
            val file = File("src/test/resources/turtle/example24.n3")
            println(transformer.toNotation3Sublanguage(RDFSurfacesParser.parseToEnd(file.readText())))
        }

        should("transform example28.n3 without exception") {
            val file = File("src/test/resources/turtle/example28.n3")
            println(transformer.toNotation3Sublanguage(RDFSurfacesParser.parseToEnd(file.readText())))
        }

        should("transform lists.n3 without exception") {
            val file = File("src/test/resources/lists/ex01/lists.n3")
            println(transformer.toNotation3Sublanguage(RDFSurfacesParser.parseToEnd(file.readText())))
        }

        context("decode and encode"){
            should("encode and decode 1"){
                val testStr = "The first line\n" +
                        "The second line\n" +
                        "  more"
                val encoded = transformer.encode(testStr)
                println(encoded)
                println(transformer.decode(encoded))
                testStr shouldBe transformer.decode(encoded)
            }

            should("encode and decode 2"){
                val testStr = "The first line\\nThe second line\\n  more"
                val encoded = transformer.encode(testStr)
                println(encoded)
                println(transformer.decode(encoded))
                testStr shouldBe transformer.decode(encoded)
            }

            should("encode and decode 3"){
                val testStr = "The first line\\nThe second line\\n  more"
                val encoded = transformer.encode(testStr)
                println(encoded)
                println(transformer.decode(encoded))
                testStr shouldBe transformer.decode(encoded)
            }
            should("encode and decode 4"){
                val testStr = "This is a multi-line                        # literal with embedded new lines and quotes\n" +
                        "literal with many quotes (\"\"\"\"\")\n" +
                        "and up to two sequential apostrophes ('')."
                val encoded = transformer.encode(testStr)
                println(encoded)
                println(transformer.decode(encoded))
                testStr shouldBe transformer.decode(encoded)
            }
        }
    })