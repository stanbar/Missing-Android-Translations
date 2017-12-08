
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Main {

    public static void main(String[] args) {

        readXMLFile();

    }

    public static void readXMLFile() {
        try {
            String rootString = "/Users/admin1/GoogleDrive/AndroidStudioProjects/VapeToolPro/app/src/main/res/";
            File rootFile = new File(rootString);
            File untranslatedFilesDirectory = new File("/Users/admin1/", "untranslated");
            untranslatedFilesDirectory.mkdir();
            File globalStrings = new File(rootString + "values/strings.xml");


            String[] localeDirs = rootFile.list((current, name) -> {
                File file1 = new File(current, name);
                return isValidLocaleResourceFolder(file1);
            });
            System.out.println(Arrays.toString(localeDirs));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document globalDoc = dBuilder.parse(globalStrings);


            globalDoc.getDocumentElement().normalize();


            NodeList globalNodeList = globalDoc.getElementsByTagName("string");


            for (String locale : localeDirs) {
                File localeStringsFile = new File(rootString + locale, "strings.xml");
                Files.copy(localeStringsFile.toPath(), new File(untranslatedFilesDirectory, "translated-" + locale.substring(7) + ".txt").toPath(), REPLACE_EXISTING);
                Document localeDoc = dBuilder.parse(localeStringsFile);
                localeDoc.getDocumentElement().normalize();
                NodeList localeNodeList = localeDoc.getElementsByTagName("string");

                List<Element> untranslated = new ArrayList<>();
                for (int temp = 0; temp < globalNodeList.getLength(); temp++) {

                    Node nNode = globalNodeList.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        if (eElement.getAttribute("translatable").isEmpty() && !foundInLocaleDoc(eElement.getAttribute("name"), localeNodeList))
                            untranslated.add(eElement);
                    }
                }

                File untranslatedFile = new File(untranslatedFilesDirectory, "untranslated-" + locale.substring(7) + ".txt");
                untranslatedFile.createNewFile();

                try (PrintWriter out = new PrintWriter(untranslatedFile)) {
                    out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                    out.println("<resources>");

                    for (Element element : untranslated)
                        out.println("<string name=\"" + element.getAttribute("name") + "\">" + element.getTextContent() + "</string>\n");

                    out.println("</resources>");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidLocaleResourceFolder(File file) {
        return  file.isDirectory() && Pattern.matches("values-[a-z]{2}(-[a-z][A-Z]{2})?",file.getName());
    }

    public static boolean foundInLocaleDoc(String key, NodeList localeNodeList) {

        for (int temp = 0; temp < localeNodeList.getLength(); temp++) {

            Node nNode = localeNodeList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (Objects.equals(eElement.getAttribute("name"), key))
                    return true;
            }
        }
        return false;
    }


}
