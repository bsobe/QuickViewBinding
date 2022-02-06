package util

import model.DataBindingLine

object XmlLineToKotlinStatementConverter {

    fun convert(dataBindingLine: DataBindingLine) : String {
        return dataBindingLine.toKotlinLine()
    }
}