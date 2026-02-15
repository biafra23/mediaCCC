package com.jaeckel.mediaccc.di

import com.jaeckel.mediaccc.MediaRepository
import com.jaeckel.mediaccc.api.MediaCCCApi
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * Unit test that verifies all Koin dependencies at build time.
 *
 * This test runs during the build process and will FAIL THE BUILD
 * if any dependencies are missing or incorrectly configured.
 *
 * This provides compile-time-like safety for dependency injection.
 */
class KoinModuleVerificationTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `verify sharedModule dependencies`() {
        // This will fail at build time if:
        // - Any required dependency is missing
        // - There are circular dependencies
        // - Types don't match
        sharedModule.verify(
            extraTypes = listOf(
                MediaCCCApi::class,
                MediaRepository::class,
                // Optional parameters in MediaCCCApi constructor
                HttpClientEngine::class,
                Function1::class  // HttpClientConfig.() -> Unit
            )
        )
    }
}


