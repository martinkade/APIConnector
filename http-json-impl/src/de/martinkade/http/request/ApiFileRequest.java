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
package de.martinkade.http.request;

import de.martinkade.http.ApiException;
import de.martinkade.http.ApiService;
import de.martinkade.http.entity.EntityBuilder;
import de.martinkade.http.entity.FileUpload;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

/**
 * ...
 * <p/>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 * <p/>
 * @param <T> The expected response class
 */
public class ApiFileRequest<T extends ApiService.Entity> extends ApiRequest<T> {

    /**
     * The file to be aploaded.
     */
    private FileUpload fileData;

    /**
     * Constructor.
     *
     * @param url The url the request will be executed on
     * @param responseClass The expected response class
     */
    public ApiFileRequest(String url, Class<T> responseClass) {
        super(url, responseClass);
        contentType = "multipart/form-data;boundary=---------------------------4664151417711";
        responseBuilder = new EntityBuilder<>();
    }

    @Override
    protected void config(String urlParams) throws IOException, ApiException {
        super.config(urlParams);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Content-Length", fileData.getContentLength());
        connection.connect();
    }

    /**
     * Specify file to be uploaded.
     *
     * @param file The file to be uploaded
     * @param mimeType The optional mime type
     */
    public void setFileData(File file, String mimeType) {
        fileData = new FileUpload(file, mimeType);
    }

    @Override
    protected T run(String urlParams) throws ApiException {
        try {

            // send request
            final DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(fileData.getStartMessage().getBytes());
            int index = 0, size = 1024, length = fileData.getBytes().length;
            do {
                if ((index + size) > length) {
                    size = length - index;
                }
                out.write(fileData.getBytes(), index, size);
                index += size;
            } while (index < length);
            out.write(fileData.getEndMessage().getBytes());
            out.flush();

            // receive response
            final StringBuilder response;
            try (InputStream in = connection.getInputStream()) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
            }

            rawResponse = response.toString();
            return responseBuilder.decode(rawResponse, responseClass);
        } catch (ParseException | IOException ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
            throw new ApiException(ex.getMessage(), ApiException.APIError.IO_ERROR);
        } finally {
            connection.disconnect();
        }
    }
}
