package eu.tutorials.lostfoundapp.navigation

object NavRoutes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val HOME = "home"
    const val REPORT_LOST = "report_lost"
    const val REPORT_FOUND = "report_found"
    const val MATCHES = "matches"
    const val NOTIFICATIONS = "notifications"
    const val CHAT = "chat/{matchId}"

    fun chatRoute(matchId: String): String = "chat/$matchId"
}