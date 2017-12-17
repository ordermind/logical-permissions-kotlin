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

}