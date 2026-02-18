package com.example.onlineexam.util;

import com.example.onlineexam.model.Question;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Import/Export questions from/to Excel (xlsx).
 * Template columns: Question | Option_A | Option_B | Option_C | Option_D | Correct_Answer | Marks
 */
public class ExcelUtil {

    public static List<Question> importQuestions(String filePath) throws IOException {
        List<Question> questions = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int startRow = sheet.getFirstRowNum() + 1; // Skip header
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String qText = getCellString(row.getCell(0));
                String optA = getCellString(row.getCell(1));
                String optB = getCellString(row.getCell(2));
                String optC = getCellString(row.getCell(3));
                String optD = getCellString(row.getCell(4));
                String correct = getCellString(row.getCell(5));
                int marks = (int) getCellNumeric(row.getCell(6));
                if (qText != null && !qText.trim().isEmpty()) {
                    char ans = (correct != null && correct.length() > 0) ? Character.toUpperCase(correct.charAt(0)) : 'A';
                    if (ans != 'A' && ans != 'B' && ans != 'C' && ans != 'D') ans = 'A';
                    Question q = new Question(qText, optA != null ? optA : "", optB != null ? optB : "",
                            optC != null ? optC : "", optD != null ? optD : "", ans, marks > 0 ? marks : 1);
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    public static void exportQuestionsTemplate(String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Questions");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Question");
            header.createCell(1).setCellValue("Option_A");
            header.createCell(2).setCellValue("Option_B");
            header.createCell(3).setCellValue("Option_C");
            header.createCell(4).setCellValue("Option_D");
            header.createCell(5).setCellValue("Correct_Answer");
            header.createCell(6).setCellValue("Marks");
            for (int i = 0; i <= 6; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    public static void exportResults(String filePath, List<String[]> rows, String[] headers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Results");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (String[] row : rows) {
                Row r = sheet.createRow(rowNum++);
                for (int i = 0; i < row.length; i++) {
                    r.createCell(i).setCellValue(row[i] != null ? row[i] : "");
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private static double getCellNumeric(Cell cell) {
        if (cell == null) return 1;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        return 1;
    }
}
