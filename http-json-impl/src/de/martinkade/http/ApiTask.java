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
import java.util.concurrent.FutureTask;

/**
 * ...
 * <p/>
 *
 * @param <T>
 * @author Martin Kade
 * @version Tue, 5 January 2016
 */
public class ApiTask<T extends ApiService.Entity> extends FutureTask<ApiRequest<T>> {

    /**
     * Delegate notification interface.
     *
     * @param <T>
     */
    public static interface Delegate<T> {

        /**
         *
         */
        void apiTaskIsDone();

        /**
         *
         */
        void apiTaskIsCancelled();
    }

    /**
     * Identifier.
     */
    private static final String TAG = ApiTask.class.getSimpleName();

    /**
     * Delegate reference.
     */
    private Delegate delegate;

    /**
     * Task identifying string. Used to identify request.
     */
    private String identifier;

    /**
     * Constructor.
     *
     * @param request
     * @param delegate
     */
    public ApiTask(ApiRequest<T> request, Delegate delegate) {
        super(request);
        this.delegate = delegate;
    }

    public final void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public final void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public final String getIdentifier() {
        return identifier;
    }

    @Override
    protected void done() {
        super.done();
        delegate.apiTaskIsDone();
    }

    @Override
    public ApiRequest get() throws InterruptedException, ExecutionException {
        return super.get();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        delegate.apiTaskIsCancelled();
        return super.cancel(mayInterruptIfRunning);
    }
}
