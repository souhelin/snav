package io.github.souhelin.snav;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSession.PromptDelegate;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.function.BooleanSupplier;

/** GeckoView has no default prompt UI. The embedder must complete every request. */
final class GeckoPromptController implements PromptDelegate {
    private static final int REQUEST_SITE_FILE = 4110;
    private final Activity activity;
    private final GeckoSession session;
    private final Supplier<String> currentUri;
    private final BooleanSupplier canShowPrompts;
    private Pending pending;
    private boolean filePickerInFlight;

    private static final class Pending {
        final BasePrompt prompt;
        final GeckoResult<PromptResponse> result = new GeckoResult<>();
        AlertDialog dialog;
        Pending(BasePrompt prompt) { this.prompt = prompt; }
    }

    GeckoPromptController(Activity activity, GeckoSession session, Supplier<String> currentUri, BooleanSupplier canShowPrompts) {
        this.activity = activity;
        this.session = session;
        this.currentUri = currentUri;
        this.canShowPrompts = canShowPrompts;
        session.setPromptDelegate(this);
    }

    private Pending begin(BasePrompt prompt) {
        cancelPending();
        Pending request = new Pending(prompt);
        pending = request;
        prompt.setDelegate(new PromptInstanceDelegate() {
            @Override public void onPromptDismiss(BasePrompt dismissed) {
                if (pending == request) finish(request, prompt::dismiss);
            }
        });
        return request;
    }

    private void finish(Pending request, Supplier<PromptResponse> response) {
        if (pending != request) return;
        pending = null;
        request.prompt.setDelegate(null);
        PromptResponse resolved;
        try {
            resolved = request.prompt.isComplete() ? null : response.get();
        } catch (RuntimeException error) {
            resolved = request.prompt.isComplete() ? null : request.prompt.dismiss();
        }
        request.result.complete(resolved);
        if (request.dialog != null) request.dialog.dismiss();
    }

    void cancelPending() {
        if (pending != null) finish(pending, pending.prompt::dismiss);
    }

    private GeckoResult<PromptResponse> show(Pending request, MaterialAlertDialogBuilder builder) {
        builder.setOnCancelListener(dialog -> finish(request, request.prompt::dismiss));
        request.dialog = builder.create();
        request.dialog.show();
        return request.result;
    }

    // Do not let page-controlled titles impersonate Android's permission dialogs.
    private MaterialAlertDialogBuilder builder(String message) {
        String uri = currentUri.get();
        String host = uri == null ? null : Uri.parse(uri).getHost();
        return new MaterialAlertDialogBuilder(activity)
            .setTitle(host == null ? "Mensagem do site" : "Site: " + host)
            .setMessage(message);
    }

    @Override public GeckoResult<PromptResponse> onAlertPrompt(GeckoSession session, AlertPrompt prompt) {
        if (!canShowPrompts.getAsBoolean()) return GeckoResult.fromValue(prompt.dismiss());
        Pending request = begin(prompt);
        return show(request, builder(prompt.message).setPositiveButton("OK",
            (dialog, which) -> finish(request, prompt::dismiss)));
    }

    @Override public GeckoResult<PromptResponse> onButtonPrompt(GeckoSession session, ButtonPrompt prompt) {
        if (!canShowPrompts.getAsBoolean()) return GeckoResult.fromValue(prompt.dismiss());
        Pending request = begin(prompt);
        return show(request, builder(prompt.message)
            .setPositiveButton("OK", (dialog, which) -> finish(request, () -> prompt.confirm(ButtonPrompt.Type.POSITIVE)))
            .setNegativeButton("Cancelar", (dialog, which) -> finish(request, () -> prompt.confirm(ButtonPrompt.Type.NEGATIVE))));
    }

    @Override public GeckoResult<PromptResponse> onTextPrompt(GeckoSession session, TextPrompt prompt) {
        if (!canShowPrompts.getAsBoolean()) return GeckoResult.fromValue(prompt.dismiss());
        Pending request = begin(prompt);
        EditText input = new EditText(activity);
        input.setText(prompt.defaultValue);
        input.setSelectAllOnFocus(true);
        return show(request, builder(prompt.message).setView(input)
            .setPositiveButton("OK", (dialog, which) -> finish(request, () -> prompt.confirm(input.getText().toString())))
            .setNegativeButton("Cancelar", (dialog, which) -> finish(request, prompt::dismiss)));
    }

