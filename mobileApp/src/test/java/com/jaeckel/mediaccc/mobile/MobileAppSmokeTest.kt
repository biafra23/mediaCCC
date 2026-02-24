package com.jaeckel.mediaccc.mobile

import kotlin.test.Test
import kotlin.test.assertTrue

class MobileAppSmokeTest {

    @Test
    fun testInfrastructureWorks() {
        assertTrue(true, "Test infrastructure is working")
    }

    @Test
    fun kotlinVersionIsAvailable() {
        val version = KotlinVersion.CURRENT
        assertTrue(version.major >= 2, "Kotlin version should be 2.x or higher")
    }
}
