package model;

/**
 * One row of the activity_logs audit trail.
 * The timestamp is already formatted in local time for display.
 */
public record ActivityLog(int logId, int userId, String actionType, String description, String timestamp) {
}
