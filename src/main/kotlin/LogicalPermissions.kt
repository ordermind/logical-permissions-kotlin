import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.json

open class LogicalPermissions: LogicalPermissionsInterface {
    open var types: Map<String, (String, Map<String, Any>) -> Boolean> = mapOf()
        set(value) {
            for((name, _) in value) {
                if(name.isEmpty()) {
                    throw InvalidArgumentValueException("The name for a type cannot be empty.")
                }
                if(this.corePermissionKeys.contains(name.toUpperCase())) {
                    throw InvalidArgumentValueException("The name for a type has the illegal value \"$name\". It cannot be one of the following values: ${this.corePermissionKeys}")
                }
            }
            field = value
        }

    open var bypassCallback: ((Map<String, Any>) -> Boolean)? = null

    open protected val corePermissionKeys: Set<String> = setOf("NO_BYPASS", "AND", "NAND", "OR", "NOR", "XOR", "NOT", "TRUE", "FALSE")

    open fun addType(name: String, callback: (String, Map<String, Any>) -> Boolean) {
        if(name.isEmpty()) {
            throw InvalidArgumentValueException("The \"name\" parameter cannot be empty.")
        }
        if(this.corePermissionKeys.contains(name.toUpperCase())) {
            throw InvalidArgumentValueException("The \"name\" parameter has the illegal value \"$name\". It cannot be one of the following values: ${this.corePermissionKeys}")
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
            throw InvalidArgumentValueException("The \"name\" parameter cannot be empty.")
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
            throw InvalidArgumentValueException("The \"name\" parameter cannot be empty.")
        }

        return this.types.containsKey(name)
    }

    open fun getTypeCallback(name: String): (String, Map<String, Any>) -> Boolean {
        if(name.isEmpty()) {
            throw InvalidArgumentValueException("The \"name\" parameter cannot be empty.")
        }

        val callback = this.types[name]
        if(callback == null) {
            throw PermissionTypeNotRegisteredException("The permission type \"$name\" has not been registered. Please use LogicalPermissions::addType() or set the value for LogicalPermission::types to register permission types.")
        }

        return callback
    }

