package io.github.humayanx.lastdrop.service

import io.github.humayanx.lastdrop.data.local.entity.NotificationRecord
import java.text.Normalizer

/** Excludes entire authentication alerts, including their expanded content, from the ledger. */
internal object AuthenticationCodeFilter {
    private val formatCharacters = Regex("[\\p{Cf}]")
    private val authentication = Regex(
        """\b(?:otp|otps|2fa|mfa|passcode|one[\s-]*time\s+(?:password|code|pin)|(?:verification|security|authentication|authorization|login|log[ -]?in|sign[ -]?in|access|confirmation)\s+(?:code|pin)|(?:code|pin)\s+(?:for\s+)?(?:verification|login|sign[ -]?in))\b|सत्यापन\s*कोड|ओटीपी|验证码|驗證碼|認証コード|인증\s*번호|رمز\s*التحقق|code\s+de\s+(?:vérification|sécurité)|código\s+de\s+(?:verificación|verificação|segurança)""",
        RegexOption.IGNORE_CASE
    )
    private val code = Regex("""(?<![\p{L}\p{N}])\p{Nd}{4,8}(?![\p{L}\p{N}])""")
    private val codeOnly = Regex("""\s*\p{Nd}{3,4}[ -]?\p{Nd}{3,4}\s*|\s*\p{Nd}{4,8}\s*""")
    private val codeContext = Regex(
        """\b(?:do\s+not\s+share|don't\s+share|never\s+share|your\s+code|use\s+(?:this\s+)?code|enter\s+(?:this\s+)?code|code\s+is)\b""",
        RegexOption.IGNORE_CASE
    )

    fun matches(record: NotificationRecord): Boolean =
        matches(record.title, record.content, record.bigText, record.subText)

    fun matches(vararg fields: String?): Boolean {
        val parts = fields.filterNotNull().map {
            Normalizer.normalize(it, Normalizer.Form.NFKC).replace(formatCharacters, "")
        }
        val text = parts.joinToString("\n")
        return text.contains("sensitive notification content hidden", ignoreCase = true) ||
            authentication.containsMatchIn(text) || parts.any(codeOnly::matches) ||
            (code.containsMatchIn(text) && codeContext.containsMatchIn(text))
    }
}
