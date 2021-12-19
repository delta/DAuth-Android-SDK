package edu.nitt.delta.auth.models

enum class ResponseType {
    Code{
        override fun toString(): String {
            return "code"
        }
    };
}
