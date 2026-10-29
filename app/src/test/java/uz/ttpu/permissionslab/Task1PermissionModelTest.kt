package uz.ttpu.permissionslab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Task1PermissionModelTest {

    @Test
    fun testDeniedTwiceFlow() {
        val model = PermissionModel()

        // Boshlang'ich holat
        assertFalse(model.granted)
        assertFalse(model.rationale)
        assertTrue(model.dialogShows)

        // 1-so'rov (rad etildi)
        model.request(userAllows = false)
        assertFalse(model.granted)
        assertTrue(model.rationale) // 1 marta rad etilgach rationale true bo'lishi kerak
        assertTrue(model.dialogShows)

        // 2-so'rov (yana rad etildi)
        model.request(userAllows = false)
        assertFalse(model.granted)
        assertFalse(model.rationale) // 2 marta rad etilgach rationale false bo'lishi kerak
        assertFalse(model.dialogShows) // dialogShows false bo'ladi ("Denied twice")

        // 3-so'rov (dialog ko'rinmaydi)
        model.request(userAllows = false)
        assertFalse(model.granted)
        assertFalse(model.rationale)
        assertFalse(model.dialogShows)
    }

    @Test
    fun testGrantFlow() {
        val model = PermissionModel()
        model.request(userAllows = true)
        assertTrue(model.granted)
        assertFalse(model.rationale)
        assertFalse(model.dialogShows)
    }

    @Test
    fun testMainRunner() {
        main()
    }
}
