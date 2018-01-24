package extensions

import java.util.stream.Collectors

inline fun String.getBaseIP() = this.split(".")
        .stream()
        .limit(3)
        .collect(Collectors.toList())