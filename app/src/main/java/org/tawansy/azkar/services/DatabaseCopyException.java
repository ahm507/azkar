package org.tawansy.azkar.services;

public class DatabaseCopyException extends Throwable {
    public DatabaseCopyException(String message, Throwable exception) {
        super(message, exception);
    }
}
