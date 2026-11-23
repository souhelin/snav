package io.github.souhelin.snav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

public class ExtensionDownloadPolicyTest {
    private void reject(String value) {
        try { ExtensionDownloadPolicy.parse(value); fail("Address accepted: " + value); }
        catch (URISyntaxException expected) { }
    }
    @Test public void requiresHttpsWithoutCredentialsOrFragment() throws Exception {
        assertEquals("example.com", ExtensionDownloadPolicy.parse("https://example.com/addon.xpi").getHost());
        reject("http://example.com/a"); reject("https://user:pass@example.com/a");
        reject("https://example.com/a#fragment"); reject("file:///tmp/a"); reject("https://example.com/a b");
    }
    @Test public void validatesEachRedirect() throws Exception {
        URI base = ExtensionDownloadPolicy.parse("https://example.com/a/b");
        assertEquals("https://example.com/a/x.xpi", ExtensionDownloadPolicy.redirect(base, "x.xpi").toString());
        try { ExtensionDownloadPolicy.redirect(base, "http://example.com/a"); fail("Downgrade accepted"); }
        catch (URISyntaxException expected) { }
        try { ExtensionDownloadPolicy.redirect(base, "//user:pass@evil.example/a"); fail("Credentials accepted"); }
        catch (URISyntaxException expected) { }
    }
}
