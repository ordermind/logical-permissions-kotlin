import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.json

open class LogicalPermissions: LogicalPermissionsInterface {
    open var types: Map<String, (String, Map<String, Any>) -> Boolean> = mapOf()
        set(value) {
            for((name, _) in value) {
                if(name.isEmpty()) {
                    throw IllegalArgumentException("The name for a type cannot be empty.")
                }
                if(this.corePermissionKeys.contains(name.toUpperCase())) {
                    throw IllegalArgumentException("The name for a type has the illegal value \"$name\". It cannot be one of the following values: ${this.corePermissionKeys}")
                }

            }

            field = value
        }

    open var bypassCallback: ((Map<String, Any>) -> Boolean)? = null

    open protected val corePermissionKeys: Set<String> = setOf("NO_BYPASS", "AND", "NAND", "OR", "NOR", "XOR", "NOT", "TRUE", "FALSE")

    open fun addType(name: String, callback: (String, Map<String, Any>) -> Boolean) {
        if(name.isEmpty()) {
            throw IllegalArgumentException("The \"name\" parameter cannot be empty.")
        }
        if(this.corePermissionKeys.contains(name.toUpperCase())) {
            throw IllegalArgumentException("The \"name\" parameter has the illegal value \"$name\". It cannot be one of the following values: ${this.corePermissionKeys}")
        }
        if(this.typeExists(name)) {
            throw PermissionTypeAlreadyExistsException("The permission type \"$name\" already exists! If you want to change the callback for an existing type, please use LogicalPermissions::setTypeCallback().")
        }

        val types = this.types.toMutableMap()
        types[name] = callback
        this.types = types.toMap()
    }

    open fun removeType(name: String) {
        if(name.isEmpty()) {
            throw IllegalArgumentException("The \"name\" parameter cannot be empty.")
        }
        if(!this.typeExists(name)) {
            throw PermissionTypeNotRegisteredException("The permission type \"$name\" has not been registered. Please use LogicalPermissions::addType() or set the value for LogicalPermission::types to register permission types.")
        }

        val types = this.types.toMutableMap()
        types.remove(name)
        this.types = types.toMap()
    }

    open fun typeExists(name: String): Boolean {
        if(name.isEmpty()) {
            throw IllegalArgumentException("The \"name\" parameter cannot be empty.")
        }

        return this.types.containsKey(name)
    }

    open fun getTypeCallback(name: String): (String, Map<String, Any>) -> Boolean {
        if(name.isEmpty()) {
            throw IllegalArgumentException("The \"name\" parameter cannot be empty.")
        }

        val callback = this.types[name]
        if(callback == null) {
            throw PermissionTypeNotRegisteredException("The permission type \"$name\" has not been registered. Please use LogicalPermissions::addType() or set the value for LogicalPermission::types to register permission types.")
        }

        return callback
    }

    open fun setTypeCallback(name: String, callback: (String, Map<String, Any>) -> Boolean) {
        if(name.isEmpty()) {
            throw IllegalArgumentException("The \"name\" parameter cannot be empty.")
        }
        if(!this.typeExists(name)) {
            throw PermissionTypeNotRegisteredException("The permission type \"$name\" has not been registered. Please use LogicalPermissions::addType() or set the value for LogicalPermission::types to register permission types.")
        }

        val types = this.types.toMutableMap()
        types[name] = callback
        this.types = types.toMap()
    }

    open fun getValidPermissionKeys(): Set<String> {
        return this.corePermissionKeys.union(this.types.keys)
    }

    open fun checkAccess(permissions: Any, context: Map<String, Any>, allowBypass: Boolean = true): Boolean {
        val mapPermissions = this.parsePermissions(permissions)

        return true
    }

    open protected fun parsePermissions(permissions: Any): JsonObject {
        var jsonPermissions = ""

        if(permissions is JsonObject) {
            jsonPermissions = permissions.toJsonString()
        }
        else if(permissions is JsonArray<*>) {
            jsonPermissions = permissions.toJsonString()
        }
        else if(permissions is String) {
            if(permissions.length <= 5 && (permissions.toUpperCase() == "TRUE" || permissions.toUpperCase() == "FALSE")) {
                return json {
                    obj("OR" to array(permissions))
                }
            }
            jsonPermissions = permissions
        }
        else if(permissions is Boolean) {
            return json {
                obj("OR" to array(permissions))
            }
        }
        else {
            throw IllegalArgumentException("Permissions must be a Boolean, a String, a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Evaluated permissions: $permissions")
        }

        if(!jsonPermissions.first().equals("{")) {
            jsonPermissions = "{\"OR\": $jsonPermissions}"
        }

        val parser: Parser = Parser()
        val stringBuilder: StringBuilder = StringBuilder(jsonPermissions)
        return parser.parse(stringBuilder) as JsonObject
    }
}