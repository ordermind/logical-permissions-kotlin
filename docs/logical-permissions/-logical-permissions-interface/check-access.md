[logical-permissions](../index.md) / [LogicalPermissionsInterface](index.md) / [checkAccess](.)

# checkAccess

`abstract fun checkAccess(permissions: JsonObject, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean`
`abstract fun checkAccess(permissions: JsonArray<Any?>, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean`
`abstract fun checkAccess(permissions: String, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean`
`abstract fun checkAccess(permissions: Boolean, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean`

Checks access for a permission tree.

### Parameters

`permissions` - The permission tree to be evaluated. This parameter can be a com.beust.klaxon.JsonObject, a com.beust.klaxon.JsonArray, a json string or a Boolean.

`context` - (optional) A context array that could for example contain the evaluated user and document. Default value is an empty map.

`allowBypass` - (optional) Determines whether bypassing access should be allowed at all. Default value is true. If this parameter is set to false, access bypass will not be allowed under any circumstance. It is mostly used for testing purposes as you can also use the "NO_BYPASS" permission key to the same effect.

**Return**
true if access is granted or false if access is denied.

