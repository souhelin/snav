package io.github.souhelin.snav;

import static org.junit.Assert.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BrowserChromeRegressionTest {
    private static Object field(Object target, String name) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception error) { throw new AssertionError(error); }
    }

    private static void screen(MainActivity activity, int screen) {
        try {
            java.lang.reflect.Method method = MainActivity.class.getDeclaredMethod("showScreen", int.class);
            method.setAccessible(true);
            method.invoke(activity, screen);
        } catch (Exception error) { throw new AssertionError(error); }
    }

    @Test public void homeAndToolsDeactivateSessionWithoutClosingIt() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                GeckoEngineController engine = (GeckoEngineController) field(activity, "geckoEngine");
                org.mozilla.geckoview.GeckoSession original = engine.getSession();
                screen(activity, 2);
                assertEquals(true, field(engine, "active"));
                screen(activity, 1);
                assertEquals(false, field(engine, "active"));
                assertSame(original, engine.getSession());
                assertTrue(original.isOpen());
                screen(activity, 3);
                assertEquals(false, field(engine, "active"));
                screen(activity, 2);
                assertEquals(true, field(engine, "active"));
            });
        }
    }

    @Test public void addressBarDoesNotLeaveSpaceWhenHidden() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                BrowserToolbarController toolbar = (BrowserToolbarController) field(activity, "browserToolbar");
                View bar = activity.findViewById(R.id.linear_b);
                View content = activity.findViewById(R.id.linear1);
                screen(activity, 2);
                assertTrue(content.getLayoutParams() instanceof FrameLayout.LayoutParams);
                int height = bar.getLayoutParams().height;
                assertEquals(height, ((FrameLayout.LayoutParams) content.getLayoutParams()).topMargin);
                toolbar.setVisible(false, false);
                assertEquals(0, ((FrameLayout.LayoutParams) content.getLayoutParams()).topMargin);
                assertEquals(View.INVISIBLE, bar.getVisibility());
                toolbar.setVisible(true, false);
                assertEquals(View.VISIBLE, bar.getVisibility());
                assertEquals(height, ((FrameLayout.LayoutParams) content.getLayoutParams()).topMargin);
                screen(activity, 1);
                assertEquals(0, ((FrameLayout.LayoutParams) content.getLayoutParams()).topMargin);
            });
        }
    }

    private static boolean hasDivider(View view) {
        if (view instanceof com.google.android.material.divider.MaterialDivider) return true;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) if (hasDivider(group.getChildAt(i))) return true;
        }
        return false;
    }

    @Test public void toolsHaveNoDividersAndEngineHasRealPromptDelegate() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertFalse(hasDivider(activity.findViewById(R.id.tela3)));
                GeckoEngineController engine = (GeckoEngineController) field(activity, "geckoEngine");
                assertTrue(engine.getSession().getPromptDelegate() instanceof GeckoPromptController);
                assertNotNull(engine.getSession().getPermissionDelegate());
                assertEquals("io.github.souhelin.snav", activity.getPackageName());
            });
        }
    }
}
