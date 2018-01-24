package executor

import checker.DicomChecker
import checker.WSChecker
import java.text.SimpleDateFormat
import java.util.*


fun main(args: Array<String>) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd hhmmss")
    System.setProperty("current.date", dateFormat.format(Date()))


    println("---------- DICOM Service Checker | Powered by Nmap ----------\n")
    println("1 - Regular Scan (C-Echo | C-Store | C-Find | C-Get | C-Move)")
    println("2 - Scan DICOM Web Services")
    print("-> ")
    val option: String = readLine()!!

    when(option) {
        "1" -> fullScan()
        "2" -> scanWs()
    }
}

fun scanWs() {
    print("Base Url/IP: ")
    val baseUrl: String = readLine()!!

    WSChecker().startChecker(baseUrl)
}

fun fullScan() {
    print("URL/IP range to scan [Start IP]-[End IP] : ")
    val ip: String = readLine()!!

    print("Port Range [Start Port]-[End Port]: ")
    val portRange: String = readLine()!!

    print("AETitle (press enter if you dont know one): ")
    val aetitle: String = readLine()!!

    DicomChecker().startChecker(ip, aetitle, portRange)
}
