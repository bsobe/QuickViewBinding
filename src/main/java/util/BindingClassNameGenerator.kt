package util

fun generateClassName(xmlName: String): String {
    val builder = StringBuilder()
    var capitalizeNextChar = true
    xmlName.toCharArray().forEach {
        if (it == '_') {
            capitalizeNextChar = true
            return@forEach
        }
        if (capitalizeNextChar) {
            builder.append(it.uppercase())
        } else {
            builder.append(it)
        }
        capitalizeNextChar = false
    }
    builder.append("Binding")
    return builder.toString()
}