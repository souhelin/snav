package io.github.souhelin.snav;

import android.content.Context;

import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.StorageController;
import org.mozilla.geckoview.WebResponse;

import java.util.List;
import java.util.Map;

/** Owns the process-wide Gecko runtime and the browser's active Gecko session. */
public final class GeckoEngineController {
    public interface Listener {
        default void onPageStarted(String uri) {}
        default void onPageStopped(boolean success) {}
        default void onProgressChanged(int progress) {}
        default void onLocationChanged(String uri) {}
        default void onTitleChanged(String title) {}
        default void onCanGoBackChanged(boolean canGoBack) {}
        default void onCanGoForwardChanged(boolean canGoForward) {}
        default void onInsecureNavigationBlocked(String uri, String sourceUri) {}
        default void onExternalNavigationRequested(String uri) {}
        default void onExternalResponse(WebResponse response) {}
        default void onFullScreenChanged(boolean fullScreen) {}
        default void onContentProcessCrashed() {}
        default void onScrollChanged(int scrollY) {}
        default void onHistoryVisited(String uri) {}
    }

    private static GeckoRuntime runtime;

    private final GeckoView view;
    private final GeckoSession session;
    private Listener listener;
    private boolean closed;
    private boolean canGoBack;
    private boolean canGoForward;
    private String currentUri;

    public GeckoEngineController(Context context, GeckoView view, Listener listener,
                                 boolean javaScriptEnabled, boolean trackingProtectionEnabled) {
        if (context == null || view == null) {
            throw new IllegalArgumentException("Context and GeckoView are required");
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
                    .javaScriptEnabled(javaScriptEnabled)
                    .aboutConfigEnabled(false)
                    .remoteDebuggingEnabled(false)
                    .allowInsecureConnections(GeckoRuntimeSettings.HTTPS_ONLY)
                    .build();
                runtime = GeckoRuntime.create(context.getApplicationContext(), settings);
            }
        }

