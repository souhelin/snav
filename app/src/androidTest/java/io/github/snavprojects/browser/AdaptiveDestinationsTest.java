package io.github.snavprojects.browser;

import static org.junit.Assert.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdaptiveDestinationsTest {
    private static void invoke(MainActivity activity, String name, Class<?>[] types, Object... values) {
        try {
            java.lang.reflect.Method method = MainActivity.class.getDeclaredMethod(name, types);
            method.setAccessible(true);
            method.invoke(activity, values);
        } catch (Exception e) { throw new AssertionError(e); }
    }

    @Test public void noEmptyCardWhenBothShortcutGroupsAreDisabled() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("Dados", Context.MODE_PRIVATE);
        boolean pinned = prefs.getBoolean("home_show_pinned", true);
        boolean recent = prefs.getBoolean("home_show_recent", true);
        boolean visible = prefs.getBoolean("home_shortcuts_visible", true);
        prefs.edit().putBoolean("home_show_pinned", false).putBoolean("home_show_recent", false)
            .putBoolean("home_shortcuts_visible", true).commit();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                invoke(activity, "refreshHomeShortcuts", new Class<?>[0]);
                View card = (View) activity.findViewById(R.id.home_shortcuts).getParent();
                assertEquals(View.GONE, card.getVisibility());
                assertTrue(activity.findViewById(R.id.wallpaper_credit) instanceof com.google.android.material.button.MaterialButton);
            });
        } finally {
            prefs.edit().putBoolean("home_show_pinned", pinned).putBoolean("home_show_recent", recent)
                .putBoolean("home_shortcuts_visible", visible).commit();
        }
    }

    @Test public void destinationsRemainOpenWhenRotatedAndBackRestoresBrowser() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                invoke(activity, "showTabsBottomSheet", new Class<?>[0]);
                ViewGroup host = activity.findViewById(R.id.snav_snackbar_host);
                assertEquals(2, host.getChildCount());
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                ViewGroup host = activity.findViewById(R.id.snav_snackbar_host);
                assertEquals(2, host.getChildCount());
                activity.onBackPressed();
                assertEquals(1, host.getChildCount());
                invoke(activity, "showBrowserLibraryBottomSheet", new Class<?>[]{boolean.class}, false);
                assertEquals(2, host.getChildCount());
                activity.onBackPressed();
                invoke(activity, "showDownloadsBottomSheet", new Class<?>[0]);
                assertEquals(2, host.getChildCount());
                activity.onBackPressed();
                assertEquals(1, host.getChildCount());
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            });
        }
    }

    @Test public void supportedPermissionsMapToIndependentPreferences() {
        assertEquals("allow_autoplay_muted", MainActivity.contentPermissionPreference(org.mozilla.geckoview.GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE));
        assertEquals("allow_autoplay_sound", MainActivity.contentPermissionPreference(org.mozilla.geckoview.GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE));
        assertEquals("allow_location", MainActivity.contentPermissionPreference(org.mozilla.geckoview.GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION));
        assertNull(MainActivity.contentPermissionPreference(org.mozilla.geckoview.GeckoSession.PermissionDelegate.PERMISSION_XR));
    }
    @Test public void imageShareProviderIsPrivateAndScopedToCache() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        android.content.pm.ProviderInfo provider = context.getPackageManager().resolveContentProvider(
            context.getPackageName() + ".fileprovider", 0);
        assertNotNull(provider);
        assertFalse(provider.exported);
        assertTrue(provider.grantUriPermissions);
        java.io.File directory = new java.io.File(context.getCacheDir(), "shared_images");
        assertTrue(directory.exists() || directory.mkdirs());
        java.io.File image = java.io.File.createTempFile("test-", ".png", directory);
        try {
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider", image);
            assertEquals("content", uri.getScheme());
            assertEquals(context.getPackageName() + ".fileprovider", uri.getAuthority());
            try {
                androidx.core.content.FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider",
                    new java.io.File(context.getCacheDir(), "outside.txt"));
                fail("O provedor não deve expor todo o cache");
            } catch (IllegalArgumentException expected) { }
        } finally { image.delete(); }
    }
}
