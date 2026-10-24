package io.github.humayanx.lastdrop.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthenticationCodeFilterTest {
    @Test fun rejectsAuthenticationAlertsAcrossFieldsAndFormats() {
        listOf("Your OTP is 123456", "Your verification code: AB-CD-EF", "One-time password: 1234",
            "Security code 123 456", "Sign-in code: A1B2C3", "123456", "123-456",
            "Don't share 123456 with anyone", "Your code is １２３４５６", "सत्यापन कोड: १२३४५६",
            "验证码：123456", "رمز التحقق 123456", "Your O\u200bTP: 123456").forEach {
            assertTrue(it, AuthenticationCodeFilter.matches("Messages", null, it))
        }
    }

    @Test fun ordinaryNotificationsAreKept() {
        listOf("Meet me at 6", "Order 123456 has shipped", "Invoice total: 1200",
            "Your friend sent you a message", "Your package arrives tomorrow").forEach {
            assertFalse(it, AuthenticationCodeFilter.matches("Messages", it))
        }
        assertFalse(AuthenticationCodeFilter.matches(null, "", null))
    }
}
