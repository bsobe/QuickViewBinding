package model

sealed class DataBindingLine(
    val viewId : String,
    val targetFunctionName: String,
    val value: String,
) {

    abstract fun toKotlinLine() : String

    class SimpleLine(
        viewId: String,
        targetFunctionName: String,
        value: String
    ) : DataBindingLine(viewId, targetFunctionName, value) {
        override fun toKotlinLine(): String {
            return "this.${viewId}.${targetFunctionName} = $value"
        }
    }

    class SimpleContextLine(
        viewId: String,
        targetFunctionName: String,
        value: String
    ) : DataBindingLine(viewId, targetFunctionName, value) {
        override fun toKotlinLine(): String {
            val contextIndex = value.indexOf("context")
            val contextValue = """${value.substring(0, contextIndex)}this.$viewId.${value.substring(contextIndex, value.length)}"""
            return "this.${viewId}.${targetFunctionName} = $contextValue"
        }
    }
}