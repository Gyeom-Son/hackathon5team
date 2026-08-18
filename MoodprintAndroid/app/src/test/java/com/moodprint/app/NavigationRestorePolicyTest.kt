package com.moodprint.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationRestorePolicyTest {
    @Test fun completedProfile_entersHomeOnlyFromOnboardingRoutes() {
        assertTrue(shouldEnterHomeAfterProfileLoad(true, true, Screen.Welcome.name))
        assertTrue(shouldEnterHomeAfterProfileLoad(true, true, Screen.Profile.name))
        assertFalse(shouldEnterHomeAfterProfileLoad(true, true, Screen.CheckIn.name))
        assertFalse(shouldEnterHomeAfterProfileLoad(true, true, Screen.Action.name))
        assertFalse(shouldEnterHomeAfterProfileLoad(true, true, Screen.Reward.name))
    }

    @Test fun incompleteOrLoadingProfile_neverRedirects() {
        assertFalse(shouldEnterHomeAfterProfileLoad(false, true, Screen.Welcome.name))
        assertFalse(shouldEnterHomeAfterProfileLoad(true, false, Screen.Welcome.name))
    }

    @Test fun clearedProfile_returnsProtectedFlowToWelcome() {
        assertTrue(shouldReturnToWelcomeAfterDataReset(true, false, Screen.Home.name))
        assertTrue(shouldReturnToWelcomeAfterDataReset(true, false, Screen.Action.name))
        assertFalse(shouldReturnToWelcomeAfterDataReset(true, false, Screen.Welcome.name))
        assertFalse(shouldReturnToWelcomeAfterDataReset(false, false, Screen.Home.name))
    }
}
