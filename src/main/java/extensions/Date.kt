package extensions

import java.text.SimpleDateFormat
import java.util.*

inline fun Date.toPTTimestamp(): String = SimpleDateFormat("HH:mm:ss",
        Locale("pt", "pt")).format(this)

