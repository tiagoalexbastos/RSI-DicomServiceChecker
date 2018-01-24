package utils

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


object SerializableUtils {
    fun loadPreviousAeTitles(): Array<String> {
        try {
            val inStream = ObjectInputStream(FileInputStream("aetitles.ser"))
            val array = inStream.readObject() as Array<String>
            inStream.close()
            return array
        } catch(e: Exception) {

        }
        return arrayOf()
    }

    fun serializeAetitles(aet: kotlin.collections.ArrayList<String>) {
        val array = aet.toTypedArray<String?>()
        val outputStream = ObjectOutputStream(FileOutputStream("aetitles.ser"))
        outputStream.writeObject(array)
        outputStream.close()
    }
}