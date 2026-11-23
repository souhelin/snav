package io.github.souhelin.snav;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.PanZoomController;

/** Queries Gecko about the initial touch without sending the event twice. */
public final class RefreshAwareGeckoView extends GeckoView {
    private final int touchSlop;
    private long gestureSerial;
    private float downX, downY;
    private boolean waitingForGecko;
    private boolean movedBeforeDecision;
    private boolean refreshEligible;

    public RefreshAwareGeckoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    boolean isRefreshEligible() { return refreshEligible && !waitingForGecko; }
    boolean isWaitingForGecko() { return waitingForGecko; }

    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                final long serial = ++gestureSerial;
                downX = event.getX(); downY = event.getY();
                waitingForGecko = true;
                movedBeforeDecision = false;
                refreshEligible = false;
                // As in Mozilla's NestedGeckoView, DOWN is dispatched through the
                // detailed API instead of also being sent through super.onTouchEvent.
                super.onTouchEventForDetailResult(event).accept(detail -> {
                    if (serial != gestureSerial) return;
                    waitingForGecko = false;
                    refreshEligible = !movedBeforeDecision && detail != null
                        && detail.handledResult() == PanZoomController.INPUT_RESULT_HANDLED
                        && (detail.scrollableDirections() & PanZoomController.SCROLLABLE_FLAG_TOP) == 0
                        && (detail.overscrollDirections() & PanZoomController.OVERSCROLL_FLAG_VERTICAL) != 0;
                }, error -> {
                    if (serial == gestureSerial) { waitingForGecko = false; refreshEligible = false; }
                });
                return true;
            case MotionEvent.ACTION_MOVE:
                if (waitingForGecko && (Math.abs(event.getX() - downX) > touchSlop
                        || Math.abs(event.getY() - downY) > touchSlop)) movedBeforeDecision = true;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                gestureSerial++;
                waitingForGecko = false;
                refreshEligible = false;
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}
