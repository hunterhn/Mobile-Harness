package com.jarves.mh.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelResponseParserTest {
    @Test
    fun parsesOpenAiStyleDataList() {
        val models = ModelResponseParser.parse(
            """{"data":[{"id":"model-b"},{"id":"model-a","display_name":"Model A"}]}""",
        )

        assertEquals(listOf("model-a", "model-b"), models.map { it.id })
        assertEquals("Model A", models.first().displayName)
    }

    @Test
    fun parsesModelsAndStringArrays() {
        assertEquals(
            listOf("alpha"),
            ModelResponseParser.parse("""{"models":[{"name":"alpha"}]}""").map { it.id },
        )
        assertEquals(
            listOf("alpha", "beta"),
            ModelResponseParser.parse("""["beta","alpha"]""").map { it.id },
        )
    }

    @Test
    fun malformedResponseReturnsEmptyList() {
        assertEquals(emptyList<DiscoveredModel>(), ModelResponseParser.parse("not json"))
    }

    @Test
    fun parsesReasoningEffortsFromCapabilities() {
        val models = ModelResponseParser.parse(
            """{"data":[{"id":"deepseek-v4.1-flash","capabilities":{"reasoning":{"supported":true,"effort":["low","high","max","none"]}}}]}""",
        )

        assertEquals(listOf("low", "high", "max"), models.single().reasoningEfforts)
    }

    @Test
    fun ignoresMissingOrDisabledReasoning() {
        val models = ModelResponseParser.parse(
            """{"data":[{"id":"plain"},{"id":"off","capabilities":{"reasoning":{"supported":false,"effort":["high"]}}}]}""",
        )

        assertEquals(emptyList<String>(), models.first { it.id == "plain" }.reasoningEfforts)
        assertEquals(emptyList<String>(), models.first { it.id == "off" }.reasoningEfforts)
    }
}
