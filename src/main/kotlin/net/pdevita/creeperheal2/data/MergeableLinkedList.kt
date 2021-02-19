package net.pdevita.creeperheal2.data

import java.lang.Exception
import java.lang.IndexOutOfBoundsException

class MergeableLinkedList<T>: MutableList<T> {
    internal var head: MergeableLinkedListNode<T>? = null
    internal var tail: MergeableLinkedListNode<T>? = null
    override var size = 0

    constructor() {

    }

    constructor(size: Int) {
    }

    constructor(elements: Collection<T>) {
        this.addAll(elements)
    }

    override fun add(element: T): Boolean {
        val newNode = MergeableLinkedListNode(element)
        // Tail isn't null, list is inited
        if (tail != null) {
            tail!!.next = newNode
            newNode.previous = tail
            tail = newNode
        // Tail is null, list is not inited
        } else {
            head = newNode
            tail = newNode
        }
        size += 1
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        for (elem in elements) {
            this.add(elem)
        }
        return true
    }

    fun peek(): T {
        if (head == null) {
            throw IndexOutOfBoundsException()
        }
        return head!!.data
    }

    fun poll(): T {
        if (head == null) {
            throw IndexOutOfBoundsException()
        }
        val data = head!!.data
        head = head!!.next
        head?.previous = null
        if (head == null) {
            tail = null
        }
        size -= 1
        return data
    }

//    fun pollLast(): T {
//        if (tail == null) {
//            throw IndexOutOfBoundsException()
//        }
//        val data = tail!!.data
//
//    }

