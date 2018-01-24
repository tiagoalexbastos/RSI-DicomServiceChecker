package xml

import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class DicomWSXmlParser {
    private val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    private val dBuilder: DocumentBuilder
    private val ROOT_TAG = "DicomAttribute"

    init {
        dBuilder = dbFactory.newDocumentBuilder()
    }


    fun parseOutput(xml: String) : String {
        val doc = dBuilder.parse(InputSource(StringReader(xml)))
        doc.documentElement.normalize()
        val ret = arrayListOf<String>()

        val nList = doc.getElementsByTagName(ROOT_TAG)

        val filter = (0 until nList.length)
                .asSequence()
                .map { nList.item(it) }
                .map { it as Element }
                .filter { it.getAttribute("tag") == "0020000D" }
                .map { it.getElementsByTagName("Value").item(0).textContent }

        return filter.first()
    }

    fun parseFile(path: String) : String {
        val doc = dBuilder.parse(File("dcm.xml"))
        doc.documentElement.normalize()
        val ret = arrayListOf<String>()

        val nList = doc.getElementsByTagName(ROOT_TAG)
        println(doc.documentElement.nodeName)

        val filter = (0 until nList.length)
                .asSequence()
                .map { nList.item(it) }
                .map { it as Element }
                .filter { it.getAttribute("tag") == "0020000D" }
                .map { it.getElementsByTagName("Value").item(0).textContent }

        return filter.first()
    }
}

