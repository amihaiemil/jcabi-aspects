/**
 * Copyright (c) 2012-2016, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Marker of a running method.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.23
 *
 */
final class MethodMarker implements Comparable<MethodMarker> {

    /**
     * StackTraceElement as text.
     */
    private final transient StackTraceText stcktrctxt = new StackTraceText();

    /**
     * When the method was started, in milliseconds.
     */
    private final transient long started;

    /**
     * Which monitoring cycle was logged recently.
     */
    private final transient AtomicInteger logged;

    /**
     * The thread it's running in.
     */
    @SuppressWarnings("PMD.DoNotUseThreads")
    private final transient Thread thread;

    /**
     * Joint point.
     */
    private final transient ProceedingJoinPoint point;

    /**
     * Execution time unit.
     */
    private final transient TimeUnit unit;

    /**
     * Execution time limit.
     */
    private final transient int limit;

    /**
     * Skip arguments.
     */
    private final transient boolean skipargs;

    /**
     * Ctor.
     * @param pnt Joint point
     * @param annt Annotation
     */
    MethodMarker(final ProceedingJoinPoint pnt, final Loggable annt) {
        this(pnt, annt.unit(), annt.limit(), annt.skipArgs());
    }

    /**
     * Ctor.
     * @param pnt Joint point
     * @param tunit Time unit
     * @param lmt Execution time limit
     * @param skpargs Skip arguments
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    MethodMarker(
        final ProceedingJoinPoint pnt, final TimeUnit tunit,
        final int lmt, final boolean skpargs
    ) {
        this.started = System.currentTimeMillis();
        this.logged = new AtomicInteger();
        this.point = pnt;
        this.unit = tunit;
        this.limit = lmt;
        this.skipargs = skpargs;
        this.thread = Thread.currentThread();
    }

    /**
     * Monitor it's status and log the problem, if any.
     */
    public void monitor() {
        final long age = this.unit.convert(
            System.currentTimeMillis() - this.started, TimeUnit.MILLISECONDS
        );
        final int cycle = (int) ((age - this.limit) / this.limit);
        if (cycle > this.logged.get()) {
            final Method method = MethodSignature.class.cast(
                this.point.getSignature()
            ).getMethod();
            Logger.warn(
                method.getDeclaringClass(),
                "%s: takes more than %[ms]s, %[ms]s already, thread=%s/%s",
                Mnemos.toText(this.point, true, this.skipargs),
                TimeUnit.MILLISECONDS.convert(this.limit, this.unit),
                TimeUnit.MILLISECONDS.convert(age, this.unit),
                this.thread.getName(),
                this.thread.getState()
            );
            Logger.debug(
                method.getDeclaringClass(),
                "%s: thread %s/%s stacktrace: %s",
                Mnemos.toText(this.point, true, this.skipargs),
                this.thread.getName(),
                this.thread.getState(),
                this.stcktrctxt.allText(this.thread.getStackTrace())
            );
            this.logged.set(cycle);
        }
    }

    @Override
    public int hashCode() {
        return this.point.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || MethodMarker.class.cast(obj)
            .point.equals(this.point);
    }

    @Override
    public int compareTo(final MethodMarker marker) {
        int cmp = 0;
        if (this.started < marker.started) {
            cmp = 1;
        } else if (this.started > marker.started) {
            cmp = -1;
        }
        return cmp;
    }

}
