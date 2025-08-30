package com.cyruspyre.lemon

import android.os.storage.StorageVolume
import com.cyruspyre.lemon.entity.FileAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.EnumMap
import java.util.concurrent.TimeUnit
import kotlin.io.path.name
import kotlin.io.path.readAttributes

val WATCH_DOG = WatchDog()
lateinit var VOLUMES: List<StorageVolume>

class WatchDog {
    private typealias OrderMap = EnumMap<FileInfo.Order, BlockList<FileInfo>?>

    private val cache = HashMap<Path, Pair<OrderMap, MutableList<FileAdapter>>>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val uiScope = MainScope()

    fun load(
        path: Path,
        order: FileInfo.Order,
        adapter: FileAdapter,
        onAdded: CoroutineScope.() -> Unit,
        onLoading: CoroutineScope.() -> Unit,
        onLoaded: CoroutineScope.(Boolean) -> Unit,
    ): BlockList<FileInfo>? {
        var entry = cache[path]

        return if (entry == null) {
            uiScope.launch { onLoading() }

            val stream = try {
                Files.newDirectoryStream(path)
            } catch (_: AccessDeniedException) {
                uiScope.launch { onLoaded(true) }
                return null
            }
            val list = BlockList(order.cmp)

            entry = Pair(OrderMap(FileInfo.Order::class.java), mutableListOf(adapter))

            entry.first.put(order, list)
            cache.put(path, entry)
            scope.launch {
                for (path in stream) {
                    val attr = path.readAttributes<BasicFileAttributes>()

                    list.add(
                        FileInfo(
                            path.name,
                            attr.size(),
                            attr.lastModifiedTime().to(TimeUnit.SECONDS),
                            attr.isDirectory,
                            attr.isSymbolicLink
                        )
                    )
                    uiScope.launch { onAdded() }
                }

                uiScope.launch { onLoaded(false) }
            }

            list
        } else {
            var list = entry.first[order]
            val adapters = entry.second

            if (!adapters.contains(adapter)) adapters.add(adapter)
            if (list == null) {
                val prev = entry.first.values.first()!!
                list = BlockList(order.cmp)

                for (i in 0..<prev.size) list.add(prev[i])

                entry.first.put(order, list)
            }

            uiScope.launch { onLoaded(true) }
            list
        }
    }
}

// communication with daemon will be in the following field order
data class FileInfo(
    val name: String,
    val size: Long,
    val time: Long,
    val isDir: Boolean,
    val isSymlink: Boolean,
) {
    enum class Order(val cmp: java.util.Comparator<FileInfo>) {
        Name(Comparator { a, b -> a.name.compareTo(b.name) }),
        Size(Comparator { a, b ->
            val one = a.isDir.compareTo(b.isDir)
            if (one != 0) return@Comparator one

            val two = b.size.compareTo(a.size)
            if (two == 0) a.name.compareTo(b.name) else two
        }),
        Type(Comparator { a, b ->
            val tmp = b.isDir.compareTo(a.isDir)
            if (tmp == 0) a.name.compareTo(b.name) else tmp
        }),
    }
}