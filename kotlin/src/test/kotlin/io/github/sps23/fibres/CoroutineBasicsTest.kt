package io.github.sps23.fibres

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

/** Tests for CoroutineBasics using Kotest FunSpec. */
class CoroutineBasicsTest :
    FunSpec({
        test("fetchUserAndOrders returns the combined result") {
            CoroutineBasics.fetchUserAndOrders() shouldBe "Alice has 2 orders"
        }

        test("fetchAllUrls returns content for every URL") {
            val results = CoroutineBasics.fetchAllUrls(listOf("url1", "url2", "url3"))
            results.size shouldBe 3
            results shouldContain "content of url1"
            results shouldContain "content of url2"
            results shouldContain "content of url3"
        }
    })
