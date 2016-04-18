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

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ...
 * <p/>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 * <p/>
 * @param <T> The expected response class
 */
public abstract class ApiRequest<T extends ApiService.Entity> implements Callable<ApiRequest<T>> {

    /**
     * Identifier.
     */
    protected static final String TAG = ApiPostRequest.class.getSimpleName();

    /**
     * Start time in milliseconds.
     */
    protected long startTime;

    /**
     * Response data of type {@link APIService.Entity}.
     */
    protected T responseData;

    /**
     * Response class.
     */
    protected Class<T> responseClass;

    /**
     * Raw json response string.
     */
    protected String rawResponse;

    /**
     * The url string.
     */
    protected final String url;

    /**
     * Url connection of the request.
     */
    protected HttpURLConnection connection;

    /**
     * Url parameters.
     */
    private Map<String, String> urlParams;

    /**
     * The connection state and the specified timeout to waot for the response.
     */
    protected int responseCode, timeoutSeconds;

    /**
     * Content type and charset as string.
     */
    protected String contentType, charset;

    /**
     * The entity builder for the response.
     */
    protected EntityBuilder<T> responseBuilder;

    /**
     * Constructor.
     *
     * @param url The url the request will be executed on
     * @param responseClass The expected response class
     */
    public ApiRequest(String url, Class<T> responseClass) {
        this.url = url;
        this.responseClass = responseClass;
        timeoutSeconds = 10;
        contentType = "application/json;charset=utf-8";
        charset = "utf-8";
    }

    @Override
    public ApiRequest<T> call() throws ApiException {
        try {
            final String p = tryConnect();
            responseData = run(p);
            responseCode = connection.getResponseCode();
        } catch (IOException ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
            throw new ApiException(ex.getMessage(), ApiException.APIError.CONNECTION_TIMEOUT);
        }
        return this;
    }

    /**
     * Try to establish the {@link #connection} to the given url string.
     *
     * @return Url parameters
     * @throws ApiException
     * @throws IOException
     */
    private String tryConnect() throws ApiException, IOException {
        final String params = encodeUrlParams();
        final URL address = new URL(url + "?" + params);
        connection = (HttpURLConnection) address.openConnection();
        config(params);
        return params;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public final void addUrlParam(String key, String value) {
        if (urlParams == null) {
            urlParams = new HashMap<>();
        }
        urlParams.put(key, value);
    }

    public final T getResponseData() {
        return responseData;
    }

    public final int getResponseCode() {
        return responseCode;
    }

    public final String getRawResponse() {
        return rawResponse;
    }

    /**
     * Specific implementation for each subclass being called in
     * {@link #call()}.
     *
     * @param urlParams The url parameters
     * @return The response entity
     * @throws ApiException
     */
    protected abstract T run(String urlParams) throws ApiException;

    /**
     * Configure the HTTP connection.
     *
     * @param urlParams Url params
     * @throws IOException
     * @throws ApiException
     * @throws ProtocolException
     */
    protected void config(String urlParams) throws IOException, ApiException {
        connection.setRequestProperty("User-Agent", "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.57 Safari/537.36");
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);
        connection.setConnectTimeout(timeoutSeconds * 1000);
        connection.setReadTimeout(timeoutSeconds * 1000);
        connection.setDoInput(true);
        connection.setDoOutput(true);
    }

    /**
     * Enocde the url paramaters to a string.
     *
     * @return The url parameters as a single string
     */
    private String encodeUrlParams() {
        if (urlParams == null || urlParams.isEmpty()) {
            return "";
        }
        final StringBuilder params = new StringBuilder();
        for (Map.Entry<String, String> e : urlParams.entrySet()) {
            params.append(String.format("%s=%s&", e.getKey(), e.getValue()));
        }

        final String s = params.toString();
        return s.substring(0, s.length() - 1);
    }
}
