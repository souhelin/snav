package io.github.souhelin.snav;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/** Animates only compositor properties; content is measured once per state change. */
final class BrowserToolbarController {
    private final View bar;
    private final View content;
    private final int height;
    private final ScrollDirectionTracker direction;
    private boolean visible;

    BrowserToolbarController(View bar, View content) {
        this.bar = bar;
        this.content = content;
        height = bar.getLayoutParams().height;
        direction = new ScrollDirectionTracker(Math.round(24 * bar.getResources().getDisplayMetrics().density));
        bar.bringToFront();
    }

    void onScroll(int y) {
        int decision = direction.update(y);
        if (decision != 0) setVisible(decision > 0, true);
    }

    void reset(boolean browser) { reset(browser, 0); }

    void reset(boolean browser, int scrollY) {
        syncScroll(scrollY);
        setVisible(browser, false);
    }

    void syncScroll(int scrollY) { direction.resetAt(scrollY); }

    void cancel() {
        bar.animate().cancel();
        content.animate().cancel();
        bar.animate().withEndAction(null);
        content.animate().withEndAction(null);
    }

    void setVisible(boolean show, boolean animate) {
        if (show == visible && animate) return;
        cancel();
        visible = show;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();
        float currentTop = params.topMargin + content.getTranslationY();
        int targetTop = show ? height : 0;
        if (params.topMargin != targetTop) {
            params.topMargin = targetTop;
            content.setLayoutParams(params);
        }
        if (!animate) {
            bar.setTranslationY(show ? 0 : -height);
            bar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            content.setTranslationY(0);
        } else {
            bar.setVisibility(View.VISIBLE);
            content.setTranslationY(currentTop - targetTop);
            bar.animate().translationY(show ? 0 : -height).setDuration(180)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    if (!visible) bar.setVisibility(View.INVISIBLE);
                }).start();
            content.animate().translationY(0).setDuration(180)
                .setInterpolator(new DecelerateInterpolator()).start();
        }
        // Hidden controls must not retain accessibility focus during the transition.
        bar.setImportantForAccessibility(show ? View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
            : View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
    }
}
