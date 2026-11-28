package io.github.souhelin.snav;

import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Test;

public class ExtensionPackageValidatorTest {
    private File archive(String[] names, byte[][] content) throws IOException {
        File file = File.createTempFile("snav-extension-test", ".xpi");
        file.deleteOnExit();
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file))) {
            for (int i = 0; i < names.length; i++) {
                zip.putNextEntry(new ZipEntry(names[i]));
                zip.write(content[i]);
                zip.closeEntry();
            }
        }
        return file;
    }

    private void rejected(File file) throws IOException {
        try { ExtensionPackageValidator.validate(file); fail("O arquivo deveria ser rejeitado"); }
        catch (IOException expected) { /* rejection */ }
    }

    @Test public void acceptsStructurallyValidArchiveButDoesNotClaimSignatureValidity() throws IOException {
        File f = archive(new String[]{"manifest.json", "background.js"},
            new byte[][]{"{}".getBytes(StandardCharsets.UTF_8), "".getBytes(StandardCharsets.UTF_8)});
        ExtensionPackageValidator.validate(f);
    }

    @Test public void rejectsMissingOrEmptyManifest() throws IOException {
        rejected(archive(new String[]{"background.js"}, new byte[][]{{1}}));
        rejected(archive(new String[]{"manifest.json"}, new byte[][]{{}}));
    }

    @Test public void rejectsTraversalAndBackslashPaths() throws IOException {
        byte[] manifest = "{}".getBytes(StandardCharsets.UTF_8);
        rejected(archive(new String[]{"manifest.json", "../payload"}, new byte[][]{manifest, {1}}));
        rejected(archive(new String[]{"manifest.json", "folder\\payload"}, new byte[][]{manifest, {1}}));
    }

    @Test public void rejectsInvalidArchive() throws IOException {
        File f = File.createTempFile("snav-extension-test", ".xpi");
        f.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(f)) { out.write(new byte[]{1, 2, 3}); }
        rejected(f);
    }

    @Test public void rejectsOversizedManifestAfterDecompression() throws IOException {
        byte[] large = new byte[1024 * 1024 + 1];
        rejected(archive(new String[]{"manifest.json"}, new byte[][]{large}));
    }
    @Test public void rejectsOversizedArchive() throws IOException {
        File f = File.createTempFile("snav-extension-test", ".xpi");
        f.deleteOnExit();
        try (java.io.RandomAccessFile out = new java.io.RandomAccessFile(f, "rw")) {
            out.setLength(ExtensionPackageValidator.MAX_ARCHIVE_BYTES + 1);
        }
        rejected(f);
    }

    @Test public void rejectsDuplicateNames() throws IOException {
        byte[] manifest = "{}".getBytes(StandardCharsets.UTF_8);
        File f = archive(new String[]{"manifest.json", "payload1", "payload2"},
            new byte[][]{manifest, {1}, {2}});
        byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
        byte[] oldName = "payload2".getBytes(StandardCharsets.UTF_8);
        byte[] newName = "payload1".getBytes(StandardCharsets.UTF_8);
        int replacements = 0;
        for (int i = 0; i <= bytes.length - oldName.length; i++) {
            int j = 0;
            while (j < oldName.length && bytes[i + j] == oldName[j]) j++;
            if (j == oldName.length) {
                System.arraycopy(newName, 0, bytes, i, newName.length);
                replacements++;
                i += oldName.length - 1;
            }
        }
        if (replacements != 2) fail("O teste deve alterar cabeçalhos locais e centrais");
        java.nio.file.Files.write(f.toPath(), bytes);
        rejected(f);
    }

}
