package io.github.souhelin.snav;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PullGestureTrackerTest {
    private PullGestureTracker tracker() { return new PullGestureTracker(110, 8, 500); }

    @Test public void requiresReleaseBeyondThreshold() {
        PullGestureTracker t = tracker();
        t.begin(0, 0, 0, true);
        t.move(2, 109, 100, 1, true);
        assertFalse(t.release(2, 109, 110, true));
        t.begin(0, 0, 200, true);
        t.move(2, 111, 300, 1, true);
        assertTrue(t.release(2, 111, 310, true));
    }

    @Test public void cancelsHorizontalDiagonalMultitouchAndLongPress() {
        PullGestureTracker t = tracker();
        t.begin(0, 0, 0, true); t.move(80, 120, 100, 1, true);
        assertFalse(t.release(80, 120, 110, true));
        t.begin(0, 0, 0, true); t.move(1, 120, 100, 2, true);
        assertFalse(t.release(1, 120, 110, true));
        t.begin(0, 0, 0, true); t.move(0, 120, 600, 1, true);
        assertFalse(t.release(0, 120, 610, true));
    }

    @Test public void cancelsWhenNotAtTopOrWhenNavigationStarts() {
        PullGestureTracker t = tracker();
        t.begin(0, 0, 0, false); t.move(0, 120, 100, 1, true);
        assertFalse(t.release(0, 120, 110, true));
        t.begin(0, 0, 0, true); t.move(0, 120, 100, 1, false);
        assertFalse(t.release(0, 120, 110, true));
    }

    @Test public void cancelDoesNotRefreshAndReturningBelowThresholdDoesNotRefresh() {
        PullGestureTracker t = tracker();
        t.begin(0, 0, 0, true); t.move(0, 120, 100, 1, true); t.cancel();
        assertFalse(t.release(0, 120, 110, true));
        t.begin(0, 0, 0, true); t.move(0, 120, 100, 1, true);
        assertFalse(t.release(0, 80, 110, true));
    }
}
