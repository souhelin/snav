package io.github.snavprojects.browser;

import static org.junit.Assert.*;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BrowserImprovementsTest {
    @Test public void extensionOptionsMustBelongToItsOwnOrigin() {
        String base = "moz-extension://abcdef/";
        assertTrue(ExtensionManager.isOwnOptionsPage(base, "moz-extension://abcdef/settings.html"));
        assertFalse(ExtensionManager.isOwnOptionsPage(base, "moz-extension://other/settings.html"));
        assertFalse(ExtensionManager.isOwnOptionsPage(base, "https://example.com/settings"));
        assertFalse(ExtensionManager.isOwnOptionsPage(base, "moz-extension://user@abcdef/settings"));
        assertFalse(ExtensionManager.isOwnOptionsPage(base, null));
    }

    @Test public void wallpapersHaveDistinctImagesAndAttribution() {
        Set<String> images = new HashSet<>();
        Set<String> pages = new HashSet<>();
        for (UnsplashWallpaperManager.Photo photo : UnsplashWallpaperManager.PHOTOS) {
            assertTrue(images.add(photo.imageId));
            assertTrue(pages.add(photo.pageUrl));
            assertFalse(photo.author.trim().isEmpty());
            Uri page = Uri.parse(photo.pageUrl);
            assertEquals("https", page.getScheme());
            assertEquals("unsplash.com", page.getHost());
        }
        assertTrue(images.size() > 8);
    }
    @Test public void unsupportedContentPermissionsAreNotGrantedByPolicy() {
        assertEquals("localização", MainActivity.requestableContentPermissionLabel(org.mozilla.geckoview.GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION));
        assertEquals("notificações", MainActivity.requestableContentPermissionLabel(org.mozilla.geckoview.GeckoSession.PermissionDelegate.PERMISSION_DESKTOP_NOTIFICATION));
        assertNull(MainActivity.requestableContentPermissionLabel(org.mozilla.geckoview.GeckoSession.PermissionDelegate.PERMISSION_XR));
        assertNull(MainActivity.requestableContentPermissionLabel(org.mozilla.geckoview.GeckoSession.PermissionDelegate.PERMISSION_LOCAL_NETWORK_ACCESS));
    }

    @Test public void webNotificationTargetsArePrivate() throws Exception {
        android.content.Context context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext();
        android.content.pm.PackageManager pm = context.getPackageManager();
        android.content.pm.ActivityInfo activity = pm.getActivityInfo(new android.content.ComponentName(context, WebNotificationBridge.WebNotificationActivity.class), 0);
        android.content.pm.ActivityInfo receiver = pm.getReceiverInfo(new android.content.ComponentName(context, WebNotificationBridge.WebNotificationDismissReceiver.class), 0);
        assertFalse(activity.exported);
        assertFalse(receiver.exported);
    }

    @Test public void disablingWallpaperHidesCredit() {
        android.content.Context context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext();
        android.content.SharedPreferences prefs = context.getSharedPreferences("Dados", android.content.Context.MODE_PRIVATE);
        boolean existed = prefs.contains("wallpaper_enabled"), old = prefs.getBoolean("wallpaper_enabled", true);
        prefs.edit().putBoolean("wallpaper_enabled", true).commit();
        try (androidx.test.core.app.ActivityScenario<MainActivity> scenario = androidx.test.core.app.ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                android.view.View credit = activity.findViewById(R.id.wallpaper_credit);
                credit.setVisibility(android.view.View.VISIBLE);
                com.google.android.material.materialswitch.MaterialSwitch toggle = activity.findViewById(R.id.checkbox_wallpaper);
                toggle.setChecked(false);
                assertEquals(android.view.View.GONE, credit.getVisibility());
                assertNotNull(activity.findViewById(R.id.tools_appearance));
                assertNotNull(activity.findViewById(R.id.tools_privacy));
            });
        } finally {
            if (existed) prefs.edit().putBoolean("wallpaper_enabled", old).commit();
            else prefs.edit().remove("wallpaper_enabled").commit();
        }
    }
    @Test public void webNotificationIsPostedAndClosed() {
        android.app.Instrumentation instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation();
        android.content.Context context = instrumentation.getTargetContext();
        boolean grantedForTest = android.os.Build.VERSION.SDK_INT >= 33
            && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED;
        if (grantedForTest) instrumentation.getUiAutomation().grantRuntimePermission(context.getPackageName(), android.Manifest.permission.POST_NOTIFICATIONS);
        try (androidx.test.core.app.ActivityScenario<MainActivity> scenario = androidx.test.core.app.ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                android.os.Parcel parcel = android.os.Parcel.obtain();
                parcel.writeInt(3); parcel.writeString("Aviso de teste"); parcel.writeString("snav-test");
                parcel.writeString(null); parcel.writeString("Mensagem"); parcel.writeString(null);
                parcel.writeString(null); parcel.writeString(null); parcel.writeInt(0);
                parcel.writeString("https://example.com/page"); parcel.writeInt(1); parcel.writeInt(0);
                parcel.writeIntArray(new int[0]); parcel.writeParcelableArray(new android.os.Parcelable[0], 0);
                parcel.writeString("https://example.com"); parcel.setDataPosition(0);
                org.mozilla.geckoview.WebNotification n = org.mozilla.geckoview.WebNotification.CREATOR.createFromParcel(parcel);
                parcel.recycle();
                WebNotificationBridge bridge = new WebNotificationBridge(activity);
                assertTrue("As notificações do teste devem estar habilitadas", WebNotificationBridge.areEnabled(activity));
                bridge.onShowNotification(n);
                android.app.NotificationManager manager = (android.app.NotificationManager) activity.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                boolean found = false;
                for (android.service.notification.StatusBarNotification item : manager.getActiveNotifications()) {
                    if ("https://example.com|snav-test".equals(item.getTag())) found = true;
                }
                assertTrue("A notificação deve ser exibida", found);
                bridge.onCloseNotification(n);
                for (android.service.notification.StatusBarNotification item : manager.getActiveNotifications()) {
                    assertNotEquals("https://example.com|snav-test", item.getTag());
                }
            });
        }
        // Revoking a runtime permission kills the target process on Android,
        // aborting the remaining instrumented tests. The emulator is isolated.
    }
}
