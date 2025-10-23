package fr.cuboria.moderation.models;

import java.util.UUID;

public class Sanction {

    private int id;
    private UUID targetUuid;
    private String targetName;
    private UUID staffUuid;
    private String staffName;
    private SanctionType type;
    private String reason;
    private long duration; // en secondes, -1 = permanent
    private long timestamp;
    private boolean active;

    public Sanction(UUID targetUuid, String targetName, UUID staffUuid, String staffName,
                    SanctionType type, String reason, long duration) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.staffUuid = staffUuid;
        this.staffName = staffName;
        this.type = type;
        this.reason = reason;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
        this.active = true;
    }

    public Sanction(int id, UUID targetUuid, String targetName, UUID staffUuid, String staffName,
                    SanctionType type, String reason, long duration, long timestamp, boolean active) {
        this.id = id;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.staffUuid = staffUuid;
        this.staffName = staffName;
        this.type = type;
        this.reason = reason;
        this.duration = duration;
        this.timestamp = timestamp;
        this.active = active;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public UUID getStaffUuid() {
        return staffUuid;
    }

    public String getStaffName() {
        return staffName;
    }

    public SanctionType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public long getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPermanent() {
        return duration == -1;
    }

    public boolean isExpired() {
        if (isPermanent()) {
            return false;
        }
        return System.currentTimeMillis() > (timestamp + (duration * 1000));
    }

    public long getExpirationTime() {
        if (isPermanent()) {
            return -1;
        }
        return timestamp + (duration * 1000);
    }

    public enum SanctionType {
        WARN("Avertissement"),
        KICK("Expulsion"),
        MUTE("Mute"),
        BAN("Bannissement");

        private final String displayName;

        SanctionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
