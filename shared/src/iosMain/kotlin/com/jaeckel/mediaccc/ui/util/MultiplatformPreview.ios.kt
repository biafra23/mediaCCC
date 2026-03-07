package com.jaeckel.mediaccc.ui.util

import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
actual annotation class MultiplatformPreview()
