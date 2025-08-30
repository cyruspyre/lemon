package com.cyruspyre.lemon

import android.util.DisplayMetrics
import android.util.TypedValue
import kotlin.math.round
import kotlin.math.roundToInt

lateinit var DISPLAY_METRICS: DisplayMetrics

inline val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        DISPLAY_METRICS,
    )
inline val Int.dp: Int get() = this.toFloat().dp.toInt()
val Float.prettyByte: String
    get() {
        val lol = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
        var tmp = this
        var idx = 0

        while (tmp > 1024) {
            idx++
            tmp /= 1024
        }

        tmp = round(tmp * 10) / 10

        return "${if (tmp % 1 == 0f) tmp.roundToInt() else tmp} ${lol[idx]}"
    }

sealed class Result<T, E> {
    data class Ok<T, E>(val data: T) : Result<T, E>()
    data class Err<T, E>(val data: E) : Result<T, E>()
}

fun <T> Result<T, T>.either(): T {
    return when (this) {
        is Result.Ok -> data
        is Result.Err -> data
    }
}

fun <T : Comparable<T>> List<T>.binarySearch(e: T): Result<Int, Int> {
    var start = 0
    var end = size - 1

    while (start <= end) {
        val mid = (start + end) / 2
        val tmp = this[mid]

        when {
            tmp > e -> end = mid - 1
            tmp < e -> start = mid + 1
            else -> return Result.Ok(mid)
        }
    }

    return Result.Err(start)
}

fun <T> List<T>.binarySearch(e: T, cmp: Comparator<T>): Result<Int, Int> {
    var start = 0
    var end = size - 1

    while (start <= end) {
        val mid = (start + end) / 2
        val tmp = cmp.compare(this[mid], e)

        when {
            tmp > 0 -> end = mid - 1
            tmp < 0 -> start = mid + 1
            else -> return Result.Ok(mid)
        }
    }

    return Result.Err(start)
}