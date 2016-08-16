package com.jcabi.aspects.aj;

/**
 * StacktraceElement as String text.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.23
 *
 */
public class StackTraceText {

	/**
     * Textualize a stacktrace.
     * @param trace Array of stacktrace elements
     * @return The text
     */
	public String allText(final StackTraceElement... trace) {
        final StringBuilder text = new StringBuilder();
        for (int pos = 0; pos < trace.length; ++pos) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(this.oneText(trace[pos]));
        }
        return text.toString();
    }

    /**
     * Textualize a stacktrace.
     * @param trace One stacktrace element
     * @return The text
     */
    public String oneText(final StackTraceElement trace) {
        return String.format(
            "%s#%s[%d]",
            trace.getClassName(),
            trace.getMethodName(),
            trace.getLineNumber()
        );
    }

}
