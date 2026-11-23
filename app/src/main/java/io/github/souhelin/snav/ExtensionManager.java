package io.github.souhelin.snav;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;

/** Runtime-level extension management. Gecko remains the source of truth. */
final class ExtensionManager implements WebExtensionController.PromptDelegate, WebExtension.ActionDelegate {
    static final int REQUEST_XPI = 6123;
    private final WeakReference<Activity> activity;
    private final WebExtensionController controller;
    private final GeckoRuntime runtime;
    private final GeckoSession browserSession;
    private final Consumer<String> openPage;
    private final Map<String, WebExtension> registered = new LinkedHashMap<>();
    private final Map<String, WebExtension.Action> browserActions = new LinkedHashMap<>();
    private final Map<String, WebExtension.Action> browserOverrides = new LinkedHashMap<>();
    private final Map<String, WebExtension.Action> pageActions = new LinkedHashMap<>();
    private final Map<String, WebExtension.Action> pageOverrides = new LinkedHashMap<>();
    private com.google.android.material.bottomsheet.BottomSheetDialog popupDialog;
    private String popupId;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Runnable> pendingDenials = new ArrayList<>();
    private boolean closed;
    private boolean active;
    private boolean installing;

    ExtensionManager(Activity owner, GeckoRuntime runtime, GeckoSession browserSession, Consumer<String> openPage) {
        activity = new WeakReference<>(owner);
        this.runtime = runtime;
        this.browserSession = browserSession;
        this.openPage = openPage;
        controller = runtime.getWebExtensionController();
        controller.setPromptDelegate(this);
        controller.list().accept(extensions -> {
            if (!closed) for (WebExtension extension : extensions) attachExtension(extension);
        }, error -> android.util.Log.w("SnavExtensions", "Não foi possível carregar delegates de extensões", error));
    }

    void setActive(boolean value) {
        active = value;
        if (!value) {
            denyPending();
            if (popupDialog != null) popupDialog.dismiss();
        }
    }

    void close() {
        closed = true;
        active = false;
        denyPending();
        if (popupDialog != null) popupDialog.dismiss();
        for (WebExtension ext : registered.values()) {
            browserSession.getWebExtensionController().setActionDelegate(ext, null);
            ext.setActionDelegate(null);
        }
        registered.clear();
        if (controller.getPromptDelegate() == this) controller.setPromptDelegate(null);
        worker.shutdown();
        activity.clear();
    }

    private Activity owner() {
        Activity value = activity.get();
        return !closed && active && value != null && !value.isFinishing() && !value.isDestroyed() ? value : null;
    }

    private void notifyUser(String message) {
        Activity a = owner();
        if (a != null) Toast.makeText(a, message, Toast.LENGTH_LONG).show();
    }

    void show() {
        final Activity a = owner();
        if (a == null) return;
        final String[] options = {"Instaladas", "Ações das extensões", "Catálogo Mozilla", "Instalar de um link HTTPS", "Instalar arquivo XPI", "Sobre a compatibilidade"};
        new MaterialAlertDialogBuilder(a).setTitle("Extensões")
            .setItems(options, (dialog, which) -> {
                if (which == 0) list();
                else if (which == 1) showActions();
                else if (which == 2) askCatalog();
                else if (which == 3) askUrl();
                else if (which == 4) pickFile();
                else new MaterialAlertDialogBuilder(a).setTitle("Compatibilidade")
                    .setMessage("O GeckoView aceita extensões assinadas pela Mozilla. Algumas funções de extensões, incluindo a integração completa de abas e menus de contexto, ainda não estão disponíveis no Snav. A instalação não garante que a extensão funcione.")
                    .setPositiveButton("Entendi", null).show();
            }).setNegativeButton("Fechar", null).show();
    }

    private void pickFile() {
        Activity a = owner();
        if (a == null || installing) { notifyUser("Aguarde a instalação em andamento"); return; }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try { a.startActivityForResult(intent, REQUEST_XPI); }
        catch (android.content.ActivityNotFoundException error) { notifyUser("Não foi possível abrir o seletor de arquivos"); }
    }

