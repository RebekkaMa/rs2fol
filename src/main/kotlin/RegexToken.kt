val pnLocalESC = "(\\\\[-_~.!$&'()*+,;=/?#@%])".toRegex()
val hex = "([0-9]|[A-F]|[a-f])".toRegex()
val percent = "(%$hex{2})".toRegex()
val plx = "$percent|$pnLocalESC".toRegex()
val pnCharsBase =
    "([A-Z]|[a-z]|[\u00C0-\u00D6]|[\u00D8-\u00F6]|[\u00F8-\u02FF]|[\u0370-\u037D]|[\u037F-\u1FFF]|[\u200C-\u200D]|[\u2070-\u218F]|[\u2C00-\u2FEF]|[\u3001-\uD7FF]|[\uF900-\uFDCF]|[\uFDF0-\uFFFD]|[\uD800\uDC00-\uDB7F\uDFFF])".toRegex()
val pnCharsU = "($pnCharsBase|_)".toRegex()
val pnChars = "($pnCharsU|-|[0-9]|\u00B7|[\u0300-\u036F]|[\u203F-\u2040])".toRegex()

val pnLocal = "(($pnCharsU|:|[0-9]|$plx)(($pnChars|\\.|:|$plx)*($pnChars|:|$plx))?)".toRegex()
val pnPrefix = "($pnCharsBase(($pnChars|\\.)*$pnChars)?)".toRegex()

val echar = "(\\\\[tbnrf\"'\\\\])".toRegex()
val uchar = "((\\\\u$hex{4})|(\\\\U$hex{8}))".toRegex()

val stringLiteralLongSingleQuote = "('''(('|(''))?([^'\\\\]|$echar|$uchar))*''')".toRegex()
val stringLiteralLongQuote = "(\"\"\"((\"|(\"\"))?([^\"\\\\]|$echar|$uchar))*\"\"\")".toRegex()
val stringLiteralQuote = "(\"([^\u0022\u000A\u000D\\\\]|$echar|$uchar)*\")".toRegex()
val stringLiteralSingleQuote = "('([^\u0022\u000A\u000D\\\\]|$echar|$uchar)*')".toRegex()
