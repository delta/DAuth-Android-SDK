package edu.nitt.delta.models

enum class Scope {
    OpenID{
        override fun toString(): String {
            return "openid"
        } },
    Email{
        override fun toString(): String {
            return "email"
        } },
    Profile{
        override fun toString(): String {
            return "profile"
        } },
    User{
        override fun toString(): String {
            return "user"
        } };

    companion object{
        fun combineScopes(scopes: List<Scope>):String{
            var out = ""
            for (scope in scopes){
                out += "+${scope}"
            }
            if (out.isNotEmpty()){
                out = out.substring(1)
            }
            return out
        }
    }
}
