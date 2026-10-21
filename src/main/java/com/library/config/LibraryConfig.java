package com.library.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class LibraryConfig {
    private final Properties props = new Properties();

    private LibraryConfig() {
        try (InputStream in = LibraryConfig.class.getResourceAsStream("/library.properties")) {
            if (in == null) {
                throw new IllegalStateException("Fajl library.properties nije pronađen");
            }
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Greška pri čitanju library.properties", e);
        }
    }

    private static class Holder {
        private static final LibraryConfig INSTANCE = new LibraryConfig();
    }

    public static LibraryConfig getInstance() {
        return Holder.INSTANCE;
    }

    public String mysqlUrl()       { return props.getProperty("mysql.url"); }
    public String mysqlUser()      { return props.getProperty("mysql.user"); }
    public String mysqlPassword()  { return props.getProperty("mysql.password"); }
    public String mongoUri()       { return props.getProperty("mongo.uri"); }
    public int maxBooksPerMember() {
        return Integer.parseInt(props.getProperty("library.maxBooksPerMember", "3"));
    }
    public int maxFee()            { return Integer.parseInt(props.getProperty("fee.max", "500")); }
}