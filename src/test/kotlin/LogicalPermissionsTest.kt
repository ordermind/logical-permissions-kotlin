import com.beust.klaxon.json
import org.junit.Test
import kotlin.test.*

class LogicalPermissionsTest {
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
        val bypassCallback = {_: Map<String, Any> -> true}
        lp.bypassCallback = bypassCallback
        assertNotNull(lp.bypassCallback)
        assertEquals(bypassCallback, lp.bypassCallback)
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
        assertTrue(lp.checkAccess(json{obj()}, mapOf<String, Any>()))
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

    @Test fun testCheckAccessSingleItem() {
        val lp = LogicalPermissions()
        val types = mapOf(
            "flag" to fun(flag: String, context: Map<String, Any>): Boolean {
                if(flag == "testflag") {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val testflag = user["testflag"]
                    if (testflag == null) return false
                    if (testflag !is Boolean) return false

                    return testflag
                }
                if(flag == "never_bypass") {
                    if(!context.containsKey("user")) return false

                    val user = context["user"]
                    if(user == null) return false
                    if(user !is Map<*, *>) return false

                    val never_bypass = user["never_bypass"]
                    if(never_bypass == null) return false
                    if(never_bypass !is Boolean) return false

                    return never_bypass
                }

                return false
            }
        )
        lp.types = types
        val permissions =
        """
        {
            "no_bypass": {
                "flag": "never_bypass"
            },
            "flag": "testflag"
        }
        """
        var user = mapOf("id" to 1, "testflag" to true)

        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        user = mapOf("id" to 1)
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessMultipleTypesShorthandOR() {
        val lp = LogicalPermissions()
        val types = mapOf(
            "flag" to fun(flag: String, context: Map<String, Any>): Boolean {
                if(flag == "testflag") {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val testflag = user["testflag"]
                    if (testflag == null) return false
                    if (testflag !is Boolean) return false

                    return testflag
                }
                if(flag == "never_bypass") {
                    if(!context.containsKey("user")) return false

                    val user = context["user"]
                    if(user == null) return false
                    if(user !is Map<*, *>) return false

                    val never_bypass = user["never_bypass"]
                    if(never_bypass == null) return false
                    if(never_bypass !is Boolean) return false

                    return never_bypass
                }

                return false
            },
            "role" to fun(role: String, context: Map<String, Any>): Boolean {
                if (!context.containsKey("user")) return false

                val user = context["user"]
                if (user == null) return false
                if (user !is Map<*, *>) return false

                val roles = user["roles"]
                if (roles == null) return false
                if (roles !is Set<*>) return false

                return roles.contains(role)
            },
            "misc" to fun(item: String, context: Map<String, Any>): Boolean {
                if (!context.containsKey("user")) return false

                val user = context["user"]
                if (user == null) return false
                if (user !is Map<*, *>) return false

                val item_value = user[item]
                if (item_value == null) return false
                if (item_value !is Boolean) return false

                return item_value
            }
        )
        lp.types = types
        val permissions =
                """
        {
            "no_bypass": {
                "flag": "never_bypass"
            },
            "flag": "testflag",
            "role": "admin",
            "misc": "test"
        }
        """
        var user = mutableMapOf<String, Any>("id" to 1)

        //OR truth table
        //0 0 0
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
        //0 0 1
        user["test"] = true
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        //0 1 0
        user["test"] = false
        user["roles"] = setOf("admin")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        //0 1 1
        user["test"] = true
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        //1 0 0
        user = mutableMapOf<String, Any>("id" to 1, "testflag" to true)
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        //1 0 1
        user["test"] = true
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        //1 1 0
        user["test"] = false
        user["roles"] = setOf("admin")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        //1 1 1
        user["test"] = true
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessMultipleItemsShorthandOR() {
        val lp = LogicalPermissions()
        val types = mapOf(
            "role" to fun(role: String, context: Map<String, Any>): Boolean {
                if (!context.containsKey("user")) return false

                val user = context["user"]
                if (user == null) return false
                if (user !is Map<*, *>) return false

                val roles = user["roles"]
                if (roles == null) return false
                if (roles !is Set<*>) return false

                return roles.contains(role)
            }
        )
        lp.types = types
        val permissions = json{obj(
            "role" to array(
                "admin",
                "editor"
            )
        )}

        var user = mutableMapOf<String, Any>("id" to 1)

        //OR truth table
        //0 0
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
        user["roles"] = setOf<String>()
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
        //0 1
        user["roles"] = setOf("editor")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        //1 0
        user["roles"] = setOf("admin")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        //1 1
        user["roles"] = setOf("editor", "admin")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test(expected = InvalidValueForLogicGateException::class) fun testCheckAccessANDWrongValueType() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = json{obj(
                "role" to obj(
                    "AND" to "admin"
                )
        )}
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))
        lp.checkAccess(permissions, mapOf("user" to user))
    }

