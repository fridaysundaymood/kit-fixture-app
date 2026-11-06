package dev.androidkit.fixture

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class StateLeakTest {
    private val preferences = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("leak-fixture", Context.MODE_PRIVATE)

    @Test fun testA_leavesStateBehind() {
        preferences.edit().putBoolean("previous_test_ran", true).commit()
    }

    @Test fun testB_requiresPreviousTest() {
        assertTrue(preferences.getBoolean("previous_test_ran", false))
    }
}