    void installFile(final Uri uri) {
        if (owner() == null || installing || uri == null || !"content".equals(uri.getScheme())) return;
        final Activity a = owner();
        new MaterialAlertDialogBuilder(a).setTitle("Verificar arquivo de extensão?")
            .setMessage("O arquivo será verificado pelo GeckoView. Você verá as permissões antes da instalação.")
            .setNegativeButton("Cancelar", null).setPositiveButton("Continuar", (dialog, which) -> {
                if (owner() == null || installing) return;
                installing = true;
                notifyUser("Verificando extensão…");
                final android.content.ContentResolver resolver = a.getContentResolver();
                final File cacheDir = a.getCacheDir();
                worker.execute(() -> {
                    File staged = null;
                    try {
                        try (InputStream in = resolver.openInputStream(uri)) {
                            if (in == null) throw new IOException("Arquivo indisponível");
                            staged = stage(in, cacheDir);
                        }
                        final File result = staged;
                        main.post(() -> installStaged(result, WebExtensionController.INSTALLATION_METHOD_FROM_FILE, "Arquivo selecionado"));
                    } catch (Exception error) {
                        if (staged != null) staged.delete();
                        main.post(() -> failed("Não foi possível ler ou validar o arquivo XPI"));
                    }
                });
            }).show();
    }

