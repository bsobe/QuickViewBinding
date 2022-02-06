package util

import addAttributes
import com.intellij.psi.impl.source.xml.XmlAttributeImpl
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.xml.XmlTag
import findLayoutTag
import model.XmlAttributeWithId
import model.XmlLayers

object XmlCleaner {

    fun cleanAndReturnVariables(rootTag: XmlTag): XmlLayers {
        // region layout operations
        val layoutTag = rootTag.findLayoutTag() as XmlTagImpl
        val nameSpaceAttributes = rootTag.attributes.filter { it.name.startsWith("xmlns:") }
        layoutTag.addAttributes(nameSpaceAttributes)
        // endregion

        // region collect DataBinding variables
        val variableList = DataBindingVariableParser.parse(rootTag)
        // endregion

        val builder = StringBuilder()
        builder.toString()
        val list = mutableListOf<String>()
        var lastNewLineIndex = -1
        val willCleanList = mutableListOf<XmlAttributeWithId>()
        val newContentList = mutableListOf<String>()
        parse(layoutTag, newContentList, willCleanList)
        newContentList
            .filterNot { it.isEmpty() }
            .filterNot { it.contains("@{") }
            .forEachIndexed { index, text ->
                if (text.contains("\n ") && index == lastNewLineIndex + 1) {
                    lastNewLineIndex = index
                    return@forEachIndexed
                } else {
                    if (text.contains("\n   ")) {
                        lastNewLineIndex = index
                    }
                    list.add(text)
                }
            }
        list.forEach { builder.append(it) }
        return XmlLayers(
            dataBindingVariables = variableList,
            dataBindingLineAttributes = willCleanList,
            content = builder.toString()
        )
    }

    private fun parse(
        xmlTagImpl: XmlTagImpl,
        targetList: MutableList<String>,
        willCleanList: MutableList<XmlAttributeWithId>
    ) {
        val nameAttr = xmlTagImpl.children.find { it !is XmlTag && it.text.contains("android:id=") }
        val id = nameAttr?.text?.substring(17, nameAttr.text.length - 1)
        parseWithName(id, xmlTagImpl, targetList, willCleanList)
    }

    private fun parseWithName(
        tagId: String?,
        xmlTagImpl: XmlTagImpl,
        targetList: MutableList<String>,
        willCleanList: MutableList<XmlAttributeWithId>
    ) {
        val current = XmlAttributeWithId(tagId.orEmpty(), mutableListOf())
        xmlTagImpl.children.forEach {
            if (it is XmlAttributeImpl && it.text.contains("@{")) {
                current.xmlAttributeImpl.add(it)
                return@forEach
            } else if (it is XmlTagImpl) {
                parse(it, targetList, willCleanList)
            } else {
                targetList.add(it.text)
            }
        }
        if (tagId == null && current.xmlAttributeImpl.isNotEmpty()) {
            // TODO showAlert
        }
        willCleanList.add(current)
    }


}