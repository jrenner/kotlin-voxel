package org.jrenner.learngl

object Direction {
    val Up = 0x01
    val Down = 0x02
    val North = 0x04
    val South = 0x08
    val East = 0x10
    val West = 0x20

    val all = intArray(Up, Down, North, South, East, West)
    val allSize = 6
    val ALL_FACES: Int = 0xFF

    fun toString(n: Int): String {
        return when (n) {
            Up -> "Up"
            Down -> "Down"
            North -> "North"
            South ->  "South"
            East -> "East"
            West -> "West"
            else -> "Non-direction integer: $n"
        }
    }
}
