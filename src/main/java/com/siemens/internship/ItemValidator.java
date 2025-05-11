package com.siemens.internship;

import java.util.regex.Pattern;

public class ItemValidator {
    // Compile the regex pattern for email validation once for performance
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9._-]*@[A-Za-z0-9_-]+\\.[A-Za-z]+$");

    // Validates the format of the provided email string against our pattern
    public boolean validateItemEmail(String email){
        if(email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}