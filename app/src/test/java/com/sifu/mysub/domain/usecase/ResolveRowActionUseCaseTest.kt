package com.sifu.mysub.domain.usecase

import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowAction
import com.sifu.mysub.domain.model.RowStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveRowActionUseCaseTest {

    private val subject = ResolveRowActionUseCase()

    @Test
    fun `copyable row yields Copy`() {
        val action = subject(DetailRow("Pin", "8466d4", style = RowStyle.COPYABLE))
        assertEquals(RowAction.Copy("Pin", "8466d4"), action)
    }

    @Test
    fun `link row yields OpenLink`() {
        val url = "https://kh.gemezz.mobi/?pin=8466d4"
        val action = subject(DetailRow("Access Link", url, style = RowStyle.LINK))
        assertEquals(RowAction.OpenLink(url), action)
    }

    @Test
    fun `plain row yields None`() {
        val action = subject(DetailRow("Receiver", "Sengleap Seang"))
        assertEquals(RowAction.None, action)
    }

    @Test
    fun `link row with non-http scheme is rejected`() {
        val action = subject(DetailRow("Bad", "javascript:alert(1)", style = RowStyle.LINK))
        assertEquals(RowAction.None, action)
    }

    @Test
    fun `blank value yields None`() {
        assertEquals(RowAction.None, subject(DetailRow("Pin", "", style = RowStyle.COPYABLE)))
    }
}
