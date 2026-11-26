package io.github.snavprojects.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Instrumentation;
import android.net.Uri;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/** Exercises actual Gecko operations, not the manager's Android permission dialogs. */
@RunWith(AndroidJUnit4.class)
public class ExtensionGeckoIntegrationTest {
    private static final String ID = "{3d31c3e3-273e-4099-88d5-a135b62be61e}";
    private static final String UNSIGNED_ID = "borderify@example.com";
    private static final Instrumentation INSTRUMENTATION = InstrumentationRegistry.getInstrumentation();
    private ActivityScenario<MainActivity> scenario;
    private WebExtensionController controller;
    private File signed;
    private File unsigned;
    private final AtomicInteger prompts = new AtomicInteger();
    private WebExtensionController.PromptDelegate testDelegate;
    private boolean mayRemoveFixtures;

    @Before public void setUp() throws Exception {
        scenario = ActivityScenario.launch(MainActivity.class);
        INSTRUMENTATION.runOnMainSync(() -> controller = GeckoEngineController.getRuntime().getWebExtensionController());
        // A Activity can be ready before Gecko registers the extension list handler.
        // Retry startup failures for a bounded period; do not skip a failed test.
        long deadline = android.os.SystemClock.uptimeMillis() + 20000;
        Exception last = null;
        do {
            try { findFixture(ID); last = null; break; }
            catch (Exception error) { last = error; android.os.SystemClock.sleep(500); }
        } while (android.os.SystemClock.uptimeMillis() < deadline);
        if (last != null) throw last;
        assertNull("Use uma instalação isolada: a extensão de teste já existe", findFixture(ID));
        assertNull("Use uma instalação isolada: a extensão sem assinatura já existe", findFixture(UNSIGNED_ID));
        mayRemoveFixtures = true;
        signed = fixture("borderify.xpi");
        unsigned = fixture("borderify-unsigned.xpi");
    }

    @After public void tearDown() throws Exception {
        try { if (controller != null && mayRemoveFixtures) removeFixtureIfInstalled(); }
        finally {
            INSTRUMENTATION.runOnMainSync(() -> {
                if (controller != null && controller.getPromptDelegate() == testDelegate)
                    controller.setPromptDelegate(null);
            });
            if (signed != null) signed.delete();
            if (unsigned != null) unsigned.delete();
            if (scenario != null) scenario.close();
        }
    }

    private File fixture(String name) throws Exception {
        File file = File.createTempFile("snav-gecko-test-", ".xpi", INSTRUMENTATION.getTargetContext().getCacheDir());
        try (InputStream in = INSTRUMENTATION.getContext().getAssets().open("web_extensions/" + name);
             FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            int n; while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        }
        return file;
    }

    private void decide(boolean allow) {
        testDelegate = new WebExtensionController.PromptDelegate() {
            @Override public GeckoResult<WebExtension.PermissionPromptResponse> onInstallPromptRequest(
                    WebExtension ext, String[] permissions, String[] origins, String[] data) {
                prompts.incrementAndGet();
                return GeckoResult.fromValue(new WebExtension.PermissionPromptResponse(allow, false, false));
            }
        };
        INSTRUMENTATION.runOnMainSync(() -> controller.setPromptDelegate(testDelegate));
    }

    private static <T> T await(Supplier<GeckoResult<T>> operation) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        INSTRUMENTATION.runOnMainSync(() -> {
            try {
                operation.get().accept(value -> { result.set(value); done.countDown(); },
                    failure -> { error.set(failure); done.countDown(); });
            } catch (Throwable failure) { error.set(failure); done.countDown(); }
        });
        assertTrue("O Gecko não concluiu a operação", done.await(60, TimeUnit.SECONDS));
        if (error.get() != null) throw new Exception("Operação Gecko falhou", error.get());
        return result.get();
    }

    private WebExtension findFixture(String id) throws Exception {
        List<WebExtension> installed = await(() -> controller.list());
        for (WebExtension extension : installed) if (id.equals(extension.id)) return extension;
        return null;
    }

    private WebExtension findFixture() throws Exception { return findFixture(ID); }

    private void removeFixtureIfInstalled() throws Exception {
        for (String id : new String[]{ID, UNSIGNED_ID}) {
            WebExtension extension = findFixture(id);
            if (extension != null) await(() -> controller.uninstall(extension));
        }
    }

    @Test public void consentStateAndActivityRecreation() throws Exception {
        decide(false);
        try {
            await(() -> controller.install(Uri.fromFile(signed).toString(), WebExtensionController.INSTALLATION_METHOD_FROM_FILE));
            fail("A instalação recusada não deveria ser concluída");
        } catch (Exception expected) {
            assertTrue("O Gecko deveria solicitar consentimento", prompts.get() > 0);
            assertInstallError(expected, WebExtension.InstallException.ErrorCodes.ERROR_USER_CANCELED);
        }
        assertNull(findFixture());

        decide(true);
        WebExtension installed = await(() -> controller.install(Uri.fromFile(signed).toString(), WebExtensionController.INSTALLATION_METHOD_FROM_FILE));
        assertNotNull(installed);
        assertEquals(ID, installed.id);
        assertTrue(installed.metaData.enabled);
        assertTrue(prompts.get() >= 2);
        WebExtension disabled = await(() -> controller.disable(installed, WebExtensionController.EnableSource.USER));
        assertFalse(disabled.metaData.enabled);
        assertFalse(findFixture().metaData.enabled);
        scenario.recreate();
        assertFalse("O estado deve sobreviver à recriação da Activity", findFixture().metaData.enabled);
        WebExtension reloaded = findFixture();
        WebExtension enabled = await(() -> controller.enable(reloaded, WebExtensionController.EnableSource.USER));
        assertTrue(enabled.metaData.enabled);
        await(() -> controller.uninstall(enabled));
        assertNull(findFixture());
    }

    @Test public void unsignedPackageIsRejected() throws Exception {
        decide(true);
        try {
            await(() -> controller.install(Uri.fromFile(unsigned).toString(), WebExtensionController.INSTALLATION_METHOD_FROM_FILE));
            fail("O Gecko não deveria instalar um pacote sem assinatura");
        } catch (Exception expected) {
            assertInstallError(expected, WebExtension.InstallException.ErrorCodes.ERROR_SIGNEDSTATE_REQUIRED);
            assertNull(findFixture(UNSIGNED_ID));
        }
    }

    private static void assertInstallError(Exception error, int expectedCode) {
        assertTrue("O Gecko deve informar o motivo da falha", error.getCause() instanceof WebExtension.InstallException);
        assertEquals(expectedCode, ((WebExtension.InstallException) error.getCause()).code);
    }
}
