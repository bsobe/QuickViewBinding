package model

class XmlLayers(
    val dataBindingVariables: List<KotlinVariable>,
    val dataBindingLineAttributes: MutableList<XmlAttributeWithId>,
    val content: String
)