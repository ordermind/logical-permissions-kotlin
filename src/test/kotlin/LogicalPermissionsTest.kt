import org.junit.Test
import kotlin.test.*

class LogicalPermissionsTest {
    @Test fun testCreation() {
        val lp = LogicalPermissions()
        assertTrue(lp is LogicalPermissionsInterface)
    }

    /*-----------LogicalPermissions::addType()-------------*/

    @Test(expected = InvalidArgumentValueException::class) fun testAddTypeParamNameEmpty() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("", type_callback)
    }

    @Test(expected = InvalidArgumentValueException::class) fun TestAddTypeParamNameIsCoreKey() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("and", type_callback)
    }

    @Test(expected = PermissionTypeAlreadyExistsException::class) fun testAddTypeParamNameExists() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", type_callback)
        lp.addType("test", type_callback)
    }

    @Test fun testAddType() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", type_callback)
        assertTrue(lp.typeExists("test"))
    }

    /*-----------LogicalPermissions::removeType()-------------*/

    @Test(expected = InvalidArgumentValueException::class) fun testRemoveTypeParamNameEmpty() {
        val lp = LogicalPermissions()
        lp.removeType("")
    }

    @Test(expected = PermissionTypeNotRegisteredException::class) fun testRemoveTypeUnregisteredType() {
        val lp = LogicalPermissions()
        lp.removeType("test")
    }

    @Test fun testRemoveType() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", type_callback)
        assertTrue(lp.typeExists("test"))
        lp.removeType("test")
        assertFalse(lp.typeExists("test"))
    }

    /*-------------LogicalPermissions::typeExists()--------------*/

    @Test(expected = InvalidArgumentValueException::class) fun testTypeExistsParamNameEmpty() {
        val lp = LogicalPermissions()
        lp.typeExists("")
    }

    @Test fun testTypeExists() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", type_callback)
        assertTrue(lp.typeExists("test"))
    }

    /*-------------LogicalPermissions::getTypeCallback()--------------*/

    @Test(expected = InvalidArgumentValueException::class) fun testGetTypeCallbackParamNameEmpty() {
        val lp = LogicalPermissions()
        lp.getTypeCallback("")
    }

    @Test(expected = PermissionTypeNotRegisteredException::class) fun testGetTypeCallbackUnregisteredType() {
        val lp = LogicalPermissions()
        lp.getTypeCallback("test")
    }

    @Test fun testGetTypeCallback() {
        val lp = LogicalPermissions()
        val callback1 = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", callback1)
        val callback2 = lp.getTypeCallback("test")
        assertSame(callback1, callback2)
    }

    /*-------------LogicalPermissions::setTypeCallback()--------------*/

    @Test(expected = InvalidArgumentValueException::class) fun testSetTypeCallbackParamNameEmpty() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.setTypeCallback("", type_callback)
    }

    @Test(expected = PermissionTypeNotRegisteredException::class) fun testSetTypeCallbackUnregisteredType() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.setTypeCallback("test", type_callback)
    }

    @Test fun testSetTypeCallback() {
        val lp = LogicalPermissions()
        lp.addType("test", {_: String, _: Map<String, Any> -> true})
        val callback1 = {_: String, _: Map<String, Any> -> true}
        val callback2 = lp.getTypeCallback("test")
        assertNotSame(callback1, callback2)

        lp.setTypeCallback("test", callback1)
        val callback3 = lp.getTypeCallback("test")
        assertSame(callback1, callback3)
    }

    /*-------------LogicalPermissions::types--------------*/

    @Test fun testGetTypes() {
        val lp = LogicalPermissions()
        // Assert empty map
        assertEquals(mapOf<String, (String, Map<String, Any>) -> Boolean>(), lp.types)

        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", type_callback)
        assertEquals(mapOf("test" to type_callback), lp.types)
    }

    @Test(expected = InvalidArgumentValueException::class) fun testSetTypesParamNameEmpty() {
        val lp = LogicalPermissions()
        val types = mapOf("" to {_: String, _: Map<String, Any> -> true})
        lp.types = types
    }

    @Test(expected = InvalidArgumentValueException::class) fun testSetTypesParamTypesNameIsCoreKey() {
        val lp = LogicalPermissions()
        val types = mapOf("and" to {_: String, _: Map<String, Any> -> true})
        lp.types = types
    }

    @Test fun testSetTypes() {
        val lp = LogicalPermissions()
        val types = mapOf("test" to {_: String, _: Map<String, Any> -> true})
        lp.types = types
        assertEquals(types, lp.types)
    }

    /*-------------LogicalPermissions::getBypassCallback()--------------*/

    @Test fun getBypassCallback() {
        val lp = LogicalPermissions()
        assertNull(lp.bypassCallback)
    }

    @Test fun setBypassCallback() {
        val lp = LogicalPermissions()
        val bypass_callback = {_: Map<String, Any> -> true}
        lp.bypassCallback = bypass_callback
        assertNotNull(lp.bypassCallback)
        assertEquals(bypass_callback, lp.bypassCallback)
    }

    /*------------LogicalPermissions::getValidPermissionKeys()------------*/

    @Test fun testGetValidPermissionKeys() {
        val lp = LogicalPermissions()
        val keys = lp.getValidPermissionKeys()
        assertEquals(setOf("NO_BYPASS", "AND", "NAND", "OR", "NOR", "XOR", "NOT", "TRUE", "FALSE"), keys)

        val types = mapOf(
            "flag" to fun(flag: String, context: Map<String, Any>): Boolean {
                if(flag != "testflag") return false
                if(!context.containsKey("user")) return false

                val user = context["user"]
                if(user == null) return false
                if(user !is Map<*, *>) return false

                val testflag = user["testflag"]
                if(testflag == null) return false
                if(testflag !is Boolean) return false

                return testflag
            },
            "role" to fun(role: String, context: Map<String, Any>): Boolean {
                if(!context.containsKey("user")) return false

                val user = context["user"]
                if(user == null) return false
                if(user !is Map<*, *>) return false

                val roles = user["roles"]
                if(roles == null) return false
                if(roles !is Set<*>) return false

                return roles.contains(role)
            },
            "misc" to fun(item: String, context: Map<String, Any>): Boolean {
                if(!context.containsKey("user")) return false

                val user = context["user"]
                if(user == null) return false
                if(user !is Map<*, *>) return false

                val item_value = user[item]
                if(item_value == null) return false
                if(item_value !is Boolean) return false

                return item_value
            }
        )
        lp.types = types
        val keys2 = lp.getValidPermissionKeys()
        assertEquals(setOf("NO_BYPASS", "AND", "NAND", "OR", "NOR", "XOR", "NOT", "TRUE", "FALSE", "flag", "role", "misc"), keys2)
    }

    /*-------------LogicalPermissions::checkAccess()--------------*/

    @Test fun testCheckAccessParamPermissionsWrongPermissionType() {
        val lp = LogicalPermissions()

        val permissions = 50
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf<String, Any>())
        }

        val intPermissions =
        """
        {
            "flag": 1
        }
        """
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(intPermissions, mapOf<String, Any>())
        }
    }
}