package io.github.souhelin.snav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class BrowserLibraryStoreTest {
    private BrowserLibraryStore store;
    private Context isolatedContext;

    @Before public void setUp() {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        isolatedContext = new ContextWrapper(target) {
            @Override public Context getApplicationContext() { return this; }
            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return super.getSharedPreferences(name + "_instrumentation_test", mode);
            }
        };
        isolatedContext.getSharedPreferences("snav_browser_library_v1", Context.MODE_PRIVATE).edit().clear().commit();
        store = new BrowserLibraryStore(isolatedContext);
    }

    @After public void tearDown() {
        isolatedContext.getSharedPreferences("snav_browser_library_v1", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test public void visitsRemainInNewestFirstOrderAcrossStoreInstances() {
        for (int i = 1; i <= 5; i++) store.addVisit("Página " + i, "https://example.com/" + i);
        BrowserLibraryStore restored = new BrowserLibraryStore(isolatedContext);
        List<BrowserLibraryStore.Entry> history = restored.getHistory();
        assertEquals(5, history.size());
        for (int i = 0; i < 5; i++) {
            assertEquals("https://example.com/" + (5 - i), history.get(i).url);
            assertEquals("Página " + (5 - i), history.get(i).title);
        }
    }

    @Test public void updatingTitleOrRevisitingPreservesOtherPages() {
        store.addVisit(null, "https://example.com/a");
        store.addVisit("B", "https://example.com/b");
        store.addVisit("A atualizado", "https://example.com/a");
        List<BrowserLibraryStore.Entry> history = store.getHistory();
        assertEquals(2, history.size());
        assertEquals("A atualizado", history.get(0).title);
        assertEquals("https://example.com/a", history.get(0).url);
        assertEquals("https://example.com/b", history.get(1).url);
    }
    @Test public void editingPinnedShortcutPreservesOrderAndRejectsDuplicates() {
        assertTrue(store.pin("Primeiro", "https://example.com/a"));
        assertTrue(store.pin("Segundo", "https://example.com/b"));
        assertFalse(store.updatePinned("https://example.com/a", "Duplicado", "https://example.com/b"));
        assertTrue(store.updatePinned("https://example.com/a", "Atualizado", "https://example.com/c"));
        List<BrowserLibraryStore.Entry> pinned = store.getPinned();
        assertEquals(2, pinned.size());
        assertEquals("Atualizado", pinned.get(0).title);
        assertEquals("https://example.com/c", pinned.get(0).url);
        assertEquals("https://example.com/b", pinned.get(1).url);
        store.unpin("https://example.com/c");
        store.unpin("https://example.com/b");
    }

}
