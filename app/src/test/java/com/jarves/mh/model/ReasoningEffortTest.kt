package com.jarves.mh.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReasoningEffortTest {
    @Test
    fun normalizesKnownLevelsInEscalationOrder() {
        assertEquals(
            listOf("low", "high", "max"),
            normalizedReasoningEfforts(listOf("max", "none", "HIGH", "low", "high")),
        )
    }

    @Test
    fun keepsAValidCurrentLevelAndFallsBackToHigh() {
        assertEquals("max", preferredReasoningEffort(listOf("low", "high", "max"), "max"))
        assertEquals("high", preferredReasoningEffort(listOf("low", "high", "max"), "medium"))
        assertEquals("high", preferredReasoningEffort(listOf("high", "max"), ""))
        assertEquals("max", preferredReasoningEffort(listOf("max"), ""))
        assertEquals("", preferredReasoningEffort(emptyList(), "high"))
    }

    @Test
    fun onlyOpenCodeGoExposesTheFeature() {
        assertTrue(providerSupportsReasoningEffort(ProviderKind.OPENCODE_GO))
        assertFalse(providerSupportsReasoningEffort(ProviderKind.ANTHROPIC))
        assertFalse(providerSupportsReasoningEffort(ProviderKind.OPENCODE_ZEN))
        assertFalse(providerSupportsReasoningEffort(ProviderKind.CUSTOM))
    }

    @Test
    fun appliesAChatSelectionOnlyWhenTheModelOffersIt() {
        val profile = ProviderProfile(ProviderKind.OPENCODE_GO)
        val updated = profile.withSelectedReasoningEffort("max")

        assertEquals("max", updated?.reasoningEffort)
        assertEquals(listOf("low", "high", "max"), updated?.reasoningEfforts)
        assertNull(profile.withSelectedReasoningEffort("medium"))
        assertNull(profile.withSelectedReasoningEffort("none"))
    }

    @Test
    fun chatSelectionUsesTheSavedCatalogListWhenPresent() {
        val profile = ProviderProfile(
            ProviderKind.OPENCODE_GO,
            reasoningEfforts = listOf("high", "max"),
        )

        assertEquals("max", profile.withSelectedReasoningEffort("max")?.reasoningEffort)
        assertNull(profile.withSelectedReasoningEffort("low"))
    }

    @Test
    fun chatSelectionNeverAppliesToOtherProviders() {
        assertNull(ProviderProfile(ProviderKind.ANTHROPIC).withSelectedReasoningEffort("max"))
        assertEquals(
            emptyList<String>(),
            fallbackReasoningEfforts(ProviderKind.ANTHROPIC, "claude-sonnet-4-6"),
        )
    }

    @Test
    fun fallbackLevelsCoverTheRecommendedGoModels() {
        assertEquals(
            listOf("low", "high", "max"),
            fallbackReasoningEfforts(ProviderKind.OPENCODE_GO, "deepseek-v4.1-flash"),
        )
        assertEquals(
            listOf("low", "medium", "high", "xhigh", "max"),
            fallbackReasoningEfforts(ProviderKind.OPENCODE_GO, "gpt-6-luna"),
        )
    }
}
