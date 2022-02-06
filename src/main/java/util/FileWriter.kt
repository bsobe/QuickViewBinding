package util

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import model.KotlinFunctionSpecs
import java.nio.file.Path

object FileWriter {

    fun writer(
        viewBindingPackageName: String,
        viewBindingClassName: String,
        functions: List<KotlinFunctionSpecs>,
        path: Path
    ): Boolean {
        return try {
            val kotlinFunctions = functions.map {
                function(
                    viewBindingPackageName = viewBindingPackageName,
                    viewBindingClassName = viewBindingClassName,
                    function = it
                )
            }
            val fileBuilder = FileSpec.builder(viewBindingPackageName, viewBindingClassName + "Binder")
            kotlinFunctions.forEach { fileBuilder.addFunction(it) }
            fileBuilder.build().writeTo(path)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun function(
        viewBindingPackageName: String,
        viewBindingClassName: String,
        function: KotlinFunctionSpecs,
    ): FunSpec {
        val variable = function.variable
        val builder = FunSpec.builder("set${variable.className}").apply {
            receiver(ClassName(viewBindingPackageName, viewBindingClassName))
            addParameter(variable.variableName, ClassName(variable.packageName, function.variable.className))
            function.statements.forEach { addStatement(it) }
        }
        return builder.build()
    }
}