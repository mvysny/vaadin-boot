package com.github.mvysny.vaadinboot.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param value the value
     * @param message the message for {@link IllegalStateException#getMessage()}
     * @return the value
     * @param <T> the value type
     * @throws IllegalStateException if value is null.
     */
    @NotNull
    public static <T> T checkNotNull(@Nullable T value, @NotNull String message) {
        if (value == null) {
            throw new IllegalStateException(message);
        }
        return value;
    }
}
