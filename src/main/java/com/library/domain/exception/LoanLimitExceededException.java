package com.library.domain.exception;

/** Član je dostigao maksimalan broj knjiga. */
public class LoanLimitExceededException extends LibraryException {
    public LoanLimitExceededException(String message) {
        super(message);
    }
}
