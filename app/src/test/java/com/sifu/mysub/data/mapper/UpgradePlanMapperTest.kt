package com.sifu.mysub.data.mapper

import com.google.gson.Gson
import com.sifu.mysub.data.dto.UpgradePlanOfferDto
import com.sifu.mysub.domain.model.PlanTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class UpgradePlanMapperTest {

    private fun map(json: String) = UpgradePlanMapper.toDomain(
        Gson().fromJson(json, UpgradePlanOfferDto::class.java)
    )

    @Test
    fun `maps plans and themes`() {
        val offer = map(
            """
            {
              "code": "0",
              "title": "Gemezz Upgrade Plan",
              "brandCode": "GEMEZZ",
              "plans": [
                {"planCode":"daily","title":"Daily","price":"$1.00","theme":"pink"},
                {"planCode":"weekly","title":"Weekly","price":"$17.00","theme":"purple","recommended":true},
                {"planCode":"monthly","title":"Monthly","price":"$60.00","theme":"blue"}
              ]
            }
            """.trimIndent()
        )

        assertEquals("Gemezz Upgrade Plan", offer.title)
        assertEquals(3, offer.plans.size)
        assertEquals(
            listOf(PlanTheme.PINK, PlanTheme.PURPLE, PlanTheme.BLUE),
            offer.plans.map { it.theme }
        )
        assertEquals(1, offer.recommendedIndex)
    }

    @Test
    fun `drops plans with no price`() {
        val offer = map(
            """
            {"code":"0","plans":[
              {"planCode":"a","title":"A","price":"$1.00"},
              {"planCode":"b","title":"B"}
            ]}
            """.trimIndent()
        )

        assertEquals(listOf("a"), offer.plans.map { it.code })
    }

    @Test
    fun `falls back to the middle card when nothing is flagged`() {
        val offer = map(
            """
            {"code":"0","plans":[
              {"planCode":"a","price":"$1.00"},
              {"planCode":"b","price":"$2.00"},
              {"planCode":"c","price":"$3.00"}
            ]}
            """.trimIndent()
        )

        assertEquals(1, offer.recommendedIndex)
        assertEquals(1, offer.plans.count { it.isRecommended })
    }

    @Test
    fun `keeps only the first recommendation when several are flagged`() {
        val offer = map(
            """
            {"code":"0","plans":[
              {"planCode":"a","price":"$1.00","recommended":true},
              {"planCode":"b","price":"$2.00","recommended":true}
            ]}
            """.trimIndent()
        )

        assertEquals(1, offer.plans.count { it.isRecommended })
        assertEquals(0, offer.recommendedIndex)
    }

    @Test
    fun `unknown theme falls back to purple`() {
        val offer = map("""{"code":"0","plans":[{"planCode":"a","price":"$1.00","theme":"neon"}]}""")
        assertEquals(PlanTheme.PURPLE, offer.plans.single().theme)
    }
}
