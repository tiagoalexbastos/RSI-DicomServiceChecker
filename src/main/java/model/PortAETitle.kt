package model

data class PortAETitle(var port: Int, var aetitle: String) : Comparable<PortAETitle> {
    override fun compareTo(other: PortAETitle): Int {
        if(this.port > other.port)
            return 1
        else if(this.port < other.port)
            return -1
        return this.aetitle.compareTo(other.aetitle)
    }
}