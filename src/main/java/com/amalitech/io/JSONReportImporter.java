
package com.amalitech.io;

import com.amalitech.Grade;
import com.amalitech.Subject;
import com.amalitech.CoreSubject;
import com.amalitech.ElectiveSubject;
import com.amalitech.reporting.StudentReport;
import com.amalitech.exceptions.ImportFailedException;
import com.amalitech.exceptions.JsonFormatException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JSONReportImporter implements Importer<StudentReport> {

    private static final Pattern STR = Pattern.compile("\"%s\"\\s*:\\s*\"(.*?)\"");
    private static final Pattern NUM = Pattern.compile("\"%s\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    // grade object matcher (greedy-safe, DOTALL)
    private static final Pattern GRADE_OBJ = Pattern.compile(
            "\\{\\s*\"id\"\\s*:\\s*\"(.*?)\".*?\"date\"\\s*:\\s*\"(.*?)\".*?\"subject\"\\s*:\\s*\\{\\s*\"name\"\\s*:\\s*\"(.*?)\"\\s*,\\s*\"type\"\\s*:\\s*\"(.*?)\"\\s*}\\s*,\\s*\"grade\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*}",
            Pattern.DOTALL);

    @Override
    public StudentReport importFrom(Path source) throws ImportFailedException, JsonFormatException {
        if (source == null || !Files.exists(source) || !Files.isRegularFile(source)) {
            throw new ImportFailedException("JSON file not found: " + source);
        }

        String json;
        try {
            json = Files.readString(source, StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            throw new ImportFailedException("Failed reading JSON: " + source.toAbsolutePath(), ioe);
        }

        String sid   = findStr(json, "id");
        String name  = findStr(json, "name");
        String email = findStr(json, "email");
        String phone = findStr(json, "phone");
        String stype = findStr(json, "type");

        if (sid == null || name == null) {
            throw new JsonFormatException("Missing required student fields (id/name).");
        }

        List<Grade> grades = new ArrayList<>();
        Matcher m = GRADE_OBJ.matcher(json);
        while (m.find()) {

            String subjName = m.group(3);
            String subjType = m.group(4);
            double gradeVal = Double.parseDouble(m.group(5));

            Subject subject = "Core".equalsIgnoreCase(subjType)
                    ? new CoreSubject(subjName, "C" + (int)(Math.random()*1000))
                    : new ElectiveSubject(subjName, "E" + (int)(Math.random()*1000));

            grades.add(new Grade(sid, subject, gradeVal));
        }

        // aggregates
        Double coreAvg   = findNum(json, "coreAverage");
        Double elecAvg   = findNum(json, "electiveAverage");
        Double overall   = findNum(json, "overallAverage");
        double c = coreAvg   == null ? 0.0 : coreAvg;
        double e = elecAvg   == null ? 0.0 : elecAvg;
        double o = overall   == null ? 0.0 : overall;

        return new StudentReport(
                "Honors Student".equalsIgnoreCase(stype)
                        ? new com.amalitech.HonorsStudent(name, 16, email, phone)
                        : new com.amalitech.RegularStudent(name, 16, email, phone),
                grades, c, e, o, phone
        );
    }

    private static String findStr(String json, String key) {
        Pattern p = Pattern.compile(String.format(STR.pattern(), Pattern.quote(key)));
        Matcher m = p.matcher(json);
        return m.find() ? unescape(m.group(1)) : null;
    }

    private static Double findNum(String json, String key) {
        Pattern p = Pattern.compile(String.format(NUM.pattern(), Pattern.quote(key)));
        Matcher m = p.matcher(json);
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\\", "\\").replace("\\\"", "\"");
    }
}
