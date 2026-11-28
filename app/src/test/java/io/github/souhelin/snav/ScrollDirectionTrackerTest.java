package io.github.souhelin.snav;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ScrollDirectionTrackerTest {
    @Test public void hidesOnUpwardSwipeAndShowsOnDownwardSwipe() {
        ScrollDirectionTracker tracker = new ScrollDirectionTracker(24);
        assertEquals(0, tracker.update(10));
        assertEquals(-1, tracker.update(30));
        assertEquals(0, tracker.update(22));
        assertEquals(1, tracker.update(5));
    }

    @Test public void ignoresJitterAndResetsTravelWhenDirectionChanges() {
        ScrollDirectionTracker tracker = new ScrollDirectionTracker(24);
        assertEquals(0, tracker.update(15));
        assertEquals(0, tracker.update(14));
        assertEquals(0, tracker.update(30));
        assertEquals(0, tracker.update(28));
        assertEquals(0, tracker.update(28));
        assertEquals(-1, tracker.update(52));
    }

    @Test public void alwaysShowsAtTopIncludingOverscroll() {
        ScrollDirectionTracker tracker = new ScrollDirectionTracker(24);
        assertEquals(-1, tracker.update(100));
        assertEquals(1, tracker.update(0));
        assertEquals(1, tracker.update(-20));
    }

    @Test public void resetDoesNotCarryTravelToAnotherPage() {
        ScrollDirectionTracker tracker = new ScrollDirectionTracker(24);
        tracker.update(20);
        tracker.reset();
        assertEquals(0, tracker.update(10));
        assertEquals(-1, tracker.update(24));
    }
    @Test public void returningToScrolledPageStartsFromItsCurrentPosition() {
        ScrollDirectionTracker tracker = new ScrollDirectionTracker(24);
        tracker.resetAt(500);
        assertEquals(0, tracker.update(490));
        assertEquals(1, tracker.update(476));
    }

}
