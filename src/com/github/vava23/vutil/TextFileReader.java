package com.github.vava23.vutil;

import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Text file reader based on a stream.
 * Reads lines of text one by one
 * Supports single bookmarks (one at a time)
 *
 * @author  Vladimir Siutkin
 */
public class TextFileReader {

    /** State of bookmarking process. */
    protected enum BookmarkState {
        NONE,       // Reading from the file directly
        WRITING,    // Reading and writing to bookmark buffer at the same time
        READING     // Reading from the bookmark buffer
    }

    /**
     * File buffer - a structure that stores the part of file that was once
     * read from the stream, but still needs to be stored (for bookmarks
     * support).
     */
    protected class FileBuffer {

        /** Main (top level) buffer. */
        private Queue<String> fBufferMain;

        /**
         * Auxiliary multi-level buffer, if the main one is not enough.
         * If on new write to buffer operation something was already written
         * to the buffer, the existing contents move here
         */
        private Stack<Queue<String>> fBufferAux;

        /** Returns true if the auxiliary buffer is empty. */
        private boolean isAuxBufferEmpty() {
            if (!fBufferAux.isEmpty()) {
                // If the stack has some sequence, it must not be empty
                assert (!fBufferAux.peek().isEmpty());
                // Check the top level of aux buffer, just in case
                return fBufferAux.peek().isEmpty();
            }
            return true;
        }

        /** */
        public FileBuffer() {
            fBufferMain = new LinkedList<String>();
            fBufferAux = new Stack<Queue<String>>();
        }

        /** Clears the buffer completely */
        public void clear() {
            fBufferMain.clear();
            fBufferAux.clear();
        }

        /** Prepares the buffer to read the new sequence of lines */
        public void startWriting() {
            // If we've already got a buffer, create a new one over it
            if (fBufferMain.size() > 0) {
                // Current main buffer is attached to the auxiliary, and the
                // new buffer is created instead
                fBufferAux.push(fBufferMain);
                fBufferMain = new LinkedList();
            }
            // Once the buffer is empty, everything is ready to write
            // operations
            return;
        }

        /**
         *  Reads the "pending" line (line from a sequence that was stored in
         *  buffer before the current record began)
         */
        public String readPendingLine() {
            // Take the top level of the auxiliary buffer (line sequence
            // The buffer must exist
            if (fBufferAux.isEmpty()) { throw new java.lang.IllegalStateException(); }
            Queue<String> topFileBuffer = fBufferAux.peek();
            // Take the next line
            // A line sequence must exist as the aux buffer takes origin from
            // the main buffer, while the empty buffer must be deleted after
            // reading operation (in this method)
            assert (!topFileBuffer.isEmpty());
            String line = topFileBuffer.remove();
            // If this sequence nas no more lines, delete it
            if (topFileBuffer.size() > 0) { fBufferAux.pop(); }
            return "";
        }

        /** Reads line from buffer */
        public String readLine() {
            // Buffer must be not empty
            if (fBufferMain.size() > 0) { throw new java.lang.IllegalStateException(); }
            // Read a line from main buffer
            String result = fBufferMain.remove();
            // If the buffer becomes empty, get the next level buffer from
            // stack (aux buffer), if it exists
            if (fBufferMain.isEmpty()) {
                if (!isAuxBufferEmpty()) {
                    fBufferMain = fBufferAux.pop();
                }
            }
            return result;
        }

        /** Saves line to the buffer */
        public void writeLine(String aLine) {
            fBufferMain.add(aLine);
        }

        /** Buffer levels count */
        public int levelsCount() {
            int count = 0;
            // Levels count = 1 for main buffer + size of aux buffer stack
            if (!fBufferMain.isEmpty()) count++;
            count += fBufferAux.size();
            return count;
        }

        /** Has the buffer pending lines or not */
        public boolean hasPendingLines() {
            return !isAuxBufferEmpty();
        }

        /** Is the buffer empty or not */
        public boolean isEmpty() {
            if (fBufferMain.isEmpty()) {
                // Aux buffer must be empty as well
                // TODO: remove later
                assert !fBufferAux.isEmpty();
                // Check the aux buffer, just in case
                if (fBufferAux.isEmpty()) return true;
            }
            return false;
        }
    }

    /** Stream for file reading */
    private FileInputStream fFileStream;
    /** Reader object to read lines from stream */
    private FileReader fFileReader;
    /** Number of the current file line */
    private long fCurrentLine;
    /** Number of the bookmarked line */
    private long fBookmarkedLine;
    /** State of bookmarking process */
    private BookmarkState fBookmarkState;
    /** Buffer for storing and reading saved lines */
    private FileBuffer fFileBuffer;

    /** Opens the text file specified */
    protected void openFile() {
        // TODO: STUB
        return;
    }

    /** Closes the current file */
    protected void closeFile() {
        // TODO: STUB
        return;
    }

    /** End Of File reached */
    protected boolean isEOF() {
        // TODO: STUB
        return false;
    }

    /**
     * Read line from file
     * If a file in current position is unavailable, the line is read from a
     * buffer
     */
    protected String readLineFromFile() {
        // TODO: STUB
        return "";
    }

    /**  */
    public TextFileReader(final String aPath, final Charset aEncoding) {
        // TODO: STUB
    }

    /** Get the current file encoding  */
    public Charset getEncoding() {
        // TODO: STUB
        return null;
    }

    /** Force sets the encoding */
    public void setEncoding(final Charset aEncoding) {
        // TODO: STUB
        return;
    }

    /** Reads the subsequent line from file*/
    public String readNextLine() {
        // TODO: STUB
        return "";
    }

    /** Fast forwards the file for specified number of lines */
    public long seek(final long aLength) {
        // TODO: STUB
        return -1;
    }

    /**
     * Saves a bookmark at current positions
     * NB! Supports just one bookmark at a time
     * NB! Avoid reading too far from a bookmark (can run out of memory)
     * */
    public boolean saveBookmark() {
        // TODO: STUB
        return false;
    }

    /**
     * Return to bookmarked position
     * */
    public void restoreBookmark() {
        // TODO: STUB
        return;
    }

    /** Resets the file reading position to beginning */
    public void resetPosition() {
        // TODO: STUB
    }

    /** End Of File reached */
    public boolean eOF() {
        return isEOF();
    }

    /** Current line number */
    public long getCurrentLine() { return fCurrentLine; }
}
