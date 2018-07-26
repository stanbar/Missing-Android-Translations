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

enum class Opts(val option: Option, val default: String) {
    SOURCE(Option("s", "src", true, "location of application root project directory"),
            "."),
    DESTINATION(Option("d", "dst", true, "destination directory where translated and untranslated files will be saved"),
            Paths.get("./missing-android-translations").toString());

    val opt get() = option.opt!!
}

fun main(args: Array<String>) {
    val cmd = parseArgs(args)!!
    val srcDir = cmd.getOptionValue(Opts.SOURCE.opt, Opts.SOURCE.default)

    val rootString = Paths.get(srcDir, "app", "src", "main", "res").toString()

    val rootFile = File(rootString)
    try {
        val untranslatedFilesDirectory = File(cmd.getOptionValue(Opts.DESTINATION.opt, Opts.DESTINATION.default))
        untranslatedFilesDirectory.mkdir()

        val globalStrings = Paths.get(rootString, "values", "strings.xml").toFile()

        val localeDirs = rootFile.list { current, name ->
            val file1 = File(current, name)
            isValidLocaleResourceFolder(file1)
        }
        println(Arrays.toString(localeDirs))

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val globalDoc = dBuilder.parse(globalStrings)

        globalDoc.documentElement.normalize()

        val globalNodeList = globalDoc.getElementsByTagName("string")

        for (locale in localeDirs!!) {
            val localeCode = locale.substring(7)

            val localeStringsFile = Paths.get(rootString, locale, "strings.xml")
            Files.copy(localeStringsFile,
                    Paths.get(untranslatedFilesDirectory.absolutePath, "translated-$localeCode.txt"),
                    StandardCopyOption.REPLACE_EXISTING)
            val localeDoc = dBuilder.parse(localeStringsFile.toString())
            localeDoc.documentElement.normalize()
            val localeNodeList = localeDoc.getElementsByTagName("string")

            val untranslated = ArrayList<Element>()
            for (temp in 0 until globalNodeList.length) {

                val nNode = globalNodeList.item(temp)

                if (nNode.nodeType == Node.ELEMENT_NODE) {

                    val eElement = nNode as Element

                    if (eElement.getAttribute("translatable").isEmpty() && !foundInLocaleDoc(eElement.getAttribute("name"), localeNodeList))
                        untranslated.add(eElement)
                }
            }

            val untranslatedFile = File(untranslatedFilesDirectory, "untranslated-$localeCode.txt")
            untranslatedFile.createNewFile()

            PrintWriter(untranslatedFile).use { out ->
                out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                out.println("<resources>")

                for (element in untranslated)
                    out.println("<string name=\"" + element.getAttribute("name") + "\">" + element.textContent + "</string>\n")

                out.println("</resources>")
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun parseArgs(args: Array<String>): CommandLine? {
    val options = Options().apply {
        addOption(Opts.SOURCE.option)
        addOption(Opts.DESTINATION.option)
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


private fun isValidLocaleResourceFolder(file: File): Boolean {
    return file.isDirectory && Pattern.matches("values-[a-z]{2}(-[a-z][A-Z]{2})?", file.name)
}

fun foundInLocaleDoc(key: String, localeNodeList: NodeList): Boolean {

    for (temp in 0 until localeNodeList.length) {

        val nNode = localeNodeList.item(temp)

        if (nNode.nodeType == Node.ELEMENT_NODE) {
            val eElement = nNode as Element
            if (eElement.getAttribute("name") == key)
                return true
        }
    }
    return false
}