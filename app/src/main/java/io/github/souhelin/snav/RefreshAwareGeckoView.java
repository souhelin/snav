package io.github.souhelin.snav;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.PanZoomController;
import java.util.function.Supplier;

/**
 * Dispatches only Gecko-approved, unconsumed vertical pull deltas through Android
 * nested scroll. All original MotionEvents still go to Gecko, exactly once.
 */
public final class RefreshAwareGeckoView extends GeckoView implements NestedScrollingChild3 {
    private final NestedScrollingChildHelper nested;
    private final PullGestureTracker gesture;
    private final int touchSlop;
    private final int[] consumed = new int[2];
    private long gestureSerial;
    private float downX, downY, decisionX, decisionY, sentDistance;
    private boolean waitingForGecko, movedBeforeDecision, refreshEligible, blocked, retryAfterUnhandled;
    private String gesturePage, refreshConfirmedPage;
    private Supplier<String> eligiblePage = () -> null;

    public RefreshAwareGeckoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        gesture = new PullGestureTracker(110f * getResources().getDisplayMetrics().density,
            touchSlop, ViewConfiguration.getLongPressTimeout());
        nested = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    void setEligiblePage(Supplier<String> supplier) { eligiblePage = supplier; }
    String consumeConfirmedRefresh() {
        String confirmed = refreshConfirmedPage;
        refreshConfirmedPage = null;
        return confirmed;
    }
    private boolean sameEligiblePage() {
        return gesturePage != null && gesturePage.equals(eligiblePage.get());
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                final long serial = ++gestureSerial;
                downX = decisionX = event.getX(); downY = decisionY = event.getY();
                waitingForGecko = true; movedBeforeDecision = false; retryAfterUnhandled = false;
                refreshEligible = false; blocked = false; refreshConfirmedPage = null; sentDistance = 0;
                gesturePage = eligiblePage.get();
                gesture.begin(downX, downY, event.getEventTime(), gesturePage != null);
                // DOWN goes through Gecko's detailed API, never through both APIs.
                super.onTouchEventForDetailResult(event).accept(detail -> {
                    if (serial != gestureSerial) return;
                    waitingForGecko = false;
                    refreshEligible = !movedBeforeDecision && permitsRefresh(detail);
                    // Short documents can return UNHANDLED for DOWN. Query the
                    // first MOVE rather than treating its undefined direction flags
                    // as approval. The MOVE must itself be browser-handled.
                    retryAfterUnhandled = !movedBeforeDecision && detail != null
                        && detail.handledResult() == PanZoomController.INPUT_RESULT_UNHANDLED;
                    if (refreshEligible) startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                }, error -> {
                    if (serial == gestureSerial) { waitingForGecko = false; refreshEligible = false; }
                });
                return true;
            case MotionEvent.ACTION_MOVE:
                if (retryAfterUnhandled && !blocked && !waitingForGecko) {
                    retryAfterUnhandled = false;
                    waitingForGecko = true;
                    movedBeforeDecision = false;
                    decisionX = event.getX(); decisionY = event.getY();
                    final float moveX = decisionX, moveY = decisionY;
                    final long moveTime = event.getEventTime(), moveSerial = gestureSerial;
                    final int pointers = event.getPointerCount();
                    // The detailed API dispatches the MOVE itself; do not send
                    // the same MotionEvent again through onTouchEvent.
                    super.onTouchEventForDetailResult(event).accept(detail -> {
                        if (moveSerial != gestureSerial) return;
                        waitingForGecko = false;
                        refreshEligible = !movedBeforeDecision && permitsRefresh(detail) && sameEligiblePage();
                        if (refreshEligible) {
                            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                            updateNestedPull(gesture.move(moveX, moveY, moveTime, pointers, true));
                        }
                    }, error -> {
                        if (moveSerial == gestureSerial) { waitingForGecko = false; refreshEligible = false; }
                    });
                    return true;
                }
                if (waitingForGecko) {
                    if (Math.abs(event.getX() - decisionX) > touchSlop || Math.abs(event.getY() - decisionY) > touchSlop)
                        movedBeforeDecision = true;
                } else if (!blocked) {
                    float distance = gesture.move(event.getX(), event.getY(), event.getEventTime(),
                        event.getPointerCount(), refreshEligible && sameEligiblePage());
                    if (distance == 0 && sentDistance > 0) {
                        cancelNestedPull(); blocked = true;
                    } else updateNestedPull(distance);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!blocked && !waitingForGecko) {
                    boolean eligible = refreshEligible && sameEligiblePage();
                    float distance = gesture.move(event.getX(), event.getY(), event.getEventTime(), 1, eligible);
                    updateNestedPull(distance);
                    refreshConfirmedPage = gesture.release(event.getX(), event.getY(), event.getEventTime(), eligible)
                        ? gesturePage : null;
                    // The Compose parent decides on release, even for zero velocity.
                    dispatchNestedPreFling(0, 0);
                } else cancelNestedPull();
                gestureSerial++; waitingForGecko = false; refreshEligible = false;
                stopNestedScroll(ViewCompat.TYPE_TOUCH);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_CANCEL:
                gesture.cancel(); cancelNestedPull(); blocked = true;
                gestureSerial++; waitingForGecko = false; refreshEligible = false;
                stopNestedScroll(ViewCompat.TYPE_TOUCH);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    static boolean permitsRefresh(PanZoomController.InputResultDetail detail) {
        // Gecko documents the direction flags as meaningful only for HANDLED.
        // UNHANDLED may represent a website gesture or text selection.
        return detail != null && detail.handledResult() == PanZoomController.INPUT_RESULT_HANDLED
            && (detail.scrollableDirections() & PanZoomController.SCROLLABLE_FLAG_TOP) == 0
            && (detail.overscrollDirections() & PanZoomController.OVERSCROLL_FLAG_VERTICAL) != 0;
    }

    private void updateNestedPull(float distance) {
        // Material 3 uses an 80 dp positional threshold and a 0.5 drag
        // multiplier. Scale 110 dp of finger travel to 160 dp nested travel.
        int delta = Math.round(distance * 16f / 11f) - Math.round(sentDistance * 16f / 11f);
        if (delta > 0) {
            consumed[0] = consumed[1] = 0;
            dispatchNestedScroll(0, 0, 0, -delta, null, ViewCompat.TYPE_TOUCH, consumed);
        } else if (delta < 0) {
            consumed[0] = consumed[1] = 0;
            dispatchNestedPreScroll(0, -delta, consumed, null, ViewCompat.TYPE_TOUCH);
        }
        sentDistance = distance;
    }

    private void cancelNestedPull() {
        refreshConfirmedPage = null;
        if (sentDistance > 0) {
            consumed[0] = consumed[1] = 0;
            dispatchNestedPreScroll(0, (int) Math.ceil(sentDistance * 16f / 11f) + 1, consumed, null, ViewCompat.TYPE_TOUCH);
            dispatchNestedPreFling(0, 0);
        }
        sentDistance = 0;
    }

    @Override public void setNestedScrollingEnabled(boolean enabled) { if (nested != null) nested.setNestedScrollingEnabled(enabled); }
    @Override public boolean isNestedScrollingEnabled() { return nested != null && nested.isNestedScrollingEnabled(); }
    @Override public boolean startNestedScroll(int axes) { return nested.startNestedScroll(axes); }
    @Override public boolean startNestedScroll(int axes, int type) { return nested.startNestedScroll(axes, type); }
    @Override public void stopNestedScroll() { nested.stopNestedScroll(); }
    @Override public void stopNestedScroll(int type) { nested.stopNestedScroll(type); }
    @Override public boolean hasNestedScrollingParent() { return nested.hasNestedScrollingParent(); }
    @Override public boolean hasNestedScrollingParent(int type) { return nested.hasNestedScrollingParent(type); }
    @Override public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offset) {
        return nested.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offset);
    }
    @Override public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offset, int type) {
        return nested.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offset, type);
    }
    @Override public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offset, int type, @NonNull int[] consumed) {
        nested.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offset, type, consumed);
    }
    @Override public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offset) {
        return nested.dispatchNestedPreScroll(dx, dy, consumed, offset);
    }
    @Override public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offset, int type) {
        return nested.dispatchNestedPreScroll(dx, dy, consumed, offset, type);
    }
    @Override public boolean dispatchNestedFling(float x, float y, boolean consumed) { return nested.dispatchNestedFling(x, y, consumed); }
    @Override public boolean dispatchNestedPreFling(float x, float y) { return nested.dispatchNestedPreFling(x, y); }
}
