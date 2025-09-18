package com.example.Payroll.Service;

import com.example.Payroll.Entity.AttendanceLog;
import com.example.Payroll.Entity.AttendanceLog.Status;
import com.example.Payroll.Entity.Employee;
import com.example.Payroll.Repository.AttendanceLogRepository;
import com.example.Payroll.Repository.EmployeeRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UnifiedImportService {

    private final AttendanceLogRepository repository;
    private final EmployeeRepository employeeRepository;

    public UnifiedImportService(AttendanceLogRepository repository, EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    public void importK4File(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename().toLowerCase();

        if (filename.endsWith(".csv")) {
            throw new UnsupportedOperationException("CSV not implemented yet");
        } else if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
            importK4RawExcel(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Only XLS/XLSX allowed.");
        }
    }

    private void importK4RawExcel(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = file.getOriginalFilename().toLowerCase().endsWith(".xls") ?
                    new HSSFWorkbook(is) : new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);

            String employeeName = null;
            LocalDate payPeriodStart = extractPayPeriodStart(sheet);

            Map<String, Integer> dayMap = Map.of(
                    "WED", 0, "THU", 1, "FRI", 2, "SAT", 3,
                    "SUN", 4, "MON", 5, "TUE", 6
            );

            LocalDate lastLogDate = null;
            Map<String, List<LocalTime>> dailyTimesMap = new HashMap<>();

            for (int r = 3; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String firstCellValue = getCellValueAsString(row.getCell(0)).trim();

                // Detect Employee
                if (firstCellValue.toUpperCase().startsWith("EMPLOYEE")) {
                    employeeName = parseEmployeeName(firstCellValue, row);
                    System.out.println("🔹 Employee detected: " + employeeName);
                    continue;
                }

                String firstCellUpper = firstCellValue.toUpperCase();
                if (!firstCellValue.isEmpty() && dayMap.containsKey(firstCellUpper)) {
                    lastLogDate = payPeriodStart.plusDays(dayMap.get(firstCellUpper));
                    dailyTimesMap.put(employeeName + "-" + lastLogDate, new ArrayList<>());
                }

                if (lastLogDate == null || employeeName == null) continue;

                Cell inCell = row.getCell(2);
                Cell outCell = row.getCell(3);

                parseAndSaveLog(employeeName, lastLogDate, inCell, Status.IN, dailyTimesMap);
                parseAndSaveLog(employeeName, lastLogDate, outCell, Status.OUT, dailyTimesMap);
            }

            workbook.close();
            System.out.println("✅ K4 import completed!");
        }
    }

    private LocalDate extractPayPeriodStart(Sheet sheet) {
        DateTimeFormatter dash = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter slash = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        Row ppRow = sheet.getRow(2);
        if (ppRow != null) {
            Cell ppCell = ppRow.getCell(3);
            if (ppCell != null && ppCell.getCellType() == CellType.STRING) {
                String raw = ppCell.getStringCellValue().trim();
                String[] tokens = raw.split("-");
                if (tokens.length >= 3) {
                    LocalDate parsed = tryParseDate(tokens[0] + "-" + tokens[1] + "-" + tokens[2], dash, slash);
                    if (parsed != null) return parsed;
                }
            } else if (ppCell != null && DateUtil.isCellDateFormatted(ppCell)) {
                return ppCell.getLocalDateTimeCellValue().toLocalDate();
            }
        }
        throw new IllegalStateException("Could not detect pay period start date!");
    }

    private String parseEmployeeName(String firstCellValue, Row row) {
        String name = getCellValueAsString(row.getCell(3)).trim();
        if (name.isEmpty()) {
            int colonIdx = firstCellValue.indexOf(":");
            if (colonIdx >= 0) {
                name = firstCellValue.substring(colonIdx + 1).trim();
            }
        }
        return name.replaceAll("\\(\\d+\\)", "").trim();
    }

    private void parseAndSaveLog(String employeeName, LocalDate logDate, Cell cell, Status status, Map<String, List<LocalTime>> dailyTimesMap) {
        if (cell == null) return;

        String val = getCellValueAsString(cell).trim();
        if (val.isEmpty()) return;

        LocalTime time = tryParseTime(val, Arrays.asList(
                DateTimeFormatter.ofPattern("hh:mm a"),
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("H:mm")
        ));

        if (time == null) return;

        // Lookup employee by full name
        Employee employee = employeeRepository.findByFullName(employeeName)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeName));

        String key = employeeName + "-" + logDate;
        List<LocalTime> times = dailyTimesMap.getOrDefault(key, new ArrayList<>());
        times.add(time);
        dailyTimesMap.put(key, times);

        double totalHours = 0;
        for (int i = 0; i < times.size(); i += 2) {
            if (i + 1 < times.size()) {
                totalHours += Duration.between(times.get(i), times.get(i + 1)).toMinutes() / 60.0;
            }
        }
        double regularHours = Math.min(totalHours, 8);
        double otHours = Math.max(totalHours - 8, 0);

        saveLog(employee, logDate, time, status, regularHours, otHours);

        System.out.println("  📌 Saved: " + employee.getFullName() + " | " + logDate + " | " + time + " | " + status
                + " | Total: " + regularHours + " | OT: " + otHours);
    }

    private void saveLog(Employee employee, LocalDate date, LocalTime time, Status status, double totalHours, double totalOT) {
        AttendanceLog log = new AttendanceLog();
        log.setEmployee(employee);
        log.setEmployeeName(employee.getFullName());
        log.setLogDate(date);
        log.setLogTime(time);
        log.setStatus(status);
        log.setTotalHours(totalHours);
        log.setTotalOT(totalOT);
        repository.save(log);
    }

    private LocalDate tryParseDate(String raw, DateTimeFormatter dash, DateTimeFormatter slash) {
        try { return LocalDate.parse(raw, dash); } catch (Exception ignored) {}
        try { return LocalDate.parse(raw, slash); } catch (Exception ignored) {}
        return null;
    }

    private LocalTime tryParseTime(String raw, List<DateTimeFormatter> formatters) {
        if (raw == null || raw.isBlank()) return null;
        String s = raw.trim().toUpperCase().replaceAll("\\.", ":");

        if (s.matches("\\d{1,2}\\s*(AM|PM)")) s = s.replaceAll("\\s*(AM|PM)", ":00 $1");
        if (s.matches("\\d{3,4}(AM|PM)")) {
            int len = s.length();
            String ampm = s.substring(len - 2);
            String digits = s.substring(0, len - 2);
            if (digits.length() == 3) digits = "0" + digits;
            s = digits.substring(0, 2) + ":" + digits.substring(2) + " " + ampm;
        }

        if ((s.endsWith("AM") || s.endsWith("PM")) && s.contains(":")) {
            String ampm = s.substring(s.length() - 2);
            String timePart = s.substring(0, s.length() - 2).trim();
            String[] parts = timePart.split(":");
            if (parts.length == 2) {
                int hour = Integer.parseInt(parts[0]);
                if (hour >= 12) s = timePart;
                else s = timePart + " " + ampm;
            }
        }

        for (DateTimeFormatter f : formatters) {
            try { return LocalTime.parse(s, f); } catch (Exception ignored) {}
        }
        try { return LocalTime.parse(s); } catch (Exception ignored) {}
        return null;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue().toLocalTime().toString();
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) return String.valueOf((long)d);
                return String.valueOf(d);
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue(); } catch (Exception e) { return String.valueOf(cell.getNumericCellValue()); }
            default: return "";
        }
    }
}
