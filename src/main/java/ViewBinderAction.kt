import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFile
import com.intellij.psi.XmlElementFactory
import com.intellij.psi.xml.XmlTag
import model.KotlinFunctionSpecs
import model.XmlAttributeWithId
import model.XmlLayers
import util.*
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

@Suppress("UnstableApiUsage", "MissingRecentApi")
class ViewBinderAction : IntentionAction {

    val logger = Logger.getInstance(ViewBinderAction::class.java)

    override fun getText(): String = "Convert to view binding layout"

    override fun getFamilyName(): String = "Convert to view binding layout"

    override fun startInWriteAction(): Boolean = true

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        val rootTag = file.getRootTag() ?: return false
        if (rootTag.isDatabindingRootTag().not()) return false
        return !BLACK_LIST.contains(rootTag.name)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        file ?: return
        val packageName = Messages.showInputDialog(
            /* message = */ "Enter a module package name which is defined in AndroidManifest",
            /* title = */"PackageName",
            /* icon = */ null
        )?.replace(".", "/") ?: return
        val xmlLayers = separateLayers(file) ?: return
        // region create kotlin files to provide backward compatibility
        val kotlinLines: List<KotlinFunctionSpecs> = createBinders(xmlLayers)
        createKotlinFile(file, packageName, kotlinLines)
        replaceXmlWithNewContent(project, file, xmlLayers)
        // migrateViewBinding(file, project)
    }

    private fun replaceXmlWithNewContent(
        project: Project,
        file: PsiFile,
        xmlLayers: XmlLayers,
    ) {
        val factory = XmlElementFactory.getInstance(project)
        val rootTag: XmlTag = file.getRootTag() ?: return
        val layoutXmlTag = factory.createTagFromText(xmlLayers.content, XMLLanguage.INSTANCE)
        rootTag.replace(layoutXmlTag)
    }

    private fun separateLayers(file: PsiFile?): XmlLayers? {
        val rootTag: XmlTag = file.getRootTag() ?: return null
        // region remove DataBinding and collect variables to create meaningful kotlin lines
        val parsedXmlLayers: XmlLayers = XmlCleaner.cleanAndReturnVariables(rootTag)
        val dataBindingLineAttributes: MutableList<XmlAttributeWithId> = parsedXmlLayers.dataBindingLineAttributes
        val viewBindingContent = parsedXmlLayers.content
        // endregion
        return parsedXmlLayers
    }

    private fun createBinders(xmlLayers: XmlLayers): List<KotlinFunctionSpecs> {
        val viewBindingStatements = DataBindingLineParser.parse(xmlLayers.dataBindingLineAttributes)
        val functionParameter = xmlLayers.dataBindingVariables.map { variable ->
            val statementList = viewBindingStatements.filter { statement ->
                statement.value.contains(variable.variableName + ".")
            }
            KotlinFunctionSpecs(variable, statementList.map { XmlLineToKotlinStatementConverter.convert(it) })
        }
        return functionParameter
    }

    private fun createKotlinFile(
        file: PsiFile,
        packageName: String,
        kotlinLines: List<KotlinFunctionSpecs>,
    ): Boolean {
        val xmlPath = file.virtualFile.toNioPath()
        val layoutFolderPath = xmlPath.parent
        val moduleRootPath = layoutFolderPath.pathString.removeSuffix("/layout").removeSuffix("/res")
        val javaPath = "$moduleRootPath/java"
        val moduleSourceCodePath = "$javaPath/$packageName"
        val moduleSourceCodeNioPath = Path.of(moduleSourceCodePath)
        return FileWriter.writer(
            viewBindingPackageName = "",
            viewBindingClassName = generateClassName(xmlPath.nameWithoutExtension),
            functions = kotlinLines,
            path = moduleSourceCodeNioPath
        )
    }

    companion object {
        val BLACK_LIST = listOf("manifest", "project", "component", "module", "selector", "menu", "resources", "alpha")
    }
}