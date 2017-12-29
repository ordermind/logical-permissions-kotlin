import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

interface LogicalPermissionsInterface {
    var types: Map<String, (String, Map<String, Any>) -> Boolean>
    var bypassCallback: ((Map<String, Any>) -> Boolean)?

    fun addType(name: String, callback: (String, Map<String, Any>) -> Boolean)

    fun removeType(name: String)

    fun typeExists(name: String): Boolean

    fun getTypeCallback(name: String): (String, Map<String, Any>) -> Boolean

    fun setTypeCallback(name: String, callback: (String, Map<String, Any>) -> Boolean)

    fun getValidPermissionKeys(): Set<String>

    fun checkAccess(permissions: JsonObject, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean

    fun checkAccess(permissions: JsonArray<Any?>, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean

    fun checkAccess(permissions: String, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean

    fun checkAccess(permissions: Boolean, context: Map<String, Any> = mapOf(), allowBypass: Boolean = true): Boolean
}