    @Test fun testCheckAccessANDTooFewElements() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))

        var permissions = json{obj(
                "role" to obj(
                        "AND" to array()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }

        permissions = json{obj(
                "role" to obj(
                        "AND" to obj()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }
    }

    @Test fun testCheckAccessMultipleItemsAND() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types

        val runTruthTable = fun(permissions: Any) {
            var user = mutableMapOf<String, Any>("id" to 1)

            //AND truth table
            //0 0 0
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            user["roles"] = setOf<String>()
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 0 1
            user["roles"] = setOf("writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 0
            user["roles"] = setOf("editor")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 1
            user["roles"] = setOf("editor", "writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 0
            user["roles"] = setOf("admin")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 1
            user["roles"] = setOf("admin", "writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 0
            user["roles"] = setOf("admin", "editor")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 1
            user["roles"] = setOf("admin", "editor", "writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        }

        var permissions = """
        {
            "role": {
                "AND": [
                    "admin",
                    "editor",
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "AND": {
                    "0": "admin",
                    "1": "editor",
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "AND": [
                    ["admin"],
                    {"0": "editor"},
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "AND": {
                    "0": ["admin"],
                    "1": {"0": "editor"},
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)
    }

    @Test(expected = InvalidValueForLogicGateException::class) fun testCheckAccessNANDWrongValueType() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = json{obj(
                "role" to obj(
                        "NAND" to "admin"
                )
        )}
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))
        lp.checkAccess(permissions, mapOf("user" to user))
    }

    @Test fun testCheckAccessNANDTooFewElements() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))

        var permissions = json{obj(
                "role" to obj(
                        "NAND" to array()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }

        permissions = json{obj(
                "role" to obj(
                        "NAND" to obj()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }
    }

    @Test fun testCheckAccessMultipleItemsNAND() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types

        val runTruthTable = fun(permissions: Any) {
            var user = mutableMapOf<String, Any>("id" to 1)

            //NAND truth table
            //0 0 0
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            user["roles"] = setOf<String>()
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 0 1
            user["roles"] = setOf("writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 0
            user["roles"] = setOf("editor")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 1
            user["roles"] = setOf("editor", "writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 0
            user["roles"] = setOf("admin")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 1
            user["roles"] = setOf("admin", "writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 0
            user["roles"] = setOf("admin", "editor")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 1
            user["roles"] = setOf("admin", "editor", "writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
        }

        var permissions = """
        {
            "role": {
                "NAND": [
                    "admin",
                    "editor",
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "NAND": {
                    "0": "admin",
                    "1": "editor",
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "NAND": [
                    ["admin"],
                    {"0": "editor"},
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "NAND": {
                    "0": ["admin"],
                    "1": {"0": "editor"},
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)
    }

    @Test(expected = InvalidValueForLogicGateException::class) fun testCheckAccessORWrongValueType() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = json{obj(
                "role" to obj(
                        "OR" to "admin"
                )
        )}
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))
        lp.checkAccess(permissions, mapOf("user" to user))
    }

    @Test fun testCheckAccessORTooFewElements() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))

        var permissions = json{obj(
                "role" to obj(
                        "OR" to array()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }

        permissions = json{obj(
                "role" to obj(
                        "OR" to obj()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }
    }

    @Test fun testCheckAccessMultipleItemsOR() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types

        val runTruthTable = fun(permissions: Any) {
            var user = mutableMapOf<String, Any>("id" to 1)

            //OR truth table
            //0 0 0
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            user["roles"] = setOf<String>()
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 0 1
            user["roles"] = setOf("writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 0
            user["roles"] = setOf("editor")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 1
            user["roles"] = setOf("editor", "writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 0
            user["roles"] = setOf("admin")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 1
            user["roles"] = setOf("admin", "writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 0
            user["roles"] = setOf("admin", "editor")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 1
            user["roles"] = setOf("admin", "editor", "writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        }

        var permissions = """
        {
            "role": {
                "OR": [
                    "admin",
                    "editor",
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "OR": {
                    "0": "admin",
                    "1": "editor",
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "OR": [
                    ["admin"],
                    {"0": "editor"},
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "OR": {
                    "0": ["admin"],
                    "1": {"0": "editor"},
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)
    }

    @Test(expected = InvalidValueForLogicGateException::class) fun testCheckAccessNORWrongValueType() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = json{obj(
                "role" to obj(
                        "NOR" to "admin"
                )
        )}
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))
        lp.checkAccess(permissions, mapOf("user" to user))
    }

    @Test fun testCheckAccessNORTooFewElements() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))

        var permissions = json{obj(
                "role" to obj(
                        "NOR" to array()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }

        permissions = json{obj(
                "role" to obj(
                        "NOR" to obj()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }
    }

    @Test fun testCheckAccessMultipleItemsNOR() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types

        val runTruthTable = fun(permissions: Any) {
            var user = mutableMapOf<String, Any>("id" to 1)

            //NOR truth table
            //0 0 0
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            user["roles"] = setOf<String>()
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 0 1
            user["roles"] = setOf("writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 0
            user["roles"] = setOf("editor")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 1
            user["roles"] = setOf("editor", "writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 0
            user["roles"] = setOf("admin")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 1
            user["roles"] = setOf("admin", "writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 0
            user["roles"] = setOf("admin", "editor")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 1
            user["roles"] = setOf("admin", "editor", "writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
        }

        var permissions = """
        {
            "role": {
                "NOR": [
                    "admin",
                    "editor",
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "NOR": {
                    "0": "admin",
                    "1": "editor",
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "NOR": [
                    ["admin"],
                    {"0": "editor"},
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "NOR": {
                    "0": ["admin"],
                    "1": {"0": "editor"},
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)
    }

    @Test(expected = InvalidValueForLogicGateException::class) fun testCheckAccessXORWrongValueType() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = json{obj(
                "role" to obj(
                        "XOR" to "admin"
                )
        )}
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))
        lp.checkAccess(permissions, mapOf("user" to user))
    }

    @Test fun testCheckAccessXORTooFewElements() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))

        var permissions = json{obj(
                "role" to obj(
                        "XOR" to array("admin")
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }

        permissions = json{obj(
                "role" to obj(
                        "XOR" to obj("0" to "admin")
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }
    }

    @Test fun testCheckAccessMultipleItemsXOR() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types

        val runTruthTable = fun(permissions: Any) {
            var user = mutableMapOf<String, Any>("id" to 1)

            //XOR truth table
            //0 0 0
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            user["roles"] = setOf<String>()
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 0 1
            user["roles"] = setOf("writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 0
            user["roles"] = setOf("editor")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //0 1 1
            user["roles"] = setOf("editor", "writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 0
            user["roles"] = setOf("admin")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 0 1
            user["roles"] = setOf("admin", "writer")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 0
            user["roles"] = setOf("admin", "editor")
            assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
            //1 1 1
            user["roles"] = setOf("admin", "editor", "writer")
            assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
        }

        var permissions = """
        {
            "role": {
                "XOR": [
                    "admin",
                    "editor",
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "XOR": {
                    "0": "admin",
                    "1": "editor",
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "XOR": [
                    ["admin"],
                    {"0": "editor"},
                    "writer"
                ]
            }
        }"""
        runTruthTable(permissions)

        permissions = """
        {
            "role": {
                "XOR": {
                    "0": ["admin"],
                    "1": {"0": "editor"},
                    "2": "writer"
                }
            }
        }"""
        runTruthTable(permissions)
    }

    @Test(expected = InvalidValueForLogicGateException::class) fun testCheckAccessNOTWrongValueType() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = json{obj(
                "role" to obj(
                        "NOT" to true
                )
        )}
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))
        lp.checkAccess(permissions, mapOf("user" to user))
    }

    @Test fun testCheckAccessNOTTooFewElements() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))

        var permissions = json{obj(
                "role" to obj(
                        "NOT" to ""
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }

        permissions = json{obj(
                "role" to obj(
                        "NOT" to obj()
                )
        )}
        assertFailsWith(InvalidValueForLogicGateException::class) {
            lp.checkAccess(permissions, mapOf("user" to user))
        }
    }

    @Test(expected = InvalidValueForLogicGateException::class) fun testCheckAccessMultipleItemsNOT() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types

        var permissions = json{obj(
                "role" to obj(
                        "NOT" to obj(
                                "0" to "admin",
                                "1" to "editor"
                        )
                )
        )}
        lp.checkAccess(permissions, mapOf())
    }

    @Test fun testCheckAccessSingleItemNOTString() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        var permissions = json{obj(
                "role" to obj(
                        "NOT" to "admin"
                )
        )}

        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin", "editor"))
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
        user.remove("roles")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        user["roles"] = setOf("editor")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessSingleItemNOTMapJSON() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types

        val permissions = json{obj(
                "role" to obj(
                        "NOT" to obj(
                                "5" to "admin"
                        )
                )
        )}

        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin", "editor"))
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))
        user.remove("roles")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
        user["roles"] = setOf("editor")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))

        val jsonPermissions = """
        {
            "role": {
                "NOT": {"5": "admin"}
            }
        }"""

        user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin", "editor"))
        assertFalse(lp.checkAccess(jsonPermissions, mapOf("user" to user)))
        user.remove("roles")
        assertTrue(lp.checkAccess(jsonPermissions, mapOf("user" to user)))
        user["roles"] = setOf("editor")
        assertTrue(lp.checkAccess(jsonPermissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessBoolTRUEIllegalDescendant() {
        val lp = LogicalPermissions()
        lp.addType("role", {_: String, _: Map<String, Any> -> true})

        val mapPermissions = json{obj(
                "role" to array(true)
        )}
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(mapPermissions, mapOf())
        }

        val jsonPermissions = """
        {
            "role": [true]
        }"""
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(jsonPermissions, mapOf())
        }
    }

    @Test fun testCheckAccessBoolTRUE() {
        val lp = LogicalPermissions()
        val permissions = true
        assertTrue(lp.checkAccess(permissions, mapOf()))
    }

    @Test fun testCheckAccessBoolTRUEArray() {
        val lp = LogicalPermissions()

        val permissions = json{array(true)}
        assertTrue(lp.checkAccess(permissions, mapOf()))

        val jsonPermissions = "[true]"
        assertTrue(lp.checkAccess(jsonPermissions, mapOf()))
    }

    @Test fun testCheckAccessBoolFALSEIllegalDescendant() {
        val lp = LogicalPermissions()
        lp.addType("role", {_: String, _: Map<String, Any> -> true})

        val permissions = json{obj(
                "role" to array(false)
        )}
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf())
        }

        val jsonPermissions = """
        {
            "role": [false]
        }"""
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(jsonPermissions, mapOf())
        }
    }
    
    @Test fun testCheckAccessBoolFALSE() {
        val lp = LogicalPermissions()
        val permissions = false
        assertFalse(lp.checkAccess(permissions, mapOf()))
    }
    
    @Test fun testCheckAccessBoolFALSEArray() {
        val lp = LogicalPermissions()

        val permissions = json{array(false)}
        assertFalse(lp.checkAccess(permissions, mapOf()))

        val jsonPermissions = "[false]"
        assertFalse(lp.checkAccess(jsonPermissions, mapOf()))
    }

    @Test fun testCheckAccessBoolFALSEBypass() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}

        val permissions = false
        assertTrue(lp.checkAccess(permissions, mapOf()))
    }

    @Test fun testCheckAccessBoolFALSENoBypass() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}

        val permissions = json{obj(
                "no_bypass" to true,
                "0" to false
        )}
        assertFalse(lp.checkAccess(permissions, mapOf()))

        val jsonPermissions = """
        {
            "no_bypass": true,
             "0": false
        }"""
        assertFalse(lp.checkAccess(jsonPermissions, mapOf()))
    }

    @Test fun testCheckAccessStringTRUEIllegalChildren() {
        val lp = LogicalPermissions()

        var permissions = json{obj(
                "TRUE" to false
        )}
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf())
        }

        permissions = json{obj(
                "TRUE" to array()
        )}
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf())
        }

        var jsonPermissions = """{
                "TRUE": false
        }"""
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(jsonPermissions, mapOf())
        }

        jsonPermissions = """{
                "TRUE": []
        }"""
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(jsonPermissions, mapOf())
        }
    }

    @Test fun testCheckAccessStringTRUEIllegalDescendant() {
        val lp = LogicalPermissions()
        lp.addType("role", {_: String, _: Map<String, Any> -> true})

        val permissions = json{obj(
                "role" to array("true")
        )}
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf())
        }

        val jsonPermissions = """{
                "role": ["TrUe"]
        }"""
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(jsonPermissions, mapOf())
        }
    }

    @Test fun testCheckAccessStringTRUE() {
        val lp = LogicalPermissions()
        val permissions = """
            TRUE
        """
        assertTrue(lp.checkAccess(permissions, mapOf()))
    }

    @Test fun testCheckAccessStringTRUEArray() {
        val lp = LogicalPermissions()

        val permissions = json{array("TRUE")}
        assertTrue(lp.checkAccess(permissions, mapOf()))

        val jsonPermissions = """
            ["TRUE"]
        """
        assertTrue(lp.checkAccess(jsonPermissions, mapOf()))
    }

    @Test fun testCheckAccessStringFALSEIllegalChildren() {
        val lp = LogicalPermissions()

        var permissions = json{obj(
                "FALSE" to true
        )}
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf())
        }

        permissions = json{obj(
                "FALSE" to array()
        )}
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf())
        }

        var jsonPermissions = """{
                "FALSE": true
        }"""
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(jsonPermissions, mapOf())
        }

        jsonPermissions = """{
                "FALSE": []
        }"""
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(jsonPermissions, mapOf())
        }
    }

    @Test fun testCheckAccessStringFALSEIllegalDescendant() {
        val lp = LogicalPermissions()
        lp.addType("role", {_: String, _: Map<String, Any> -> true})

        val permissions = json{obj(
                "role" to array("False")
        )}
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(permissions, mapOf())
        }

        val jsonPermissions = """{
                "role": ["FALSE"]
        }"""
        assertFailsWith(InvalidArgumentValueException::class) {
            lp.checkAccess(jsonPermissions, mapOf())
        }
    }

    @Test fun testCheckAccessStringFALSE() {
        val lp = LogicalPermissions()
        val permissions = """
            FALSE
        """
        assertFalse(lp.checkAccess(permissions, mapOf()))
    }

    @Test fun testCheckAccessStringFALSEArray() {
        val lp = LogicalPermissions()

        val permissions = json{array("FALSE")}
        assertFalse(lp.checkAccess(permissions, mapOf()))

        val jsonPermissions = """
            ["false"]
        """
        assertFalse(lp.checkAccess(jsonPermissions, mapOf()))
    }

    @Test fun testCheckAccessStringFALSEBypass() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}

        val permissions = "FALSE"
        assertTrue(lp.checkAccess(permissions, mapOf()))
    }

    @Test fun testCheckAccessStringFALSENoBypass() {
        val lp = LogicalPermissions()
        lp.bypassCallback = {_: Map<String, Any> -> true}

        val permissions = json{obj(
                "no_bypass" to true,
                "0" to "FALSE"
        )}
        assertFalse(lp.checkAccess(permissions, mapOf()))

        val jsonPermissions = """
        {
            "no_bypass": true,
             "0": "FALSE"
        }"""
        assertFalse(lp.checkAccess(jsonPermissions, mapOf()))
    }

    @Test fun testMixedBooleans() {
        val lp = LogicalPermissions()

        var arrayPermissions = json{array("FALSE", true)}
        assertTrue(lp.checkAccess(arrayPermissions, mapOf()))

        var jsonPermissions = """
        [
            "FALSE",
            true
        ]
        """
        assertTrue(lp.checkAccess(jsonPermissions, mapOf()))

        var permissions = json{obj(
                "OR" to array(
                        false,
                        "TRUE"
                )
        )}
        assertTrue(lp.checkAccess(permissions, mapOf()))

        jsonPermissions = """
        {
            "OR": [
              false,
              "TRUE"
            ]
        }
        """
        assertTrue(lp.checkAccess(jsonPermissions, mapOf()))

        permissions = json{obj(
                "AND" to array(
                        "TRUE",
                        false
                )
        )}
        assertFalse(lp.checkAccess(permissions, mapOf()))

        jsonPermissions = """
        {
            "AND": [
              "TRUE",
              false
            ]
        }
        """
        assertFalse(lp.checkAccess(jsonPermissions, mapOf()))
    }

    @Test fun testCheckAccessNestedLogic() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = """
        {
            "role": {
                "OR": {
                    "NOT": {
                        "AND": [
                            "admin",
                            "editor"
                        ]
                    }
                }
            },
            "0": false,
            "1": "FALSE"
        }
        """

        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin", "editor"))
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))

        user.remove("roles")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))

        user["roles"] = setOf("editor")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessLogicGateFirst() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = """
        {
            "AND": {
                "role": {
                    "OR": {
                        "NOT": {
                            "AND": [
                                "admin",
                                "editor"
                            ]
                        }
                    }
                },
                "0": true,
                "1": "TRUE"
            }
        }
        """

        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin", "editor"))
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))

        user.remove("roles")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))

        user["roles"] = setOf("editor")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
    }

    @Test fun testCheckAccessShorthandORMixedObjectsArrays() {
        val lp = LogicalPermissions()
        val types = mapOf(
                "role" to fun(role: String, context: Map<String, Any>): Boolean {
                    if (!context.containsKey("user")) return false

                    val user = context["user"]
                    if (user == null) return false
                    if (user !is Map<*, *>) return false

                    val roles = user["roles"]
                    if (roles == null) return false
                    if (roles !is Set<*>) return false

                    return roles.contains(role)
                }
        )
        lp.types = types
        val permissions = """
        {
            "role": [
                "admin",
                {
                    "AND": [
                        "editor",
                        "writer",
                        {
                            "OR": [
                                "role1",
                                "role2"
                            ]
                        }
                    ]
                }
            ]
        }
        """

        var user = mutableMapOf<String, Any>("id" to 1, "roles" to setOf("admin"))
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))

        user.remove("roles")
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))

        user["roles"] = setOf("editor")
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))

        user["roles"] = setOf("editor", "writer")
        assertFalse(lp.checkAccess(permissions, mapOf("user" to user)))

        user["roles"] = setOf("editor", "writer", "role1")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))

        user["roles"] = setOf("editor", "writer", "role2")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))

        user["roles"] = setOf("admin", "writer")
        assertTrue(lp.checkAccess(permissions, mapOf("user" to user)))
    }
}