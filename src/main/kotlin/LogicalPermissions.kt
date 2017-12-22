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
        var mapPermissions = this.parsePermissions(permissions)

        // Uppercase no_bypass key
        if(mapPermissions.containsKey("no_bypass")) {
            mapPermissions["NO_BYPASS"] = mapPermissions.getValue("no_bypass")
            mapPermissions.remove("no_bypass")
        }

        var allowBypass = allowBypass

        // Bypass access check
        if(mapPermissions.containsKey("NO_BYPASS")) {
            val noBypass = mapPermissions["NO_BYPASS"]
            if(noBypass != null && allowBypass) {
                allowBypass = this.checkAllowBypass(noBypass, context)
            }
            mapPermissions.remove("NO_BYPASS")
        }
        if(allowBypass) {
            try {
                if(this.checkBypassAccess(context)) {
                    return true
                }
            }
            catch(e: Exception) {
                throw Exception("Error checking bypass access: ${e.localizedMessage}")
            }
        }

        // Normal access check
        if(mapPermissions.size > 0) {
            try {
                return this.processOR(permissions = mapPermissions, context = context)
            } catch (e: Exception) {
                throw Exception("Error checking access: ${e.localizedMessage}")
            }
        }

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

    open protected fun checkAllowBypass(noBypass: Any, context: Map<String, Any>): Boolean {
        if(noBypass is Boolean) {
            return !noBypass
        }
        if(noBypass is String) {
            if(noBypass.toUpperCase() != "TRUE" && noBypass.toUpperCase() != "FALSE") {
                throw IllegalArgumentException("The NO_BYPASS value must be a Boolean, a boolean string or a com.beust.klaxon.JsonObject. Current value: $noBypass")
            }
            return noBypass.toBoolean()
        }
        if(noBypass is JsonObject) {
            try {
                return !this.processOR(permissions = noBypass, context = context)
            }
            catch(e: Exception) {
                throw Exception("Error checking NO_BYPASS permissions: ${e.localizedMessage}")
            }
        }

        throw IllegalArgumentException("The NO_BYPASS value must be a Boolean, a boolean string or a com.beust.klaxon.JsonObject. Current value: $noBypass")
    }

    open protected fun checkBypassAccess(context: Map<String, Any>): Boolean {
        if(this.bypassCallback == null) {
            return false
        }

        return this.bypassCallback?.invoke(context) ?: false
    }

    open protected fun dispatch(permissions: Any, context: Map<String, Any> = mapOf(), type: String = ""): Boolean {
        var type = type

        if(permissions is Boolean) {
            if(!type.isEmpty()) {
                throw IllegalArgumentException("You cannot put a boolean permission as a descendant to a permission type. Existing type: $type. Evaluated permissions: $permissions")
            }

            return permissions
        }

        if(permissions is String) {
            if(permissions.toUpperCase() == "TRUE") {
                if(!type.isEmpty()) {
                    throw IllegalArgumentException("You cannot put a boolean permission as a descendant to a permission type. Existing type: $type. Evaluated permissions: $permissions")
                }

                return true
            }
            else if(permissions.toUpperCase() == "FALSE") {
                if(!type.isEmpty()) {
                    throw IllegalArgumentException("You cannot put a boolean permission as a descendant to a permission type. Existing type: $type. Evaluated permissions: $permissions")
                }

                return false
            }

            return this.externalAccessCheck(permissions = permissions, context = context, type = type)
        }

        if(permissions is JsonArray<*>) {
            if(permissions.size > 0) {
                return this.processOR(permissions = permissions, context = context, type = type)
            }

            return false
        }

        if(permissions is JsonObject) {
            if(permissions.size == 1) {
                val item = permissions.entries.first()
                val key = item.key
                val value = item.value
                if(key.toIntOrNull() == null) {
                    val keyUpper = key.toUpperCase()
                    if(keyUpper == "NO_BYPASS") {
                        throw IllegalArgumentException("The NO_BYPASS key must be placed highest in the permission hierarchy. Evaluated permissions: $permissions")
                    }
                    if(keyUpper == "AND") {
                        return this.processAND(permissions = value, context = context, type = type)
                    }
                    if(keyUpper == "NAND") {
                        return this.processNAND(permissions = value, context = context, type = type)
                    }
                    if(keyUpper == "OR") {
                        return this.processOR(permissions = value, context = context, type = type)
                    }
                    if(keyUpper == "NOR") {
                        return this.processNOR(permissions = value, context = context, type = type)
                    }
                    if(keyUpper == "XOR") {
                        return this.processXOR(permissions = value, context = context, type = type)
                    }
                    if(keyUpper == "NOT") {
                        return this.processNOT(permissions = value, context = context, type = type)
                    }
                    if(keyUpper == "TRUE" || keyUpper == "FALSE") {
                        throw IllegalArgumentException("A Boolean permission cannot have children. Evaluated permissions: $permissions")
                    }

                    if(!type.isEmpty()) {
                        throw IllegalArgumentException("You cannot put a permission type as a descendant to another permission type. Existing type: $type. Evaluated permissions: $permissions")
                    }
                    type = key
                }
                if(value is JsonArray<*> || value is JsonObject) {
                    return this.processOR(permissions = value, context = context, type = type)
                }

                return this.dispatch(permissions = value, context = context, type = type)
            }
            if(permissions.size > 1) {
                return this.processOR(permissions = permissions, context = context, type = type)
            }

            return false
        }

        throw IllegalArgumentException("A permission value must either be a Boolean, a String, a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Evaluated permissions: $permissions")
    }

}