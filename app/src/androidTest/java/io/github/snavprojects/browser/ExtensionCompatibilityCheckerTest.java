package io.github.snavprojects.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExtensionCompatibilityCheckerTest {
    private File makeXpi(String manifest) throws IOException {
        File f = File.createTempFile("compat-test", ".xpi", InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir());
        try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(f))) {
            output.putNextEntry(new ZipEntry("manifest.json"));
            output.write(manifest.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return f;
    }
    @Test public void warnsAboutUnsupportedEmbedderFeatures() throws Exception {
        File f = makeXpi("{\"manifest_version\":3,\"permissions\":[\"tabs\",\"menus\",\"nativeMessaging\"],\"background\":{\"service_worker\":\"bg.js\"}}");
        try {
            List<String> warnings = ExtensionCompatibilityChecker.inspect(f);
            assertEquals(4, warnings.size());
            assertTrue(warnings.get(0).contains("contexto"));
        } finally { f.delete(); }
    }
    @Test public void rejectsMalformedOrUnsupportedManifests() throws Exception {
        for (String manifest : new String[]{"{", "{\"manifest_version\":4}"}) {
            File f = makeXpi(manifest);
            try {
                try { ExtensionCompatibilityChecker.inspect(f); fail("Manifesto aceito"); }
                catch (IOException expected) { }
            } finally { f.delete(); }
        }
    }
}
