
package com.amalitech;

import com.amalitech.exceptions.ValidationException;
import com.amalitech.util.RegexValidators;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class RegexValidationTest {

    //Student ID
    @Test
    public void studentId_valid_10plus() throws ValidationException {
        List<String> ids = Arrays.asList(
                "STU001","STU042","STU999","stu123","Stu000","stU987",
                "STU100","STU200","STU300","STU777","STU555","STU888"
        );
        for (String id : ids) {
            String v = RegexValidators.requireStudentId(id);
            assertTrue(v.startsWith("STU"));
            assertEquals(6, v.length());
        }
    }



    // Email
    @Test
    public void email_valid_10plus() throws ValidationException {
        List<String> emails = Arrays.asList(
                "alice@school.edu","bob.smith@university.edu","jsmith@college.org",
                "user.name+tag@domain.com","u@d.io","first_last@dept.school.edu",
                "abc123@host.net","john-doe@org.co","x@y.zw","foo.bar@sub.domain.com"
        );
        for (String e : emails) assertEquals(e, RegexValidators.requireEmail(e));
    }



    //  Phone
    @Test
    public void phone_valid_10plus() throws ValidationException {
        List<String> phones = Arrays.asList(
                "(123) 456-7890","123-456-7890","+1-123-456-7890","1234567890",
                "(555) 000-0000","555-111-2222","+233-024-123-4567","0000000000",
                "(024) 111-2222","+44-020-123-4567"
        );
        for (String p : phones) assertEquals(p, RegexValidators.requirePhone(p));
    }

    @Test
    public void phone_invalid_10plus() {
        List<String> bad = Arrays.asList(
                "123-4567", "(12) 345-6789", "123-456-789", "abcdefghij",
                "123 456 7890", "+1-123-4567", "+-123-456-7890", "",
                "   ", "123456789" // 9 digits
        );
        for (String p : bad) {
            try {
                RegexValidators.requirePhone(p);
                fail("Expected ValidationException for: " + p);
            } catch (ValidationException expected) { /* ok */ }
        }
    }

    //  Name
    @Test
    public void name_valid_10plus() throws ValidationException {
        List<String> names = Arrays.asList(
                "John Smith","Mary-Jane O'Connor","Ama Boateng","Kwame Nkrumah",
                "Alice Johnson","Bob Smith","Carol Martinez","David Chen",
                "Emma Wilson","Kofi Mensah","Yaw Osei"
        );
        for (String n : names) assertEquals(n, RegexValidators.requireName(n));
    }



    //  Date
    @Test
    public void date_valid_10plus() throws ValidationException {
        List<String> dates = Arrays.asList(
                "2024-01-01","2023-12-31","2025-11-03","2000-02-29",
                "2012-02-29","1999-03-15","2020-06-30","2022-07-07",
                "2030-10-10","2010-05-05"
        );
        for (String d : dates) assertEquals(d, RegexValidators.requireDateYYYYMMDD(d));
    }



    //  Edge cases
    @Test
    public void emptyStrings_rejected() {
        List<String> empties = Arrays.asList("", "   ");
        for (String s : empties) {
            try { RegexValidators.requireStudentId(s); fail(); } catch (ValidationException ignored) {}
            try { RegexValidators.requireEmail(s);     fail(); } catch (ValidationException ignored) {}
            try { RegexValidators.requirePhone(s);     fail(); } catch (ValidationException ignored) {}
            try { RegexValidators.requireName(s);      fail(); } catch (ValidationException ignored) {}
            try { RegexValidators.requireDateYYYYMMDD(s); fail(); } catch (ValidationException ignored) {}
        }
    }

    //  Pattern compilation performance
    @Test
    public void compiledPattern_isFaster_thanCompileEachTime() throws ValidationException {
        // Compare precompiled email pattern usage vs compiling on every match
        String sample = "user.name+tag@domain.com";
        // warm-up
        RegexValidators.requireEmail(sample);

        // Precompiled runs
        long t0 = System.nanoTime();
        for (int i = 0; i < 10000; i++) RegexValidators.requireEmail(sample);
        long preNs = System.nanoTime() - t0;

        // Compile each time
        t0 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            Pattern p = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            assertTrue(p.matcher(sample).matches());
        }
        long eachNs = System.nanoTime() - t0;

        // Precompiled should be meaningfully faster;
        assertTrue("Precompiled should be faster: pre=" + preNs + " ns, each=" + eachNs + " ns",
                preNs < eachNs);
    }

    //  Capturing groups extraction (email domain)
    @Test
    public void capturingGroups_extractEmailDomain() {
        Pattern domainCapture = Pattern.compile("^[^@]+@([A-Za-z0-9.-]+)$");
        Matcher m = domainCapture.matcher("alice@school.edu");
        assertTrue(m.find());
        assertEquals("school.edu", m.group(1));
    }
}
