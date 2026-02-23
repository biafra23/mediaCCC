package com.jaeckel.mediaccc.tv.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TvKoinModulesTest {

    @Test
    fun tvAppModulesContainsTwoModules() {
        assertEquals(2, tvAppModules.size)
    }

    @Test
    fun tvAppModulesIsNotEmpty() {
        assertTrue(tvAppModules.isNotEmpty())
    }
}
