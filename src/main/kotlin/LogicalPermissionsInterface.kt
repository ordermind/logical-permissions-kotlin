import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

interface LogicalPermissionsInterface {
    /**
     * Permission types. Each item in the map consists of a permission type name and a callback. See the documentation for addType() for more information.
     */
    var types: Map<String, (String, Map<String, Any>) -> Boolean>

    /**
     * (optional) Callback for determining whether access should be bypassed. If it exists it is called in the beginning of each access check. If access should be bypassed, the regular access check is never made and instead access is automatically granted.
     */
    var bypassCallback: ((Map<String, Any>) -> Boolean)?

    /**
     * Adds a permission type.
     * @param name The name of the permission type.
     * @param callback The callback that evaluates the permission type. Upon calling checkAccess() the registered callback will be passed two parameters: a permission string (such as a role) and the context parameter passed to checkAccess(). The permission will always be a single string even if for example multiple roles are accepted. In that case the callback will be called once for each role that is to be evaluated. The callback must return a boolean which determines whether access should be granted.
     */
    fun addType(name: String, callback: (String, Map<String, Any>) -> Boolean)

    /**
     * Removes a permission type.
     * @param name The name of the permission type.
     */
    fun removeType(name: String)

    /**
     * Checks whether a permission type is registered.
     * @param name The name of the permission type.
     * @return true if the type is found or false if the type isn't found.
     */
    fun typeExists(name: String): Boolean

    /**
     * Gets the callback for a permission type.
     * @param name The name of the permission type.
     * @return Callback for the permission type.
     */
    fun getTypeCallback(name: String): (String, Map<String, Any>) -> Boolean

    /**
     * Changes the callback for an existing permission type.
     * @param name The name of the permission type.
     * @param callback The callback that evaluates the permission type. See the documentation for addType() for more information.
     */
    fun setTypeCallback(name: String, callback: (String, Map<String, Any>) -> Boolean)

    /**
     * Gets all keys that can be part of a permission tree.
     * @return Valid permission keys
     */
    fun getValidPermissionKeys(): Set<String>

    /**
     * Checks access for a permission tree.
     * @param permissions The permission tree to be evaluated. This parameter can be a com.beust.klaxon.JsonObject, a com.beust.klaxon.JsonArray, a json string or a Boolean.
     * @param context (optional) A context array that could for example contain the evaluated user and document. Default value is an empty map.
     * @param allowBypass (optional) Determines whether bypassing access should be allowed at all. Default value is true. If this parameter is set to false, access bypass will not be allowed under any circumstance. It is mostly used for testing purposes as you can also use the "NO_BYPASS" permission key to the same effect.
     * @return true if access is granted or false if access is denied.
     */
    fun checkAccess(permissions: JsonObject, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean
    /**
     * Checks access for a permission tree.
     * @param permissions The permission tree to be evaluated. This parameter can be a com.beust.klaxon.JsonObject, a com.beust.klaxon.JsonArray, a json string or a Boolean.
     * @param context (optional) A context array that could for example contain the evaluated user and document. Default value is an empty map.
     * @param allowBypass (optional) Determines whether bypassing access should be allowed at all. Default value is true. If this parameter is set to false, access bypass will not be allowed under any circumstance. It is mostly used for testing purposes as you can also use the "NO_BYPASS" permission key to the same effect.
     * @return true if access is granted or false if access is denied.
     */
    fun checkAccess(permissions: JsonArray<Any?>, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean
    /**
     * Checks access for a permission tree.
     * @param permissions The permission tree to be evaluated. This parameter can be a com.beust.klaxon.JsonObject, a com.beust.klaxon.JsonArray, a json string or a Boolean.
     * @param context (optional) A context array that could for example contain the evaluated user and document. Default value is an empty map.
     * @param allowBypass (optional) Determines whether bypassing access should be allowed at all. Default value is true. If this parameter is set to false, access bypass will not be allowed under any circumstance. It is mostly used for testing purposes as you can also use the "NO_BYPASS" permission key to the same effect.
     * @return true if access is granted or false if access is denied.
     */
    fun checkAccess(permissions: String, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean
    /**
     * Checks access for a permission tree.
     * @param permissions The permission tree to be evaluated. This parameter can be a com.beust.klaxon.JsonObject, a com.beust.klaxon.JsonArray, a json string or a Boolean.
     * @param context (optional) A context array that could for example contain the evaluated user and document. Default value is an empty map.
     * @param allowBypass (optional) Determines whether bypassing access should be allowed at all. Default value is true. If this parameter is set to false, access bypass will not be allowed under any circumstance. It is mostly used for testing purposes as you can also use the "NO_BYPASS" permission key to the same effect.
     * @return true if access is granted or false if access is denied.
     */
    fun checkAccess(permissions: Boolean, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean
}