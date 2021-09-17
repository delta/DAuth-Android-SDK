package edu.nitt.delta.models

enum class ResponseType {
    Code{
        override fun toString(): String {
            return "code"
        }
    };
}