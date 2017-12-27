import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
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
        val callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("", callback)
    }

    @Test(expected = InvalidArgumentValueException::class) fun TestAddTypeParamNameIsCoreKey() {
        val lp = LogicalPermissions()
        val callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("and", callback)
    }

    @Test(expected = PermissionTypeAlreadyExistsException::class) fun testAddTypeParamNameExists() {
        val lp = LogicalPermissions()
        val callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", callback)
        lp.addType("test", callback)
    }

    @Test fun testAddType() {
        val lp = LogicalPermissions()
        val callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", callback)
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
        val callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", callback)
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
        val callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", callback)
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
        val callback = {_: String, _: Map<String, Any> -> true}
        lp.setTypeCallback("", callback)
    }

    @Test(expected = PermissionTypeNotRegisteredException::class) fun testSetTypeCallbackUnregisteredType() {
        val lp = LogicalPermissions()
        val callback = {_: String, _: Map<String, Any> -> true}
        lp.setTypeCallback("test", callback)
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

        val callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", callback)
        assertEquals(mapOf("test" to callback), lp.types)
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
        lp.addType("flag", {_: String, _: Map<String, Any> -> true})

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

        val strPermissions =
        """
            "flag": "testflag"
        """
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(strPermissions, mapOf<String, Any>())
        }
    }

    @Test fun testCheckAccessParamPermissionsNestedTypes() {
        val lp = LogicalPermissions()
        lp.addType("flag", {_: String, _: Map<String, Any> -> true})

        //Directly nested
        var permissions =
        """
        {
            "flag": {
                "flag": "testflag"
            }
        }
        """
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf<String, Any>())
        }

        //Indirectly nested
        permissions =
        """
        {
            "flag": {
                "OR": {
                    "flag": "testflag"
                }
            }
        }
        """
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf<String, Any>())
        }
    }

    @Test(expected = PermissionTypeNotRegisteredException::class) fun testCheckAccessParamPermissionsUnregisteredType() {
        val lp = LogicalPermissions()
        val permissions =
        """
        {
            "flag": "testflag"
        }
        """
        lp.checkAccess(permissions, mapOf<String, Any>())
    }

    @Test fun testCheckAccessEmptyMapAllow() {
        val lp = LogicalPermissions()
        assertTrue(lp.checkAccess(JsonObject(), mapOf<String, Any>()))
    }

    @Test fun testCheckAccessBypassAccessCheckContextPassing() {
        val lp = LogicalPermissions()
        val user = mapOf<String, Any>("id" to 1)
        val bypassCallback = fun(context: Map<String, Any>): Boolean {
            assertTrue(context.containsKey("user"))
            assertEquals(user, context["user"])
            return true
        }
        lp.bypassCallback = bypassCallback
        lp.checkAccess(false, mapOf<String, Any>("user" to user))
    }

    @Test(expected = InvalidArgumentValueException::class) fun testCheckAccessBypassAccessIllegalDescendant() {
        val lp = LogicalPermissions()
        val permissions = json {obj("OR" to obj("no_bypass" to true))}
        lp.checkAccess(permissions, mapOf<String, Any>())
    }

    @Test fun testCheckAccessBypassAccessAllow() {
        val lp = LogicalPermissions()
        val bypassCallback = {_: Map<String, Any> -> true}
        lp.bypassCallback = bypassCallback
        assertTrue(lp.checkAccess(false, mapOf<String, Any>()))
    }

    @Test fun testCheckAccessBypassAccessDeny() {
        val lp = LogicalPermissions()
        val bypassCallback = {_: Map<String, Any> -> false}
        lp.bypassCallback = bypassCallback
        assertFalse(lp.checkAccess(false, mapOf<String, Any>()))
    }

    @Test fun testCheckAccessBypassAccessDeny2() {
        val lp = LogicalPermissions()
        val bypassCallback = {_: Map<String, Any> -> true}
        lp.bypassCallback = bypassCallback
        assertFalse(lp.checkAccess(false, mapOf<String, Any>(), false))
    }

    @Test(expected = InvalidArgumentValueException::class) fun testCheckAccessNoBypassWrongType() {
        val lp = LogicalPermissions()
        val bypassCallback = {_: Map<String, Any> -> true}
        lp.bypassCallback = bypassCallback
        lp.checkAccess(json {obj("no_bypass" to array("test"))}, mapOf<String, Any>())
    }

    @Test fun testCheckAccessNoBypassEmptyPermissionsAllow() {
        val lp = LogicalPermissions()
        assertTrue(lp.checkAccess(json{obj("no_bypass" to true)}, mapOf<String, Any>()))
    }

    @Test(expected = InvalidArgumentValueException::class) fun testCheckAccessNoBypassWrongValue() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}
        lp.addType("test", {_: String, _: Map<String, Any> -> true})
        val permissions = json{obj(
            "NO_BYPASS" to obj(
                "test" to true
            )
        )}
        lp.checkAccess(permissions, mapOf<String, Any>())
    }

    @Test fun testCheckAccessNoBypassAccessBooleanAllow() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}
        val permissions = json{obj(
            "NO_BYPASS" to false
        )}
        assertTrue(lp.checkAccess(permissions, mapOf<String, Any>()))
        //Test that permission object is not changed
        assertTrue(permissions.containsKey("NO_BYPASS"))
    }

    @Test fun testCheckAccessNoBypassAccessBooleanDeny() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}
        val permissions = json{obj(
            "no_bypass" to true,
            "0" to false
        )}
        assertFalse(lp.checkAccess(permissions, mapOf<String, Any>()))
    }

    @Test fun testCheckAccessNoBypassAccessStringAllow() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}
        val permissions = json{obj(
            "no_bypass" to "False"
        )}
        assertTrue(lp.checkAccess(permissions, mapOf<String, Any>()))
        //Test that permission object is not changed
        assertTrue(permissions.containsKey("no_bypass"))
    }

    @Test fun testCheckAccessNoBypassAccessStringDeny() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}
        val permissions = json{obj(
            "no_bypass" to "True",
            "0" to "FALSE"
        )}
        assertFalse(lp.checkAccess(permissions, mapOf<String, Any>()))
    }

    @Test fun testCheckAccessNoBypassAccessMapAllow() {
        val lp = LogicalPermissions()
        val types = mapOf(
        "flag" to fun(flag: String, context: Map<String, Any>): Boolean {
                if(flag != "never_bypass") return false
                if(!context.containsKey("user")) return false

                val user = context["user"]
                if(user == null) return false
                if(user !is Map<*, *>) return false

                val never_bypass = user["never_bypass"]
                if(never_bypass == null) return false
                if(never_bypass !is Boolean) return false

                return never_bypass
            }
        )
        lp.types = types
        lp.bypassCallback = {_: Map<String, Any> -> true}
        val permissions = json{obj(
            "no_bypass" to obj(
                "flag" to "never_bypass"
            )
        )}
        val user = mapOf("id" to 1, "never_bypass" to false)
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessNoBypassAccessJSONAllow() {
        val lp = LogicalPermissions()
        val types = mapOf(
            "flag" to fun(flag: String, context: Map<String, Any>): Boolean {
                if(flag != "never_bypass") return false
                if(!context.containsKey("user")) return false

                val user = context["user"]
                if(user == null) return false
                if(user !is Map<*, *>) return false

                val never_bypass = user["never_bypass"]
                if(never_bypass == null) return false
                if(never_bypass !is Boolean) return false

                return never_bypass
            }
        )
        lp.types = types
        lp.bypassCallback = {_: Map<String, Any> -> true}
        val permissions =
        """
        {
            "no_bypass": {
                "flag": "never_bypass"
            }
        }
        """
        val user = mapOf("id" to 1, "never_bypass" to false)
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessNoBypassAccessMapDeny() {
        val lp = LogicalPermissions()
        val types = mapOf(
            "flag" to fun(flag: String, context: Map<String, Any>): Boolean {
                if(flag != "never_bypass") return false
                if(!context.containsKey("user")) return false

                val user = context["user"]
                if(user == null) return false
                if(user !is Map<*, *>) return false

                val never_bypass = user["never_bypass"]
                if(never_bypass == null) return false
                if(never_bypass !is Boolean) return false

                return never_bypass
            }
        )
        lp.types = types
        lp.bypassCallback = {_: Map<String, Any> -> true}
        val permissions = json{
            obj(
                "no_bypass" to obj(
                        "flag" to "never_bypass"
                ),
                "0" to false
            )
        }
        val user = mapOf("id" to 1, "never_bypass" to true)
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessNoBypassAccessJSONDeny() {
        val lp = LogicalPermissions()
        val types = mapOf(
            "flag" to fun(flag: String, context: Map<String, Any>): Boolean {
                if(flag != "never_bypass") return false
                if(!context.containsKey("user")) return false

                val user = context["user"]
                if(user == null) return false
                if(user !is Map<*, *>) return false

                val never_bypass = user["never_bypass"]
                if(never_bypass == null) return false
                if(never_bypass !is Boolean) return false

                return never_bypass
            }
        )
        lp.types = types
        lp.bypassCallback = {_: Map<String, Any> -> true}
        val permissions =
        """
        {
            "no_bypass": {
                "flag": "never_bypass"
            },
            "0": false
        }
        """
        val user = mapOf("id" to 1, "never_bypass" to true)
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
    }
}