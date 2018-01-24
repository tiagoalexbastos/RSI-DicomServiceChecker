package xml

import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class NmapOutputParser {
    private val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    private val dBuilder: DocumentBuilder
    private val ROOT_TAG = "port"

    init {
        dBuilder = dbFactory.newDocumentBuilder()
    }


    fun parseResults(results: String): ArrayList<String> {
        if (results.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
            val doc = dBuilder.parse(InputSource(StringReader(results)))
            doc.documentElement.normalize()
            val ret = arrayListOf<String>()

            val nList = doc.getElementsByTagName(ROOT_TAG)

            return (0 until nList.length)
                    .asSequence()
                    .map { nList.item(it) }
                    .map { it as Element }
                    .filter { it.getAttribute("protocol") == "tcp" }
                    .map { it.getAttribute("portid") }
                    .toMutableList() as ArrayList<String>
        } else {
            return results.split("\n")
                    .filter { it.contains("/tcp") }
                    .map { it.substringBefore("/tcp") }
                    .toMutableList() as ArrayList<String>
        }
    }

}