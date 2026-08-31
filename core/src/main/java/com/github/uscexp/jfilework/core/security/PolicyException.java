package com.github.uscexp.jfilework.core.security;

/**
 * Raised when a configured action violates a pragmatic security policy.
 */
public class PolicyException extends RuntimeException {

    public PolicyException(String message) {
        super(message);
    }
}

