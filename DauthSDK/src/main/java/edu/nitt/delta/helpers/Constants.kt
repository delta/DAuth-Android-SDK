package edu.nitt.delta.helpers

internal object DAuthConstants {
    const val SCHEME = "https"
    const val BASE_AUTHORITY = "auth.delta.nitt.edu"
    const val BASE_URL ="https://auth.delta.nitt.edu"
    const val ACCOUNT_TYPE = "auth.delta.nitt.edu"
    const val TIME_DIFF = 2592000000
}


internal object ErrorCodes{
    const val USER_DISMISS="User Dismiss"
    const val NO_ACCOUNT="Account Not Found"
    const val INVALID_CREDENTIALS="Invalid Credentials"
}
