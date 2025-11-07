package com.example.carbon;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SignUpValidationTest {

    private SignUpActivity signUp;

    @Before
    public void setup() {
        signUp = new SignUpActivity();
    }

    @Test
    public void testValidEmail() {
        assertTrue(signUp.validateEmail("mats@test.com"));
    }

    @Test
    public void testInvalidEmail() {
        assertFalse(signUp.validateEmail("mats@@test..com"));
        assertFalse(signUp.validateEmail("invalidemail"));
    }

    @Test
    public void testValidPhone() {
        assertTrue(signUp.validatePhone("5875556789"));
    }

    @Test
    public void testInvalidPhone() {
        assertFalse(signUp.validatePhone("123"));
        assertFalse(signUp.validatePhone("587-5556789"));
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(signUp.validatePasswords("abcdefgh", "abcdefgh"));
        assertFalse(signUp.validatePasswords("short", "short"));
        assertFalse(signUp.validatePasswords("abcdefgh", "abcdzzzz"));
    }
}
