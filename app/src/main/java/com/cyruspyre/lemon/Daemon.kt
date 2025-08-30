package com.cyruspyre.lemon

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.nio.file.Path

class Daemon(pwd: File) {
    val lock = Mutex(false)
    val scope = CoroutineScope(Dispatchers.IO)
    val process = ProcessBuilder("su", "-c", "./librust.so").directory(pwd).start()!!
    val stdin = process.outputStream!!
    val stdout = process.inputStream.buffered()

    suspend fun load(path: Path, cb: (FileInfo) -> Unit) {
        lock.lock()
        stdin.write(0)
        stdin.write(path.toString().toByteArray())
        stdin.write(0)
        stdin.flush()

        while (true) {
            val name = readUntilNull() ?: break
            val size = readUntilNull()!!.toLong()
            val time = readUntilNull()!!.toLong()
            val flags = readNBytes(4)

            cb(FileInfo(name, size, time, flags[0] == 1.toByte(), flags[2] == 1.toByte()))
        }

        lock.unlock()
    }

    private fun readUntilNull(): String? {
        val buf = StringBuilder()
        var tmp = stdout.read()

        if (tmp == 0) return null

        while (true) {
            buf.appendCodePoint(tmp)
            tmp = stdout.read()
            if (tmp == 0) break
        }

        return buf.toString()
    }

    private fun readNBytes(len: Int): ByteArray {
        val buf = ByteArray(len)
        var count = 0

        while (count != len) {
            count += stdout.read(buf, count, len - count)
        }

        return buf
    }
}