package com.jaeckel.mediaccc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform