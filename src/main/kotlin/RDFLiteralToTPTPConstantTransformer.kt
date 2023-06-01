import javax.xml.bind.DatatypeConverter
import javax.xml.datatype.DatatypeFactory

class RDFLiteralToTPTPConstantTransformer {

    fun test(lexicalValue : String, datatype: String): String{
        val datatypeFactory = DatatypeFactory.newDefaultInstance()
        //TODO(Speparate "http://www.w3.org/2001/XMLSchema#" from ...)
        //TODO(Catch exceptions)
        val result =  when (datatype){
            "http://www.w3.org/2001/XMLSchema#string" -> DatatypeConverter.parseString(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#boolean" -> DatatypeConverter.parseBoolean(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#decimal" -> DatatypeConverter.parseDecimal(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#integer" -> DatatypeConverter.parseInteger(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#double" -> DatatypeConverter.parseDouble(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#float" -> DatatypeConverter.parseFloat(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#date" -> DatatypeConverter.parseDate(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#time" -> DatatypeConverter.parseTime(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#dateTime" -> DatatypeConverter.parseDateTime(lexicalValue) //TODO()
            "http://www.w3.org/2001/XMLSchema#dateTimeStamp" -> TODO()
            "http://www.w3.org/2001/XMLSchema#gYear" -> TODO()
            "http://www.w3.org/2001/XMLSchema#gMonth" -> TODO()
            "http://www.w3.org/2001/XMLSchema#gDay" -> TODO()
            "http://www.w3.org/2001/XMLSchema#gYearMonth" -> TODO()
            "http://www.w3.org/2001/XMLSchema#duration" -> datatypeFactory.newDuration(lexicalValue).toString()
            "http://www.w3.org/2001/XMLSchema#yearMonthDuration" -> datatypeFactory.newDurationYearMonth(lexicalValue).toString()
            "http://www.w3.org/2001/XMLSchema#dayTimeDuration" -> datatypeFactory.newDurationDayTime(lexicalValue).toString()
            "http://www.w3.org/2001/XMLSchema#byte" -> DatatypeConverter.parseByte(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#short" -> DatatypeConverter.parseShort(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#int" -> DatatypeConverter.parseInt(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#long" -> DatatypeConverter.parseLong(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#unsignedByte" -> TODO()
            "http://www.w3.org/2001/XMLSchema#unsignedShort" -> DatatypeConverter.parseUnsignedShort(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#unsignedInt" -> DatatypeConverter.parseUnsignedInt(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#unsignedLong" -> TODO()
            "http://www.w3.org/2001/XMLSchema#positiveInteger" -> TODO()
            "http://www.w3.org/2001/XMLSchema#nonNegativeInteger" -> TODO()
            "http://www.w3.org/2001/XMLSchema#negativeInteger" -> TODO()
            "http://www.w3.org/2001/XMLSchema#nonPositiveInteger" -> TODO()
            "http://www.w3.org/2001/XMLSchema#hexBinary" -> DatatypeConverter.parseHexBinary(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#base64Binary" -> DatatypeConverter.parseBase64Binary(lexicalValue)
            "http://www.w3.org/2001/XMLSchema#anyURI" -> TODO()
            "http://www.w3.org/2001/XMLSchema#language" -> TODO()
            "http://www.w3.org/2001/XMLSchema#normalizedString" -> TODO()
            "http://www.w3.org/2001/XMLSchema#token" -> TODO()
            "http://www.w3.org/2001/XMLSchema#NMTOKEN" -> TODO()
            "http://www.w3.org/2001/XMLSchema#Name" -> TODO()
            "http://www.w3.org/2001/XMLSchema#NCName" -> TODO()
            else -> lexicalValue
        }
        return "'$result'"
    }




}