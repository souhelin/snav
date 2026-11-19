package io.github.souhelin.snav;

import android.content.Context;

import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebResponse;

/**
 * Owns the process-wide GeckoRuntime and the GeckoSession attached to Snav's
 * browser surface. UI code must communicate with Gecko through this class.
 */
public final class GeckoEngineController {
    public interface Listener {
        default void onPageStarted(String uri) {}
        default void onPageStopped(boolean success) {}
        default void onProgressChanged(int progress) {}
        default void onLocationChanged(String uri) {}
        default void onTitleChanged(String title) {}
        default void onCanGoBackChanged(boolean canGoBack) {}
        default void onInsecureNavigationBlocked(String uri, String sourceUri) {}
        default void onExternalResponse(WebResponse response) {}
        default void onFullScreenChanged(boolean fullScreen) {}
        default void onContentProcessCrashed() {}
    }

    private static GeckoRuntime runtime;

    private final GeckoView view;
    private final GeckoSession session;
    private Listener listener;
    private boolean closed;
    private boolean canGoBack;
    private String currentUri;

    public GeckoEngineController(Context context, GeckoView view, Listener listener) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }
        if (view == null) {
            throw new IllegalArgumentException("GeckoView must not be null");
        }

        this.view = view;
        this.listener = listener;
        synchronized (GeckoEngineController.class) {
            if (runtime == null) {
                ContentBlocking.Settings contentBlocking =
                    new ContentBlocking.Settings.Builder()
                        .cookieBehavior(ContentBlocking.CookieBehavior.ACCEPT_FIRST_PARTY_AND_ISOLATE_OTHERS)
                        .cookiePurging(true)
                        .queryParameterStrippingEnabled(true)
                        .queryParameterStrippingPrivateBrowsingEnabled(true)
                        .build();

                GeckoRuntimeSettings settings = new GeckoRuntimeSettings.Builder()
                    .contentBlocking(contentBlocking)
                    .javaScriptEnabled(true)
                    .aboutConfigEnabled(false)
                    .remoteDebuggingEnabled(false)
                    .build();
                runtime = GeckoRuntime.create(context.getApplicationContext(), settings);
            }
        }

        session = new GeckoSession();
        session.getSettings().setUseTrackingProtection(true);
        session.getSettings().setAllowJavascript(true);
        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onTitleChange(GeckoSession currentSession, String title) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onTitleChanged(title);
            }

            @Override
            public void onExternalResponse(GeckoSession currentSession, WebResponse response) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onExternalResponse(response);
            }

            @Override
            public void onFullScreen(GeckoSession currentSession, boolean fullScreen) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onFullScreenChanged(fullScreen);
            }

            @Override
            public void onCrash(GeckoSession currentSession) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onContentProcessCrashed();
            }
        });
        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override
            public void onPageStart(GeckoSession currentSession, String uri) {
                currentUri = uri;
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onPageStarted(uri);
            }

            @Override
            public void onPageStop(GeckoSession currentSession, boolean success) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onPageStopped(success);
            }

            @Override
            public void onProgressChange(GeckoSession currentSession, int progress) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onProgressChanged(progress);
            }
        });
        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override
            public GeckoResult<AllowOrDeny> onLoadRequest(
                GeckoSession currentSession,
                GeckoSession.NavigationDelegate.LoadRequest request
            ) {
                String uri = request == null ? null : request.uri;
                if (uri != null && uri.regionMatches(true, 0, "http://", 0, 7)) {
                    Listener callback = GeckoEngineController.this.listener;
                    if (!closed && callback != null) {
                        callback.onInsecureNavigationBlocked(uri, request.triggerUri);
                    }
                    return GeckoResult.deny();
                }
                return GeckoResult.allow();
            }

            @Override
            public void onLocationChange(
                GeckoSession currentSession,
                String uri,
                java.util.List<GeckoSession.PermissionDelegate.ContentPermission> permissions,
                Boolean hasUserGesture
            ) {
                currentUri = uri;
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onLocationChanged(uri);
            }

            @Override
            public void onCanGoBack(GeckoSession currentSession, boolean value) {
                canGoBack = value;
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onCanGoBackChanged(value);
            }
        });

        // No web-origin permission is granted implicitly. The Activity must
        // install an explicit, origin-aware permission flow before enabling it.
        session.setPermissionDelegate(new GeckoSession.PermissionDelegate() {});
        session.open(runtime);
        view.setSession(session);
    }

    public GeckoSession getSession() {
        ensureOpen();
        return session;
    }

    public String getCurrentUri() {
        ensureOpen();
        return currentUri;
    }

    public boolean canGoBack() {
        ensureOpen();
        return canGoBack;
    }

    public void setListener(Listener listener) {
        ensureOpen();
        this.listener = listener;
    }

    public void loadUri(String uri) {
        ensureOpen();
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("URI must not be empty");
        }
        if (uri.regionMatches(true, 0, "http://", 0, 7)) {
            throw new SecurityException("Cleartext HTTP navigation is blocked");
        }
        session.loadUri(uri);
    }

    public void setJavaScriptEnabled(boolean enabled) {
        ensureOpen();
        session.getSettings().setAllowJavascript(enabled);
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
        if (canGoBack) session.goBack();
    }

    public void close() {
        if (closed) return;
        closed = true;
        listener = null;
        GeckoSession detached = view.releaseSession();
        if (detached != null && detached.isOpen()) detached.close();
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Gecko engine session is closed");
        }
    }
}
