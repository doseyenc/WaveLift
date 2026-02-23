package com.doseyenc.wavelift

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform