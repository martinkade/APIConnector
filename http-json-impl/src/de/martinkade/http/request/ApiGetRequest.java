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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

/**
 * ...
 * <p/>
 *
 * @param <T>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 */
public class ApiGetRequest<T extends ApiService.Entity> extends ApiRequest<T> {

    /**
     * Construtcor.
     *
     * @param url The url the request will be executed on
     * @param responseClass
     */
    public ApiGetRequest(String url, Class<T> responseClass) {
        super(url, responseClass);
    }

    @Override
    protected void config(String urlParams) throws IOException, ApiException {
        super.config(urlParams);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Content-Length", "0");
        connection.connect();
    }

    @Override
    protected T run(String urlParams) throws ApiException {
        try {

            // receive response
            final StringBuilder response;
            final InputStream in = connection.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }

            rawResponse = response.toString();
            return builder.decode(rawResponse, responseClass);
        } catch (ParseException | IOException ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
            throw new ApiException(ex.getMessage(), ApiException.APIError.IO_ERROR);
        } finally {
            connection.disconnect();
        }
    }

}
