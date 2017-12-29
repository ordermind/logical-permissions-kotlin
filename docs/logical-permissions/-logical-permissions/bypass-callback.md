[logical-permissions](../index.md) / [LogicalPermissions](index.md) / [bypassCallback](.)

# bypassCallback

`open var bypassCallback: (Map<String, Any>) -> Boolean`

Overrides [LogicalPermissionsInterface.bypassCallback](../-logical-permissions-interface/bypass-callback.md)

(optional) Callback for determining whether access should be bypassed. If it exists it is called in the beginning of each access check. If access should be bypassed, the regular access check is never made and instead access is automatically granted.