    @Override public GeckoResult<PromptResponse> onBeforeUnloadPrompt(GeckoSession session, BeforeUnloadPrompt prompt) {
        if (!canShowPrompts.getAsBoolean()) return GeckoResult.fromValue(prompt.dismiss());
        Pending request = begin(prompt);
        return show(request, builder("Sair desta página? Alterações não salvas podem ser perdidas.")
            .setPositiveButton("Sair", (dialog, which) -> finish(request, () -> prompt.confirm(AllowOrDeny.ALLOW)))
            .setNegativeButton("Ficar", (dialog, which) -> finish(request, () -> prompt.confirm(AllowOrDeny.DENY))));
    }

    @Override public GeckoResult<PromptResponse> onRepostConfirmPrompt(GeckoSession session, RepostConfirmPrompt prompt) {
        if (!canShowPrompts.getAsBoolean()) return GeckoResult.fromValue(prompt.dismiss());
        Pending request = begin(prompt);
        return show(request, builder("Reenviar os dados deste formulário? Isso pode repetir uma operação.")
            .setPositiveButton("Reenviar", (dialog, which) -> finish(request, () -> prompt.confirm(AllowOrDeny.ALLOW)))
            .setNegativeButton("Cancelar", (dialog, which) -> finish(request, () -> prompt.confirm(AllowOrDeny.DENY))));
    }

    @Override public GeckoResult<PromptResponse> onFilePrompt(GeckoSession session, FilePrompt prompt) {
        if (!canShowPrompts.getAsBoolean()) return GeckoResult.fromValue(prompt.dismiss());
        // Directory traversal and direct camera capture need separate, reviewed implementations.
        if (filePickerInFlight || prompt.type == FilePrompt.Type.FOLDER)
            return GeckoResult.fromValue(prompt.dismiss());
        Pending request = begin(prompt);
        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*").putExtra(Intent.EXTRA_ALLOW_MULTIPLE, prompt.type == FilePrompt.Type.MULTIPLE)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ArrayList<String> types = new ArrayList<>();
        if (prompt.mimeTypes != null) for (String type : prompt.mimeTypes)
            if (type != null && type.contains("/")) types.add(type);
        if (!types.isEmpty()) picker.putExtra(Intent.EXTRA_MIME_TYPES, types.toArray(new String[0]));
        try {
            activity.startActivityForResult(picker, REQUEST_SITE_FILE);
            filePickerInFlight = true;
        } catch (ActivityNotFoundException | SecurityException error) {
            finish(request, prompt::dismiss);
        }
        return request.result;
    }

    boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_SITE_FILE) return false;
        filePickerInFlight = false;
        Pending request = pending;
        if (request == null || !(request.prompt instanceof FilePrompt)) return true;
        FilePrompt prompt = (FilePrompt) request.prompt;
        if (resultCode != Activity.RESULT_OK || data == null) {
            finish(request, prompt::dismiss);
            return true;
        }
        ArrayList<Uri> selected = new ArrayList<>();
        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++)
                selected.add(data.getClipData().getItemAt(i).getUri());
        } else if (data.getData() != null) selected.add(data.getData());
        // Accept only content selected through the Android picker, never arbitrary file/network URIs.
        selected.removeIf(uri -> uri == null || !"content".equals(uri.getScheme()));
        if (selected.isEmpty() || (prompt.type == FilePrompt.Type.SINGLE && selected.size() != 1)) {
            finish(request, prompt::dismiss);
        } else {
            // A provider can revoke its grant before Gecko resolves the content URI.
            finish(request, () -> prompt.confirm(activity, selected.toArray(new Uri[0])));
        }
        return true;
    }

    void close() {
        cancelPending();
        session.setPromptDelegate(null);
    }
}
