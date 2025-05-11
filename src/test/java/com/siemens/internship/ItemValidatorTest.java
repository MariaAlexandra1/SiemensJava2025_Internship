package com.siemens.internship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the ItemValidator class.
 * This class tests the email validation logic of the ItemValidator class.
 * Tests for ItemValidator reach 100% coverage.
 * */

public class ItemValidatorTest {
    private final ItemValidator validator = new ItemValidator();
    /**
     * Email string validation rules:
     * - start with a letter
     * - contain only letters, digits, dot, underscore, or "-" before '@'
     * - have characters after '@'
     * - contain a dot in the domain part
     */
    @Test
    void validEmails() {
        assertTrue(validator.validateItemEmail("user_123@google.com"));
        assertTrue(validator.validateItemEmail("Z-First.Last@subdomain.ro"));
    }

    @Test
    void invalidEmails() {
        // does not start with letter
        assertFalse(validator.validateItemEmail("1abc@example.com"));
        // missing '@'
        assertFalse(validator.validateItemEmail("user.google.com"));
        // invalid domain
        assertFalse(validator.validateItemEmail("user@.com"));
        // invalid characters
        assertFalse(validator.validateItemEmail("user!@google.com"));
    }
}
