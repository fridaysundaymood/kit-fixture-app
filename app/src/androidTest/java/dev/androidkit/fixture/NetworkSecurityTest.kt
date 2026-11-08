package dev.androidkit.fixture

import android.security.NetworkSecurityPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NetworkSecurityTest {
    @Test fun cleartextIsRestrictedToDebugLoopback() {
        val policy = NetworkSecurityPolicy.getInstance()
        assertFalse(policy.isCleartextTrafficPermitted("example.com"))
        assertEquals(BuildConfig.DEBUG, policy.isCleartextTrafficPermitted("10.0.2.2"))
        assertEquals(BuildConfig.DEBUG, policy.isCleartextTrafficPermitted("localhost"))
    }
}
