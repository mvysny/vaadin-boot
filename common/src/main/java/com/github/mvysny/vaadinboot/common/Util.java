package com.github.mvysny.vaadinboot.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Internal utility methods.
 */
public final class Util {
    /**
     * Not meant to be instantiated.
     */
    private Util() {
    }

    /**
     * Checks that value isn't null; if it is, throws {@link IllegalStateException}.
     *
     * @param value   the value
     * @param message the message for {@link IllegalStateException#getMessage()}
     * @param <T>     the value type
     * @return the value
     * @throws IllegalStateException if value is null.
     */
    @NotNull
    public static <T> T checkNotNull(@Nullable T value, @NotNull String message) {
        if (value == null) {
            throw new IllegalStateException(message);
        }
        return value;
    }

    /**
     * If given URL is a <code>file://</code> URL, converts it to
     * {@link File}.
     * @param url the URL to convert to.
     * @return {@link File} if the URL is a local URL, null if it's a remote URL.
     */
    @Nullable
    public static File toFile(@NotNull final URL url) {
        if (!"file".equalsIgnoreCase(url.getProtocol())) {
            return null;
        }
        final String fileName = url.getFile().replace('/', File.separatorChar);
        return new File(fileName);
    }

    /**
     * Downloads the file from given URL and returns it as a String.
     * @param url the URL to download from.
     * @param charset the charset to use.
     * @return the file contents
     * @throws IOException
     */
    @NotNull
    public static String toString(@NotNull URL url, @NotNull Charset charset) throws IOException {
        try (InputStream is = url.openStream()) {
            return new String(is.readAllBytes(), charset);
        }
    }
}
