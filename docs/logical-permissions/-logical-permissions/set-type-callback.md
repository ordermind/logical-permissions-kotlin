[logical-permissions](../index.md) / [LogicalPermissions](index.md) / [setTypeCallback](.)

# setTypeCallback

`open fun setTypeCallback(name: String, callback: (String, Map<String, Any>) -> Boolean): Unit`

Overrides [LogicalPermissionsInterface.setTypeCallback](../-logical-permissions-interface/set-type-callback.md)

Changes the callback for an existing permission type.

### Parameters

`name` - The name of the permission type.

`callback` - The callback that evaluates the permission type. See the documentation for addType() for more information.