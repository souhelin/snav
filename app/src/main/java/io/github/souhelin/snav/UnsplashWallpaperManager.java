package io.github.souhelin.snav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** Downloads and keeps a bounded offline set of Unsplash wallpapers. */
public final class UnsplashWallpaperManager {
    public static final class Photo {
        public final String imageId, author, pageUrl;
        Photo(String imageId, String author, String pageId) {
            this.imageId = imageId;
            this.author = author;
            this.pageUrl = "https://unsplash.com/photos/" + pageId + "?utm_source=snav_browser&utm_medium=referral";
        }
    }

    public interface Callback {
        void onWallpaperReady(Bitmap bitmap, Photo photo);
    }

    // Curated from the linked Unsplash photo pages; never associate old cache
    // entries with guessed author metadata. Subjects include forest, sea,
    // mountains, desert and the night sky.
    static final Photo[] PHOTOS = {
        new Photo("photo-1441974231531-c6227db76b6e", "Lukasz Szmigiel", "jFCViYFYcus"),
        new Photo("photo-1774650802872-db8045c04a7b", "Mihály Köles", "3wWZNi4tZb0"),
        new Photo("photo-1775750265219-ef98f7df0444", "Riley Brockett", "f1wVwqpcnrw"),
        new Photo("photo-1759310347910-091a3f3c26e6", "Mathias Reding", "3zKUh7rEJqI"),
        new Photo("photo-1718567234333-e60566ef06e4", "Rodrigo Silva", "DQ7-ouISDNc"),
        new Photo("photo-1718308055947-2f3d8d5ea983", "Bram Bergers", "YHaNHQXFIhU"),
        new Photo("photo-1759042753972-9e857c6fd0a9", "Wietse Jongsma", "3fxV1g2UG3M"),
        new Photo("photo-1658835495224-50998dd4503a", "Yujin Yano", "eKAwsFSz7cY"),
        new Photo("photo-1534796636912-3b95b3ab5986", "Kai Pilger", "Ef6iL87-vOA"),
        new Photo("photo-1530180061773-77ff7cbc99f4", "Annie Spratt", "NZ3bvBaBWFo")
    };
    private static final int MAX_IMAGES = 8;
    private static final int MAX_IMAGE_BYTES = 1536 * 1024;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final File cacheDirectory;
    private final Random random = new Random();
    private final AtomicBoolean loadInProgress = new AtomicBoolean(false);
    private volatile WeakReference<Callback> pendingCallback = new WeakReference<>(null);

    public UnsplashWallpaperManager(Context context) {
        appContext = context.getApplicationContext();
        cacheDirectory = new File(appContext.getFilesDir(), "unsplash_wallpapers");
    }

    public void load(boolean enabled, final Callback callback) {
        if (!enabled || callback == null) return;
        pendingCallback = new WeakReference<>(callback);
        if (!loadInProgress.compareAndSet(false, true)) return;
        try {
            EXECUTOR.execute(() -> {
                try {
                    if (!cacheDirectory.exists() && !cacheDirectory.mkdirs()) return;
                    List<Photo> candidates = new ArrayList<>();
                    Collections.addAll(candidates, PHOTOS);
                    Collections.shuffle(candidates, random);
                    String last = appContext.getSharedPreferences("unsplash_wallpaper", Context.MODE_PRIVATE)
                        .getString("last_photo", "");
                    // Last displayed item is tried only as a fallback.
                    Collections.sort(candidates, (a, b) -> Boolean.compare(a.imageId.equals(last), b.imageId.equals(last)));
                    boolean online = hasInternet();
                    int requests = 0;
                    for (Photo photo : candidates) {
                        File file = cacheFile(photo);
                        if (!file.isFile() || !isValidImage(file)) {
                            if (!online || requests >= 3) continue;
                            requests++;
                            if (!downloadPhoto(photo.imageId, file)) continue;
                        }
                        Bitmap bitmap = decodeWallpaper(file);
                        if (bitmap == null) continue;
                        file.setLastModified(System.currentTimeMillis());
                        appContext.getSharedPreferences("unsplash_wallpaper", Context.MODE_PRIVATE)
                            .edit().putString("last_photo", photo.imageId).apply();
                        trimCache();
                        notifyReady(bitmap, photo);
                        break;
                    }
                } finally { loadInProgress.set(false); }
            });
        } catch (java.util.concurrent.RejectedExecutionException error) {
            loadInProgress.set(false);
        }
    }

    private File cacheFile(Photo photo) { return new File(cacheDirectory, photo.imageId + ".img"); }

    private boolean isValidImage(File file) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        return bounds.outWidth >= 500 && bounds.outHeight >= 300;
    }

    private boolean hasInternet() {
        try {
            ConnectivityManager manager = (ConnectivityManager)
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager == null ? null : manager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean downloadPhoto(String photoId, File target) {
        HttpURLConnection connection = null;
        InputStream input = null;
        FileOutputStream output = null;
        File temporary = new File(target.getParentFile(), target.getName() + ".part");
        try {
            String address = "https://images.unsplash.com/" + photoId
                + "?fit=crop&w=1280&h=800&q=68&fm=jpg";
            URL url = new URL(address);
            if (!"https".equalsIgnoreCase(url.getProtocol())
                || !"images.unsplash.com".equalsIgnoreCase(url.getHost())) return false;

            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(12000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Snav-Browser/1.0");
            connection.setRequestProperty("Accept", "image/jpeg");
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) return false;
            String contentType = connection.getContentType();
            if (contentType == null || !contentType.toLowerCase(java.util.Locale.US).startsWith("image/")) {
                return false;
            }
            int declaredLength = connection.getContentLength();
            if (declaredLength > MAX_IMAGE_BYTES) return false;

            input = new BufferedInputStream(connection.getInputStream());
            output = new FileOutputStream(temporary, false);
            byte[] buffer = new byte[16 * 1024];
            int count;
            int total = 0;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (total > MAX_IMAGE_BYTES) return false;
                output.write(buffer, 0, count);
            }
            output.flush();
            output.close();
            output = null;
            if (total == 0) return false;
            BitmapFactory.Options validation = new BitmapFactory.Options();
            validation.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(temporary.getAbsolutePath(), validation);
            if (validation.outWidth < 500 || validation.outHeight < 300) return false;
            if (target.exists()) target.delete();
            return temporary.renameTo(target);
        } catch (Exception ignored) {
            return false;
        } finally {
            try { if (input != null) input.close(); } catch (Exception ignored) { }
            try { if (output != null) output.close(); } catch (Exception ignored) { }
            if (temporary.exists()) temporary.delete();
            if (connection != null) connection.disconnect();
        }
    }

    private Bitmap decodeWallpaper(File file) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Math.max(1, Math.min(
            bounds.outWidth / 1600, bounds.outHeight / 1000));
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    private void notifyReady(final Bitmap bitmap, final Photo photo) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Callback callback = pendingCallback.get();
                if (callback != null) callback.onWallpaperReady(bitmap, photo);
                else if (!bitmap.isRecycled()) bitmap.recycle();
            }
        });
    }

    private void trimCache() {
        File[] files = cacheDirectory.listFiles((dir, name) -> name.endsWith(".img"));
        if (files == null) return;
        List<File> entries = new ArrayList<>();
        Collections.addAll(entries, files);
        Collections.sort(entries, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        for (int i = MAX_IMAGES; i < entries.size(); i++) entries.get(i).delete();
    }
}
