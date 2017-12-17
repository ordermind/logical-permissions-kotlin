import org.junit.Assert
import org.junit.Test

class LogicalPermissionsTest {
    @Test fun testCreation() {
        val lp = LogicalPermissions()
        Assert.assertTrue(lp is LogicalPermissionsInterface)
    }

    /*-----------LogicalPermissions::addType()-------------*/

    @Test(expected = IllegalArgumentException::class) fun testAddTypeParamNameEmpty() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("", type_callback)
    }

    @Test(expected = IllegalArgumentException::class) fun TestAddTypeParamNameIsCoreKey() {
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
        Assert.assertTrue(lp.typeExists("test"))
    }

    /*-----------LogicalPermissions::removeType()-------------*/

    @Test(expected = IllegalArgumentException::class) fun testRemoveTypeParamNameEmpty() {
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
        Assert.assertTrue(lp.typeExists("test"))
        lp.removeType("test")
        Assert.assertFalse(lp.typeExists("test"))
    }

    /*-------------LogicalPermissions::typeExists()--------------*/

    @Test(expected = IllegalArgumentException::class) fun testTypeExistsParamNameEmpty() {
        val lp = LogicalPermissions()
        lp.typeExists("")
    }

    @Test fun testTypeExists() {
        val lp = LogicalPermissions()
        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", type_callback)
        Assert.assertTrue(lp.typeExists("test"))
    }

    /*-------------LogicalPermissions::getTypeCallback()--------------*/

    @Test(expected = IllegalArgumentException::class) fun testGetTypeCallbackParamNameEmpty() {
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
        Assert.assertSame(callback1, callback2)
    }

    /*-------------LogicalPermissions::setTypeCallback()--------------*/

    @Test(expected = IllegalArgumentException::class) fun testSetTypeCallbackParamNameEmpty() {
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
        Assert.assertNotSame(callback1, callback2)

        lp.setTypeCallback("test", callback1)
        val callback3 = lp.getTypeCallback("test")
        Assert.assertSame(callback1, callback3)
    }

    /*-------------LogicalPermissions::types--------------*/

    @Test fun testGetTypes() {
        val lp = LogicalPermissions()
        // Assert empty map
        Assert.assertEquals(mapOf<String, (String, Map<String, Any>) -> Boolean>(), lp.types)

        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", type_callback)
        Assert.assertEquals(mapOf("test" to type_callback), lp.types)
    }

    @Test(expected = IllegalArgumentException::class) fun testSetTypesParamNameEmpty() {
        val lp = LogicalPermissions()
        val types = mapOf("" to {_: String, _: Map<String, Any> -> true})
        lp.types = types
    }

    @Test(expected = IllegalArgumentException::class) fun testSetTypesParamTypesNameIsCoreKey() {
        val lp = LogicalPermissions()
        val types = mapOf("and" to {_: String, _: Map<String, Any> -> true})
        lp.types = types
    }

    @Test fun testSetTypes() {
        val lp = LogicalPermissions()
        val types = mapOf("test" to {_: String, _: Map<String, Any> -> true})
        lp.types = types
        Assert.assertEquals(types, lp.types)
    }

    /*-------------LogicalPermissions::getBypassCallback()--------------*/

    @Test fun getBypassCallback() {
        val lp = LogicalPermissions()
        Assert.assertNull(lp.bypassCallback)
    }

    @Test fun setBypassCallback() {
        val lp = LogicalPermissions()
        val bypass_callback = {_: Map<String, Any> -> true}
        lp.bypassCallback = bypass_callback
        Assert.assertNotNull(lp.bypassCallback)
        Assert.assertEquals(bypass_callback, lp.bypassCallback)
    }

    /*------------LogicalPermissions::getValidPermissionKeys()------------*/

    @Test fun testGetValidPermissionKeys() {
        val lp = LogicalPermissions()
        val keys = lp.getValidPermissionKeys()
        Assert.assertEquals(setOf("NO_BYPASS", "AND", "NAND", "OR", "NOR", "XOR", "NOT", "TRUE", "FALSE"), keys)

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
        Assert.assertEquals(setOf("NO_BYPASS", "AND", "NAND", "OR", "NOR", "XOR", "NOT", "TRUE", "FALSE", "flag", "role", "misc"), keys2)
    }
}