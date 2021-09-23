package edu.nitt.delta.models

enum class Scope {
    OpenID {
        override fun toString(): String {
            return "openid"
        }
    },
    Email {
        override fun toString(): String {
            return "email"
        }
    },
    Profile {
        override fun toString(): String {
            return "profile"
        }
    },
    User {
        override fun toString(): String {
            return "user"
        }
    };

    companion object {
        fun combineScopes(scopes: List<Scope>) =
            scopes.map { it.toString() }.joinToString(separator = "+") { it }
    }
}
