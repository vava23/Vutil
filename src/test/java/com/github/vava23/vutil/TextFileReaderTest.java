package com.github.vava23.vutil;

import java.io.IOException;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class TextFileReaderTest {
    private TextFileReader fTextReader;

    @Test
    public void testMain (/*String aPath*/) throws IOException {
        // Take these two values as parameters
        final String aPath = "src\\test\\java\\com\\github\\vava23\\vutil\\Lines.txt";
        final String encoding = "Cp1251";
        fTextReader = new TextFileReader(aPath, encoding);
        assertNotNull(fTextReader, "Couldn't open the file");
        long currentStr = fTextReader.getCurrentLine();
        assertEquals(0, currentStr);
        assertTrue(fTextReader.hasNext());
        String tmpStr = fTextReader.readNextLine();
        assertEquals("Строка1", tmpStr);
    }
}