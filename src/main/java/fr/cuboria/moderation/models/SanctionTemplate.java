package fr.cuboria.moderation.models;

public class SanctionTemplate {

    private String reason;
    private long duration; // en secondes
    private String durationText;
    private String message;

    public SanctionTemplate(String reason, long duration, String durationText, String message) {
        this.reason = reason;
        this.duration = duration;
        this.durationText = durationText;
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public long getDuration() {
        return duration;
    }

    public String getDurationText() {
        return durationText != null ? durationText : (duration == -1 ? "Permanent" : duration + "s");
    }

    public String getMessage() {
        return message;
    }

    public boolean isPermanent() {
        return duration == -1;
    }
}
