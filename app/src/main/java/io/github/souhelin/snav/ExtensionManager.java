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
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;

/** Runtime-level extension management. Gecko remains the source of truth. */
final class ExtensionManager implements WebExtensionController.PromptDelegate {
    static final int REQUEST_XPI = 6123;
    private final WeakReference<Activity> activity;
    private final WebExtensionController controller;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Runnable> pendingDenials = new ArrayList<>();
    private boolean closed;
    private boolean active;
    private boolean installing;

    ExtensionManager(Activity owner, GeckoRuntime runtime) {
        activity = new WeakReference<>(owner);
        controller = runtime.getWebExtensionController();
        controller.setPromptDelegate(this);
    }

    void setActive(boolean value) {
        active = value;
        if (!value) denyPending();
    }

    void close() {
        closed = true;
        active = false;
        denyPending();
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
        final String[] options = {"Instaladas", "Instalar de um link HTTPS", "Instalar arquivo XPI", "Sobre a compatibilidade"};
        new MaterialAlertDialogBuilder(a).setTitle("Extensões")
            .setItems(options, (dialog, which) -> {
                if (which == 0) list();
                else if (which == 1) askUrl();
                else if (which == 2) pickFile();
                else new MaterialAlertDialogBuilder(a).setTitle("Compatibilidade")
                    .setMessage("O GeckoView aceita extensões assinadas pela Mozilla. Algumas funções de extensões ainda não estão disponíveis no Snav, incluindo botões e janelas de extensão. A instalação não garante que a extensão funcione.")
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
            .setMessage("Use um link HTTPS direto para um arquivo XPI assinado. Redirecionamentos não são aceitos.")
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
                .setMessage("O servidor " + uri.getHost() + " receberá uma solicitação. Você verá as permissões antes de instalar.")
                .setNegativeButton("Cancelar", null).setPositiveButton("Baixar", (d, w) -> download(address))
                .show();
        }));
        dialog.show();
    }

    private void download(final String address) {
        final Activity a = owner();
        if (a == null || installing) return;
        installing = true;
        final File cacheDir = a.getCacheDir();
        notifyUser("Baixando extensão…");
        worker.execute(() -> {
            File staged = null;
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) new URL(address).openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                int status = connection.getResponseCode();
                if (status != 200 || connection.getContentLengthLong() > ExtensionPackageValidator.MAX_ARCHIVE_BYTES)
                    throw new IOException("Resposta inválida ou arquivo muito grande");
                try (InputStream in = connection.getInputStream()) { staged = stage(in, cacheDir); }
                final File result = staged;
                main.post(() -> installStaged(result, WebExtensionController.INSTALLATION_METHOD_MANAGER, "HTTPS: " + Uri.parse(address).getHost()));
            } catch (Exception error) {
                if (staged != null) staged.delete();
                main.post(() -> failed("Não foi possível baixar ou validar a extensão. Use um link direto HTTPS."));
            } finally { if (connection != null) connection.disconnect(); }
        });
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
        new MaterialAlertDialogBuilder(a).setTitle(name(extension)).setMessage(info.toString())
            .setPositiveButton("Ações", (dialog, which) -> new MaterialAlertDialogBuilder(a).setTitle(name(extension))
                .setItems(options, (d, selected) -> {
                    if (selected == 0) toggle(extension);
                    else if (selected == 1) update(extension);
                    else if (selected == 2) revoke(extension);
                    else uninstall(extension);
                }).setNegativeButton("Cancelar", null).show())
            .setNegativeButton("Voltar", (dialog, which) -> list()).show();
    }

    private void toggle(WebExtension extension) {
        GeckoResult<WebExtension> result = extension.metaData.enabled
            ? controller.disable(extension, WebExtensionController.EnableSource.USER)
            : controller.enable(extension, WebExtensionController.EnableSource.USER);
        result.accept(updated -> { notifyUser(updated.metaData.enabled ? "Extensão ativada" : "Extensão desativada"); list(); },
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
