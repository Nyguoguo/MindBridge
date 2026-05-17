package com.mindbridge.mcp;

import com.mindbridge.common.entity.PsychologicalRecord;
import com.mindbridge.common.repository.PsychologicalRecordRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class McpExcelService {

    private final PsychologicalRecordRepository recordRepo;
    private final Path exportDir = Paths.get("mindbridge-exports");

    public McpExcelService(PsychologicalRecordRepository recordRepo) {
        this.recordRepo = recordRepo;
        try {
            Files.createDirectories(exportDir);
        } catch (IOException ignored) {}
    }

    public String exportRecords() {
        List<PsychologicalRecord> records = recordRepo.findAll();
        String filename = "psychological-data-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".xlsx";
        File file = exportDir.resolve(filename).toFile();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Psychological Records");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Session", "User", "Risk Level", "Risk Score", "Keywords", "Summary", "Suggestion", "Alert Sent", "Time"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }

            int rowIdx = 1;
            for (PsychologicalRecord r : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getSessionId() != null ? r.getSessionId() : 0);
                row.createCell(2).setCellValue(r.getUserId() != null ? r.getUserId() : 0);
                row.createCell(3).setCellValue(r.getRiskLevel());
                row.createCell(4).setCellValue(r.getRiskScore() != null ? r.getRiskScore() : 0);
                row.createCell(5).setCellValue(r.getKeywords() != null ? r.getKeywords() : "");
                row.createCell(6).setCellValue(r.getSummary() != null ? r.getSummary() : "");
                row.createCell(7).setCellValue(r.getSuggestion() != null ? r.getSuggestion() : "");
                row.createCell(8).setCellValue(r.getAlertSent() != null && r.getAlertSent() ? "Yes" : "No");
                row.createCell(9).setCellValue(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getExportDir() {
        return exportDir.toAbsolutePath().toString();
    }
}
