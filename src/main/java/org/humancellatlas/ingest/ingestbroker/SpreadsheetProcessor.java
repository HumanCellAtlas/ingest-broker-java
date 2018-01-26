package org.humancellatlas.ingest.ingestbroker;

import org.apache.poi.EmptyFileException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpreadsheetProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetProcessor.class);

    private Workbook workbook;

    @Value("#{'${ingest.broker.valid-sheets}'.split(',')}")
    private List<String> validSheets;

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
        spreadsheetValidationResults.setCheckSheetsResult(checkSheets());
        return spreadsheetValidationResults;
    }

    CheckSheetsResult checkSheets() {
        CheckSheetsResult checkSheetsResult = new CheckSheetsResult();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            String sheetName = workbook.getSheetName(i);
            if (validSheets.contains(sheetName))
            {
                if(!checkSheetsResult.getExpectedSheets().contains(sheetName)) {
                    checkSheetsResult.addExpectedSheet(sheetName);
                }
                else
                {
                    checkSheetsResult.addDuplicatedSheet(sheetName);
                }
            }
            else
            {
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
