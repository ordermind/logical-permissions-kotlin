[logical-permissions](../index.md) / [LogicalPermissions](.)

# LogicalPermissions

`open class LogicalPermissions : `[`LogicalPermissionsInterface`](../-logical-permissions-interface/index.md)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `LogicalPermissions()` |

### Properties

| Name | Summary |
|---|---|
| [bypassCallback](bypass-callback.md) | `open var bypassCallback: (Map<String, Any>) -> Boolean`<br>(optional) Callback for determining whether access should be bypassed. If it exists it is called in the beginning of each access check. If access should be bypassed, the regular access check is never made and instead access is automatically granted. |
| [corePermissionKeys](core-permission-keys.md) | `open val corePermissionKeys: Set<String>` |
| [types](types.md) | `open var types: Map<String, (String, Map<String, Any>) -> Boolean>`<br>Permission types. Each item in the map consists of a permission type name and a callback. See the documentation for addType() for more information. |

### Functions

| Name | Summary |
|---|---|
| [addType](add-type.md) | `open fun addType(name: String, callback: (String, Map<String, Any>) -> Boolean): Unit`<br>Adds a permission type. |
| [checkAccess](check-access.md) | `open fun checkAccess(permissions: JsonObject, context: Map<String, Any>, allowBypass: Boolean): Boolean`<br>`open fun checkAccess(permissions: JsonArray<Any?>, context: Map<String, Any>, allowBypass: Boolean): Boolean`<br>`open fun checkAccess(permissions: String, context: Map<String, Any>, allowBypass: Boolean): Boolean`<br>`open fun checkAccess(permissions: Boolean, context: Map<String, Any>, allowBypass: Boolean): Boolean`<br>Checks access for a permission tree. |
| [checkAccessParsed](check-access-parsed.md) | `open fun checkAccessParsed(permissions: JsonObject, context: Map<String, Any> = mapOf(), allowBypass: Boolean): Boolean` |
| [checkAllowBypass](check-allow-bypass.md) | `open fun checkAllowBypass(permissions: JsonObject, context: Map<String, Any>, allowBypass: Boolean): Boolean` |
| [checkBypassAccess](check-bypass-access.md) | `open fun checkBypassAccess(context: Map<String, Any>): Boolean` |
| [createPermissionsObject](create-permissions-object.md) | `open fun createPermissionsObject(jsonPermissions: String): JsonObject` |
| [dispatch](dispatch.md) | `open fun dispatch(permissions: Any, context: Map<String, Any>, type: String): Boolean` |
| [externalAccessCheck](external-access-check.md) | `open fun externalAccessCheck(permission: String, context: Map<String, Any>, type: String): Boolean` |
| [getTypeCallback](get-type-callback.md) | `open fun getTypeCallback(name: String): (String, Map<String, Any>) -> Boolean`<br>Gets the callback for a permission type. |
| [getValidPermissionKeys](get-valid-permission-keys.md) | `open fun getValidPermissionKeys(): Set<String>`<br>Gets all keys that can be part of a permission tree. |
| [processAND](process-a-n-d.md) | `open fun processAND(permissions: Any, context: Map<String, Any>, type: String): Boolean` |
| [processNAND](process-n-a-n-d.md) | `open fun processNAND(permissions: Any, context: Map<String, Any>, type: String): Boolean` |
| [processNOR](process-n-o-r.md) | `open fun processNOR(permissions: Any, context: Map<String, Any>, type: String): Boolean` |
| [processNOT](process-n-o-t.md) | `open fun processNOT(permissions: Any, context: Map<String, Any>, type: String): Boolean` |
| [processOR](process-o-r.md) | `open fun processOR(permissions: Any, context: Map<String, Any>, type: String): Boolean` |
| [processXOR](process-x-o-r.md) | `open fun processXOR(permissions: Any, context: Map<String, Any>, type: String): Boolean` |
| [removeType](remove-type.md) | `open fun removeType(name: String): Unit`<br>Removes a permission type. |
| [setTypeCallback](set-type-callback.md) | `open fun setTypeCallback(name: String, callback: (String, Map<String, Any>) -> Boolean): Unit`<br>Changes the callback for an existing permission type. |
| [typeExists](type-exists.md) | `open fun typeExists(name: String): Boolean`<br>Checks whether a permission type is registered. |
