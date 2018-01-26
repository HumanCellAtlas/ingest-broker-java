package org.humancellatlas.ingest.ingestbroker;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class SpreadsheetProcessorTest {

    private static final String VALID_SPREADSHEET_FILE = "example-spreadsheets/Q4DemoSS2Metadata_VALID.xlsx";
    private static final String NON_EXISTENT_SPREADSHEET_FILE = "example-spreadsheets/non-existent.xlsx";

    @Test
    public void given_valid_spreadsheet_open_successfully() {
        SpreadsheetProcessor spreadsheetProcessor = new SpreadsheetProcessor(TestUtils.getAbsolutePath(VALID_SPREADSHEET_FILE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void given_non_existent_spreadsheet_fail() {
        SpreadsheetProcessor spreadsheetProcessor = new SpreadsheetProcessor(NON_EXISTENT_SPREADSHEET_FILE);
    }
}
