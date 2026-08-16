package com.ibnalsham.app.library.core.environment

enum class BuildEnvironment(
    val baseUrl: String,
    val isDebug: Boolean
) {
    PRODUCTION(
        baseUrl = "https://ibnalsham.prolabtech.net/api/",
        isDebug = false
    ),
    STAGING(
        baseUrl = "https://ibnalsham.prolabtech.net/api/", // Currently same for this project
        isDebug = true
    )
}

object EnvironmentConfig {
    var current = BuildEnvironment.PRODUCTION
}
