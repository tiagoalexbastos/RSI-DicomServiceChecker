package writer

import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import extensions.toPTTimestamp
import model.AvailableWServices
import utils.ServiceEvaluator
import java.io.File
import java.io.FileOutputStream
import java.util.*

class WSReportWriter(private var connectionMap: TreeMap<String, AvailableWServices>, private var url: String) {
    private val titleFont = Font(Font.FontFamily.TIMES_ROMAN, 20f, Font.BOLD)
    private val headerFont = Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.BOLD)
    private val subHeader = Font(Font.FontFamily.TIMES_ROMAN, 16f, Font.NORMAL)
    private val textFont = Font(Font.FontFamily.TIMES_ROMAN, 14f, Font.NORMAL)
    private val fileName = "WSScan${Date().toPTTimestamp().replace(":", "_")}.pdf"

    fun writeReport() {
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(File(fileName)))
        document.open()
        addTitlePage(document)
        document.close()
    }

    private fun addTitlePage(document: Document) {
        val preface = Paragraph()
        addEmptyLine(preface, 1)
        preface.add(Paragraph("Dicom Web Services Scan - RSI 2017/2018", titleFont))
        addEmptyLine(preface, 2)
        preface.add(Paragraph("-> URL to Scan: ${trimUrl(url)}", textFont))
        addEmptyLine(preface, 2)

        val table = PdfPTable(4)
        table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER
        table.defaultCell.verticalAlignment = Element.ALIGN_CENTER
        table.widthPercentage = 100f
        var c1 = PdfPCell(Phrase("URL", headerFont))
        c1.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("QIDO-RS", headerFont))
        c1.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("WADO-RS", headerFont))
        c1.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(c1)
        c1 = PdfPCell(Phrase("STOW-RS", headerFont))
        c1.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(c1)
        table.setHeaderRows(1)


        connectionMap.forEach { url, result ->
            table.addCell(Phrase(trimUrl(url), subHeader))
            table.addCell(ServiceEvaluator.getAvailability(result.qido))
            table.addCell(ServiceEvaluator.getAvailability(result.wado))
            table.addCell(ServiceEvaluator.getAvailability(result.stow))
        }


        document.add(preface)
        document.add(table)
    }

    private fun addEmptyLine(paragraph: Paragraph, number: Int) {
        for (i in 0 until number) {
            paragraph.add(Paragraph(" "))
        }
    }

    private fun trimUrl(url: String) : String {
        if(url.contains("qido") || url.contains("wado") || url.contains("stow")) {
            return url.substringBeforeLast("/")
        }
        return url
    }
}