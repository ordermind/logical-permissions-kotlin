[logical-permissions](../index.md) / [LogicalPermissions](index.md) / [addType](.)

# addType

`open fun addType(name: String, callback: (String, Map<String, Any>) -> Boolean): Unit`

Overrides [LogicalPermissionsInterface.addType](../-logical-permissions-interface/add-type.md)

Adds a permission type.

### Parameters

`name` - The name of the permission type.

`callback` - The callback that evaluates the permission type. Upon calling checkAccess() the registered callback will be passed two parameters: a permission string (such as a role) and the context parameter passed to checkAccess(). The permission will always be a single string even if for example multiple roles are accepted. In that case the callback will be called once for each role that is to be evaluated. The callback must return a boolean which determines whether access should be granted.