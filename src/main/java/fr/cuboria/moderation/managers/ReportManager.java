package fr.cuboria.moderation.managers;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReportManager {

    private final ModerationPlugin plugin;
    private final Map<UUID, Long> reportCooldowns;
    private static final long COOLDOWN_TIME = 60000; // 1 minute

    public ReportManager(ModerationPlugin plugin) {
        this.plugin = plugin;
        this.reportCooldowns = new HashMap<>();
    }

    public boolean createReport(Player reporter, Player target, String reason) {
        // Vérifier le cooldown
        if (isOnCooldown(reporter.getUniqueId())) {
            long remainingTime = getRemainingCooldown(reporter.getUniqueId());
            reporter.sendMessage(plugin.getConfigurationManager().getPrefix() +
                    "§cVeuillez attendre " + (remainingTime / 1000) + " secondes avant de faire un autre report.");
            return false;
        }

        // Créer le report
        Report report = new Report(
                reporter.getUniqueId(),
                reporter.getName(),
                target.getUniqueId(),
                target.getName(),
                reason
        );

        // Sauvegarder dans la base de données
        plugin.getDatabaseManager().saveReport(report);

        // Ajouter le cooldown
        reportCooldowns.put(reporter.getUniqueId(), System.currentTimeMillis());

        // Notifier le reporter
        reporter.sendMessage(plugin.getConfigurationManager().getPrefix() +
                plugin.getConfigurationManager().getMessage("report-sent"));

        // Notifier le staff
        String staffNotification = plugin.getConfigurationManager().getMessage("report-received")
                .replace("{reporter}", reporter.getName())
                .replace("{target}", target.getName())
                .replace("{reason}", reason);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("cuboria.staff")) {
                online.sendMessage(plugin.getConfigurationManager().getPrefix() + staffNotification);
            }
        }

        // Log Discord
        plugin.getDiscordWebhookManager().sendReportLog(report);

        return true;
    }

    public List<Report> getPendingReports() {
        return plugin.getDatabaseManager().getPendingReports();
    }

    public int getPendingReportsCount() {
        return getPendingReports().size();
    }

    public void markReportAsHandled(Report report, Player handler) {
        plugin.getDatabaseManager().markReportAsHandled(report, handler.getUniqueId(), handler.getName());
    }

    private boolean isOnCooldown(UUID playerUuid) {
        if (!reportCooldowns.containsKey(playerUuid)) {
            return false;
        }
        long lastReport = reportCooldowns.get(playerUuid);
        return System.currentTimeMillis() - lastReport < COOLDOWN_TIME;
    }

    private long getRemainingCooldown(UUID playerUuid) {
        long lastReport = reportCooldowns.getOrDefault(playerUuid, 0L);
        long elapsed = System.currentTimeMillis() - lastReport;
        return COOLDOWN_TIME - elapsed;
    }
}
