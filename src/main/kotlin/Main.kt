import org.apache.commons.cli.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

enum class Opts(val option: Option, val default: String?) {
    SOURCE(Option("s", "src", true, "location of application root project directory"),
            "."),
    DESTINATION(Option("d", "dst", true, "destination directory where translated and untranslated files will be saved"),
            Paths.get("./missing-android-translations").toString()),
    CLONE(Option("c", "download", true, "repository url location to fetch the project from"), null),
    LOGIN(Option("l", "login", true, "login to the github account"), null),
    PASSWORD(Option("p", "password", true, "password to the github account"), null),
    XML(Option("xml", "xml", false, "use .xml extension instead of .txt to output files"), null);

    val opt get() = option.opt!!
}

fun main(args: Array<String>) {
    val cmd = parseArgs(args)!!
    var srcDir = cmd.getOptionValue(Opts.SOURCE.opt, Opts.SOURCE.default)

    cmd.getOptionValue(Opts.CLONE.opt, Opts.CLONE.default)?.let { repoUrl ->
        // If clone is present then clone it to tempDir and set it as srcDir
        val name = repoUrl.substring(repoUrl.lastIndexOf("/") + 1, repoUrl.length - 4)
        println("Project name: $name")

        val tempDir = Files.createTempDirectory(name)
        tempDir.toFile().deleteOnExit()
        println("Temp directory: $tempDir")
        // tempDir becomes the srcDirectory
        srcDir = tempDir.toString()

        val login = cmd.getOptionValue(Opts.LOGIN.opt, Opts.LOGIN.default)
        val password = cmd.getOptionValue(Opts.PASSWORD.opt, Opts.PASSWORD.default)

        Git.cloneRepository(repoUrl, tempDir, login, password)
    }

    val projectResources = Paths.get(srcDir, "app", "src", "main", "res")
    val untranslatedFilesDirectory = File(cmd.getOptionValue(Opts.DESTINATION.opt, Opts.DESTINATION.default))
    untranslatedFilesDirectory.mkdir()

    val globalStrings = projectResources.resolve("values").resolve("strings.xml").toFile()

    val localeDirs = projectResources.toFile().list { current, name ->
        isValidLocaleResourceFolder(File(current, name))
    }
    println(Arrays.toString(localeDirs))

    val dbFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dbFactory.newDocumentBuilder()
    val globalDoc = dBuilder.parse(globalStrings)

    globalDoc.documentElement.normalize()

    val globalNodeList = globalDoc.getElementsByTagName("string")

    val outputExtension = if (cmd.hasOption(Opts.XML.opt)) "xml" else "txt"

    for (localeDirName in localeDirs!!) {
        val localeCode = localeDirName.substring(7)
        val localeStringsFile = projectResources.resolve(localeDirName).resolve("strings.xml")

        Files.copy(localeStringsFile,
                Paths.get(untranslatedFilesDirectory.absolutePath, "translated-$localeCode.$outputExtension"),
                StandardCopyOption.REPLACE_EXISTING)

        val localeDoc = dBuilder.parse(localeStringsFile.toString())
        localeDoc.documentElement.normalize()
        val localeNodeList = localeDoc.getElementsByTagName("string")
        val untranslated = getUntranslatedElements(globalNodeList, localeNodeList)

        saveToFile(untranslatedFilesDirectory, localeCode, outputExtension, untranslated)
    }
    globalStrings.copyTo(File(untranslatedFilesDirectory, "new-language-strings.$outputExtension"), true)

}


fun parseArgs(args: Array<String>): CommandLine? {
    val options = Options().apply {
        addOption(Opts.SOURCE.option)
        addOption(Opts.DESTINATION.option)
        addOption(Opts.CLONE.option)
        addOption(Opts.LOGIN.option)
        addOption(Opts.PASSWORD.option)
        addOption(Opts.XML.option)
    }
    val helpFormatter = HelpFormatter()
    val parser = DefaultParser()

    return try {
        parser.parse(options, args)
    } catch (e: ParseException) {
        helpFormatter.printHelp("android-missing-translations", options, true)
        println(e.message)
        System.exit(0)
        null
    }
}


private fun isValidLocaleResourceFolder(file: File) =
        file.isDirectory && Pattern.matches("values-[a-z]{2}(-[a-z][A-Z]{2})?", file.name)


fun foundInLocaleDoc(key: String, localeNodeList: NodeList): Boolean {
    for (index in 0 until localeNodeList.length) {
        val node = localeNodeList.item(index)

        if (node.nodeType == Node.ELEMENT_NODE) {
            val element = node as Element
            if (element.getAttribute("name") == key)
                return true
        }
    }
    return false
}

fun getUntranslatedElements(globalNodeList: NodeList, localeNodeList: NodeList): List<Element> {
    val untranslated = arrayListOf<Element>()
    for (index in 0 until globalNodeList.length) {
        val node = globalNodeList.item(index)
        if (node.nodeType == Node.ELEMENT_NODE) {
            val element = node as Element
            if (element.getAttribute("translatable").isEmpty()
                    && !foundInLocaleDoc(element.getAttribute("name"), localeNodeList))
                untranslated.add(element)
        }
    }
    return untranslated
}

fun saveToFile(untranslatedFilesDirectory: File, localeCode: String, outputExtension: String, untranslated: List<Element>) {
    val untranslatedFile = File(untranslatedFilesDirectory, "untranslated-$localeCode.$outputExtension")
    untranslatedFile.createNewFile()

    PrintWriter(untranslatedFile).use { out ->
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        out.println("<resources>")

        for (element in untranslated)
            out.println("<string name=\"" + element.getAttribute("name") + "\">" + element.textContent + "</string>\n")

        out.println("</resources>")
    }
}