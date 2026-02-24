package com.jaeckel.mediaccc.tv.di

import com.jaeckel.mediaccc.di.sharedModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
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

    @Test
    fun sharedModuleIsFirstElement() {
        assertSame(sharedModule, tvAppModules[0])
    }

    @Test
    fun tvViewModelModuleIsSecondElement() {
        assertSame(tvViewModelModule, tvAppModules[1])
    }
}
