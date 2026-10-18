package com.doseyenc.wavelift.engine

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpdateManagerTest {

    @Test
    fun newerVersionIsDetected() {
        assertTrue(UpdateManager.isNewer("1.1.2", "1.1.1"))
        assertTrue(UpdateManager.isNewer("1.2.0", "1.1.9"))
        assertTrue(UpdateManager.isNewer("2.0.0", "1.99.99"))
    }

    @Test
    fun comparesNumericallyNotAlphabetically() {
        assertTrue(UpdateManager.isNewer("1.10.0", "1.9.0"))
        assertFalse(UpdateManager.isNewer("1.9.0", "1.10.0"))
    }

    @Test
    fun sameOrOlderVersionIsNotNewer() {
        assertFalse(UpdateManager.isNewer("1.1.1", "1.1.1"))
        assertFalse(UpdateManager.isNewer("1.1.0", "1.1.1"))
        assertFalse(UpdateManager.isNewer("1.1", "1.1.0"))
    }

    @Test
    fun ignoresTagPrefixAndSuffix() {
        assertTrue(UpdateManager.isNewer("v1.2.0", "1.1.1"))
        assertFalse(UpdateManager.isNewer("1.1.1-beta", "1.1.1"))
    }
}