    open fun setTypeCallback(name: String, callback: (String, Map<String, Any>) -> Boolean) {
        if(name.isEmpty()) {
            throw InvalidArgumentValueException("The \"name\" parameter cannot be empty.")
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

    open fun checkAccess(permissions: Any, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean {
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
            catch(e: MutableException) {
                e.message = "Error checking bypass access: ${e.message}"
                throw e
            }
        }

        // Normal access check
        if(mapPermissions.size > 0) {
            try {
                return this.processOR(permissions = mapPermissions, context = context, type = "")
            } catch (e: MutableException) {
                e.message = "Error checking access: ${e.message}"
                throw e
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
            val trimmedPermissions = permissions.trim()
            if(trimmedPermissions.length <= 5 && (trimmedPermissions.toUpperCase() == "TRUE" || trimmedPermissions.toUpperCase() == "FALSE")) {
                return json {
                    obj("OR" to array(trimmedPermissions))
                }
            }
            jsonPermissions = trimmedPermissions
        }
        else if(permissions is Boolean) {
            return json {
                obj("OR" to array(permissions))
            }
        }
        else {
            throw InvalidArgumentValueException("Permissions must be a Boolean, a String, a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Evaluated permissions: $permissions")
        }

        if(jsonPermissions.first().equals('[')) {
            jsonPermissions = "{\"OR\": $jsonPermissions}"
        }
        val parser: Parser = Parser()
        val stringBuilder: StringBuilder = StringBuilder(jsonPermissions)
        try {
            return parser.parse(stringBuilder) as JsonObject
        }
        catch(e: Exception) {
            throw InvalidArgumentValueException("Error parsing json permissions: ${e.message}. Evaluated permissions: $permissions")
        }
    }

    open protected fun checkAllowBypass(noBypass: Any, context: Map<String, Any>): Boolean {
        if(noBypass is Boolean) {
            return !noBypass
        }
        if(noBypass is String) {
            if(noBypass.toUpperCase() != "TRUE" && noBypass.toUpperCase() != "FALSE") {
                throw InvalidArgumentValueException("The NO_BYPASS value must be a Boolean, a boolean string or a com.beust.klaxon.JsonObject. Current value: $noBypass")
            }
            return !noBypass.toBoolean()
        }
        if(noBypass is JsonObject) {
            try {
                return !this.processOR(permissions = noBypass, context = context, type = "")
            }
            catch(e: MutableException) {
                e.message = "Error checking NO_BYPASS permissions: ${e.message}"
                throw e
            }
        }

        throw InvalidArgumentValueException("The NO_BYPASS value must be a Boolean, a boolean string or a com.beust.klaxon.JsonObject. Current value: $noBypass")
    }

    open protected fun checkBypassAccess(context: Map<String, Any>): Boolean {
        if(this.bypassCallback == null) {
            return false
        }

        return this.bypassCallback?.invoke(context) ?: false
    }

    open protected fun dispatch(permissions: Any, context: Map<String, Any>, type: String = ""): Boolean {
        var type = type

        if(permissions is Boolean) {
            if(type.isNotEmpty()) {
                throw InvalidArgumentValueException("You cannot put a boolean permission as a descendant to a permission type. Existing type: $type. Evaluated permissions: $permissions")
            }

            return permissions
        }

        if(permissions is String) {
            if(permissions.toUpperCase() == "TRUE") {
                if(type.isNotEmpty()) {
                    throw InvalidArgumentValueException("You cannot put a boolean permission as a descendant to a permission type. Existing type: $type. Evaluated permissions: $permissions")
                }

                return true
            }
            else if(permissions.toUpperCase() == "FALSE") {
                if(type.isNotEmpty()) {
                    throw InvalidArgumentValueException("You cannot put a boolean permission as a descendant to a permission type. Existing type: $type. Evaluated permissions: $permissions")
                }

                return false
            }

            return this.externalAccessCheck(permission = permissions, context = context, type = type)
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
                if(value == null) {
                    return false
                }
                if(key.toIntOrNull() == null) {
                    val keyUpper = key.toUpperCase()
                    if(keyUpper == "NO_BYPASS") {
                        throw InvalidArgumentValueException("The NO_BYPASS key must be placed highest in the permission hierarchy. Evaluated permissions: $permissions")
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
                        throw InvalidArgumentValueException("A Boolean permission cannot have children. Evaluated permissions: $permissions")
                    }

                    if(type.isNotEmpty()) {
                        throw InvalidArgumentValueException("You cannot put a permission type as a descendant to another permission type. Existing type: $type. Evaluated permissions: $permissions")
                    }
                    if(!this.typeExists(key)) {
                        throw PermissionTypeNotRegisteredException("The permission type \"$key\" has not been registered. Please use LogicalPermissions::addType() or set the value for LogicalPermission::types to register permission types.")
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

        throw InvalidArgumentValueException("A permission value must either be a Boolean, a String, a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Class is ${permissions::class}. Evaluated permissions: $permissions")
    }

    open protected fun processAND(permissions: Any, context: Map<String, Any>, type: String): Boolean {
        if(permissions is JsonArray<*>) {
            if(permissions.size < 1) {
                throw InvalidValueForLogicGateException("The value of an AND gate must contain a minimum of one element. Current value: $permissions")
            }

            var access = true
            for(item in permissions) {
                if(item == null) {
                    continue
                }
                access = access && this.dispatch(permissions = item, context = context, type = type)
                if(!access) {
                    break
                }
            }

            return access
        }

        if(permissions is JsonObject) {
            if(permissions.size < 1) {
                throw InvalidValueForLogicGateException("The value of an AND gate must contain a minimum of one element. Current value: $permissions")
            }

            var access= true
            for(item in permissions) {
                val subPermissions = json {obj(item.key to item.value)}
                access = access && this.dispatch(permissions = subPermissions, context = context, type = type)
                if(!access) {
                    break
                }
            }

            return access
        }

        throw InvalidValueForLogicGateException("The value of an AND gate must be a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Current value: $permissions")
    }

    open protected fun processNAND(permissions: Any, context: Map<String, Any>, type: String): Boolean {
        if(permissions is JsonArray<*>) {
            if(permissions.size < 1) {
                throw InvalidValueForLogicGateException("The value of a NAND gate must contain a minimum of one element. Current value: $permissions")
            }
        }
        else if(permissions is JsonObject) {
            if(permissions.size < 1) {
                throw InvalidValueForLogicGateException("The value of a NAND gate must contain a minimum of one element. Current value: $permissions")
            }
        }
        else {
            throw InvalidValueForLogicGateException("The value of a NAND gate must be a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Current value: $permissions")
        }

        return !this.processAND(permissions = permissions, context = context, type = type)
    }

    open protected fun processOR(permissions: Any, context: Map<String, Any>, type: String): Boolean {
        if(permissions is JsonArray<*>) {
            if(permissions.size < 1) {
                throw InvalidValueForLogicGateException("The value of an OR gate must contain a minimum of one element. Current value: $permissions")
            }

            var access = false
            for(item in permissions) {
                if(item == null) {
                    continue
                }
                access = access || this.dispatch(permissions = item, context = context, type = type)
                if(access) {
                    break
                }
            }

            return access
        }

        if(permissions is JsonObject) {
            if(permissions.size < 1) {
                throw InvalidValueForLogicGateException("The value of an OR gate must contain a minimum of one element. Current value: $permissions")
            }
            
            var access = false
            for(item in permissions) {
                val subPermissions = json {obj(item.key to item.value)}
                access = access || this.dispatch(permissions = subPermissions, context = context, type = type)
                if(access) {
                    break
                }
            }

            return access
        }

        throw InvalidValueForLogicGateException("The value of an OR gate must be a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Current value: $permissions")
    }

    open protected fun processNOR(permissions: Any, context: Map<String, Any>, type: String): Boolean {
        if(permissions is JsonArray<*>) {
            if(permissions.size < 1) {
                throw InvalidValueForLogicGateException("The value of a NOR gate must contain a minimum of one element. Current value: $permissions")
            }
        }
        else if(permissions is JsonObject) {
            if(permissions.size < 1) {
                throw InvalidValueForLogicGateException("The value of a NOR gate must contain a minimum of one element. Current value: $permissions")
            }
        }
        else {
            throw InvalidValueForLogicGateException("The value of a NOR gate must be a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Current value: $permissions")
        }

        return !this.processOR(permissions = permissions, context = context, type = type)
    }

    open protected fun processXOR(permissions: Any, context: Map<String, Any>, type: String): Boolean {
        if(permissions is JsonArray<*>) {
            if(permissions.size < 2) {
                throw InvalidValueForLogicGateException("The value of an XOR gate must contain a minimum of two elements. Current value: $permissions")
            }

            var access = false
            var countTrue = 0
            var countFalse = 0
            for(item in permissions) {
                if(item == null) {
                    continue
                }

                val result = this.dispatch(permissions = item, context = context, type = type)
                if(result) {
                    countTrue++
                }
                else {
                    countFalse++
                }

                if(countTrue > 0 && countFalse > 0) {
                    access = true
                    break
                }
            }

            return access
        }

        if(permissions is JsonObject) {
            if(permissions.size < 2) {
                throw InvalidValueForLogicGateException("The value of an XOR gate must contain a minimum of two elements. Current value: $permissions")
            }

            var access = false
            var countTrue = 0
            var countFalse = 0
            for(item in permissions) {
                val subPermissions = json {obj(item.key to item.value)}
                val result = this.dispatch(permissions = subPermissions, context = context, type = type)
                if(result) {
                    countTrue++
                }
                else {
                    countFalse++
                }

                if(countTrue > 0 && countFalse > 0) {
                    access = true
                    break
                }
            }

            return access
        }

        throw InvalidValueForLogicGateException("The value of an XOR gate must be a com.beust.klaxon.JsonArray or a com.beust.klaxon.JsonObject. Current value: $permissions")
    }

    open protected fun processNOT(permissions: Any, context: Map<String, Any>, type: String): Boolean {
        if(permissions is JsonObject) {
            if(permissions.size != 1) {
                throw InvalidValueForLogicGateException("The value of a NOT gate must have exactly one child in the value object. Current value: $permissions")
            }
        }
        else if(permissions is String) {
            if(permissions.isEmpty()) {
                throw InvalidValueForLogicGateException("A NOT permission cannot have an empty string as its value.")
            }
        }
        else {
            throw InvalidValueForLogicGateException("The value of a NOT gate must be a com.beust.klaxon.JsonObject or a String. Current value: $permissions")
        }

        return !this.dispatch(permissions = permissions, context = context, type = type)
    }

    open protected fun externalAccessCheck(permission: String, context: Map<String, Any>, type: String): Boolean {
        if(!this.typeExists(type)) {
            throw PermissionTypeNotRegisteredException("The permission type \"$type\" has not been registered. Please use LogicalPermissions::addType() or set the value for LogicalPermission::types to register permission types.")
        }

        val callback = this.getTypeCallback(type)

        return callback(permission, context)
    }
}