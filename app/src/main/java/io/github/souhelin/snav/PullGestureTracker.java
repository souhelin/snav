package io.github.souhelin.snav;

/** Conservative single-pointer classifier. All events still go to GeckoView. */
final class PullGestureTracker {
    private final float threshold;
    private final float slop;
    private final long longPressTimeout;
    private float startX, startY, distance;
    private long downAt;
    private boolean tracking, started;

    PullGestureTracker(float threshold, float slop, long longPressTimeout) {
        this.threshold = threshold;
        this.slop = slop;
        this.longPressTimeout = longPressTimeout;
    }

    void begin(float x, float y, long time, boolean eligible) {
        startX = x;
        startY = y;
        downAt = time;
        distance = 0;
        started = false;
        tracking = eligible;
    }

    float move(float x, float y, long time, int pointers, boolean eligible) {
        if (!tracking) return 0;
        if (!eligible || pointers != 1) { cancel(); return 0; }
        float dx = x - startX;
        float dy = y - startY;
        if (dy < -slop || (Math.abs(dx) > slop && Math.abs(dx) * 2f >= Math.max(0, dy))) {
            cancel(); return 0;
        }
        if (!started && time - downAt >= longPressTimeout) { cancel(); return 0; }
        if (dy > slop && dy > Math.abs(dx) * 2f) started = true;
        distance = started ? Math.max(0, dy) : 0;
        return distance;
    }

    boolean release(float x, float y, long time, boolean eligible) {
        move(x, y, time, 1, eligible);
        boolean refresh = tracking && started && distance >= threshold && eligible;
        cancel();
        return refresh;
    }

    void cancel() { tracking = false; started = false; distance = 0; }
}
