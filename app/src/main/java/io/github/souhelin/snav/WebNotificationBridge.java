package io.github.souhelin.snav;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import org.mozilla.geckoview.WebNotification;
import org.mozilla.geckoview.WebNotificationDelegate;

/** Gecko Web Notifications bridge. Intents carrying Gecko objects are private. */
final class WebNotificationBridge implements WebNotificationDelegate {
    static final String CHANNEL = "snav_sites";
    private static final String EXTRA_NOTIFICATION = "web_notification";
    private final Context context;

    WebNotificationBridge(Context context) { this.context = context.getApplicationContext(); }

    static boolean areEnabled(Context context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return false;
        manager.createNotificationChannel(new NotificationChannel(CHANNEL, "Notificações de sites", NotificationManager.IMPORTANCE_DEFAULT));
        NotificationChannel channel = manager.getNotificationChannel(CHANNEL);
        return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    private static String key(WebNotification n) { return n.origin + "|" + n.tag; }
    private static Uri uniqueUri(WebNotification n, String event) {
        return new Uri.Builder().scheme("snav-notification").authority(event)
            .appendPath(android.util.Base64.encodeToString(key(n).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP | android.util.Base64.NO_PADDING)).build();
    }

    @Override public void onShowNotification(WebNotification n) {
        Uri source = n.source == null ? null : Uri.parse(n.source);
        if (source == null || !"https".equalsIgnoreCase(source.getScheme()) || source.getHost() == null
            || source.getUserInfo() != null || !areEnabled(context)) {
            n.dismiss(); return;
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) { n.dismiss(); return; }
        Intent click = new Intent(context, WebNotificationActivity.class).setData(uniqueUri(n, "click"))
            .putExtra(EXTRA_NOTIFICATION, n);
        Intent dismiss = new Intent(context, WebNotificationDismissReceiver.class).setData(uniqueUri(n, "dismiss"))
            .putExtra(EXTRA_NOTIFICATION, n);
        PendingIntent clickPending = PendingIntent.getActivity(context, 0, click, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent dismissPending = PendingIntent.getBroadcast(context, 0, dismiss, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(n.title == null ? source.getHost() : n.title)
            .setContentText(n.text).setSubText(source.getHost())
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setSilent(n.silent)
            .setAutoCancel(true).setContentIntent(clickPending).setDeleteIntent(dismissPending);
        try {
            manager.notify(key(n), 0, builder.build());
            n.show();
        } catch (SecurityException error) { n.dismiss(); }
    }

    @Override public void onCloseNotification(WebNotification n) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(key(n), 0);
        n.dismiss();
    }

    public static final class WebNotificationActivity extends Activity {
        @Override protected void onCreate(Bundle state) {
            super.onCreate(state);
            WebNotification n = getIntent().getParcelableExtra(EXTRA_NOTIFICATION);
            if (n != null) { n.click(); n.dismiss(); }
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        }
    }

    public static final class WebNotificationDismissReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            WebNotification n = intent.getParcelableExtra(EXTRA_NOTIFICATION);
            if (n != null) n.dismiss();
        }
    }
}
