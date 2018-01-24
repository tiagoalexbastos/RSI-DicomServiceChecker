package checker

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import model.AvailableWServices
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.DicomCheckerUtils
import writer.WSReportWriter
import xml.DicomWSXmlParser
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class WSChecker {
    val logger: Logger = LoggerFactory.getLogger(WSChecker::class.java)
    private var connectionMap = TreeMap<String, AvailableWServices>()
    private lateinit var suid: String

    fun startChecker(ip: String) {
        var finalIp = ip
        if(!(ip.startsWith("http://") || ip.startsWith("https://")))
            finalIp = "http://" + ip
        logPrint("Starting Dicom Web Service Check on URL $finalIp", true)
        qidoTest(finalIp)
        wadoTest(finalIp)
        stowTest(finalIp)

        logPrint("SCAN ENDED SUCESSFULLY ------ WRITING PDF REPORT!", true)
        val pdfWriter = WSReportWriter(connectionMap, finalIp)
        pdfWriter.writeReport()
    }

    private fun stowTest(ip: String) {
        val availableWS = connectionMap[ip]!!
        val ip2check = ip.replace("qido", "stow")

        try {
            val url = "$ip2check/studies/"
            logPrint("URL to try STOW-RS -> $url", true)
            val obj = URL(url)
            val con = obj.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("Content-Type", "multipart/related; type=application/dicom; boundary=hellorsi")
            con.setRequestProperty("Accept", "application/json")
            logPrint("Trying POST method with HEaders: \"Content-Type\", \"multipart/related; type=application/dicom; boundary=hellorsi\"", false)
            con.doOutput = true
            val outStream = DataOutputStream(con.outputStream)
            outStream.writeBytes(DicomCheckerUtils.jsonFile)
            outStream.flush()
            outStream.close()

            val responseCode = con.responseCode
            logPrint("URL response to STOW-RS $responseCode", false)
            if (responseCode == 200) {
                println("STOW-RS Available on $url")
                availableWS.stow = true
                connectionMap.replace(ip, availableWS)
            } else
                connectionMap.replace(ip, availableWS)
        } catch (e: Exception) {
            e.printStackTrace()
            logPrint("ERROR ON $ip", false)
        }

    }

    private fun wadoTest(ip: String) {
        val availableWS = connectionMap[ip]!!
        val ip2check = ip.replace("qido", "wado")
        try {
            val url = "$ip2check/studies/$suid/metadata"
            logPrint("URL to try WADO-RS -> $url", true)
            val response = HttpRequest.get(url).code()
            if (response == 200) {
                println("WADO-RS Available on $url")
                availableWS.wado = true
                connectionMap.replace(ip, availableWS)
            } else
                availableWS.wado = true
            connectionMap.replace(ip, availableWS)
            logPrint("URL response to WADO-RS $response", false)

        } catch (e: Exception) {
            e.printStackTrace()
            logPrint("ERROR ON $ip", false)
        }
    }

    private fun qidoTest(ip: String) {
        val availableWS = AvailableWServices(false, false, false)

        try {
            val url = "$ip/studies"
            logPrint("URL To try QIDO-RS test -> $url", true)
            val obj = URL(url)
            val con = obj.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.readTimeout = 20000


            val responseCode = con.responseCode
            val inputStream = BufferedReader(
                    InputStreamReader(con.inputStream))
            var inputLine: String? = ""
            val response = StringBuffer()

            do {
                inputLine = inputStream.readLine()
                if (inputLine == null)
                    break
                response.append(inputLine)
            } while (true)
            inputStream.close()

            logPrint("$url QIDO-RS Response -> $responseCode", false)
            if (responseCode == 200) {
                println("QIDO-RS Available on $url")
                availableWS.qido = true
                connectionMap.putIfAbsent(ip, availableWS)
            } else
                connectionMap.putIfAbsent(ip, availableWS)

            if (con.contentType.contains("json"))
                parseDicomJson(response)
            else if (con.contentType.contains("xml"))
                parseDicomXml(response)

            logPrint("Study Instance ID received from first QIDO-RS Dicom File -> $suid", true)
        } catch (e: Exception) {
            e.printStackTrace()
            logPrint("ERROR ON $ip", false)
        }
    }

    private fun parseDicomXml(response: StringBuffer) {
        suid = DicomWSXmlParser().parseOutput(response.toString()
                .substringAfter("MIME-Version: 1.0", "")
                .substringBefore("</NativeDicomModel>")
                .plus("</NativeDicomModel>")).trim()
    }

    private fun parseDicomJson(response: StringBuffer) {
        val jparser = JsonParser()
        val jarr = jparser.parse(response.toString()) as JsonArray

        val fstStudy = jarr.first().toString()
        println(fstStudy)
        suid = fstStudy.substringAfter("\"0020000D\":{", "rsi")
                .substringBefore("}", "rsi")
                .substringAfter("\"Value\":[\"", "rsi")
                .substringBefore("\"]", "rsi")
    }

    private fun logPrint(msg: String, both: Boolean) {
        if(both)
            println(msg)
        msg.replace("\n", "")
        logger.info(msg)
    }
}