package com.library.infrastructure.jdbc.dao;

import java.time.LocalDate;
import java.util.UUID;

/** Sirov red iz tabele loans. returnedAt je null ako knjiga nije vraćena. */
public record LoanRow(UUID id, long bookId, long memberId,
                      LocalDate borrowedAt, LocalDate dueDate, LocalDate returnedAt) {}
