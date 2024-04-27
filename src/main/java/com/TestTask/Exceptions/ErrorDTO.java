package com.TestTask.Exceptions;

import java.sql.Timestamp;

public record ErrorDTO(
        Timestamp timestamp,
        int status,
        String error,
        String message,
        String path) {}
