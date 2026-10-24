package io.github.humayanx.lastdrop.ui.screens.setup

import org.junit.Assert.assertEquals
import org.junit.Test

class SetupGateTest {
    @Test fun firstSetupRequiresEveryStep() {
        for (mask in 0..15) {
            assertEquals("Permission combination $mask", mask == 15,
                canFinishSetup(mask and 1 != 0, mask and 2 != 0, mask and 4 != 0, mask and 8 != 0))
        }
    }
}
