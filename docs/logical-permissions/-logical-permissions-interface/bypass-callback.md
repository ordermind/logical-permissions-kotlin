[logical-permissions](../index.md) / [LogicalPermissionsInterface](index.md) / [bypassCallback](.)

# bypassCallback

`abstract var bypassCallback: (Map<String, Any>) -> Boolean`

(optional) Callback for determining whether access should be bypassed. If it exists it is called in the beginning of each access check. If access should be bypassed, the regular access check is never made and instead access is automatically granted.

