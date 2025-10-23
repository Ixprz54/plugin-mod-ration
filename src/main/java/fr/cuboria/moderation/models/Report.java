package fr.cuboria.moderation.models;

import java.util.UUID;

public class Report {

    private int id;
    private UUID reporterUuid;
    private String reporterName;
    private UUID targetUuid;
    private String targetName;
    private String reason;
    private long timestamp;
    private boolean handled;
    private UUID handledByUuid;
    private String handledByName;

    public Report(UUID reporterUuid, String reporterName, UUID targetUuid, String targetName, String reason) {
        this.reporterUuid = reporterUuid;
        this.reporterName = reporterName;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
        this.handled = false;
    }

    public Report(int id, UUID reporterUuid, String reporterName, UUID targetUuid, String targetName,
                  String reason, long timestamp, boolean handled, UUID handledByUuid, String handledByName) {
        this.id = id;
        this.reporterUuid = reporterUuid;
        this.reporterName = reporterName;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.reason = reason;
        this.timestamp = timestamp;
        this.handled = handled;
        this.handledByUuid = handledByUuid;
        this.handledByName = handledByName;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getReporterUuid() {
        return reporterUuid;
    }

    public String getReporterName() {
        return reporterName;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public UUID getHandledByUuid() {
        return handledByUuid;
    }

    public void setHandledByUuid(UUID handledByUuid) {
        this.handledByUuid = handledByUuid;
    }

    public String getHandledByName() {
        return handledByName;
    }

    public void setHandledByName(String handledByName) {
        this.handledByName = handledByName;
    }
}
