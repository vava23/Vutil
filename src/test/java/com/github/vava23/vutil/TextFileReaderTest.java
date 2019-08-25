package com.github.vava23.vutil;

import java.io.IOException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextFileReaderTest {
    private TextFileReader fTextReader;

    @Test
    public void testMain (/*String aPath*/) throws IOException {
        final String aPath = "src\\test\\java\\com\\github\\vava23\\vutil\\Lines.txt";
        fTextReader = new TextFileReader(aPath);
        assertTrue(true);
    }
}