package com.example.hire

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform