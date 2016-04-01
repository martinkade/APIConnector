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

import de.martinkade.http.request.ApiRequest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ...
 * <p/>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 * @param <T>
 */
public class ApiService<T extends ApiService.Entity> implements ApiTask.Delegate<T> {

    /**
     * Entity type interface.
     */
    public static interface Entity {

    }

    /**
     * Delegate notification interface.
     *
     * @param <T>
     */
    public static interface Delegate<T> {

        /**
         *
         * @param obj
         * @param jsonString
         * @param execTimeMillis
         * @param id
         * @param httpStatusCode
         */
        void apiServiceDidReceiveResponse(T obj, String jsonString, long execTimeMillis, String id, int httpStatusCode);

        /**
         *
         * @param ex
         * @param id
         * @param httpStatusCode
         */
        void apiServiceDidThrowException(ApiException ex, String id, int httpStatusCode);
    }

    /**
     * Identifier.
     */
    protected static final String TAG = ApiService.class.getSimpleName();

    /**
     * Delegate reference.
     */
    private ApiService.Delegate delegate;

    /**
     * The service executing the different {@link ApiTask} instances.
     */
    private ScheduledExecutorService executor;

    /**
     * Constructor.
     *
     * @param delegate Delegate implementation reference
     */
    public ApiService(ApiService.Delegate delegate) {
        this.delegate = delegate;
    }

    public final void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    /**
     *
     * @param request
     * @param delayMillis
     * @param id
     * @throws ApiException
     */
    public final void exec(ApiRequest<T> request, long delayMillis, String id) throws ApiException {
        final long startTime = System.currentTimeMillis();
        final ApiTask task = new ApiTask(request, this);
        task.setIdentifier(id);
        executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);

        try {
            final ApiRequest r = task.get();
            delegate.apiServiceDidReceiveResponse(
                    r.getResponseData(), r.getRawResponse(), System.currentTimeMillis() - startTime,
                    id, request.getResponseCode()
            );
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
            if (ex instanceof ApiException) {
                final ApiException e = (ApiException) ex;
                delegate.apiServiceDidThrowException(e, id, request.getResponseCode());
                throw e;
            } else {
                final ApiException e = new ApiException(
                        ex.getMessage(),
                        ApiException.APIError.CONNECTION_TIMEOUT
                );
                delegate.apiServiceDidThrowException(e, id, request.getResponseCode());
                throw e;
            }
        } finally {
            task.done();
        }
    }

    @Override
    public void apiTaskIsDone() {
    }

    @Override
    public void apiTaskIsCancelled() {
    }

    public void prepare() {
        executor = Executors.newScheduledThreadPool(1);
    }

    public void release() {
        executor.shutdown();
    }
}
