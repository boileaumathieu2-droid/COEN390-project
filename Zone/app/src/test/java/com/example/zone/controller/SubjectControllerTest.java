package com.example.zone.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SubjectControllerTest {

    @Test
    public void gradeRangeAcceptsBoundaryValuesAndDecimals() {
        assertTrue(SubjectController.isGradeInRange("0"));
        assertTrue(SubjectController.isGradeInRange("84.5"));
        assertTrue(SubjectController.isGradeInRange("100"));
    }

    @Test
    public void gradeRangeRejectsInvalidValues() {
        assertFalse(SubjectController.isGradeInRange("-1"));
        assertFalse(SubjectController.isGradeInRange("100.01"));
        assertFalse(SubjectController.isGradeInRange("grade"));
        assertFalse(SubjectController.isGradeInRange(""));
    }

    @Test
    public void gradeIsNormalizedBeforeItIsSaved() {
        assertEquals("85", SubjectController.normalizeGrade("85.00"));
        assertEquals("72.5", SubjectController.normalizeGrade(" 72.50 "));
    }
}
