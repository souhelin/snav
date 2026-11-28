package io.github.souhelin.snav;

/** Hysteresis for chrome visibility. Positive means show; negative means hide. */
final class ScrollDirectionTracker {
    private final int threshold;
    private int lastY;
    private int travel;

    ScrollDirectionTracker(int threshold) {
        this.threshold = Math.max(1, threshold);
    }

    int update(int scrollY) {
        int y = Math.max(0, scrollY);
        int delta = y - lastY;
        lastY = y;
        if (y == 0) { travel = 0; return 1; }
        if (delta == 0) return 0;
        if ((delta > 0 && travel < 0) || (delta < 0 && travel > 0)) travel = 0;
        travel += delta;
        if (Math.abs(travel) < threshold) return 0;
        int decision = travel > 0 ? -1 : 1;
        travel = 0;
        return decision;
    }

    void reset() { resetAt(0); }
    void resetAt(int y) { lastY = Math.max(0, y); travel = 0; }
}
