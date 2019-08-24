package com.github.vava23.vutil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
        public String readPendingLine() throws IllegalStateException {
            // Take the top level of the auxiliary buffer (line sequence
            // The buffer must exist
            if (fBufferAux.isEmpty()) { throw new IllegalStateException(); }
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
        public String readLine() throws IllegalStateException {
            // Buffer must be not empty
            if (fBufferMain.size() > 0) { throw new IllegalStateException(); }
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
//    private FileInputStream fFileStream;
    /** Reader object to read characters from file */
    private FileReader fFileReader;
    /** Reader object to read lines from file */
    private BufferedReader fLineReader;
    /** Path to the current file */
    private String fPath;
    /** Number of the current file line */
    private long fCurrentLine;
    /** Number of the bookmarked line */
    private long fBookmarkedLine;
    /** State of bookmarking process */
    private BookmarkState fBookmarkState;
    /** Buffer for storing and reading saved lines */
    private FileBuffer fFileBuffer;

    /** Opens the text file specified */
    protected void openFile(final String aPath) throws IOException {
        // Close a previously opened file
        if (fLineReader != null)
            closeFile();
        assert (fLineReader == null);
        // TODO: consider using BufferedReader for huge files
        fFileReader = new FileReader(aPath);
        fLineReader = new BufferedReader(fFileReader);
        fPath = aPath;
        fBookmarkState = BookmarkState.NONE;
        fCurrentLine = 0;
        fBookmarkedLine = 0;
    }

    /** Closes the current file */
    protected void closeFile() throws IOException {
        // TODO: remove these checks later
        assert (fLineReader != null);
        assert (fFileReader != null);
        assert (fFileBuffer != null);
        fLineReader.close();
        fFileReader.close();
        fLineReader = null;
        fFileReader = null;
        fFileBuffer.clear();
        fPath = "";
    }

    /** End Of FileReader stream reached */
    protected boolean fileReaderEOF() throws IOException {
        // TODO: find a better EOF sign
        return !fLineReader.ready();
    }

    /** End Of File reached */
    protected boolean isEOF() throws IOException {
        switch (fBookmarkState) {
            case NONE:
                // EOF if the end of file is reached directly
                return fileReaderEOF();
            case WRITING:
                // EOF if stream has ended and buffer has no pending lines
                return (fileReaderEOF() && !fFileBuffer.hasPendingLines());
            case READING:
                // EOF if stream has ended and buffer is empty
                return (fileReaderEOF() && fFileBuffer.isEmpty());
            default:
                return fileReaderEOF();
        }
    }

    /**
     * Read line from file
     * If a file in current position is unavailable, the line is read from a
     * buffer
     */
    protected String readLineFromFile() throws IOException {
        // Possibility of reading from file is determined by buffer state. If
        // it has pending lines (lines not read when the new writing process
        // began), then the required part of file is stored in the buffer
        if (fFileBuffer.hasPendingLines())
            // If the line was once read and is now stored in the buffer
            return fFileBuffer.readPendingLine();
        else
            // If the line was not read before, read directly from file
            return fLineReader.readLine();
    }

    /**  */
    public TextFileReader(final String aPath) throws IOException {
        fFileBuffer = new FileBuffer();
        fBookmarkState = BookmarkState.NONE;
        openFile(aPath);
        fCurrentLine = 0;
        fBookmarkedLine = 0;
    }

    /** Get the current file encoding  */
    public String getEncoding() {
        return fFileReader.getEncoding();
    }

    /** Reads the subsequent line from file*/
    public String readNextLine() throws IOException, IllegalStateException {
        if (isEOF()) throw new IllegalStateException();
        String line;
        // Read line considering bookmark state
        switch (fBookmarkState) {
            // No bookmarks, read directly
            case NONE:
                line = fLineReader.readLine();
                break;
            // Read line and simultaneously write it to buffer
            case WRITING:
                line = readLineFromFile();
                fFileBuffer.writeLine(line);
                break;
            case READING:
                line = fFileBuffer.readLine();
                // If the buffer is empty, we've returned to the original
                // file position
                if (fFileBuffer.isEmpty()) {
                    fBookmarkState = BookmarkState.NONE;
                }
                break;
            default:
                line = fLineReader.readLine();
        }
        fCurrentLine++;
        return line;
    }

    /** Fast forwards the file for specified number of lines */
    public long seek(final long aLength) throws IOException {
        // Negative seek not supported
        if (aLength < 1)
            return 0;
        else
            return fLineReader.skip(aLength);
    }

    /**
     * Saves a bookmark at current positions
     * NB! Supports just one bookmark at a time
     * NB! Avoid reading too far from a bookmark (can run out of memory)
     * */
    public boolean saveBookmark() {
        // No multi-level bookmarks
        if (fBookmarkState == BookmarkState.WRITING) return false;
        // From now on, begin to save to buffer
        fFileBuffer.startWriting();
        fBookmarkState = BookmarkState.WRITING;
        // Save the position of bookmark
        fBookmarkedLine = fCurrentLine;
        return true;
    }

    /**
     * Return to bookmarked position
     * */
    public boolean restoreBookmark() {
        // Return to bookmark only if there is one
        if ((fBookmarkState != BookmarkState.WRITING) || (fBookmarkedLine < 1))
            return false;

        // From now on, begin to read from buffer
        if (fFileBuffer.isEmpty()) {
            fBookmarkState = BookmarkState.NONE;
        } else {
            fBookmarkState = BookmarkState.READING;
            // Line number was saved, now just restore it
            fCurrentLine = fBookmarkedLine;
        }
        return true;
    }

    /** Resets the file reading position to beginning */
    public void resetPosition() throws IOException {
        // TODO: haven't found a better solution so far
        String path = fPath;
        closeFile();
        openFile(fPath);
    }

    /** End Of File reached */
    public boolean hasNext() throws IOException {
        return isEOF();
    }

    /** Current line number */
    public long getCurrentLine() { return fCurrentLine; }
}
