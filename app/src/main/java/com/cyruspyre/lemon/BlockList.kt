package com.cyruspyre.lemon

class BlockList<T>(private val cmp: Comparator<T>) {
    class Node<T>(val data: MutableList<T>, var prev: Node<T>?, var next: Node<T>?)
    data class Cache<T>(var offset: Int, var node: Node<T>)

    var root = Node(mutableListOf<T>(), null, null)
    var cur = root
    var cache: Cache<T>? = null
    var size = 0

    fun add(data: T) {
        if (root.data.isEmpty()) {
            root.data.add(data)
            size++
            return
        }

        var node = cur

        while (cmp.compare(node.data[0], data) == 1) node = node.prev ?: break
        if (node == cur) while (cmp.compare(node.data.last(), data) == -1) node = node.next ?: break

        var idx = node.data.binarySearch(data, cmp).either()
        val list = if (node.data.size != 512) node.data else {
            val node = if (idx == 0) {
                val tmp = Node(mutableListOf(), node.prev, node)

                if (node == root) root = tmp

                node.prev?.next = tmp
                node.prev = tmp
                idx = 0

                tmp
            } else {
                var flag = true
                val list = if (idx == node.data.size) {
                    idx = 0
                    mutableListOf()
                } else {
                    val mid = node.data.size / 2
                    val tmp = node.data.subList(mid, node.data.size).toMutableList()

                    node.data.subList(mid, node.data.size).clear()
                    flag = mid <= idx

                    if (flag) idx -= node.data.size

                    tmp
                }

                val tmp = Node(list, node, node.next)

                node.next?.prev = tmp
                node.next = tmp

                if (flag) tmp else node
            }

            cur = node
            node.data
        }

        size++
        list.add(idx, data)
    }

    operator fun get(idx: Int): T {
        val cache = cacheAtIdx(idx)

        return cache.node.data[idx - cache.offset]
    }

    fun remove(idx: Int) {
        if (idx < 0 || idx >= size) throw IndexOutOfBoundsException()

        val cache = cacheAtIdx(idx)
        val node = cache.node
        val localIdx = idx - cache.offset

        // Remove the element from the node's data
        node.data.removeAt(localIdx)
        size--

        // If the node becomes empty after removal
        if (node.data.isEmpty()) {
            // Update root if we're removing the root node
            if (node == root) {
                if (node.next != null) root = node.next!!
                cur = root
            } else {
                // Connect previous and next nodes
                node.prev?.next = node.next
                node.next?.prev = node.prev

                // Update cur if needed
                if (node == cur) {
                    cur = node.prev ?: root
                }
            }

            // Clear cache if it points to the removed node
            if (cache.node == node) {
                cache.node = node.next ?: root
                cache.offset = if (node.next == null) 0 else cache.offset
            }
        }
    }

    private fun cacheAtIdx(idx: Int): Cache<T> {
        if (cache == null) cache = Cache(0, root)

        val cache = cache!!
        val forward = cache.offset <= idx

        if (forward) while (true) {
            val node = cache.node
            val tmp = node.data.size

            cache.offset += tmp

            if (cache.offset > idx) {
                cache.offset -= tmp
                break
            }

            cache.node = node.next ?: break
        } else while (true) {
            val node = cache.node.prev

            if (cache.offset <= idx || node == null) break

            cache.node = node
            cache.offset -= node.data.size
        }

        return cache
    }

    fun isSorted(): Boolean {
        var cur = root
        var prev: T? = null

        while (true) {
            for (v in cur.data) {
                if (prev != null && cmp.compare(prev, v) == 1) return false
                prev = v
            }
            cur = cur.next ?: break
        }

        return true
    }

    fun debug(): String {
        val buf = mutableListOf<String>()
        var cur = root

        while (true) {
            buf.add(cur.data.toString())
            cur = cur.next ?: break
        }

        return buf.joinToString("->")
    }

    override fun toString(): String {
        val tmp = mutableListOf<T>()
        var cur = root

        while (true) {
            tmp.addAll(cur.data)
            cur = cur.next ?: break
        }

        return tmp.toString()
    }
}