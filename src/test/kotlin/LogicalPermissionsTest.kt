import org.junit.Assert
import org.junit.Test

class LogicalPermissionsTest {
    @Test fun testCreation() {
        val lp = LogicalPermissions()
        Assert.assertTrue(lp is LogicalPermissionsInterface)
    }

    /*-----------LogicalPermissions::AddType()-------------*/

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

    /*-----------LogicalPermissions::RemoveType()-------------*/

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

    /*-------------LogicalPermissions::TypeExists()--------------*/

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

    /*-------------LogicalPermissions::GetTypeCallback()--------------*/

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

    /*-------------LogicalPermissions::SetTypeCallback()--------------*/

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
        Assert.assertEquals(lp.types, mapOf<String, (String, Map<String, Any>) -> Boolean>())

        val type_callback = {_: String, _: Map<String, Any> -> true}
        lp.addType("test", type_callback)
        Assert.assertEquals(lp.types, mapOf("test" to type_callback))
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
        Assert.assertEquals(lp.types, types)
    }

    /*-------------LogicalPermissions::GetBypassCallback()--------------*/

    @Test fun getBypassCallback() {
        val lp = LogicalPermissions()
        Assert.assertNull(lp.bypassCallback)
    }

    @Test fun setBypassCallback() {
        val lp = LogicalPermissions()
        val bypass_callback = {_: Map<String, Any> -> true}
        lp.bypassCallback = bypass_callback
        Assert.assertNotNull(lp.bypassCallback)
        Assert.assertEquals(lp.bypassCallback, bypass_callback)
    }
}