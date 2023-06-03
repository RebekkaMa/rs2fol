import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.comparables.shouldNotBeEqualComparingTo

class RDFLiteralToTPTPConstantTransformerTest : ShouldSpec(
    {

        val transformer = RDFLiteralToTPTPConstantTransformer()

        val xmlString = "http://www.w3.org/2001/XMLSchema#string"
        val xmlBoolean = "http://www.w3.org/2001/XMLSchema#boolean"
        val xmlDecimal = "http://www.w3.org/2001/XMLSchema#decimal"
        val xmlInteger = "http://www.w3.org/2001/XMLSchema#integer"
        val xmlDouble = "http://www.w3.org/2001/XMLSchema#double"
        val xmlFloat = "http://www.w3.org/2001/XMLSchema#float"
        val xmlDate = "http://www.w3.org/2001/XMLSchema#date"
        val xmlTime = "http://www.w3.org/2001/XMLSchema#time"
        val xmlDateTime = "http://www.w3.org/2001/XMLSchema#dateTime"
        val xmlDateTimeStamp = "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
        val xmlGYear = "http://www.w3.org/2001/XMLSchema#gYear"
        val xmlGMonth = "http://www.w3.org/2001/XMLSchema#gMonth"
        val xmlGDay = "http://www.w3.org/2001/XMLSchema#gDay"
        val xmlGYearMonth = "http://www.w3.org/2001/XMLSchema#gYearMonth"
        val xmlDuration = "http://www.w3.org/2001/XMLSchema#duration"
        val xmlYearMonthDuration = "http://www.w3.org/2001/XMLSchema#yearMonthDuration"
        val xmlDayTimeDuration = "http://www.w3.org/2001/XMLSchema#dayTimeDuration"
        val xmlByte = "http://www.w3.org/2001/XMLSchema#byte"
        val xmlShort = "http://www.w3.org/2001/XMLSchema#short"
        val xmlInt = "http://www.w3.org/2001/XMLSchema#int"
        val xmlLong = "http://www.w3.org/2001/XMLSchema#long"
        val xmlUnsignedByte = "http://www.w3.org/2001/XMLSchema#unsignedByte"
        val xmlUnsignedShort = "http://www.w3.org/2001/XMLSchema#unsignedShort"
        val xmlUnsignedInt = "http://www.w3.org/2001/XMLSchema#unsignedInt"
        val xmlUnsignedLong = "http://www.w3.org/2001/XMLSchema#unsignedLong"
        val xmlPositiveInteger = "http://www.w3.org/2001/XMLSchema#positiveInteger"
        val xmlNonNegativeInteger = "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"
        val xmlNegativeInteger = "http://www.w3.org/2001/XMLSchema#negativeInteger"
        val xmlNonPositiveInteger = "http://www.w3.org/2001/XMLSchema#nonPositiveInteger"
        val xmlHexBinary = "http://www.w3.org/2001/XMLSchema#hexBinary"
        val xmlBase64Binary = "http://www.w3.org/2001/XMLSchema#base64Binary"
        val xmlAnyURI = "http://www.w3.org/2001/XMLSchema#anyURI"
        val xmlLanguage = "http://www.w3.org/2001/XMLSchema#language"
        val xmlNormalizedString = "http://www.w3.org/2001/XMLSchema#normalizedString"
        val xmlToken = "http://www.w3.org/2001/XMLSchema#token"
        val xmlNMToken = "http://www.w3.org/2001/XMLSchema#NMTOKEN"
        val xmlName = "http://www.w3.org/2001/XMLSchema#Name"
        val xmlNCName = "http://www.w3.org/2001/XMLSchema#NCName"

        should("return equal integer numbers") {

            val res1 = transformer.transformLexicalValue("005", xmlInteger)
            val res2 = transformer.transformLexicalValue("5", xmlInteger)
            val res3 = transformer.transformLexicalValue("+5", xmlInteger)

            res1 shouldBeEqualComparingTo res2
            res2 shouldBeEqualComparingTo res3
        }


        should("return equal decimal numbers") {

            //"Precision is not reflected in this value space." <-- https://www.w3.org/TR/xmlschema11-2/#decimal
            val res1 = transformer.transformLexicalValue("005.", xmlDecimal)
            val res2 = transformer.transformLexicalValue("5.000", xmlDecimal)
            val res3 = transformer.transformLexicalValue("+5", xmlDecimal)
            val res4 = transformer.transformLexicalValue("+5.000", xmlDecimal)

            res1 shouldBeEqualComparingTo res2
            res2 shouldBeEqualComparingTo  res3
            res3 shouldBeEqualComparingTo res4
        }

        should("return equal float numbers") {

            val res1 = transformer.transformLexicalValue("005.", xmlDecimal)
            val res2 = transformer.transformLexicalValue("5.000", xmlDecimal)
            val res3 = transformer.transformLexicalValue("+5", xmlDecimal)
            val res4 = transformer.transformLexicalValue("+5.000", xmlDecimal)

            val res5 = transformer.transformLexicalValue("100", xmlFloat)
            val res6 = transformer.transformLexicalValue("1.0E2", xmlFloat)


            res1 shouldBeEqualComparingTo res2
            res2 shouldBeEqualComparingTo  res3
            res3 shouldBeEqualComparingTo res4

            res5 shouldBeEqualComparingTo res6

        }

        should("return equal double numbers") {

            val res1 = transformer.transformLexicalValue("005.", xmlDecimal)
            val res2 = transformer.transformLexicalValue("5.000", xmlDecimal)
            val res3 = transformer.transformLexicalValue("+5", xmlDecimal)
            val res4 = transformer.transformLexicalValue("+5.000", xmlDecimal)
            val res5 = transformer.transformLexicalValue("NaN", xmlDouble)

            res1 shouldBeEqualComparingTo res2
            res2 shouldBeEqualComparingTo  res3
            res3 shouldBeEqualComparingTo res4
        }

        should("return equal boolean values"){
            val res1 = transformer.transformLexicalValue("true", xmlBoolean)
            val res2 = transformer.transformLexicalValue("1", xmlBoolean)
            val res3 = transformer.transformLexicalValue("false", xmlBoolean)
            val res4 = transformer.transformLexicalValue("0", xmlBoolean)

            res1 shouldBeEqualComparingTo res2
            res3 shouldBeEqualComparingTo res4

        }
        should("return equal date values"){
            val res1 = transformer.transformLexicalValue("2002-10-10+13:00", xmlDate)
            val res2 = transformer.transformLexicalValue("2002-10-09-11:00", xmlDate)

            res1 shouldBeEqualComparingTo res2

        }

        should("return unequal gYear values"){
            val res1 = transformer.transformLexicalValue("2002", xmlGYear)
            val res2 = transformer.transformLexicalValue("2002Z", xmlGYear)

            res1 shouldNotBeEqualComparingTo  res2
        }

        should("return unequal gMonth values"){
            val res1 = transformer.transformLexicalValue("--12", xmlGMonth)
            val res2 = transformer.transformLexicalValue("--12Z", xmlGMonth)
            val res3 = transformer.transformLexicalValue("--12+02:00", xmlGMonth)
            val res4 = transformer.transformLexicalValue("--12-02:00", xmlGMonth)


            res1 shouldNotBeEqualComparingTo  res2

        }

    }


)