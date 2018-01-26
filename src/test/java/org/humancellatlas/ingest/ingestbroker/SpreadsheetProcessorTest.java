package org.humancellatlas.ingest.ingestbroker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpreadsheetProcessorTest {

    private static final String VALID_SPREADSHEET_FILE = "example-spreadsheets/Q4DemoSS2Metadata_VALID.xlsx";
    private static final String NON_EXISTENT_SPREADSHEET_FILE = "example-spreadsheets/non-existent.xlsx";
    private static final String NON_SPREADSHEET_FILE = "example-spreadsheets/non-spreadsheet.xlsx";
    private static final String EMPTY_SPREADSHEET_FILE = "example-spreadsheets/empty-spreadsheet-file.xlsx";

    @Autowired
    private SpreadsheetProcessor spreadsheetProcessor;

    @Test
    public void given_valid_spreadsheet_open_successfully() {
        spreadsheetProcessor.loadSpreadsheet(TestUtils.getAbsolutePath(VALID_SPREADSHEET_FILE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_non_spreadsheet_fail() {
        spreadsheetProcessor.loadSpreadsheet(TestUtils.getAbsolutePath(NON_SPREADSHEET_FILE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_empty_spreadsheet_file_fail() {
        spreadsheetProcessor.loadSpreadsheet(TestUtils.getAbsolutePath(EMPTY_SPREADSHEET_FILE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_non_existent_spreadsheet_fail() {
        spreadsheetProcessor.loadSpreadsheet(NON_EXISTENT_SPREADSHEET_FILE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_null_spreadsheet_fail() {
        spreadsheetProcessor.loadSpreadsheet(null);
    }

    @Test
    public void given_valid_spreadsheet_validate() {
        spreadsheetProcessor.loadSpreadsheet(TestUtils.getAbsolutePath(VALID_SPREADSHEET_FILE));
        SpreadsheetProcessor.SpreadsheetValidationResults spreadsheetValidationResults = spreadsheetProcessor.validate();
        assertFalse(spreadsheetValidationResults.getCheckSheetsResult().getExpectedSheets().isEmpty());
        assertTrue(spreadsheetValidationResults.getCheckSheetsResult().getUnexpectedSheets().isEmpty());
        assertTrue(spreadsheetValidationResults.getCheckSheetsResult().getDuplicatedSheets().isEmpty());
    }


}
