package fr.cuboria.moderation.managers;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Sanction;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SanctionManager {

    private final ModerationPlugin plugin;

    public SanctionManager(ModerationPlugin plugin) {
        this.plugin = plugin;
    }

    public void applySanction(Player target, Player staff, Sanction.SanctionType type, String reason, long duration, String message) {
        // Créer la sanction
        Sanction sanction = new Sanction(
                target.getUniqueId(),
                target.getName(),
                staff.getUniqueId(),
                staff.getName(),
                type,
                reason,
                duration
        );

        // Sauvegarder dans la base de données
        plugin.getDatabaseManager().saveSanction(sanction);

        // Appliquer la sanction selon le type
        switch (type) {
            case WARN -> applyWarn(target, staff, message);
            case KICK -> applyKick(target, staff, message);
            case MUTE -> applyMute(target, staff, duration, message);
            case BAN -> applyBan(target, staff, duration, message, reason);
        }

        // Notification au staff
        String staffMessage = plugin.getConfigurationManager().getMessage("sanction-applied")
                .replace("{player}", target.getName())
                .replace("{type}", type.getDisplayName());
        staff.sendMessage(plugin.getConfigurationManager().getPrefix() + staffMessage);

        // Log Discord
        plugin.getDiscordWebhookManager().sendSanctionLog(sanction, message);
    }

    private void applyWarn(Player target, Player staff, String message) {
        target.sendMessage("§c§l⚠ AVERTISSEMENT ⚠");
        target.sendMessage(plugin.getConfigurationManager().colorize(message));
        target.sendMessage("§7Par: §e" + staff.getName());

        // Notifier tous les staff en ligne
        notifyStaff(staff.getName() + " a averti " + target.getName());
    }

    private void applyKick(Player target, Player staff, String message) {
        String kickMessage = "§c§l[KICK]\n\n" +
                plugin.getConfigurationManager().colorize(message) + "\n\n" +
                "§7Par: §e" + staff.getName();

        target.kickPlayer(kickMessage);

        // Notifier tous les staff en ligne
        notifyStaff(staff.getName() + " a expulsé " + target.getName());
    }

    private void applyMute(Player target, Player staff, long duration, String message) {
        target.sendMessage("§c§l[MUTE]");
        target.sendMessage(plugin.getConfigurationManager().colorize(message));
        target.sendMessage("§7Par: §e" + staff.getName());

        if (duration == -1) {
            target.sendMessage("§7Durée: §cPermanent");
        } else {
            target.sendMessage("§7Durée: §e" + formatDuration(duration));
        }

        // Notifier tous les staff en ligne
        notifyStaff(staff.getName() + " a mute " + target.getName());
    }

    private void applyBan(Player target, Player staff, long duration, String message, String reason) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        Date expiration = null;
        if (duration != -1) {
            expiration = new Date(System.currentTimeMillis() + (duration * 1000));
        }

        String banMessage = "§4§l[BANNI]\n\n" +
                plugin.getConfigurationManager().colorize(message) + "\n\n" +
                "§7Raison: §f" + reason + "\n" +
                "§7Par: §e" + staff.getName() + "\n";

        if (duration == -1) {
            banMessage += "§7Durée: §cPermanent\n\n";
        } else {
            banMessage += "§7Durée: §e" + formatDuration(duration) + "\n\n";
        }

        banMessage += "§eContester sur: §9discord.gg/cuboria";

        banList.addBan(target.getName(), banMessage, expiration, staff.getName());
        target.kickPlayer(banMessage);

        // Notifier tous les staff en ligne
        notifyStaff(staff.getName() + " a banni " + target.getName());
    }

    public void applySummonEscapeBan(Player target) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        String banMessage = "§4§l[BANNI - FUITE DE CONVOCATION]\n\n" +
                "§cVous avez été banni définitivement pour avoir quitté\n" +
                "§cpendant une convocation staff.\n\n" +
                "§eContester sur: §9discord.gg/cuboria";

        banList.addBan(target.getName(), banMessage, null, "SYSTEM");

        // Créer la sanction dans la base de données
        Sanction sanction = new Sanction(
                target.getUniqueId(),
                target.getName(),
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "SYSTEM",
                Sanction.SanctionType.BAN,
                "Fuite de convocation",
                -1
        );
        plugin.getDatabaseManager().saveSanction(sanction);

        // Log Discord
        plugin.getDiscordWebhookManager().sendSanctionLog(sanction, banMessage);

        // Notifier tous les staff en ligne
        notifyStaff(target.getName() + " a été banni pour fuite de convocation");
    }

    public List<Sanction> getPlayerSanctions(UUID playerUuid) {
        return plugin.getDatabaseManager().getPlayerSanctions(playerUuid);
    }

    public Sanction getActiveBan(UUID playerUuid) {
        return plugin.getDatabaseManager().getActiveBan(playerUuid);
    }

    public Sanction getActiveMute(UUID playerUuid) {
        return plugin.getDatabaseManager().getActiveMute(playerUuid);
    }

    private void notifyStaff(String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("cuboria.staff")) {
                online.sendMessage(plugin.getConfigurationManager().getPrefix() + "§7[STAFF] §f" + message);
            }
        }
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " seconde(s)";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minute(s)";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " heure(s)";
        } else if (seconds < 2592000) {
            return (seconds / 86400) + " jour(s)";
        } else {
            return (seconds / 2592000) + " mois";
        }
    }
}
