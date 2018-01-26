package org.humancellatlas.ingest.ingestbroker;

import java.io.File;

public class TestUtils {

    public static String getAbsolutePath(String filePath) {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(filePath).getFile());
        return file.getAbsolutePath();
    }
}