    private void askUrl() {
        final Activity a = owner();
        if (a == null || installing) { notifyUser("Aguarde a instalação em andamento"); return; }
        EditText input = new EditText(a);
        input.setSingleLine(true);
        input.setHint("https://exemplo.com/extensao.xpi");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout box = new LinearLayout(a);
        box.setPadding(dp(a, 24), 0, dp(a, 24), 0);
        box.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog dialog = new MaterialAlertDialogBuilder(a).setTitle("Instalar de um link")
            .setMessage("Use um link HTTPS para um arquivo XPI assinado. O servidor pode redirecionar para outros endereços HTTPS.")
            .setView(box).setNegativeButton("Cancelar", null).setPositiveButton("Continuar", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String address = input.getText().toString().trim();
            Uri uri = Uri.parse(address);
            if (address.length() > 8192 || address.matches(".*\\s+.*") || !"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null) {
                input.setError("Insira um link HTTPS válido, sem credenciais"); return;
            }
            dialog.dismiss();
            new MaterialAlertDialogBuilder(a).setTitle("Baixar extensão?")
                .setMessage("O servidor " + uri.getHost() + " e possíveis destinos HTTPS de redirecionamento receberão solicitações. Você verá as permissões antes de instalar.")
                .setNegativeButton("Cancelar", null).setPositiveButton("Baixar", (d, w) -> download(address, null))
                .show();
        }));
        dialog.show();
    }

    private void download(final String address, final String expectedHash) {
        final Activity a = owner();
        if (a == null || installing) return;
        installing = true;
        final File cacheDir = a.getCacheDir();
        notifyUser("Baixando extensão…");
        worker.execute(() -> {
            File staged = null;
            HttpsURLConnection connection = null;
            try {
                URI target = ExtensionDownloadPolicy.parse(address);
                int redirects = 0;
                while (true) {
                    connection = (HttpsURLConnection) target.toURL().openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(30000);
                    int status = connection.getResponseCode();
                    if (status >= 300 && status <= 399) {
                        String location = connection.getHeaderField("Location");
                        connection.disconnect(); connection = null;
                        if (++redirects > 5) throw new IOException("Redirecionamentos demais");
                        target = ExtensionDownloadPolicy.redirect(target, location);
                        continue;
                    }
                    if (status != 200 || connection.getContentLengthLong() > ExtensionPackageValidator.MAX_ARCHIVE_BYTES)
                        throw new IOException("Resposta inválida ou arquivo muito grande");
                    try (InputStream in = connection.getInputStream()) { staged = stage(in, cacheDir); }
                    break;
                }
                if (expectedHash != null && !expectedHash.equalsIgnoreCase(sha256(staged)))
                    throw new IOException("A integridade do arquivo não corresponde ao catálogo");
                final String origin = "HTTPS: " + target.getHost();
                final File result = staged;
                main.post(() -> installStaged(result, WebExtensionController.INSTALLATION_METHOD_MANAGER, origin));
            } catch (Exception error) {
                if (staged != null) staged.delete();
                main.post(() -> failed("Não foi possível baixar ou validar a extensão."));
            } finally { if (connection != null) connection.disconnect(); }
        });
    }

    private static String sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        try (InputStream input = new java.io.FileInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
        }
        StringBuilder value = new StringBuilder(64);
        for (byte b : digest.digest()) value.append(String.format(Locale.ROOT, "%02x", b & 0xff));
        return value.toString();
    }

    private static final class CatalogEntry {
        final String name, summary, version, url, hash;
        CatalogEntry(String name, String summary, String version, String url, String hash) {
            this.name = name; this.summary = summary; this.version = version; this.url = url; this.hash = hash;
        }
    }

    private void askCatalog() {
        Activity a = owner();
        if (a == null) return;
        EditText search = new EditText(a);
        search.setSingleLine(true); search.setHint("Nome da extensão (opcional)");
        search.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(100)});
        LinearLayout box = new LinearLayout(a);
        box.setPadding(dp(a, 24), 0, dp(a, 24), 0);
        box.addView(search, new LinearLayout.LayoutParams(-1, -2));
        new MaterialAlertDialogBuilder(a).setTitle("Catálogo Mozilla")
            .setMessage("O termo pesquisado será enviado à Mozilla. O catálogo indica compatibilidade com Android, mas não garante que todas as funções funcionem no Snav.")
            .setView(box).setNegativeButton("Cancelar", null).setPositiveButton("Pesquisar", (d, w) ->
                loadCatalog(search.getText().toString().trim())).show();
    }

    private static String localized(JSONObject object, String fallback) {
        if (object == null) return fallback;
        String text = object.optString("pt-BR", "");
        if (text.isEmpty()) text = object.optString("en-US", "");
        if (text.isEmpty()) {
            java.util.Iterator<String> keys = object.keys();
            if (keys.hasNext()) text = object.optString(keys.next(), "");
        }
        return text.isEmpty() ? fallback : text;
    }

    private void loadCatalog(final String query) {
        if (owner() == null) return;
        notifyUser("Consultando catálogo…");
        worker.execute(() -> {
            HttpsURLConnection connection = null;
            try {
                String address = "https://addons.mozilla.org/api/v5/addons/search/?app=android&appversion=160.0&type=extension&promoted=recommended&page_size=10&lang=pt-BR&q="
                    + java.net.URLEncoder.encode(query, "UTF-8");
                connection = (HttpsURLConnection) new URL(address).openConnection();
                connection.setInstanceFollowRedirects(false); connection.setConnectTimeout(15000); connection.setReadTimeout(30000);
                if (connection.getResponseCode() != 200) throw new IOException("Catálogo indisponível");
                java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                try (InputStream input = connection.getInputStream()) {
                    byte[] buffer = new byte[8192]; int count, total = 0;
                    while ((count = input.read(buffer)) != -1) {
                        total += count;
                        if (total > 4 * 1024 * 1024) throw new IOException("Resposta muito grande");
                        bytes.write(buffer, 0, count);
                    }
                }
                JSONArray results = new JSONObject(bytes.toString("UTF-8")).getJSONArray("results");
                List<CatalogEntry> entries = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject item = results.optJSONObject(i);
                    if (item == null) continue;
                    JSONObject version = item.optJSONObject("current_version");
                    JSONObject file = version == null ? null : version.optJSONObject("file");
                    if (file == null) continue;
                    String url = file.optString("url", ""), hash = file.optString("hash", "");
                    if (!hash.matches("(?i)sha256:[0-9a-f]{64}")) continue;
                    try { ExtensionDownloadPolicy.parse(url); } catch (Exception invalid) { continue; }
                    entries.add(new CatalogEntry(localized(item.optJSONObject("name"), "Extensão"),
                        localized(item.optJSONObject("summary"), "Sem descrição"), version.optString("version", ""),
                        url, hash.substring(7)));
                }
                main.post(() -> showCatalog(entries));
            } catch (Exception error) {
                main.post(() -> notifyUser("Não foi possível consultar o catálogo"));
            } finally { if (connection != null) connection.disconnect(); }
        });
    }

    private void showCatalog(List<CatalogEntry> entries) {
        Activity a = owner(); if (a == null) return;
        if (entries.isEmpty()) {
            new MaterialAlertDialogBuilder(a).setTitle("Catálogo Mozilla").setMessage("Nenhuma extensão recomendada encontrada")
                .setPositiveButton("Fechar", null).show(); return;
        }
        String[] names = new String[entries.size()];
        for (int i = 0; i < names.length; i++) names[i] = entries.get(i).name + " · " + entries.get(i).version;
        new MaterialAlertDialogBuilder(a).setTitle("Catálogo Mozilla").setItems(names, (dialog, selected) -> {
            CatalogEntry item = entries.get(selected);
            new MaterialAlertDialogBuilder(a).setTitle(item.name).setMessage(item.summary
                + "\n\nVersão " + item.version + "\nO download pode usar redirecionamentos HTTPS. A compatibilidade com o Snav não é garantida.")
                .setNegativeButton("Cancelar", null).setPositiveButton("Baixar", (d, w) -> download(item.url, item.hash)).show();
        }).setNegativeButton("Fechar", null).show();
    }

    private static File stage(InputStream input, File directory) throws IOException {
        File file = File.createTempFile("snav-extension-", ".xpi", directory);
        boolean success = false;
        try {
            long size = 0;
            byte[] buffer = new byte[8192];
            try (FileOutputStream output = new FileOutputStream(file)) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    size += read;
                    if (size > ExtensionPackageValidator.MAX_ARCHIVE_BYTES) throw new IOException("Arquivo muito grande");
                    output.write(buffer, 0, read);
                }
            }
            ExtensionPackageValidator.validate(file);
            success = true;
            return file;
        } finally { if (!success) file.delete(); }
    }

    private void installStaged(final File file, String method, final String origin) {
        if (owner() == null) { file.delete(); installing = false; return; }
        try {
            controller.install(Uri.fromFile(file).toString(), method).accept(
                extension -> {
                    file.delete(); installing = false;
                    if (!closed) attachExtension(extension);
                    Activity a = activity.get();
                    if (a != null) a.getSharedPreferences("snav_extension_origins", Activity.MODE_PRIVATE)
                        .edit().putString(extension.id, origin).apply();
                    notifyUser("Extensão instalada");
                },
                error -> { file.delete(); failed("Não foi possível instalar. Verifique a assinatura e a compatibilidade da extensão."); });
        } catch (RuntimeException error) {
            file.delete(); failed("Não foi possível iniciar a instalação");
        }
    }

    private void failed(String message) { installing = false; notifyUser(message); }
    private static int dp(Activity a, int value) { return Math.round(value * a.getResources().getDisplayMetrics().density); }

    private void list() {
        final Activity a = owner();
        if (a == null) return;
        notifyUser("Carregando extensões…");
        controller.list().accept(extensions -> {
            if (owner() == null) return;
            for (WebExtension extension : extensions) attachExtension(extension);
            if (extensions.isEmpty()) {
                new MaterialAlertDialogBuilder(a).setTitle("Extensões instaladas").setMessage("Nenhuma extensão instalada")
                    .setPositiveButton("Fechar", null).show(); return;
            }
            String[] names = new String[extensions.size()];
            for (int i = 0; i < names.length; i++) {
                WebExtension ext = extensions.get(i);
                names[i] = name(ext) + " · " + (ext.metaData.enabled ? "Ativada" : "Desativada");
            }
            new MaterialAlertDialogBuilder(a).setTitle("Extensões instaladas").setItems(names,
                (dialog, which) -> details(extensions.get(which)))
                .setNegativeButton("Fechar", null).show();
        }, error -> notifyUser("Não foi possível carregar as extensões"));
    }

    private void details(final WebExtension extension) {
        final Activity a = owner();
        if (a == null) return;
        WebExtension.MetaData meta = extension.metaData;
        StringBuilder info = new StringBuilder();
        info.append(meta.description == null ? "Sem descrição" : meta.description);
        info.append("\n\nVersão: ").append(meta.version).append("\nID: ").append(extension.id);
        info.append("\nEstado: ").append(meta.enabled ? "Ativada" : "Desativada");
        String source = a.getSharedPreferences("snav_extension_origins", Activity.MODE_PRIVATE)
            .getString(extension.id, "Indisponível");
        info.append("\nOrigem: ").append(source);
        appendPermissions(info, "Permissões obrigatórias", meta.requiredPermissions);
        appendPermissions(info, "Sites obrigatórios", meta.requiredOrigins);
        appendPermissions(info, "Dados solicitados", meta.requiredDataCollectionPermissions);
        appendPermissions(info, "Permissões opcionais concedidas", meta.grantedOptionalPermissions);
        appendPermissions(info, "Sites opcionais concedidos", meta.grantedOptionalOrigins);
        appendPermissions(info, "Dados opcionais concedidos", meta.grantedOptionalDataCollectionPermissions);
        info.append("\n\nAlgumas APIs e ações da extensão podem não estar disponíveis no Snav.");
        final String[] options = {meta.enabled ? "Desativar" : "Ativar", "Verificar atualização", "Revogar permissões opcionais", "Desinstalar"};
        AlertDialog detailsDialog = new MaterialAlertDialogBuilder(a).setTitle(name(extension)).setMessage(info.toString())
            .setPositiveButton("Ações", (dialog, which) -> new MaterialAlertDialogBuilder(a).setTitle(name(extension))
                .setItems(options, (d, selected) -> {
                    if (selected == 0) toggle(extension);
                    else if (selected == 1) update(extension);
                    else if (selected == 2) revoke(extension);
                    else uninstall(extension);
                }).setNegativeButton("Cancelar", null).show())
            .setNegativeButton("Voltar", (dialog, which) -> list()).create();
        detailsDialog.show();
        meta.icon.getBitmap(dp(a, 40)).accept(bitmap -> {
            if (bitmap != null && detailsDialog.isShowing() && owner() != null)
                detailsDialog.setIcon(new android.graphics.drawable.BitmapDrawable(a.getResources(), bitmap));
        }, error -> android.util.Log.w("SnavExtensions", "Ícone da extensão indisponível", error));
    }

    private void attachExtension(WebExtension extension) {
        if (closed || extension == null) return;
        registered.put(extension.id, extension);
        if (!extension.metaData.enabled) {
            browserSession.getWebExtensionController().setActionDelegate(extension, null);
            extension.setActionDelegate(null);
            removeActions(extension.id); return;
        }
        extension.setActionDelegate(this);
        browserSession.getWebExtensionController().setActionDelegate(extension, this);
    }

    private void removeActions(String id) {
        WebExtension old = registered.remove(id);
        if (old != null) {
            browserSession.getWebExtensionController().setActionDelegate(old, null);
            old.setActionDelegate(null);
        }
        browserActions.remove(id); browserOverrides.remove(id);
        pageActions.remove(id); pageOverrides.remove(id);
    }

    @Override public void onBrowserAction(WebExtension extension, GeckoSession session, WebExtension.Action action) {
        if (!closed) (session == null ? browserActions : browserOverrides).put(extension.id, action);
    }

    @Override public void onPageAction(WebExtension extension, GeckoSession session, WebExtension.Action action) {
        if (!closed) (session == null ? pageActions : pageOverrides).put(extension.id, action);
    }

    private static WebExtension.Action resolved(WebExtension.Action base, WebExtension.Action override) {
        return override == null ? base : base == null ? override : override.withDefault(base);
    }

    void onPageChanged() {
        browserOverrides.clear(); pageOverrides.clear();
        if (popupDialog != null) popupDialog.dismiss();
    }

    void showActions() {
        Activity a = owner(); if (a == null) return;
        List<WebExtension.Action> actions = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (Map.Entry<String, WebExtension> entry : registered.entrySet()) {
            String id = entry.getKey();
            WebExtension.Action browser = resolved(browserActions.get(id), browserOverrides.get(id));
            WebExtension.Action page = resolved(pageActions.get(id), pageOverrides.get(id));
            if (browser != null && Boolean.TRUE.equals(browser.enabled)) {
                actions.add(browser); labels.add(name(entry.getValue()) + (browser.title == null ? "" : " · " + browser.title));
            }
            if (page != null && Boolean.TRUE.equals(page.enabled)) {
                actions.add(page); labels.add(name(entry.getValue()) + (page.title == null ? " · Página" : " · " + page.title));
            }
        }
        if (actions.isEmpty()) {
            new MaterialAlertDialogBuilder(a).setTitle("Ações das extensões").setMessage("Nenhuma ação disponível nesta página")
                .setPositiveButton("Fechar", null).show(); return;
        }
        new MaterialAlertDialogBuilder(a).setTitle("Ações das extensões")
            .setItems(labels.toArray(new String[0]), (d, which) -> actions.get(which).click())
            .setNegativeButton("Fechar", null).show();
    }

    @Override public GeckoResult<GeckoSession> onTogglePopup(WebExtension extension, WebExtension.Action action) {
        if (popupDialog != null && popupDialog.isShowing() && extension.id.equals(popupId)) {
            popupDialog.dismiss(); return null;
        }
        return openPopup(extension);
    }

    @Override public GeckoResult<GeckoSession> onOpenPopup(WebExtension extension, WebExtension.Action action) {
        return openPopup(extension);
    }

    private GeckoResult<GeckoSession> openPopup(WebExtension extension) {
        Activity a = owner();
        if (a == null) return null;
        if (popupDialog != null) popupDialog.dismiss();
        final GeckoSession popup = new GeckoSession();
        final GeckoView view = new GeckoView(a);
        view.setViewBackend(GeckoView.BACKEND_TEXTURE_VIEW);
        final com.google.android.material.bottomsheet.BottomSheetDialog sheet =
            new com.google.android.material.bottomsheet.BottomSheetDialog(a);
        popup.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override public void onCloseRequest(GeckoSession session) { sheet.dismiss(); }
            @Override public void onCrash(GeckoSession session) { sheet.dismiss(); notifyUser("A janela da extensão falhou"); }
        });
        final Uri base = Uri.parse(extension.metaData.baseUrl);
        popup.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override public GeckoResult<AllowOrDeny> onLoadRequest(GeckoSession session, LoadRequest request) {
                if (request == null || request.uri == null) return GeckoResult.deny();
                Uri target = Uri.parse(request.uri);
                if ("about:blank".equals(request.uri)) return GeckoResult.allow();
                if ("moz-extension".equalsIgnoreCase(target.getScheme()) && target.getHost() != null
                    && target.getHost().equals(base.getHost())) return GeckoResult.allow();
                if (request.hasUserGesture && "https".equalsIgnoreCase(target.getScheme())
                    && target.getHost() != null && target.getUserInfo() == null) openPage.accept(request.uri);
                return GeckoResult.deny();
            }
        });
        popup.open(runtime);
        view.setSession(popup);
        view.setLayoutParams(new ViewGroup.LayoutParams(-1, dp(a, 520)));
        sheet.setContentView(view);
        sheet.setOnDismissListener(ignored -> {
            view.releaseSession(); popup.close();
            if (popupDialog == sheet) { popupDialog = null; popupId = null; }
        });
        popupDialog = sheet; popupId = extension.id;
        sheet.show(); popup.setActive(true);
        return GeckoResult.fromValue(popup);
    }

    private void toggle(WebExtension extension) {
        GeckoResult<WebExtension> result = extension.metaData.enabled
            ? controller.disable(extension, WebExtensionController.EnableSource.USER)
            : controller.enable(extension, WebExtensionController.EnableSource.USER);
        result.accept(updated -> { attachExtension(updated); notifyUser(updated.metaData.enabled ? "Extensão ativada" : "Extensão desativada"); list(); },
            error -> notifyUser("Não foi possível alterar o estado da extensão"));
    }

    private void update(WebExtension extension) {
        Activity a = owner();
        if (a == null) return;
        new MaterialAlertDialogBuilder(a).setTitle("Verificar atualização?")
            .setMessage("O GeckoView baixará e instalará uma atualização disponível. Novas permissões serão solicitadas.")
            .setNegativeButton("Cancelar", null).setPositiveButton("Verificar", (d, w) -> {
                notifyUser("Verificando atualização…");
                controller.update(extension).accept(updated -> {
                    if (updated != null && !closed) attachExtension(updated);
                    notifyUser(updated == null ? "Nenhuma atualização disponível" : "Extensão atualizada");
                }, error -> notifyUser("Não foi possível atualizar a extensão"));
            }).show();
    }

    private void uninstall(WebExtension extension) {
        Activity a = owner();
        if (a == null) return;
        new MaterialAlertDialogBuilder(a).setTitle("Desinstalar " + name(extension) + "?")
            .setMessage("Os dados da extensão serão removidos.")
            .setNegativeButton("Cancelar", null).setPositiveButton("Desinstalar", (d, w) ->
                controller.uninstall(extension).accept(ignored -> {
                    Activity current = activity.get();
                    if (current != null) current.getSharedPreferences("snav_extension_origins", Activity.MODE_PRIVATE)
                        .edit().remove(extension.id).apply();
                    removeActions(extension.id);
                    notifyUser("Extensão desinstalada"); list();
                },
                    error -> notifyUser("Não foi possível desinstalar a extensão"))).show();
    }

    private void revoke(WebExtension extension) {
        Activity a = owner();
        if (a == null) return;
        WebExtension.MetaData meta = extension.metaData;
        if (meta.grantedOptionalPermissions.length == 0 && meta.grantedOptionalOrigins.length == 0
                && meta.grantedOptionalDataCollectionPermissions.length == 0) {
            notifyUser("Nenhuma permissão opcional concedida"); return;
        }
        new MaterialAlertDialogBuilder(a).setTitle("Revogar permissões opcionais?")
            .setMessage("Permissões obrigatórias não podem ser revogadas separadamente. A extensão pode deixar de funcionar.")
            .setNegativeButton("Cancelar", null).setPositiveButton("Revogar", (d, w) ->
                controller.removeOptionalPermissions(extension.id, meta.grantedOptionalPermissions,
                    meta.grantedOptionalOrigins, meta.grantedOptionalDataCollectionPermissions).accept(
                    updated -> { notifyUser("Permissões opcionais revogadas"); details(updated); },
                    error -> notifyUser("Não foi possível revogar as permissões"))).show();
    }

    private static String name(WebExtension ext) {
        return ext.metaData.name == null || ext.metaData.name.trim().isEmpty() ? ext.id : ext.metaData.name;
    }

    private static void appendPermissions(StringBuilder text, String heading, String[] values) {
        text.append("\n\n").append(heading).append(":");
        if (values == null || values.length == 0) text.append(" nenhuma");
        else for (String value : values) text.append("\n• ").append(value);
    }

    private String promptText(WebExtension ext, String[] permissions, String[] origins, String[] data) {
        StringBuilder text = new StringBuilder(name(ext));
        appendPermissions(text, "Permissões", permissions);
        appendPermissions(text, "Acesso a sites", origins);
        appendPermissions(text, "Coleta de dados", data);
        text.append("\n\nConceda apenas se você confiar nesta extensão.");
        return text.toString();
    }

    @Override public GeckoResult<WebExtension.PermissionPromptResponse> onInstallPromptRequest(
            WebExtension ext, String[] permissions, String[] origins, String[] data) {
        final GeckoResult<WebExtension.PermissionPromptResponse> result = new GeckoResult<>();
        askPermission("Instalar extensão?", promptText(ext, permissions, origins, data), allowed ->
            result.complete(new WebExtension.PermissionPromptResponse(allowed, false, allowed)));
        return result;
    }

    @Override public GeckoResult<AllowOrDeny> onUpdatePrompt(WebExtension ext, String[] permissions,
            String[] origins, String[] data) {
        return allowPrompt("Permissões para atualização", promptText(ext, permissions, origins, data));
    }

    @Override public GeckoResult<AllowOrDeny> onOptionalPrompt(WebExtension ext, String[] permissions,
            String[] origins, String[] data) {
        return allowPrompt("Permissões adicionais", promptText(ext, permissions, origins, data));
    }

    private GeckoResult<AllowOrDeny> allowPrompt(String title, String text) {
        GeckoResult<AllowOrDeny> result = new GeckoResult<>();
        askPermission(title, text, allowed -> result.complete(allowed ? AllowOrDeny.ALLOW : AllowOrDeny.DENY));
        return result;
    }

    private interface Decision { void resolve(boolean allowed); }

    private void askPermission(String title, String text, Decision decision) {
        Activity a = owner();
        if (a == null) { decision.resolve(false); return; }
        final boolean[] finished = {false};
        final AlertDialog[] dialog = new AlertDialog[1];
        Runnable deny = () -> {
            if (finished[0]) return;
            finished[0] = true;
            decision.resolve(false);
            if (dialog[0] != null) dialog[0].dismiss();
        };
        pendingDenials.add(deny);
        dialog[0] = new MaterialAlertDialogBuilder(a).setTitle(title).setMessage(text)
            .setNegativeButton("Recusar", (d, w) -> deny.run())
            .setPositiveButton("Permitir", (d, w) -> {
                if (!finished[0]) { finished[0] = true; decision.resolve(true); }
            }).setOnCancelListener(d -> deny.run()).setOnDismissListener(d -> {
                if (!finished[0]) deny.run();
                pendingDenials.remove(deny);
            }).create();
        dialog[0].show();
    }

    private void denyPending() {
        for (Runnable denial : new ArrayList<>(pendingDenials)) denial.run();
        pendingDenials.clear();
    }
}
