package app.numberguessgame

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform