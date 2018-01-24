package utils

import com.itextpdf.text.BaseColor.GREEN
import com.itextpdf.text.BaseColor.RED
import com.itextpdf.text.Font
import com.itextpdf.text.Font.BOLD
import com.itextpdf.text.Font.FontFamily.TIMES_ROMAN
import com.itextpdf.text.Font.NORMAL
import com.itextpdf.text.Phrase

object ServiceEvaluator {

    private val greenFont = Font(TIMES_ROMAN, 12f, BOLD, GREEN)
    private val redFont = Font(TIMES_ROMAN, 12f, NORMAL, RED)

    fun getAvailability(available: Boolean) : Phrase {
        return if(available)
            Phrase("Available", greenFont)
        else
            Phrase("Unavailable", redFont)
    }

}