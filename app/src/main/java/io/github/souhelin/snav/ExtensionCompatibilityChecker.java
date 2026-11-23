package io.github.souhelin.snav;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Conservative preflight. Gecko still decides signature, manifest and compatibility. */
final class ExtensionCompatibilityChecker {
    private ExtensionCompatibilityChecker() {}

    static List<String> inspect(File file) throws IOException {
        try (ZipFile zip = new ZipFile(file)) {
            ZipEntry entry = zip.getEntry("manifest.json");
            if (entry == null) throw new IOException("Manifesto não encontrado");
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            try (InputStream input = zip.getInputStream(entry)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    if (content.size() + read > 1024 * 1024) throw new IOException("Manifesto muito grande");
                    content.write(buffer, 0, read);
                }
            }
            JSONObject manifest = new JSONObject(content.toString("UTF-8"));
            int version = manifest.optInt("manifest_version", -1);
            if (version != 2 && version != 3) throw new IOException("Versão de manifesto não reconhecida");
            List<String> warnings = new ArrayList<>();
            JSONArray permissions = manifest.optJSONArray("permissions");
            JSONArray optional = manifest.optJSONArray("optional_permissions");
            if (contains(permissions, "menus") || contains(permissions, "contextMenus")
                || contains(optional, "menus") || contains(optional, "contextMenus"))
                warnings.add("Menus de contexto de extensões ainda não são exibidos pelo Snav.");
            if (contains(permissions, "tabs") || contains(optional, "tabs"))
                warnings.add("A integração de abas é parcial; criar ou gerenciar abas pela extensão pode não funcionar.");
            if (contains(permissions, "nativeMessaging") || contains(optional, "nativeMessaging"))
                warnings.add("O Snav não oferece um canal genérico de mensagens nativas para extensões de terceiros.");
            if (contains(permissions, "declarativeNetRequest") || contains(optional, "declarativeNetRequest"))
                warnings.add("Verifique a compatibilidade de declarativeNetRequest com esta versão do GeckoView.");
            JSONObject background = manifest.optJSONObject("background");
            if (background != null && background.has("service_worker"))
                warnings.add("Service workers de extensões podem não funcionar no Gecko; verifique o manifesto para Firefox.");
            if (manifest.has("sidebar_action") || manifest.has("sidebarAction"))
                warnings.add("A barra lateral de extensões não está disponível no Snav.");
            return Collections.unmodifiableList(warnings);
        } catch (JSONException error) { throw new IOException("Manifesto inválido", error); }
    }

    private static boolean contains(JSONArray values, String name) {
        if (values == null) return false;
        for (int i = 0; i < values.length(); i++) if (name.equals(values.optString(i))) return true;
        return false;
    }
}
