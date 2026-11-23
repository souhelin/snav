package io.github.snavprojects.browser;

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
    public interface Callback {
        void onWallpaperReady(Bitmap bitmap);
    }

    private static final String[] PHOTO_IDS = {
        "photo-1500530855697-b586d89ba3ee",
        "photo-1470770841072-f978cf4d019e",
        "photo-1501785888041-af3ef285b470",
        "photo-1470252649378-9c29740c9fa8",
        "photo-1441974231531-c6227db76b6e",
        "photo-1472396961693-142e6e269027",
        "photo-1464822759023-fed622ff2c3b",
        "photo-1518837695005-2083093ee35b"
    };
    private static final int MAX_IMAGES = 8;
    private static final int MAX_IMAGE_BYTES = 1536 * 1024;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final File cacheDirectory;
    private final Random random = new Random();
    private final AtomicBoolean loadInProgress = new AtomicBoolean(false);

    public UnsplashWallpaperManager(Context context) {
        appContext = context.getApplicationContext();
        cacheDirectory = new File(appContext.getFilesDir(), "unsplash_wallpapers");
    }

    public void load(boolean enabled, final Callback callback) {
        if (!enabled || callback == null || !loadInProgress.compareAndSet(false, true)) return;
        final WeakReference<Callback> callbackReference = new WeakReference<Callback>(callback);
        try {
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!cacheDirectory.exists() && !cacheDirectory.mkdirs()) return;
                        List<File> cached = listCachedFiles();
                        boolean delivered = false;
                        if (!cached.isEmpty()) {
                            File selected = cached.get(random.nextInt(cached.size()));
                            Bitmap bitmap = decodeWallpaper(selected);
                            if (bitmap != null) {
                                notifyReady(callbackReference, bitmap);
                                delivered = true;
                            }
                        }
                        if (!hasInternet()) return;
                        for (int i = 0; i < MAX_IMAGES; i++) {
                            File target = new File(cacheDirectory, "wallpaper_" + i + ".img");
                            if (target.isFile() && target.length() > 0 && isValidImage(target)) continue;
                            if (target.exists()) target.delete();
                            if (!downloadPhoto(PHOTO_IDS[i], target)) continue;
                            if (!delivered) {
                                Bitmap bitmap = decodeWallpaper(target);
                                if (bitmap != null) {
                                    notifyReady(callbackReference, bitmap);
                                    delivered = true;
                                }
                            }
                        }
                        trimCache();
                    } finally {
                        loadInProgress.set(false);
                    }
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException error) {
            loadInProgress.set(false);
        }
    }

    private List<File> listCachedFiles() {
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < MAX_IMAGES; i++) {
            File file = new File(cacheDirectory, "wallpaper_" + i + ".img");
            if (file.isFile() && file.length() > 0) files.add(file);
        }
        return files;
    }

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

    private void notifyReady(final WeakReference<Callback> callbackReference, final Bitmap bitmap) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Callback callback = callbackReference.get();
                if (callback != null) callback.onWallpaperReady(bitmap);
                else if (!bitmap.isRecycled()) bitmap.recycle();
            }
        });
    }

    private void trimCache() {
        File[] files = cacheDirectory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".img") && !file.getName().endsWith(".part")) {
                file.delete();
            }
        }
        List<File> valid = listCachedFiles();
        if (valid.size() > MAX_IMAGES) {
            Collections.sort(valid);
            for (int i = MAX_IMAGES; i < valid.size(); i++) valid.get(i).delete();
        }
    }
}
