package writer

import com.itextpdf.text.Document
import com.itextpdf.text.Element.ALIGN_CENTER
import com.itextpdf.text.Font
import com.itextpdf.text.Font.BOLD
import com.itextpdf.text.Font.FontFamily.TIMES_ROMAN
import com.itextpdf.text.Font.NORMAL
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import extensions.toPTTimestamp
import model.AvailableDCMServices
import model.PortAETitle
import utils.ServiceEvaluator.getAvailability
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList


class ScanReportWriter(private var connectionMap: TreeMap<PortAETitle, AvailableDCMServices>, private var ip: String,
                       private var aetitlesArg: ArrayList<String>) {
    private val titleFont = Font(TIMES_ROMAN, 20f, BOLD)
    private val headerFont = Font(TIMES_ROMAN, 16f, BOLD)
    private val subHeader = Font(TIMES_ROMAN, 16f, NORMAL)
    private val subsubHeader = Font(TIMES_ROMAN, 12f, NORMAL)
    private val textFont = Font(TIMES_ROMAN, 14f, NORMAL)
    private val fileName = "DCMScan_${ip}_${Date().toPTTimestamp().replace(":", "_")}.pdf"

    fun writeReport() {
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(File(fileName)))
        document.open()
        addTitlePage(document)
        addInformationPage(document)
        document.close()
    }

    private fun addInformationPage(document: Document) {
        val echos = connectionMap.filter { !it.value.echo.first }
        val finds = connectionMap.filter { !it.value.find.first }
        val gets = connectionMap.filter { !it.value.get.first }
        val stores = connectionMap.filter { !it.value.store.first }
        val moves = connectionMap.filter { !it.value.move.first }

        val preface = Paragraph()
        addEmptyLine(preface, 1)
        preface.add(Paragraph("Connection Failure Details", titleFont))
        addEmptyLine(preface, 1)

        val echoTable = PdfPTable(3)
        echoTable.defaultCell.horizontalAlignment = ALIGN_CENTER
        echoTable.defaultCell.verticalAlignment = ALIGN_CENTER
        echoTable.widthPercentage = 100f
        echoTable.setWidths(intArrayOf(1, 1, 3))
        var cecho = PdfPCell(Phrase("Ports", headerFont))
        cecho.horizontalAlignment = ALIGN_CENTER
        echoTable.addCell(cecho)
        cecho = PdfPCell(Phrase("AETitle", headerFont))
        cecho.horizontalAlignment = ALIGN_CENTER
        echoTable.addCell(cecho)
        cecho = PdfPCell(Phrase("C-ECHO Failure Reason", headerFont))
        cecho.horizontalAlignment = ALIGN_CENTER
        echoTable.addCell(cecho)
        echoTable.headerRows = 1
        echos.forEach {
            echoTable.addCell(Phrase(it.key.port.toString(), subHeader))
            echoTable.addCell(Phrase(it.key.aetitle, subHeader))
            echoTable.addCell(Phrase(evaluateState(it.value.echo.second), subsubHeader))
        }

        val storeTable = PdfPTable(3)
        storeTable.defaultCell.horizontalAlignment = ALIGN_CENTER
        storeTable.defaultCell.verticalAlignment = ALIGN_CENTER
        storeTable.widthPercentage = 100f
        storeTable.setWidths(intArrayOf(1, 1, 3))
        var cstore = PdfPCell(Phrase("Ports", headerFont))
        cstore.horizontalAlignment = ALIGN_CENTER
        storeTable.addCell(cstore)
        cstore = PdfPCell(Phrase("AETitle", headerFont))
        cstore.horizontalAlignment = ALIGN_CENTER
        storeTable.addCell(cstore)
        cstore = PdfPCell(Phrase("C-STORE Failure Reason", headerFont))
        cstore.horizontalAlignment = ALIGN_CENTER
        storeTable.addCell(cstore)
        storeTable.headerRows = 1
        stores.forEach {
            storeTable.addCell(Phrase(it.key.port.toString(), subHeader))
            storeTable.addCell(Phrase(it.key.aetitle, subHeader))
            storeTable.addCell(Phrase(evaluateState(it.value.store.second), subsubHeader))
        }

        val findTable = PdfPTable(3)
        findTable.defaultCell.horizontalAlignment = ALIGN_CENTER
        findTable.defaultCell.verticalAlignment = ALIGN_CENTER
        findTable.widthPercentage = 100f
        findTable.setWidths(intArrayOf(1, 1, 3))
        var cfind = PdfPCell(Phrase("Ports", headerFont))
        cfind.horizontalAlignment = ALIGN_CENTER
        findTable.addCell(cfind)
        cfind = PdfPCell(Phrase("AETitle", headerFont))
        cfind.horizontalAlignment = ALIGN_CENTER
        findTable.addCell(cfind)
        cfind = PdfPCell(Phrase("C-FIND Failure Reason", headerFont))
        cfind.horizontalAlignment = ALIGN_CENTER
        findTable.addCell(cfind)
        findTable.headerRows = 1
        finds.forEach {
            findTable.addCell(Phrase(it.key.port.toString(), subHeader))
            findTable.addCell(Phrase(it.key.aetitle, subHeader))
            findTable.addCell(Phrase(evaluateState(it.value.find.second), subsubHeader))
        }

        val getTable = PdfPTable(3)
        getTable.defaultCell.horizontalAlignment = ALIGN_CENTER
        getTable.defaultCell.verticalAlignment = ALIGN_CENTER
        getTable.widthPercentage = 100f
        getTable.setWidths(intArrayOf(1, 1, 3))
        var cget = PdfPCell(Phrase("Ports", headerFont))
        cget.horizontalAlignment = ALIGN_CENTER
        getTable.addCell(cget)
        cget = PdfPCell(Phrase("AETitle", headerFont))
        cget.horizontalAlignment = ALIGN_CENTER
        getTable.addCell(cget)
        cget = PdfPCell(Phrase("C-GET Failure Reason", headerFont))
        cget.horizontalAlignment = ALIGN_CENTER
        getTable.addCell(cget)
        getTable.headerRows = 1
        gets.forEach {
            getTable.addCell(Phrase(it.key.port.toString(), subHeader))
            getTable.addCell(Phrase(it.key.aetitle, subHeader))
            getTable.addCell(Phrase(evaluateState(it.value.get.second), subsubHeader))
        }

        val moveTable = PdfPTable(3)
        moveTable.defaultCell.horizontalAlignment = ALIGN_CENTER
        moveTable.defaultCell.verticalAlignment = ALIGN_CENTER
        moveTable.widthPercentage = 100f
        moveTable.setWidths(intArrayOf(1, 1, 3))
        var cmove = PdfPCell(Phrase("Ports", headerFont))
        cmove.horizontalAlignment = ALIGN_CENTER
        moveTable.addCell(cmove)
        cmove = PdfPCell(Phrase("AETitle", headerFont))
        cmove.horizontalAlignment = ALIGN_CENTER
        moveTable.addCell(cmove)
        cmove = PdfPCell(Phrase("C-MOVE Failure Reason", headerFont))
        cmove.horizontalAlignment = ALIGN_CENTER
        moveTable.addCell(cmove)
        moveTable.headerRows = 1
        moves.forEach {
            moveTable.addCell(Phrase(it.key.port.toString(), subHeader))
            moveTable.addCell(Phrase(it.key.aetitle, subHeader))
            moveTable.addCell(Phrase(evaluateState(it.value.move.second), subsubHeader))
        }


        document.add(preface)

        document.add(echoTable)

        val space1 = Paragraph()
        addEmptyLine(space1, 2)
        document.add(space1)

        document.add(storeTable)

        val space2 = Paragraph()
        addEmptyLine(space2, 2)
        document.add(space2)

        document.add(findTable)

        val space3 = Paragraph()
        addEmptyLine(space3, 2)
        document.add(space3)

        document.add(getTable)

        val space4 = Paragraph()
        addEmptyLine(space4, 2)
        document.add(space4)

        document.add(moveTable)


    }

    private fun addTitlePage(document: Document) {
        val preface = Paragraph()
        addEmptyLine(preface, 1)
        preface.add(Paragraph("DICOM Service Checker - RSI 2017/2018 - Full Service Scan", titleFont))
        addEmptyLine(preface, 1)
        preface.add(Paragraph("Scan Results: ", textFont))
        preface.add(Paragraph("-> IP to Scan: $ip", textFont))


        var aetitles = ""
        aetitlesArg.forEach { aetitles += "$it, " }
        aetitles = aetitles.substring(0, aetitles.length-2)
        preface.add(Paragraph("-> Scanned AETitles: $aetitles", textFont))
        addEmptyLine(preface, 1)


        val singlePorts = connectionMap.keys.map { it.port }.toSet()
        preface.add(Paragraph("Found ${singlePorts.size} relevant ports:", textFont))
        var toPrint = ""
        singlePorts.forEach { toPrint += "$it\n" }
        preface.add(Paragraph(toPrint, textFont))


        addEmptyLine(preface, 2)
        preface.add(Paragraph("", textFont))

        val table = PdfPTable(7)
        table.defaultCell.horizontalAlignment = ALIGN_CENTER
        table.defaultCell.verticalAlignment = ALIGN_CENTER
        table.widthPercentage = 100f
        var c1 = PdfPCell(Phrase("Ports", headerFont))
        c1.horizontalAlignment = ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("AETitle", headerFont))
        c1.horizontalAlignment = ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("C-Echo", headerFont))
        c1.horizontalAlignment = ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("C-Store", headerFont))
        c1.horizontalAlignment = ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("C-Find", headerFont))
        c1.horizontalAlignment = ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("C-Get", headerFont))
        c1.horizontalAlignment = ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("C-Move", headerFont))
        c1.horizontalAlignment = ALIGN_CENTER
        table.addCell(c1)
        table.headerRows = 1


        connectionMap.forEach { port, result ->
            table.addCell(Phrase(port.port.toString(), subHeader))
            table.addCell(Phrase(port.aetitle, subsubHeader))
            table.addCell(getAvailability(result.echo.first))
            table.addCell(getAvailability(result.store.first))
            table.addCell(getAvailability(result.find.first))
            table.addCell(getAvailability(result.get.first))
            table.addCell(getAvailability(result.move.first))
        }


        document.add(preface)
        document.add(table)

        document.newPage()
    }


    private fun addEmptyLine(paragraph: Paragraph, number: Int) {
        for (i in 0 until number) {
            paragraph.add(Paragraph(" "))
        }
    }

    private fun evaluateState(state: String) : String {
        if(state.isEmpty() || state.isNullOrBlank() || state.trim() == "null") {
            return "No information Found \n"
        }
        return "$state \n"
    }
}