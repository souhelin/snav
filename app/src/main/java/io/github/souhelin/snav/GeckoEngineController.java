package io.github.souhelin.snav;

import android.content.Context;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;

/**
 * Owns the GeckoView runtime/session pair used by Snav.
 *
 * GeckoRuntime is process-scoped and must only be created once. A session is
 * owned by this controller and must be closed when the browser surface is
 * permanently destroyed.
 */
public final class GeckoEngineController {
    private static GeckoRuntime runtime;

    private final GeckoView view;
    private final GeckoSession session;
    private boolean closed;

    public GeckoEngineController(Context context, GeckoView view) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }
        if (view == null) {
            throw new IllegalArgumentException("GeckoView must not be null");
        }

        this.view = view;
        synchronized (GeckoEngineController.class) {
            if (runtime == null) {
                runtime = GeckoRuntime.create(context.getApplicationContext());
            }
        }

        session = new GeckoSession();
        // Required by GeckoView's current embedding guidance.
        session.setContentDelegate(new GeckoSession.ContentDelegate() {});
        session.open(runtime);
        view.setSession(session);
    }

    public GeckoSession getSession() {
        ensureOpen();
        return session;
    }

    public void loadUri(String uri) {
        ensureOpen();
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("URI must not be empty");
        }
        session.loadUri(uri);
    }

    public void reload() {
        ensureOpen();
        session.reload();
    }

    public void stop() {
        ensureOpen();
        session.stop();
    }

    public void goBack() {
        ensureOpen();
        session.goBack();
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        GeckoSession detached = view.releaseSession();
        if (detached != null && detached.isOpen()) {
            detached.close();
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Gecko engine session is closed");
        }
    }
}
