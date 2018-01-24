package checker

import extensions.getBaseIP
import model.AvailableDCMServices
import model.PortAETitle
import org.dcm4che3.data.Attributes
import org.dcm4che3.data.Tag
import org.dcm4che3.data.VR
import org.dcm4che3.net.QueryOption
import org.nmap4j.Nmap4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.weasis.dicom.op.*
import org.weasis.dicom.param.*
import utils.DicomCheckerUtils.MY_AETITLE
import utils.SerializableUtils.loadPreviousAeTitles
import utils.SerializableUtils.serializeAetitles
import writer.ScanReportWriter
import xml.NmapOutputParser
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class DicomChecker {
    val logger: Logger = LoggerFactory.getLogger(DicomChecker::class.java)
    private var aetitles = arrayListOf<String>()
    private var connectionMap = TreeMap<PortAETitle, AvailableDCMServices>()

    fun startChecker(ip: String, aetitle: String, portRange: String) {
        aetitles = loadPreviousAeTitles().toMutableList() as ArrayList<String>

        if (ip.contains("-")) {
            val firstIP = ip.split("-").first().trim()
            val lastIP = ip.split("-").last().trim()
            val firstSubnet = firstIP.split(".").last().toInt()
            val lastSubnet = lastIP.split(".").last().toInt()
            val firstBaseIP = firstIP.getBaseIP()
            val lastBaseIP = lastIP.getBaseIP()

            if (firstBaseIP != lastBaseIP) {
                logPrint("IP's from different networks. Please try again.", true)
                System.exit(0)
            }

            var counter = firstSubnet
            while (counter != lastSubnet + 1) {
                val ports = checkPorts("$firstBaseIP.$counter", portRange)
                logPrint("NMap Scan complete: Found ${ports.size} TCP ports available. -> $ports", true)
                scan(ports, aetitle, "$firstBaseIP.$counter")
                counter++
                endScan(aetitle, "$firstBaseIP.$counter")
                connectionMap = TreeMap()
            }
        } else {
            val ports = checkPorts(ip, portRange)
            logPrint("NMap Scan complete: Found ${ports.size} TCP ports available. -> $ports", true)
            scan(ports, aetitle, ip)
            endScan(aetitle, ip)
        }
    }

    private fun endScan(aetitle: String, ip: String) {
        var aetitlesArg = arrayListOf<String>()
        if (aetitle.isBlank() || aetitle.isEmpty())
            aetitlesArg = loadPreviousAeTitles().toMutableList() as ArrayList<String>
        else
            aetitlesArg.add(aetitle)
        serializeAetitles(aetitles)
        logPrint("SCAN ENDED SUCESSFULLY ------ WRITING PDF REPORT", true)
        val writer = ScanReportWriter(connectionMap, ip, aetitlesArg)
        writer.writeReport()
    }

    private fun scan(ports: ArrayList<String>, aetitle: String, ip: String) {
        ports.forEach { port ->
            if ((aetitle.isEmpty() || aetitle.isBlank()) && !aetitles.isEmpty()) {
                aetitles.forEach { scanCEcho(port, it, ip) }
                aetitles.forEach { scanCStore(port, it, ip) }
                aetitles.forEach { scanCFind(port, it, ip) }
                aetitles.forEach { scanCGet(port, it, ip) }
                aetitles.forEach { scanCMove(port, it, ip) }
            } else {
                scanCEcho(port, aetitle, ip)
                scanCStore(port, aetitle, ip)
                scanCFind(port, aetitle, ip)
                scanCGet(port, aetitle, ip)
                scanCMove(port, aetitle, ip)
            }
        }
    }

    private fun checkPorts(ip: String, portRange: String): ArrayList<String> {
        logPrint("IP to Scan ---> $ip", true)
        logPrint("Starting NMAP TCP Scan", true)
        var ports = arrayListOf<String>()

        if (ip !in arrayListOf("www.dicomserver.co.uk", "dicomserver.co.uk")) {
            val nMap = Nmap4j("/usr")
            if (!portRange.isEmpty())
                nMap.addFlags("-p $portRange")
            else
                nMap.addFlags("-p ${"*"}")
            nMap.includeHosts(ip)
            nMap.execute()

            val parser = NmapOutputParser()
            try {
                if (!nMap.hasError())
                    ports = parser.parseResults(nMap.output)
                else
                    print(nMap.executionResults.errors)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else
            ports = arrayListOf("104", "11112")
        return ports
    }

    private fun scanCMove(port: String, aetitle: String, ip: String) {
        var aet = aetitle
        if (aet.isEmpty() || aet.isBlank())
            aet = "-"
        val availableServ = connectionMap[PortAETitle(port.toInt(), aet)]!!

        logPrint("\nScanning Port $port for C-MOVE reply with AETitle $aetitle", true)

        val progress = DicomProgress()
        progress.addProgressListener { }

        val params = arrayOf(DicomParam(Tag.StudyInstanceUID, "1.3.46.670589.16.5.100.20091127134147.64690"))
        val calling = DicomNode("STORESCP")
        val called = DicomNode(aet, ip, port.toInt())
        val options = AdvancedParams()
        val connectOptions = ConnectOptions()
        connectOptions.connectTimeout = 3000
        connectOptions.acceptTimeout = 5000
        options.connectOptions = connectOptions
        options.queryOptions.add(QueryOption.RELATIONAL)
        val state = CMove.process(options, calling, called, "STORESCP", progress, *params)


        println("C-MOVE status ${state.status} ---- msg ${state.message}" )
        logPrint("C-MOVE DICOM Status: ${state.status}", false)
        logPrint("REPLY State ${state.message} \n", false)


        if (state.status == 0) {
            println("$port C-Move service Available!")
            if (aet != "-" && aet !in aetitles)
                aetitles.add(aet)
            availableServ.move = Pair(true, "")
            connectionMap.replace(PortAETitle(port.toInt(), aet), availableServ)
        } else {
            println("$port C-Move service Unavailable!")
            availableServ.move = Pair(false, state.message)
            connectionMap.replace(PortAETitle(port.toInt(), aet), availableServ)
        }

    }

    private fun scanCGet(port: String, aetitle: String, ip: String) {
        var aet = aetitle
        if (aet.isEmpty() || aet.isBlank())
            aet = "-"
        val availableServ = connectionMap[PortAETitle(port.toInt(), aet)]!!

        logPrint("\nScanning Port $port for C-GET reply with AETitle $aetitle", true)

        val progress = DicomProgress()
        progress.addProgressListener { }

        val params = arrayOf<DicomParam>()
        val calling = DicomNode(MY_AETITLE)
        val called = DicomNode(aet, ip, port.toInt())
        val options = AdvancedParams()
        val connectOptions = ConnectOptions()
        connectOptions.connectTimeout = 3000
        connectOptions.acceptTimeout = 5000
        options.connectOptions = connectOptions
        val state = CGet.process(options, calling, called, progress, File("testdir"), *params)

        println("C-GET status ${state.status} ---- msg ${state.message}")
        logPrint("C-GET DICOM Status: ${state.status}", false)
        logPrint("REPLY State ${state.message} \n", false)

        if (state.status == 0) {
            println("$port C-Get service Available!")
            if (aet != "-" && aet !in aetitles)
                aetitles.add(aet)
            availableServ.get = Pair(true, "")
            connectionMap.replace(PortAETitle(port.toInt(), aet), availableServ)
        } else {
            println("$port C-Get service Unavailable")
            availableServ.get = Pair(false, state.message)
            connectionMap.replace(PortAETitle(port.toInt(), aet), availableServ)
        }

    }

    private fun scanCFind(port: String, aetitle: String, ip: String) {
        var aet = aetitle
        if (aet.isEmpty() || aet.isBlank())
            aet = "-"
        val availableServ = connectionMap[PortAETitle(port.toInt(), aet)]!!

        logPrint("\nScanning Port $port for C-FIND reply with AETitle $aetitle", true)

        val params = arrayOf<DicomParam>()
        val calling = DicomNode(MY_AETITLE)
        val called = DicomNode(aet, ip, port.toInt())
        val options = AdvancedParams()
        val connectOptions = ConnectOptions()
        connectOptions.connectTimeout = 3000
        connectOptions.acceptTimeout = 5000
        options.connectOptions = connectOptions
        val state = CFind.process(options, calling, called, *params)

        println("C-FIND status ${state.status} ---- msg ${state.message}")
        logPrint("C-FIND DICOM Status: ${state.status}", false)
        logPrint("REPLY State ${state.message} \n", false)

        if (state.status == 0) {
            println("$port C-Find service Available")
            if (aet != "-" && aet !in aetitles)
                aetitles.add(aet)
            availableServ.find = Pair(true, "")
            connectionMap.replace(PortAETitle(port.toInt(), aet), availableServ)
        } else {
            println("$port C-Find service Unavailable")
            availableServ.find = Pair(false, state.message)
            connectionMap.replace(PortAETitle(port.toInt(), aet), availableServ)
        }
    }

    private fun scanCStore(port: String, aetitle: String, ip: String) {
        var aet = aetitle
        if (aet.isEmpty() || aet.isBlank())
            aet = "-"
        val availableServ = connectionMap[PortAETitle(port.toInt(), aet)]!!

        logPrint("\nScanning Port $port for C-STORE reply with AETitle $aetitle", true)

        val params = AdvancedParams()
        val connectOptions = ConnectOptions()
        connectOptions.connectTimeout = 3000
        connectOptions.acceptTimeout = 5000
        params.connectOptions = connectOptions

        val progress = DicomProgress()
        progress.addProgressListener { }

        val calling = DicomNode(MY_AETITLE)
        val called = DicomNode(aet, ip, port.toInt())
        val files = java.util.ArrayList<String>()
        try {
            files.add(File("mr.dcm").toURI().path)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val attrs = Attributes()
        attrs.setString(Tag.PatientName, VR.PN, "Override^Patient^Name")
        attrs.setString(Tag.PatientID, VR.LO, "TiagoPatientID")
        val editor = DefaultAttributeEditor(true, attrs)
        val cstoreParams = CstoreParams(editor, false, null)
        val state = CStore.process(params, calling, called, files, progress, cstoreParams)

        println("C-STORE status ${state.status} ---- msg ${state.message}")
        logPrint("C-STORE DICOM Status: ${state.status}", false)
        logPrint("REPLY State ${state.message} \n\n\n", false)

        if (state.status == 0) {
            println("$port C-Store service Available")
            if (aet != "-" && aet !in aetitles)
                aetitles.add(aet)
            availableServ.store = Pair(true, "")
            connectionMap.replace(PortAETitle(port.toInt(), aet), availableServ)
        } else {
            println("$port C-Store service Unavailable")
            availableServ.store = Pair(false, state.message)
            connectionMap.replace(PortAETitle(port.toInt(), aet), availableServ)
        }
    }

    private fun scanCEcho(port: String, aetitle: String, ip: String) {
        var aet = aetitle
        val availableServ = AvailableDCMServices(Pair(true, ""), Pair(true, ""), Pair(true, ""), Pair(true, ""), Pair(true, ""))
        if (aet.isEmpty() || aet.isBlank())
            aet = "-"

        logPrint("\nScanning Port $port for C-Echo reply with AETitle $aetitle", true)

        val callingNode = DicomNode(MY_AETITLE)
        val calledNode = DicomNode(aet, ip, port.toInt())
        val params = AdvancedParams()
        val connectOptions = ConnectOptions()
        connectOptions.connectTimeout = 3000
        connectOptions.acceptTimeout = 5000
        params.connectOptions = connectOptions
        val state = Echo.process(params, callingNode, calledNode)

        println("C-ECHO status ${state.status} ---- msg ${state.message}")
        logPrint("C-ECHO DICOM Status: ${state.status}", false)
        logPrint("REPLY State ${state.message} \n", false)

        if (state.status == 0) {
            println("$port C-Echo service Available")
            if (aet != "-" && aet !in aetitles)
                aetitles.add(aet)
            availableServ.echo = Pair(true, "")
            connectionMap.putIfAbsent(PortAETitle(port.toInt(), aet), availableServ)
        } else {
            println("$port C-Echo service Unavailable")
            availableServ.echo = Pair(false, state.message)
            connectionMap.putIfAbsent(PortAETitle(port.toInt(), aet), availableServ)
        }
    }

    private fun logPrint(msg: String, both: Boolean) {
        if (both)
            println(msg)
        msg.replace("\n", "")
        logger.info(msg)
    }
}