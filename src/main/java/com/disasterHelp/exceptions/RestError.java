package com.disasterHelp.exceptions;

public record RestError(
    int cod,
    String message
) {}
