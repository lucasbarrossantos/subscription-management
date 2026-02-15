package com.globo.subscription.adapter.kafka.util;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

public class TraceUtil {

    public static String getCurrentTraceId() {
        SpanContext context = Span.current().getSpanContext();
        if (context != null && context.isValid()) {
            return context.getTraceId();
        }
        return null;
    }
}
