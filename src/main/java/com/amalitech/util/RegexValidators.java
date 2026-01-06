
package com.amalitech.util;

import com.amalitech.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Centralized, regex-based input validators.
 * All patterns are compiled once and reused.
 */
public final class RegexValidators {
    private RegexValidators() {}

    // STU followed by exactly 3 digits (case-insensitive acceptable input, normalized to uppercase)
    private static final Pattern STUDENT_ID = Pattern.compile("^STU\\d{3}$", Pattern.CASE_INSENSITIVE);

    // Name: letters + spaces + hyphens + apostrophes (no digits). Examples: John Smith, Mary-Jane O'Connor
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z'\\- ]*[A-Za-z]$");

    // Email: username@domain.extension (simple, robust)
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Phone: four accepted formats:
    // (123) 456-7890   |  123-456-7890   |  +1-123-456-7890   |  1234567890
    private static final Pattern PHONE1 = Pattern.compile("^\\(\\d{3}\\) \\d{3}-\\d{4}$");
    private static final Pattern PHONE2 = Pattern.compile("^\\d{3}-\\d{3}-\\d{4}$");
    private static final Pattern PHONE3 = Pattern.compile("^\\+\\d{1,3}-\\d{3}-\\d{3}-\\d{4}$");
    private static final Pattern PHONE4 = Pattern.compile("^\\d{10}$");

    // Date: strict YYYY-MM-DD, with logical date check
    private static final Pattern DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    public static String requireStudentId(String input) throws ValidationException {
        String v = safeTrim(input).toUpperCase();
        if (!STUDENT_ID.matcher(v).matches()) {
            throw new ValidationException(
                    "X VALIDATION ERROR: Invalid Student ID format\n" +
                            "  Pattern required: STU### (STU followed by exactly 3 digits)\n" +
                            "  Examples: STU001, STU042, STU999\n" +
                            "  Your input: " + safeTrim(input)
            );
        }
        return v;
    }

    public static String requireName(String input) throws ValidationException {
        String v = safeTrim(input);
        if (!NAME.matcher(v).matches()) {
            throw new ValidationException(
                    "X VALIDATION ERROR: Invalid name format\n" +
                            "  Pattern required: Only letters, spaces, hyphens, and apostrophes\n" +
                            "  Examples: John Smith, Mary-Jane O'Connor\n" +
                            "  Your input: " + v + (containsDigits(v) ? " (contains digits)" : "")
            );
        }
        return v;
    }

    public static String requireEmail(String input) throws ValidationException {
        String v = safeTrim(input);
        if (!EMAIL.matcher(v).matches()) {
            throw new ValidationException(
                    "X VALIDATION ERROR: Invalid email format\n" +
                            "  Pattern required: username@domain.extension\n" +
                            "  Examples: john.smith@university.edu, jsmith@college.org\n" +
                            "  Your input: " + v
            );
        }
        return v;
    }

    public static String requirePhone(String input) throws ValidationException {
        String v = safeTrim(input);
        if (!(PHONE1.matcher(v).matches() || PHONE2.matcher(v).matches()
                || PHONE3.matcher(v).matches() || PHONE4.matcher(v).matches())) {
            throw new ValidationException(
                    "X VALIDATION ERROR: Invalid phone format\n" +
                            "  Accepted patterns:\n" +
                            "    - (123) 456-7890\n" +
                            "    - 123-456-7890\n" +
                            "    - +1-123-456-7890\n" +
                            "    - 1234567890\n" +
                            "  Your input: " + v
            );
        }
        return v;
    }

    public static String requireDateYYYYMMDD(String input) throws ValidationException {
        String v = safeTrim(input);
        if (!DATE.matcher(v).matches()) {
            throw new ValidationException(
                    "X VALIDATION ERROR: Invalid date format\n" +
                            "  Pattern required: YYYY-MM-DD\n" +
                            "  Example: 2024-11-03\n" +
                            "  Your input: " + v + " (wrong separators)"
            );
        }
        // logical check
        try { LocalDate.parse(v); }
        catch (DateTimeParseException dpe) {
            throw new ValidationException(
                    "X VALIDATION ERROR: Invalid calendar date\n" +
                            "  Pattern required: YYYY-MM-DD\n" +
                            "  Example: 2024-11-03\n" +
                            "  Your input: " + v
            );
        }
        return v;
    }

    private static String safeTrim(String s) { return (s == null) ? "" : s.trim(); }
    private static boolean containsDigits(String s) { return s.chars().anyMatch(Character::isDigit); }
}

