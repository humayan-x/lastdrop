package io.github.humayanx.lastdrop.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class QuerySanitizerTest {

    @Test
    fun sanitizeQuery_removesFtsWildcardsAndQuotes() {
        val input = "whatsapp* 'test' \"alert\""
        val output = NotificationRepositoryImpl.sanitizeQuery(input)
        assertEquals("whatsapp test alert", output)
    }

    @Test
    fun sanitizeQuery_trimsWhitespace() {
        val input = "   telegram   "
        val output = NotificationRepositoryImpl.sanitizeQuery(input)
        assertEquals("telegram", output)
    }

    @Test
    fun sanitizeQuery_handlesEmptyString() {
        val input = "   *\"'   "
        val output = NotificationRepositoryImpl.sanitizeQuery(input)
        assertEquals("", output)
    }

    @Test
    fun buildFtsQuery_treatsOperatorsAsLiteralPrefixTerms() {
        assertEquals("\"AND*\" \"OR*\" \"NOT*\"",
            NotificationRepositoryImpl.buildFtsQuery("AND (OR) NOT:"))
    }

    @Test
    fun buildFtsQuery_handlesPunctuationOnlyAndUnicode() {
        assertEquals("", NotificationRepositoryImpl.buildFtsQuery("():-\"*"))
        assertEquals("\"café*\" \"हैलो*\"", NotificationRepositoryImpl.buildFtsQuery("café हैलो"))
    }
}
