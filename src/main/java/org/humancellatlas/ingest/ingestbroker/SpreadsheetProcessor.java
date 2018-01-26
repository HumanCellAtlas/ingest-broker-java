package org.humancellatlas.ingest.ingestbroker;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.apache.poi.EmptyFileException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpreadsheetProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetProcessor.class);

    private Workbook workbook;

    @Value("#{'${ingest.broker.valid-sheets}'.split(',')}")
    private List<String> validSheets;

    @Value("#{'${ingest.broker.vertical-sheets}'.split(',')}")
    private List<String> verticalSheets;

    public void loadSpreadsheet(String spreadsheetPath) {
        if (spreadsheetPath == null) {
            String message = "A spreadsheet path must be specified.";
            LOGGER.debug(message);
            throw new IllegalArgumentException(message);
        }
        try {
            FileInputStream excelFile = new FileInputStream(spreadsheetPath);
            workbook = new XSSFWorkbook(excelFile);
        } catch (NotOfficeXmlFileException e) {
            String message = "Spreadsheet: " + spreadsheetPath + " was not a valid XLSX file.";
            LOGGER.debug(message);
            throw new IllegalArgumentException(message);
        } catch (EmptyFileException e) {
            String message = "Spreadsheet: " + spreadsheetPath + " was an empty file.";
            LOGGER.debug(message);
            throw new IllegalArgumentException(message);
        } catch (IOException e) {
            String message = "Spreadsheet: " + spreadsheetPath + " was not found.";
            LOGGER.debug(message);
            throw new IllegalArgumentException(message);
        }
    }

    public SpreadsheetValidationResults validate() {
        SpreadsheetValidationResults spreadsheetValidationResults = new SpreadsheetValidationResults();
        CheckSheetsResult checkSheetsResult = checkSheets();
        spreadsheetValidationResults.setCheckSheetsResult(checkSheetsResult);
        for (String sheetName : checkSheetsResult.getExpectedSheets()) {
            convertSheetToObject(sheetName);
        }
        return spreadsheetValidationResults;
    }

    private void convertSheetToObject(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (!verticalSheets.contains(sheetName)) {
            List<String> headers = new ArrayList<>();
            Row firstRow = sheet.getRow(sheet.getFirstRowNum());
            for (int cellnum = 0; cellnum < firstRow.getLastCellNum(); cellnum++) {
                Cell cell = firstRow.getCell(cellnum);
                if (cell != null) {
                    String header = cell.getStringCellValue();
                    if (!header.isEmpty()) {
                        headers.add(header);
                    }
                }
            }
            List<List<String>> data = new ArrayList<List<String>>();
            for (int rownum = sheet.getFirstRowNum() + 1; rownum <= sheet.getLastRowNum(); rownum++) {
                List<String> values = new ArrayList<>();
                Row row = sheet.getRow(rownum);
                boolean empty = true;
                for (int cellnum = 0; cellnum < headers.size(); cellnum++) {
                    Cell cell = row.getCell(cellnum);
                    String value = "";
                    if (cell != null) {
                        switch (cell.getCellTypeEnum()) {
                            case STRING:
                                value = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                value = String.valueOf(cell.getNumericCellValue());
                        }
                    }
                    if (!value.isEmpty()) {
                        empty = false;
                    }
                    values.add(value);
                }
                if (!empty) {
                    data.add(values);
                }
            }
            printResults(sheetName, headers, data);
            generateJson(sheetName, headers, data);
        }
    }

    private void generateJson(String sheetName, List<String> headers, List<List<String>> data) {
        {
            for (List<String> values : data) {
                JsonFactory jsonFactory = new JsonFactory();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                JsonGenerator jsonGen = null;
                try {
                    jsonGen = jsonFactory.createJsonGenerator(output, JsonEncoding.UTF8);
                    jsonGen.setPrettyPrinter(new DefaultPrettyPrinter());
                    jsonGen.writeStartObject();
                    int i = 0;
                    for (String header : headers) {
                        jsonGen.writeStringField(header, values.get(i));
                        i++;
                    }
                    jsonGen.writeEndObject();
                    jsonGen.close();
                    System.out.println(output.toString(StandardCharsets.UTF_8.name()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printResults(String sheetName, List<String> headers, List<List<String>> data) {
        System.out.println("\n\nSheet: " + sheetName);
        for (String header : headers) {
            System.out.print(header + '\t');
        }
        for (List<String> values : data) {
            System.out.println("");
            for (String value : values) {
                System.out.print(value + '\t');
            }
        }
    }

    CheckSheetsResult checkSheets() {
        CheckSheetsResult checkSheetsResult = new CheckSheetsResult();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            String sheetName = workbook.getSheetName(i);
            if (validSheets.contains(sheetName)) {
                if (!checkSheetsResult.getExpectedSheets().contains(sheetName)) {
                    checkSheetsResult.addExpectedSheet(sheetName);
                } else {
                    checkSheetsResult.addDuplicatedSheet(sheetName);
                }
            } else {
                checkSheetsResult.addUnexpectedSheet(sheetName);
            }
        }
        return checkSheetsResult;
    }

    public class CheckSheetsResult {

        public List<String> getDuplicatedSheets() {
            return duplicatedSheets;
        }

        private final List<String> duplicatedSheets = new ArrayList<>();

        private final List<String> expectedSheets = new ArrayList<>();

        private final List<String> unexpectedSheets = new ArrayList<>();

        public List<String> getExpectedSheets() {
            return expectedSheets;
        }

        public List<String> getUnexpectedSheets() {
            return unexpectedSheets;
        }


        public void addExpectedSheet(String sheetName) {
            expectedSheets.add(sheetName);
        }

        public void addUnexpectedSheet(String sheetName) {
            unexpectedSheets.add(sheetName);
        }

        @Override
        public String toString() {
            return "CheckSheetsResult{" +
                    "validSheets=" + validSheets +
                    ", unexpectedSheets=" + unexpectedSheets +
                    '}';
        }

        public void addDuplicatedSheet(String sheetName) {
            duplicatedSheets.add(sheetName);
        }
    }

    public class SpreadsheetValidationResults {

        public CheckSheetsResult getCheckSheetsResult() {
            return checkSheetsResult;
        }

        public void setCheckSheetsResult(CheckSheetsResult checkSheetsResult) {
            this.checkSheetsResult = checkSheetsResult;
        }

        private CheckSheetsResult checkSheetsResult = new CheckSheetsResult();

        @Override
        public String toString() {
            return "SpreadsheetValidationResults{" +
                    "checkSheetsResult=" + checkSheetsResult +
                    '}';
        }
    }
}
