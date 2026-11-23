package io.github.souhelin.snav;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.geckoview.PanZoomController;

@RunWith(AndroidJUnit4.class)
public class RefreshGeckoDecisionTest {
    private static class Detail extends PanZoomController.InputResultDetail {
        Detail(int result, int scroll, int overscroll) { super(result, scroll, overscroll); }
    }

    @Test public void requiresBrowserHandledTouchAtOverscrollableTop() {
        int vertical = PanZoomController.OVERSCROLL_FLAG_VERTICAL;
        assertTrue(RefreshAwareGeckoView.permitsRefresh(new Detail(
            PanZoomController.INPUT_RESULT_HANDLED, PanZoomController.SCROLLABLE_FLAG_NONE, vertical)));
        assertFalse(RefreshAwareGeckoView.permitsRefresh(null));
        assertFalse(RefreshAwareGeckoView.permitsRefresh(new Detail(
            PanZoomController.INPUT_RESULT_UNHANDLED, PanZoomController.SCROLLABLE_FLAG_NONE, vertical)));
        assertFalse(RefreshAwareGeckoView.permitsRefresh(new Detail(
            PanZoomController.INPUT_RESULT_HANDLED_CONTENT, PanZoomController.SCROLLABLE_FLAG_NONE, vertical)));
        assertFalse(RefreshAwareGeckoView.permitsRefresh(new Detail(
            PanZoomController.INPUT_RESULT_IGNORED, PanZoomController.SCROLLABLE_FLAG_NONE, vertical)));
        assertFalse(RefreshAwareGeckoView.permitsRefresh(new Detail(
            PanZoomController.INPUT_RESULT_HANDLED, PanZoomController.SCROLLABLE_FLAG_TOP, vertical)));
        assertFalse(RefreshAwareGeckoView.permitsRefresh(new Detail(
            PanZoomController.INPUT_RESULT_HANDLED, PanZoomController.SCROLLABLE_FLAG_NONE,
            PanZoomController.OVERSCROLL_FLAG_NONE)));
    }
}
