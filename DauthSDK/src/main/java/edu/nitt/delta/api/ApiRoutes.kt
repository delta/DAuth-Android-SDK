package edu.nitt.delta.api

/**
 * Singleton object containing the [api routes](https://delta.github.io/DAuth-Docs/#endpoints)
 */
internal object ApiRoutes {
    /**
     * Token route
     */
    const val Token = "/api/oauth/token"

    /**
     * User route
     */
    const val User = "/api/resources/user"

    /**
     * Login route
     */
    const val Login = "/api/auth/login"

    /**
     * Key route
     */
    const val Key="/api/oauth/oidc/key"
}
