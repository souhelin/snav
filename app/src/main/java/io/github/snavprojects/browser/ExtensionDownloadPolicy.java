package io.github.snavprojects.browser;

import java.net.URI;
import java.net.URISyntaxException;

/** Validate every hop before requesting a package, without forwarding credentials. */
final class ExtensionDownloadPolicy {
    private ExtensionDownloadPolicy() {}

    static URI parse(String value) throws URISyntaxException {
        if (value == null || value.length() > 8192 || value.matches(".*\\s+.*"))
            throw new URISyntaxException(String.valueOf(value), "Invalid address");
        URI uri = new URI(value);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getFragment() != null
                || uri.getPort() > 65535) throw new URISyntaxException(value, "HTTPS address required");
        return uri;
    }

    static URI redirect(URI from, String location) throws URISyntaxException {
        if (location == null || location.length() > 8192) throw new URISyntaxException("", "Missing redirect");
        return parse(from.resolve(new URI(location)).toString());
    }
}
