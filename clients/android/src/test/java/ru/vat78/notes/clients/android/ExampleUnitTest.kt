package ru.vat78.notes.clients.android

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val f1 = flowOf(setOf("111", "222", "333", "444"))
        val f2 = flowOf(setOf("111"))
        runBlocking {
            val result = listOf(f2).asIterable()
                .map { it.first() }
                .fold(emptySet<String>()) { acc, list ->
                    if (acc.isEmpty() || list.isEmpty()) acc + list else acc.intersect(list)
                }
                .subtract(emptySet())

            println("Result: $result")
        }
    }
}