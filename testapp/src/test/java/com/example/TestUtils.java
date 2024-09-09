package com.example;

import kotlin.text.Charsets;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

public class TestUtils {
    @NotNull
    public static String wget(@NotNull String url) throws IOException {
        return IOUtils.toString(new URL(url), Charsets.UTF_8);
    }
}
