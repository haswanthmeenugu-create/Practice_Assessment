package com.acme.salary.service;

/** Thrown when a uniqueness constraint (e.g. email) would be violated. Maps to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
