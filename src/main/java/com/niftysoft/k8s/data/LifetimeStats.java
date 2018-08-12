package com.niftysoft.k8s.data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.LongAdder;

/**
 * Static value-class for storing the stats collected by this node during its lifetime.
 */
public abstract class LifetimeStats {
    public static final LocalDateTime START_TIME = LocalDateTime.now();

    public static final LongAdder SUCCESSFUL_OUTGOING_SYNCS = new LongAdder();
    public static final LongAdder SUCCESSFUL_INCOMING_SYNCS = new LongAdder();

    public static final LongAdder SUCCESSFUL_HTTP_REQUESTS = new LongAdder();
    public static final LongAdder FAILED_HTTP_REQUESTS = new LongAdder();
}
