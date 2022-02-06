package model

import com.intellij.psi.impl.source.xml.XmlAttributeImpl

class XmlAttributeWithId(
    val viewId: String,
    val xmlAttributeImpl: MutableList<XmlAttributeImpl>
)