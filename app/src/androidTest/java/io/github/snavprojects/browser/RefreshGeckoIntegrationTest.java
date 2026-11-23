package io.github.snavprojects.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.geckoview.GeckoSession;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/** Exercises the real Compose/AndroidView/Gecko nested-scroll path on an emulator. */
@RunWith(AndroidJUnit4.class)
public class RefreshGeckoIntegrationTest {
    private static final Instrumentation INSTRUMENTATION = InstrumentationRegistry.getInstrumentation();

    private static final class Harness implements AutoCloseable {
        final ActivityScenario<MainActivity> scenario;
        final AtomicInteger scroll = new AtomicInteger();
        final AtomicInteger refreshes = new AtomicInteger();
        final CountDownLatch loaded = new CountDownLatch(1);
        GeckoSession session;
        RefreshAwareGeckoView browser;
        RefreshComposeHost host;
        float density;
        int[] location = new int[2];
        int width, height;

        Harness(String html) throws Exception {
            scenario = ActivityScenario.launch(MainActivity.class);
            scenario.onActivity(activity -> {
                density = activity.getResources().getDisplayMetrics().density;
                FrameLayout frame = new FrameLayout(activity);
                activity.setContentView(frame);
                browser = new RefreshAwareGeckoView(activity, null);
                frame.addView(browser, new FrameLayout.LayoutParams(-1, -1));
                session = new GeckoSession();
                session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
                    @Override public void onPageStop(GeckoSession current, boolean success) {
                        loaded.countDown();
                        if (host != null) host.setRefreshing(false);
                    }
                });
                session.setScrollDelegate(new GeckoSession.ScrollDelegate() {
                    @Override public void onScrollChanged(GeckoSession current, int x, int y) { scroll.set(y); }
                });
                session.open(GeckoEngineController.getRuntime());
                browser.setSession(session);
                browser.setEligiblePage(() -> scroll.get() <= 0 ? "fixture" : null);
                host = new RefreshComposeHost(activity, frame, browser, () -> {
                    if (browser.consumeConfirmedRefresh() != null) {
                        refreshes.incrementAndGet(); host.setRefreshing(true); session.reload();
                    }
                });
                try { session.loadUri("data:text/html;charset=utf-8," + URLEncoder.encode(html, "UTF-8").replace("+", "%20")); }
                catch (Exception error) { throw new RuntimeException(error); }
            });
            assertTrue("Gecko não carregou a página de teste", loaded.await(45, TimeUnit.SECONDS));
            INSTRUMENTATION.waitForIdleSync();
            SystemClock.sleep(800); // allow compositor and Compose to attach
            scenario.onActivity(activity -> {
                browser.getLocationOnScreen(location);
                width = browser.getWidth(); height = browser.getHeight();
            });
            assertTrue("A superfície Gecko não foi medida", width > 0 && height > 0);
        }

        void drag(float dxDp, float dyDp) { drag(dxDp, dyDp, 150); }

        void drag(float dxDp, float dyDp, long holdMillis) {
            AtomicBoolean focused = new AtomicBoolean();
            long deadline = SystemClock.uptimeMillis() + 10000;
            do {
                scenario.onActivity(activity -> focused.set(activity.hasWindowFocus()));
                if (focused.get()) break;
                SystemClock.sleep(100);
            } while (SystemClock.uptimeMillis() < deadline);
            assertTrue("A janela do navegador não recebeu foco para o gesto", focused.get());
            float x = location[0] + width * 0.5f;
            float y = location[1] + Math.max(40 * density, height * 0.25f);
            int before = refreshes.get();
            long down = SystemClock.uptimeMillis();
            send(down, down, MotionEvent.ACTION_DOWN, x, y);
            SystemClock.sleep(holdMillis); // allow Gecko's asynchronous hit-test response
            for (int i = 1; i <= 12; i++) {
                float fraction = i / 12f;
                send(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE,
                    x + dxDp * density * fraction, y + dyDp * density * fraction);
                SystemClock.sleep(16);
            }
            assertEquals("A atualização só deve começar após soltar", before, refreshes.get());
            send(down, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
                x + dxDp * density, y + dyDp * density);
            SystemClock.sleep(1500);
            INSTRUMENTATION.waitForIdleSync();
        }

        private void send(long down, long time, int action, float x, float y) {
            MotionEvent event = MotionEvent.obtain(down, time, action, x, y, 0);
            INSTRUMENTATION.sendPointerSync(event);
            event.recycle();
        }

        @Override public void close() {
            scenario.onActivity(activity -> {
                browser.releaseSession(); session.close();
            });
            scenario.close();
        }
    }

    private static String page(boolean longPage) {
        return "<!doctype html><meta name='viewport' content='width=device-width,initial-scale=1'>"
            + "<style>body{margin:0}section{height:" + (longPage ? "4000px" : "120px") + ";background:#eee}</style>"
            + "<section>Conteúdo de teste</section>";
    }

    @Test public void longPageRequiresReleaseAndRejectsShortAndDiagonalDrags() throws Exception {
        try (Harness h = new Harness(page(true))) {
            h.drag(0, 60); assertEquals(0, h.refreshes.get());
            h.drag(100, 140); assertEquals(0, h.refreshes.get());
            h.drag(0, 160); assertEquals(1, h.refreshes.get());
        }
    }

    @Test public void shortPageCanRefreshAtTop() throws Exception {
        try (Harness h = new Harness(page(false))) {
            h.drag(0, 160); assertEquals(1, h.refreshes.get());
        }
    }

    @Test public void notAtTopDoesNotRefresh() throws Exception {
        try (Harness h = new Harness(page(true))) {
            h.drag(0, -200);
            long until = SystemClock.uptimeMillis() + 4000;
            while (h.scroll.get() == 0 && SystemClock.uptimeMillis() < until) SystemClock.sleep(50);
            assertTrue("A página deveria ter rolado", h.scroll.get() > 0);
            h.drag(0, 160); assertEquals(0, h.refreshes.get());
        }
    }


    @Test public void shortPageHonorsTouchActionNone() throws Exception {
        // The short body does not fill the viewport; the gesture starts on the
        // root element below it, so set touch-action on that actual ancestor.
        String html = page(false).replace("<style>", "<style>html{touch-action:none}");
        try (Harness h = new Harness(html)) {
            h.drag(0, 160); assertEquals(0, h.refreshes.get());
        }
    }

    @Test public void longPressDoesNotRefreshShortPage() throws Exception {
        try (Harness h = new Harness(page(false))) {
            h.drag(0, 160, 700); assertEquals(0, h.refreshes.get());
        }
    }

    @Test public void pageOwnedTouchDoesNotRefresh() throws Exception {
        String html = page(true).replace("</section>", "</section><script>"
            + "document.addEventListener('touchstart',function(e){e.preventDefault()}, {passive:false});"
            + "</script>");
        try (Harness h = new Harness(html)) {
            h.drag(0, 160); assertEquals(0, h.refreshes.get());
        }
    }
}
