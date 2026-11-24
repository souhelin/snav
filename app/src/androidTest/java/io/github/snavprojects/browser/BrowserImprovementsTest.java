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
}
