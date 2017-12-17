open class LogicalPermissions: LogicalPermissionsInterface {
    open var types: Map<String, (String, Map<String, Any>) -> Boolean> = mapOf()
        set(value) {
            for((name, _) in value) {
                if(name == "") {
                    throw IllegalArgumentException("The name for a type cannot be empty.")
                }
                if(this.corePermissionKeys.contains(name.toUpperCase())) {
                    throw IllegalArgumentException("The name for a type has the illegal value \"${name}\". It cannot be one of the following values: ${this.corePermissionKeys}")
                }

            }

            field = value
        }
    open protected val corePermissionKeys: List<String> = listOf("NO_BYPASS", "AND", "NAND", "OR", "NOR", "XOR", "NOT", "TRUE", "FALSE")

    open fun addType(name: String, callback: (String, Map<String, Any>) -> Boolean) {
        if(name == "") {
            throw IllegalArgumentException("The \"name\" parameter cannot be empty.")
        }
        if(this.corePermissionKeys.contains(name.toUpperCase())) {
            throw IllegalArgumentException("The \"name\" parameter has the illegal value \"${name}\". It cannot be one of the following values: ${this.corePermissionKeys}")
        }
        if(this.types.containsKey(name)) {
            throw PermissionTypeAlreadyExistsException("The permission type \"${name}\" already exists! If you want to change the callback for an existing type, please use LogicalPermissions::SetTypeCallback().")
        }

        val types = this.types.toMutableMap()
        types[name] = callback
        this.types = types.toMap()
    }

    open fun removeType(name: String) {
        if(name == "") {
            throw IllegalArgumentException("The \"name\" parameter cannot be empty.")
        }

        val types = this.types.toMutableMap()
        types.remove(name)
        this.types = types.toMap()
    }

    open fun typeExists(name: String): Boolean {
        return this.types.containsKey(name)
    }

}