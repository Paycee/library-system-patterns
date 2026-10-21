package com.library;

import com.library.ui.ConsoleApp;

public class MainMongo {
    public static void main(String[] args) {

        try (var app = LibraryApp.withMongo()) {
            new ConsoleApp(app.service(), app.circulation()).run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}