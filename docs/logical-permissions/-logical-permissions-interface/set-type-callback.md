[logical-permissions](../index.md) / [LogicalPermissionsInterface](index.md) / [setTypeCallback](.)

# setTypeCallback

`abstract fun setTypeCallback(name: String, callback: (String, Map<String, Any>) -> Boolean): Unit`

Changes the callback for an existing permission type.

### Parameters

`name` - The name of the permission type.

`callback` - The callback that evaluates the permission type. See the documentation for addType() for more information.