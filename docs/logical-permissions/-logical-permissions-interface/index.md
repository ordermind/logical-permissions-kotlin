[logical-permissions](../index.md) / [LogicalPermissionsInterface](.)

# LogicalPermissionsInterface

`interface LogicalPermissionsInterface`

### Properties

| Name | Summary |
|---|---|
| [bypassCallback](bypass-callback.md) | `abstract var bypassCallback: (Map<String, Any>) -> Boolean`<br>(optional) Callback for determining whether access should be bypassed. If it exists it is called in the beginning of each access check. If access should be bypassed, the regular access check is never made and instead access is automatically granted. |
| [types](types.md) | `abstract var types: Map<String, (String, Map<String, Any>) -> Boolean>`<br>Permission types. Each item in the map consists of a permission type name and a callback. See the documentation for addType() for more information. |

### Functions

| Name | Summary |
|---|---|
| [addType](add-type.md) | `abstract fun addType(name: String, callback: (String, Map<String, Any>) -> Boolean): Unit`<br>Adds a permission type. |
| [checkAccess](check-access.md) | `abstract fun checkAccess(permissions: JsonObject, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean`<br>Checks access for a permission tree.`abstract fun checkAccess(permissions: JsonArray<Any?>, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean`<br>`abstract fun checkAccess(permissions: String, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean`<br>`abstract fun checkAccess(permissions: Boolean, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean` |
| [getTypeCallback](get-type-callback.md) | `abstract fun getTypeCallback(name: String): (String, Map<String, Any>) -> Boolean`<br>Gets the callback for a permission type. |
| [getValidPermissionKeys](get-valid-permission-keys.md) | `abstract fun getValidPermissionKeys(): Set<String>`<br>Gets all keys that can be part of a permission tree. |
| [removeType](remove-type.md) | `abstract fun removeType(name: String): Unit`<br>Removes a permission type. |
| [setTypeCallback](set-type-callback.md) | `abstract fun setTypeCallback(name: String, callback: (String, Map<String, Any>) -> Boolean): Unit`<br>Changes the callback for an existing permission type. |
| [typeExists](type-exists.md) | `abstract fun typeExists(name: String): Boolean`<br>Checks whether a permission type is registered. |

### Inheritors

| Name | Summary |
|---|---|
| [LogicalPermissions](../-logical-permissions/index.md) | `open class LogicalPermissions : LogicalPermissionsInterface` |
