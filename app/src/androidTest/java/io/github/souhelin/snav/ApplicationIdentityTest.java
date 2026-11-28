package io.github.souhelin.snav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ApplicationIdentityTest {
    @Test public void packageAndMainActivityUseNewIdentity() {
        assertEquals("io.github.souhelin.snav",
            InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertEquals("io.github.souhelin.snav", activity.getComponentName().getPackageName());
                assertEquals("io.github.souhelin.snav.MainActivity", activity.getComponentName().getClassName());
                assertNotNull(activity.findViewById(R.id.browser_content_container));
            });
        }
    }
}