        session = new GeckoSession();
        session.getSettings().setUseTrackingProtection(trackingProtectionEnabled);
        session.getSettings().setAllowJavascript(javaScriptEnabled);

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
                else if (response != null && response.body != null) {
                    try { response.body.close(); } catch (Exception ignored) {}
                }
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
                if (uri == null) return GeckoResult.deny();
                if (uri.regionMatches(true, 0, "https://", 0, 8)
                    || uri.equals("about:blank")
                    || uri.regionMatches(true, 0, "about:reader?", 0, 13)) {
                    return GeckoResult.allow();
                }
                Listener callback = GeckoEngineController.this.listener;
                if (uri.regionMatches(true, 0, "http://", 0, 7)) {
                    if (!closed && callback != null) callback.onInsecureNavigationBlocked(uri, request.triggerUri);
                } else if (!closed && callback != null) {
                    callback.onExternalNavigationRequested(uri);
                }
                return GeckoResult.deny();
            }

            @Override
            public void onLocationChange(
                GeckoSession currentSession,
                String uri,
                List<GeckoSession.PermissionDelegate.ContentPermission> permissions,
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

            @Override
            public void onCanGoForward(GeckoSession currentSession, boolean value) {
                canGoForward = value;
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onCanGoForwardChanged(value);
            }
        });

        session.setScrollDelegate(new GeckoSession.ScrollDelegate() {
            @Override
            public void onScrollChanged(GeckoSession currentSession, int scrollX, int scrollY) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onScrollChanged(scrollY);
            }
        });

        session.setHistoryDelegate(new GeckoSession.HistoryDelegate() {
            @Override
            public GeckoResult<Boolean> onVisited(
                GeckoSession currentSession, String url, String lastVisitedURL, int flags
            ) {
                Listener callback = GeckoEngineController.this.listener;
                if (!closed && callback != null) callback.onHistoryVisited(url);
                return GeckoResult.fromValue(true);
            }
        });

        // The Activity installs the origin-aware PermissionDelegate before loading content.
        session.open(runtime);
        view.setSession(session);
    }

    public static GeckoRuntime getRuntime() {
        synchronized (GeckoEngineController.class) {
            if (runtime == null) throw new IllegalStateException("GeckoRuntime has not been initialized");
            return runtime;
        }
    }

    public GeckoSession getSession() { ensureOpen(); return session; }
    public String getCurrentUri() { ensureOpen(); return currentUri; }
    public boolean canGoBack() { ensureOpen(); return canGoBack; }
    public boolean canGoForward() { ensureOpen(); return canGoForward; }

    public void setListener(Listener listener) {
        ensureOpen();
        this.listener = listener;
    }

    public void loadUri(String uri, boolean doNotTrack) {
        ensureOpen();
        if (uri == null || uri.trim().isEmpty()) throw new IllegalArgumentException("URI must not be empty");
        if (!uri.regionMatches(true, 0, "https://", 0, 8)
            && !uri.equals("about:blank")
            && !uri.regionMatches(true, 0, "about:reader?", 0, 13)) {
            throw new SecurityException("Only HTTPS and internal reader pages are allowed");
        }
        if (doNotTrack) {
            Map<String, String> headers = new java.util.HashMap<>();
            headers.put("DNT", "1");
            session.load(new GeckoSession.Loader()
                .uri(uri)
                .additionalHeaders(headers)
                .headerFilter(GeckoSession.HEADER_FILTER_UNRESTRICTED_UNSAFE));
        } else {
            session.load(new GeckoSession.Loader().uri(uri));
        }
    }

    public void setJavaScriptEnabled(boolean enabled) {
        ensureOpen();
        session.getSettings().setAllowJavascript(enabled);
    }

    public void setTrackingProtectionEnabled(boolean enabled) {
        ensureOpen();
        session.getSettings().setUseTrackingProtection(enabled);
    }

    public GeckoResult<Boolean> processBackPressed() { ensureOpen(); return session.processBackPressed(); }
    public void reload() { ensureOpen(); session.reload(); }
    public void stop() { ensureOpen(); session.stop(); }
    public void goBack() { ensureOpen(); if (canGoBack) session.goBack(); }
    public void goForward() { ensureOpen(); if (canGoForward) session.goForward(); }
    public void setActive(boolean active) { if (!closed) session.setActive(active); }

    public GeckoResult<Void> clearSiteData(String host) {
        ensureOpen();
        if (host == null || host.trim().isEmpty()) throw new IllegalArgumentException("Host is required");
        return clearAndReattach(runtime.getStorageController().clearDataFromHost(
            host, StorageController.ClearFlags.SITE_DATA | StorageController.ClearFlags.SITE_SETTINGS));
    }

    public GeckoResult<Void> clearBrowsingData() {
        ensureOpen();
        session.purgeHistory();
        return clearAndReattach(runtime.getStorageController().clearData(StorageController.ClearFlags.ALL));
    }

    private GeckoResult<Void> clearAndReattach(GeckoResult<Void> clearing) {
        view.releaseSession();
        if (session.isOpen()) session.close();
        final GeckoResult<Void> result = new GeckoResult<>();
        clearing.accept(
            new GeckoResult.Consumer<Void>() {
                @Override public void accept(Void ignored) {
                    session.open(runtime);
                    view.setSession(session);
                    session.loadUri("about:blank");
                    currentUri = "about:blank";
                    canGoBack = false;
                    canGoForward = false;
                    result.complete(null);
                }
            },
            new GeckoResult.Consumer<Throwable>() {
                @Override public void accept(Throwable error) {
                    session.open(runtime);
                    view.setSession(session);
                    result.completeExceptionally(error);
                }
            });
        return result;
    }

    public void close() {
        if (closed) return;
        closed = true;
        listener = null;
        GeckoSession detached = view.releaseSession();
        if (detached != null && detached.isOpen()) detached.close();
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Gecko engine session is closed");
    }
}
