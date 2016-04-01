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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.Charset;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * ...
 * <p/>
 *
 * @param <T>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 */
public class ApiPostRequest<T extends ApiService.Entity> extends ApiRequest<T> {

    /**
     * Request data of type {@link APIService.Entity}.
     */
    private T requestData;

    /**
     * Raw, already as {@link JSONObject} encoded request data.
     */
    private JSONObject rawRequestData;

    /**
     * Constructor.
     *
     * @param url The url the request will be executed on
     * @param responseClass
     */
    public ApiPostRequest(String url, Class<T> responseClass) {
        super(url, responseClass);
        builder = new EntityBuilder<>();
    }

    @Override
    protected void config(String urlParams) throws IOException, ApiException {
        super.config(urlParams);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Content-Length",
                Integer.toString(requestData == null
                                ? rawRequestData.toJSONString().getBytes(Charset.forName(charset)).length
                                : builder.encode(requestData).getBytes(Charset.forName(charset)).length)
        );
        connection.connect();
    }

    public final void setRequestData(T requestData) {
        this.requestData = requestData;
        this.rawRequestData = null;
    }

    public void setRawRequestData(Map rawRequestData) {
        this.rawRequestData = new JSONObject(rawRequestData);
        this.requestData = null;
    }

    @Override
    protected T run(String urlParams) throws ApiException {
        try {

            // send request
            final DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(requestData == null
                    ? rawRequestData.toJSONString().getBytes(Charset.forName(charset))
                    : builder.encode(requestData).getBytes(Charset.forName(charset)));
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
            return builder.decode(rawResponse, responseClass);
        } catch (ParseException | IOException ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
            throw new ApiException(ex.getMessage(), ApiException.APIError.IO_ERROR);
        } finally {
            connection.disconnect();
        }
    }
}
