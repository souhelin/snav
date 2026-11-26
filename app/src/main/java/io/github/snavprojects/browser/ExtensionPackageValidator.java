package io.github.snavprojects.browser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Bounded structural preflight for an XPI staged in app-private storage.
 * This is not signature, manifest, or Gecko compatibility verification; the
 * GeckoView installer must still perform those checks and request consent.
 */
final class ExtensionPackageValidator {
    static final long MAX_ARCHIVE_BYTES = 20L * 1024 * 1024;
    private static final long MAX_UNPACKED_BYTES = 64L * 1024 * 1024;
    private static final long MAX_ENTRY_BYTES = 16L * 1024 * 1024;
    private static final long MAX_MANIFEST_BYTES = 1024L * 1024;
    private static final int MAX_ENTRIES = 4096;

    private ExtensionPackageValidator() {}

    static void validate(File archive) throws IOException {
        if (archive == null || !archive.isFile() || archive.length() <= 0
                || archive.length() > MAX_ARCHIVE_BYTES) {
            throw new IOException("Arquivo de extensão vazio, indisponível ou muito grande");
        }
        try (ZipFile zip = new ZipFile(archive)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            Set<String> names = new HashSet<>();
            boolean manifestFound = false;
            long total = 0;
            int count = 0;
            byte[] buffer = new byte[8192];
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (++count > MAX_ENTRIES || !safeName(entry.getName()) || !names.add(entry.getName())) {
                    throw new IOException("Arquivo de extensão contém entradas inválidas");
                }
                if (entry.isDirectory()) continue;
                boolean manifest = "manifest.json".equals(entry.getName());
                long limit = manifest ? MAX_MANIFEST_BYTES : MAX_ENTRY_BYTES;
                long expanded = 0;
                try (InputStream input = zip.getInputStream(entry)) {
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        expanded += read;
                        total += read;
                        if (expanded > limit || total > MAX_UNPACKED_BYTES) {
                            throw new IOException("Conteúdo da extensão excede o limite de tamanho");
                        }
                    }
                }
                if (manifest) {
                    if (expanded == 0) throw new IOException("Manifesto vazio");
                    manifestFound = true;
                }
            }
            if (!manifestFound) throw new IOException("Manifesto da extensão não encontrado");
        } catch (ZipException error) {
            throw new IOException("Arquivo de extensão inválido", error);
        }
    }

    private static boolean safeName(String name) {
        if (name == null || name.isEmpty() || name.startsWith("/") || name.indexOf('\\') >= 0
                || name.indexOf('\0') >= 0 || name.indexOf(':') >= 0) return false;
        for (String part : name.split("/", -1)) {
            if (".".equals(part) || "..".equals(part)) return false;
        }
        return true;
    }
}
