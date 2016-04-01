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
package de.martinkade.http;

/**
 * Error type class for this library. Exceptions can be accessed via delegate
 * {@link APIService#delegate} implementations.
 * <p/>
 *
 * @author Martin Kade
 * @version Tue, 5 January 2016
 */
public class ApiException extends RuntimeException {

    /**
     * Error types.
     * <p/>
     * <li>CONNECTION_TIMEOUT</li>
     * <li>JSON_ENCODE_ERROR</li>
     * <li>JSON_DECODE_ERROR</li>
     * <li>IO_ERROR</li>
     */
    public enum APIError {

        /**
         * Establishing a connection takes longer than allowed.
         */
        CONNECTION_TIMEOUT,
        /**
         *
         */
        JSON_ENCODE_ERROR,
        /**
         *
         */
        JSON_DECODE_ERROR,
        /**
         *
         */
        IO_ERROR
    }

    /**
     * The specific error.
     */
    private final APIError error;

    /**
     * Constructor.
     *
     * @param msg The exception message being displayed calling
     * {@link Exception#getMessage()}
     * @param error The specific error
     */
    public ApiException(String msg, APIError error) {
        super(msg);
        this.error = error;
    }

    public final APIError getError() {
        return error;
    }
}
