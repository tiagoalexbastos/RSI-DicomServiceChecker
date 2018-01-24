package model

data class AvailableDCMServices(var echo: Pair<Boolean, String>, var store: Pair<Boolean, String>, var get: Pair<Boolean, String>,
                                var find: Pair<Boolean, String>, var move: Pair<Boolean, String>)