    override fun contains(element: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
//        println("head $head, $size thoughts ($tail)")
        return head == null
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun iterator(): MutableIterator<T> {
        return MergeableLinkedListIterator<T>(this)
    }

    override fun remove(element: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    fun append(other: MergeableLinkedList<T>) {
        // If this list isn't null
        if (tail != null) {
            // and other list isn't null
            if (other.head != null) {
                // Append other onto our tail
                this.tail!!.next = other.head
                other.head!!.previous = this.tail
                this.tail = other.tail
            // and the other list is null
            } else {
                // Do nothing
            }
        // If this list is null
        } else {
            // and other list isn't null
            if (other.head != null) {
                // Inherit it
                this.head = other.head
                this.tail = other.tail
                // and the other list is null
            } else {
                // Do nothing
            }
        }
        size += other.size
    }

    override fun get(index: Int): T {
        TODO("Not yet implemented")
    }

    override fun indexOf(element: T): Int {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: T): Int {
        TODO("Not yet implemented")
    }

    override fun add(index: Int, element: T) {
        TODO("Not yet implemented")
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun listIterator(): MutableListIterator<T> {
        return MergeableLinkedListIterator(this)
    }

    override fun listIterator(index: Int): MutableListIterator<T> {
        TODO("Not yet implemented")
    }

    override fun removeAt(index: Int): T {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, element: T): T {
        TODO("Not yet implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        TODO("Not yet implemented")
    }

    fun duplicateCheck() {
        println("Running duplicate check, tell my children I love them")
        val set = HashSet<T>()
        val itr = listIterator()
        var counter = 0
        while (itr.hasNext()) {
            val element = itr.next()
            if (element in set) {
                counter += 1
            } else {
                set.add(element)
            }
            if (counter > 100000) {
                println("Probably an infinite loop")
                break
            }
        }
        println("$counter duplicates out of $size")
    }

}

class MergeableLinkedListNode<T>(var data: T) {
    var next: MergeableLinkedListNode<T>? = null
    var previous: MergeableLinkedListNode<T>? = null
}

class MergeableLinkedListIterator<T>(private val list: MergeableLinkedList<T>): MutableListIterator<T> {
    private var nextNode: MergeableLinkedListNode<T>? = list.head
    private var previousNode: MergeableLinkedListNode<T>? = null
    private var lastReturned: MergeableLinkedListNode<T>? = null
    private var index = 0

    override fun hasNext(): Boolean {
        return nextNode != null
    }

    override fun next(): T {
        if (nextNode == null) {
            throw IndexOutOfBoundsException()
        }
        previousNode = nextNode
        nextNode = nextNode!!.next
        index += 1
        lastReturned = previousNode
        return previousNode!!.data
    }

    override fun remove() {
        if (lastReturned == null) {
            throw Exception("No element has been returned yet")
        }
        if (lastReturned == list.head) {
            list.head = lastReturned?.next
        }
        if (lastReturned == list.tail) {
            list.tail = lastReturned?.previous
        }
        lastReturned!!.next?.previous = lastReturned!!.previous
        lastReturned!!.previous?.next = lastReturned!!.next
        if (lastReturned == previousNode) {
            previousNode = previousNode?.previous
        }
        if (lastReturned == nextNode) {
            nextNode = nextNode?.next
        }
        index -= 1
        list.size -= 1
        lastReturned = null
    }

    override fun hasPrevious(): Boolean {
        return previousNode != null
    }

    override fun nextIndex(): Int {
        return index
    }

    override fun previous(): T {
        if (previousNode == null) {
            throw IndexOutOfBoundsException()
        }
        nextNode = previousNode
        previousNode = previousNode!!.previous
        index -= 1
        lastReturned = nextNode
        return nextNode!!.data
    }

    override fun previousIndex(): Int {
        return index - 1
    }

    override fun add(element: T) {
        val newNode = MergeableLinkedListNode(element)
        if (nextNode == list.head || previousNode == list.tail) {
            // If inserting before the list head
            if (nextNode == list.head) {
                newNode.next = list.head    // If this is null it should also replace the head with newNode
                list.head?.previous = newNode
                list.head = newNode
            }
            // If inserting after the list tail
            if (previousNode == list.tail) {
                newNode.previous = list.tail  // If this is null it should also replace the tail with newNode
                list.tail?.next = newNode
                list.tail = newNode
            }
        } else {
            // Link it in
            previousNode?.next = newNode
            newNode.previous = previousNode
            nextNode?.previous = newNode
            newNode.next = nextNode
        }
        // Put it in the previousNode ref
        previousNode = newNode
        // Increment the index and make it the lastReturned
        index += 1
        list.size += 1
        lastReturned = newNode
    }

    override fun set(element: T) {
        lastReturned?.data = element
    }

}


class MergeableLinkedListTest {
    init {
        println("Single itr test")
        test()
        println("Remake itr test")
        test(true)
    }

    private fun test(redoItr: Boolean = false) {
        val list = MergeableLinkedList<Int>()
        var itr = list.listIterator()
        for (i in 0..5) {
            itr.add(i)
        }
        printList(itr)
        if (redoItr) {
            itr = list.listIterator()
            printList(itr)
        }
        itr.next()
        itr.next()
        itr.add(22)
        printList(itr)
        if (redoItr) {
            itr = list.listIterator()
            printList(itr)
        }
        itr.add(9)
        while (itr.hasNext()) {
            itr.next()
        }
        itr.add(99)
        printList(itr)
        if (redoItr) {
            itr = list.listIterator()
            printList(itr)
        }
        itr.next()
        itr.next()
        itr.next()
        itr.next()
        itr.remove()
        printList(itr)
        if (redoItr) {
            itr = list.listIterator()
            printList(itr)
        }
        while (itr.hasNext()) {
            println("Removing ${itr.next()}")
            itr.remove()
        }
        printList(itr)
        if (redoItr) {
            itr = list.listIterator()
            printList(itr)
        }
        itr.add(100)
        printList(itr)
        if (redoItr) {
            itr = list.listIterator()
            printList(itr)
        }
    }

    private fun printList(itr: MutableListIterator<Int>) {
        while (itr.hasPrevious()) {
            itr.previous()
        }

        var str = ""
        while (itr.hasNext()) {
            str += "${itr.next()} "
        }
        println(str)

        str = ""
        while (itr.hasPrevious()) {
            str += "${itr.previous()} "
        }
        println(str)
    }
}

