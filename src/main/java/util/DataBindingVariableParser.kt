package util

import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import findFirstDataTag
import model.KotlinVariable

object DataBindingVariableParser {

    fun parse(rootTag: XmlTag): List<KotlinVariable> {
        val variableList = (rootTag.findFirstDataTag() as XmlTagImpl)
            .findSubTags("variable")
            .map {
                it.children
                    .filterIsInstance<XmlAttribute>()
                    .map { childTag -> childTag.text }
            }
        return variableList.map {
            val variableName = it.first().substring(6, it.first().length - 1)
            val variableFullClassName = it.last()
            val indexOfClassNameSeparator = variableFullClassName.lastIndexOf(".")
            val parsedPackageName = variableFullClassName.substring(
                startIndex = 6,
                endIndex = indexOfClassNameSeparator
            )
            val parsedClassName = variableFullClassName.substring(
                startIndex = indexOfClassNameSeparator + 1,
                endIndex = variableFullClassName.length - 1
            )
            KotlinVariable(
                variableName = variableName,
                className = parsedClassName,
                packageName = parsedPackageName
            )
        }
    }
}