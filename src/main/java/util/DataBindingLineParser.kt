package util

import model.DataBindingLine
import model.XmlAttributeWithId

object DataBindingLineParser {

    fun parse(list: MutableList<XmlAttributeWithId>): List<DataBindingLine> =
        list.flatMap {
            it.xmlAttributeImpl.map { attribute ->
                val children = attribute.children
                val withNameSpace = children[0].text
                val separatorIndex = withNameSpace.indexOf(":") + 1
                val targetFunctionName = withNameSpace.substring(separatorIndex)
                val dirtyValue = children[2].text
                val value = dirtyValue.removePrefix("\"@{").removeSuffix("}\"")
                generateDataBindingLine(it.viewId, targetFunctionName, value)
            }
        }

    private fun generateDataBindingLine(
        viewId: String,
        targetFunctionName: String,
        value: String
    ): DataBindingLine {
        return if (value.contains("context")) {
            DataBindingLine.SimpleContextLine(viewId, targetFunctionName, value)
        } else {
            DataBindingLine.SimpleLine(viewId, targetFunctionName, value)
        }
    }
}