package org.humancellatlas.ingest.ingestbroker;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

public class SpreadsheetProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetProcessor.class);

    public SpreadsheetProcessor(String spreadsheetPath) {
        try {
            FileInputStream excelFile = new FileInputStream(spreadsheetPath);
            Workbook workbook = new XSSFWorkbook(excelFile);
        } catch (IOException e) {
            String message = "Spreadsheet: " + spreadsheetPath + " was not found.";
            LOGGER.info(message);
            throw new IllegalArgumentException(message);
        }
    }

    public void process(String spreadsheetPath) {

    }
}
