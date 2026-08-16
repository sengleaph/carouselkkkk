package com.sifu.mysub.data.mapper

import com.google.gson.Gson
import com.sifu.mysub.data.dto.SubscriptionDto
import com.sifu.mysub.domain.model.RowEmphasis
import com.sifu.mysub.domain.model.RowStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionMapperTest {

    private val json = """
        {
          "code": "0",
          "msg": "success",
          "subCode": "gemez",
          "planName": "Gemez Daily Plan",
          "regId": "85512479896",
          "subscribeToken": "jzuqheqiiuyfcxjguqbbioxbvhyxmv",
          "accountId": "855124798976",
          "amount": "0.28 $",
          "renew": "Tomorrow",
          "dataList": [
            { "title": "Receiver", "val": "Sengleap Seang", "color": "NONE",
              "isLine": "false", "isBold": "false", "remark": "NONE" },
            { "title": "Debit Amount", "val": "-0.28 USD", "color": "RED",
              "isLine": "false", "isBold": "false", "remark": "NONE" },
            { "title": "Pin", "val": "8466d4", "color": "NONE",
              "isLine": "false", "isBold": "false", "remark": "NONE", "style": "copyGold" },
            { "title": "Access Link", "val": "https://kh.gemezz.mobi/?pin=8466d4",
              "color": "NONE", "isLine": "true", "isBold": "false",
              "remark": "NONE", "style": "link" }
          ],
          "haveSub": true
        }
    """.trimIndent()

    private val subject = SubscriptionMapper.toDomain(
        Gson().fromJson(json, SubscriptionDto::class.java)
    )

    @Test
    fun `maps header fields`() {
        assertEquals("Gemez Daily Plan", subject.planName)
        assertEquals("0.28 $", subject.amount)
        assertEquals("Tomorrow", subject.renew)
        assertEquals("855124798976", subject.accountId)
        assertTrue(subject.hasSubscription)
    }

    @Test
    fun `maps RED color to NEGATIVE emphasis`() {
        val debit = subject.rows.single { it.title == "Debit Amount" }
        assertEquals(RowEmphasis.NEGATIVE, debit.emphasis)

        val receiver = subject.rows.single { it.title == "Receiver" }
        assertEquals(RowEmphasis.NORMAL, receiver.emphasis)
    }

    @Test
    fun `maps string booleans to real booleans`() {
        val link = subject.rows.single { it.title == "Access Link" }
        assertTrue(link.showDivider)

        val receiver = subject.rows.single { it.title == "Receiver" }
        assertEquals(false, receiver.showDivider)
        assertEquals(false, receiver.bold)
    }

    @Test
    fun `treats NONE remark as null`() {
        assertTrue(subject.rows.all { it.remark == null })
    }

    @Test
    fun `maps styles`() {
        assertEquals(RowStyle.COPYABLE, subject.rows.single { it.title == "Pin" }.style)
        assertEquals(RowStyle.LINK, subject.rows.single { it.title == "Access Link" }.style)
        assertEquals(RowStyle.PLAIN, subject.rows.single { it.title == "Receiver" }.style)
    }

    @Test
    fun `exposes pin and access link convenience accessors`() {
        assertEquals("8466d4", subject.pin)
        assertEquals("https://kh.gemezz.mobi/?pin=8466d4", subject.accessLink)
    }

    @Test
    fun `tolerates missing dataList and null fields`() {
        val sparse = SubscriptionMapper.toDomain(
            Gson().fromJson("""{"code":"0"}""", SubscriptionDto::class.java)
        )
        assertEquals(emptyList<Any>(), sparse.rows)
        assertEquals("", sparse.planName)
        assertEquals(false, sparse.hasSubscription)
        assertNull(sparse.pin)
    }
}
