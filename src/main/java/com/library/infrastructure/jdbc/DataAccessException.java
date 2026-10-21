package com.library.infrastructure.jdbc;

/**
 * Sopstveni unchecked izuzetak u koji se prevodi SQLException.
 * Tako tehnologija (JDBC) ne "curi" kroz interfejs repository-ja.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
