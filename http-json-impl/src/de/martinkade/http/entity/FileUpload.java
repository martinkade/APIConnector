/*
 * The MIT License
 *
 * Copyright 2016 Martin Kade.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.martinkade.http.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ...
 * <p/>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 */
public class FileUpload {

    /**
     * The data as byte array.
     */
    private byte[] bytes;

    /**
     *
     */
    private final String startMessage, endMessage;

    /**
     * The mime type of the file.
     */
    private final String mimeType;

    /**
     * The name of the file.
     */
    private final String fileName;

    /**
     * Constructor.
     *
     * @param file The file to be uploaded
     * @param mimeType The mime type (optionally)
     */
    public FileUpload(File file, String mimeType) {
        final String seq = "-----------------------------4664151417711";
        this.mimeType = mimeType == null ? extractMimeType(file) : mimeType;
        fileName = file.getName();
        startMessage = String.format(
                "%s\r\nContent-Disposition:form-data;name=\"%s\";filename=\"%s\"\r\nContent-Type:%s\r\n\r\n",
                seq, "file-to-be-uploaded", fileName, this.mimeType);
        endMessage = String.format("\r\n%s--\r\n", seq);
        try {
            final InputStream in = new FileInputStream(file);
            bytes = new byte[in.available()];
            in.read(bytes);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the mime type from the given file.
     *
     * @param file The file to be uploaded
     * @return The mime type as string
     */
    private String extractMimeType(File file) {
        return URLConnection.guessContentTypeFromName(file.getName());
    }

    public final String getContentLength() {
        return String.valueOf(startMessage.length() + endMessage.length() + bytes.length);
    }

    public String getStartMessage() {
        return startMessage;
    }

    public String getEndMessage() {
        return endMessage;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
