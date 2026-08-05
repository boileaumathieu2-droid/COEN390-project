package com.example.zone.controller;

import com.example.zone.model.Database;

import java.math.BigDecimal;
import java.util.ArrayList;

public class SubjectController {
    private  Database database;
    public SubjectController(Database database){this.database = database;}
    public boolean addGrade(long subjectID, String grade){
        String normalizedGrade = normalizeGrade(grade);
        return normalizedGrade != null
                && database.addGrade(subjectID, normalizedGrade);
    }

    public static boolean isGradeInRange(String grade) {
        return normalizeGrade(grade) != null;
    }

    public static String normalizeGrade(String grade) {
        if (grade == null || grade.trim().isEmpty()) {
            return null;
        }

        try {
            BigDecimal value = new BigDecimal(grade.trim());
            if (value.compareTo(BigDecimal.ZERO) < 0
                    || value.compareTo(BigDecimal.valueOf(100)) > 0) {
                return null;
            }
            return value.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public ArrayList<String> getGrades(int subjectId){
        return database.getGrades(subjectId);
    }

    public boolean deleteSubject(int subjectID){
        return database.deleteSubject(subjectID);
    }
}
