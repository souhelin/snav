package io.github.souhelin.snav;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small bounded local store for browser bookmarks and visit history. */
public final class BrowserLibraryStore {
    private static final String PREFS = "snav_browser_library_v1";
    private static final String BOOKMARKS = "bookmarks";
    private static final String HISTORY = "history";
    private static final int MAX_HISTORY = 1000;
    private final SharedPreferences prefs;

    public static final class Entry {
        public final String title;
        public final String url;
        public final long visitedAt;
        Entry(String title, String url, long visitedAt) {
            this.title = title;
            this.url = url;
            this.visitedAt = visitedAt;
        }
    }

    public BrowserLibraryStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private JSONArray read(String key) {
        try { return new JSONArray(prefs.getString(key, "[]")); }
        catch (JSONException error) { return new JSONArray(); }
    }

    private void write(String key, JSONArray values) {
        prefs.edit().putString(key, values.toString()).apply();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public synchronized boolean addBookmark(String title, String url) {
        String safeUrl = clean(url);
        Uri parsed = Uri.parse(safeUrl);
        if (!"https".equalsIgnoreCase(parsed.getScheme()) || parsed.getHost() == null
            || parsed.getUserInfo() != null) return false;
        JSONArray values = read(BOOKMARKS);
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = values.optJSONObject(i);
            if (item != null && safeUrl.equals(item.optString("url"))) return true;
        }
        JSONObject item = new JSONObject();
        try {
            item.put("title", clean(title).isEmpty() ? safeUrl : clean(title));
            item.put("url", safeUrl);
            item.put("createdAt", System.currentTimeMillis());
            values.put(item);
            write(BOOKMARKS, values);
            return true;
        } catch (JSONException error) { return false; }
    }

    public synchronized boolean removeBookmark(String url) {
        JSONArray source = read(BOOKMARKS), result = new JSONArray();
        boolean removed = false;
        for (int i = 0; i < source.length(); i++) {
            JSONObject item = source.optJSONObject(i);
            if (item == null) continue;
            if (clean(url).equals(item.optString("url"))) removed = true;
            else result.put(item);
        }
        if (removed) write(BOOKMARKS, result);
        return removed;
    }

    public synchronized void addVisit(String title, String url) {
        String safeUrl = clean(url);
        Uri parsed = Uri.parse(safeUrl);
        if (!"https".equalsIgnoreCase(parsed.getScheme()) || parsed.getHost() == null
            || parsed.getUserInfo() != null) return;
        JSONArray source = read(HISTORY), result = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            JSONObject item = source.optJSONObject(i);
            if (item != null && !safeUrl.equals(item.optString("url"))) result.put(item);
        }
        JSONObject item = new JSONObject();
        try {
            item.put("title", clean(title).isEmpty() ? safeUrl : clean(title));
            item.put("url", safeUrl);
            item.put("visitedAt", System.currentTimeMillis());
            result.put(0, item);
            JSONArray bounded = new JSONArray();
            for (int i = 0; i < result.length() && i < MAX_HISTORY; i++) bounded.put(result.get(i));
            write(HISTORY, bounded);
        } catch (JSONException ignored) {}
    }

    private List<Entry> entries(String key, String timeKey) {
        JSONArray values = read(key);
        List<Entry> result = new ArrayList<>();
        for (int i = 0; i < values.length(); i++) {
            JSONObject item = values.optJSONObject(i);
            if (item == null) continue;
            result.add(new Entry(item.optString("title"), item.optString("url"),
                item.optLong(timeKey, 0L)));
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<Entry> getBookmarks() { return entries(BOOKMARKS, "createdAt"); }
    public synchronized List<Entry> getHistory() { return entries(HISTORY, "visitedAt"); }
    public synchronized void clearHistory() { write(HISTORY, new JSONArray()); }

    /** Validates and replaces bookmarks from a Netscape bookmark HTML import. */
    public synchronized int importBookmarks(String html) {
        if (html == null || html.length() > 2_000_000) return 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
            "(?is)<A[^>]*HREF\\s*=\\s*[\"'](https://[^\"']+)[\"'][^>]*>(.*?)</A>"
        ).matcher(html);
        int imported = 0;
        while (matcher.find()) {
            String url = matcher.group(1);
            String title = android.text.Html.fromHtml(matcher.group(2)).toString().trim();
            if (addBookmark(title, url)) imported++;
        }
        return imported;
    }

    public synchronized String exportBookmarksHtml() {
        StringBuilder html = new StringBuilder(
            "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n<TITLE>Snav Bookmarks</TITLE>\n<H1>Bookmarks</H1>\n<DL><p>\n");
        for (Entry entry : getBookmarks()) {
            html.append("<DT><A HREF=\"").append(escape(entry.url)).append("\">")
                .append(escape(entry.title)).append("</A>\n");
        }
        return html.append("</DL><p>\n").toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("\"", "&quot;")
            .replace("<", "&lt;").replace(">", "&gt;");
    }
}
