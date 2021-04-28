package com.mashreq.transfercoreservice.paylater.enums;

/**
 * Scheduler lifecycle event status
 *
 * @author Kalim
 *
 */
public enum SchedulerStatus {
    INITIATED,
    RETRIEVAL,
    PROCESSING,
    COMPLETED,
    ABORTED
}