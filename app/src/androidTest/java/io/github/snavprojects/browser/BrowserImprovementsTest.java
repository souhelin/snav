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
    @Test public void responseStreamIsSavedAndReportsProgress() throws Exception {
        org.junit.Assume.assumeTrue(android.os.Build.VERSION.SDK_INT >= 29);
        android.app.Instrumentation instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation();
        android.content.Context context = instrumentation.getTargetContext();
        if (android.os.Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            instrumentation.getUiAutomation().grantRuntimePermission(context.getPackageName(), android.Manifest.permission.POST_NOTIFICATIONS);
        }
        String name = "snav-test-" + System.nanoTime() + ".txt";
        byte[] bytes = "conteudo de teste".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        java.io.InputStream stream = new java.io.InputStream() {
            boolean sent;
            @Override public int read() throws java.io.IOException { throw new java.io.IOException("unexpected single-byte read"); }
            @Override public int read(byte[] b, int off, int len) throws java.io.IOException {
                if (!sent) { sent = true; System.arraycopy(bytes, 0, b, off, bytes.length); return bytes.length; }
                started.countDown();
                try { if (!release.await(15, java.util.concurrent.TimeUnit.SECONDS)) throw new java.io.IOException("timed out"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new java.io.IOException(e); }
                return -1;
            }
        };
        android.net.Uri saved = null;
        try (androidx.test.core.app.ActivityScenario<MainActivity> scenario = androidx.test.core.app.ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                org.mozilla.geckoview.WebResponse response = new org.mozilla.geckoview.WebResponse.Builder("https://example.com/test.txt")
                    .isSecure(true).statusCode(200).header("Content-Length", Integer.toString(bytes.length))
                    .header("Content-Type", "text/plain").body(stream).build();
                try {
                    java.lang.reflect.Method save = MainActivity.class.getDeclaredMethod("saveGeckoDownload", org.mozilla.geckoview.WebResponse.class, String.class);
                    save.setAccessible(true); save.invoke(activity, response, name);
                } catch (Exception e) { throw new AssertionError(e); }
            });
            assertTrue("O download deve começar", started.await(15, java.util.concurrent.TimeUnit.SECONDS));
            android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            boolean found = false;
            for (android.service.notification.StatusBarNotification item : manager.getActiveNotifications()) {
                if (name.contentEquals(item.getNotification().extras.getCharSequence(android.app.Notification.EXTRA_TEXT, ""))) found = true;
            }
            assertTrue("O download em andamento deve ter uma notificação", found);
            release.countDown();
            long deadline = android.os.SystemClock.uptimeMillis() + 15000;
            while (saved == null && android.os.SystemClock.uptimeMillis() < deadline) {
                try (android.database.Cursor cursor = context.getContentResolver().query(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        new String[]{android.provider.MediaStore.MediaColumns._ID},
                        android.provider.MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " + android.provider.MediaStore.MediaColumns.IS_PENDING + "=0",
                        new String[]{name}, null)) {
                    if (cursor != null && cursor.moveToFirst()) saved = android.content.ContentUris.withAppendedId(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, cursor.getLong(0));
                }
                if (saved == null) android.os.SystemClock.sleep(100);
            }
            assertNotNull("O arquivo deve estar disponível em Downloads", saved);
            try (java.io.InputStream in = context.getContentResolver().openInputStream(saved)) {
                assertArrayEquals(bytes, in.readAllBytes());
            }
        } finally {
            release.countDown();
            if (saved != null) context.getContentResolver().delete(saved, null, null);
        }
    }
}
