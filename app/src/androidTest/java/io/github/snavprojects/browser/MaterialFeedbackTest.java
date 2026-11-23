package io.github.snavprojects.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MaterialFeedbackTest {
    @Test public void snackbarDisplaysAndRunsOptionalAction() throws Exception {
        AtomicBoolean clicked = new AtomicBoolean();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> activity.showFeedback("Página desafixada", "Desfazer", () -> clicked.set(true)));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                TextView text = activity.findViewById(com.google.android.material.R.id.snackbar_text);
                TextView action = activity.findViewById(com.google.android.material.R.id.snackbar_action);
                assertNotNull(text);
                assertEquals("Página desafixada", text.getText().toString());
                assertNotNull(action);
                action.performClick();
            });
            assertTrue(clicked.get());
        }
    }

    @Test public void fallbackAndDynamicThemesUseReadableSurfaceColors() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("Dados", Context.MODE_PRIVATE);
        boolean existed = prefs.contains("dynamic_color");
        boolean old = prefs.getBoolean("dynamic_color", true);
        try {
            for (boolean dynamic : new boolean[]{false, true}) {
                prefs.edit().putBoolean("dynamic_color", dynamic).commit();
                try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
                    scenario.onActivity(activity -> {
                        int surface = MaterialColors.getColor(activity, com.google.android.material.R.attr.colorSurface, 0);
                        int text = MaterialColors.getColor(activity, com.google.android.material.R.attr.colorOnSurface, 0);
                        assertTrue("Texto deve ter contraste na superfície", ColorUtils.calculateContrast(text, surface) >= 4.5);
                        if (!dynamic || !DynamicColors.isDynamicColorAvailable()) {
                            assertEquals(activity.getColor(R.color.snav_surface), surface);
                        }
                    });
                }
            }
        } finally {
            if (existed) prefs.edit().putBoolean("dynamic_color", old).commit();
            else prefs.edit().remove("dynamic_color").commit();
        }
    }
}
