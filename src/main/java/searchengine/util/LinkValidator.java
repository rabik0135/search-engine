package searchengine.util;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

@Component
public class LinkValidator {
    private static final Pattern FILE_EXTENSION_PATTERN =
                Pattern.compile(".*\\.(zip|sql|yaml|pdf|png|jpg|jpeg|gif|svg|mp4|mp3|json|xml|csv|docx?)$");

    public boolean isValid(String url, String baseUrl) {
        if (url==null || url.isEmpty() || url.contains("#")) {
            return false;
        }

        if (isFile(url)) {
            return false;
        }

        if (!url.startsWith(baseUrl)) {
            return false;
        }

        try {
            URI uri = new URI(url).normalize();
            URI baseUri = new URI(baseUrl).normalize();
            return uri.getHost().equalsIgnoreCase(baseUri.getHost());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean isFile(String url) {
        return FILE_EXTENSION_PATTERN.matcher(url.toLowerCase()).matches();
    }
}
