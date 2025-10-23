package fr.cuboria.moderation.managers;

import com.google.gson.JsonObject;
import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Report;
import fr.cuboria.moderation.models.Sanction;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class DiscordWebhookManager {

    private final ModerationPlugin plugin;
    private final SimpleDateFormat dateFormat;

    public DiscordWebhookManager(ModerationPlugin plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    public void sendStaffLog(String title, String description, int color) {
        String webhookUrl = plugin.getConfigurationManager().getWebhookUrl("logs");
        if (webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK")) {
            return; // Webhook non configuré
        }

        JsonObject embed = new JsonObject();
        embed.addProperty("title", title);
        embed.addProperty("description", description);
        embed.addProperty("color", color);
        embed.addProperty("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Cuboria Modération");
        embed.add("footer", footer);

        sendWebhook(webhookUrl, embed);
    }

    public void sendSanctionLog(Sanction sanction, String message) {
        String webhookUrl = plugin.getConfigurationManager().getWebhookUrl("logs");
        if (webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK")) {
            return;
        }

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "Nouvelle Sanction: " + sanction.getType().getDisplayName());

        String description = "**Joueur:** " + sanction.getTargetName() + "\n" +
                "**Staff:** " + sanction.getStaffName() + "\n" +
                "**Raison:** " + sanction.getReason() + "\n" +
                "**Durée:** " + (sanction.isPermanent() ? "Permanent" : formatDuration(sanction.getDuration())) + "\n" +
                "**Date:** " + dateFormat.format(new Date(sanction.getTimestamp()));

        embed.addProperty("description", description);

        // Couleur selon le type
        int color = switch (sanction.getType()) {
            case WARN -> 0xFFFF00; // Jaune
            case KICK -> 0xFFA500; // Orange
            case MUTE -> 0xFF6600; // Orange foncé
            case BAN -> 0xFF0000; // Rouge
        };
        embed.addProperty("color", color);
        embed.addProperty("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Cuboria Modération");
        embed.add("footer", footer);

        sendWebhook(webhookUrl, embed);
    }

    public void sendReportLog(Report report) {
        String webhookUrl = plugin.getConfigurationManager().getWebhookUrl("logs");
        if (webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK")) {
            return;
        }

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "Nouveau Signalement");

        String description = "**Rapporteur:** " + report.getReporterName() + "\n" +
                "**Cible:** " + report.getTargetName() + "\n" +
                "**Raison:** " + report.getReason() + "\n" +
                "**Date:** " + dateFormat.format(new Date(report.getTimestamp()));

        embed.addProperty("description", description);
        embed.addProperty("color", 0xFF9900); // Orange
        embed.addProperty("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Cuboria Modération");
        embed.add("footer", footer);

        sendWebhook(webhookUrl, embed);
    }

    public void sendChangelogUpdate(String change) {
        String webhookUrl = plugin.getConfigurationManager().getWebhookUrl("changelog");
        if (webhookUrl.isEmpty() || webhookUrl.contains("YOUR_WEBHOOK")) {
            return;
        }

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "Mise à jour - Système de Modération");
        embed.addProperty("description", change);
        embed.addProperty("color", 0x00AAFF); // Bleu
        embed.addProperty("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date()));

        JsonObject footer = new JsonObject();
        footer.addProperty("text", "Cuboria Changelog");
        embed.add("footer", footer);

        sendWebhook(webhookUrl, embed);
    }

    private void sendWebhook(String webhookUrl, JsonObject embed) {
        // Exécuter dans un thread async pour ne pas bloquer le serveur
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Cuboria-Moderation-Plugin");
                connection.setDoOutput(true);

                JsonObject payload = new JsonObject();
                payload.addProperty("username", "Cuboria Modération");
                payload.addProperty("avatar_url", "https://cdn.discordapp.com/embed/avatars/0.png");

                // Créer un array d'embeds
                com.google.gson.JsonArray embeds = new com.google.gson.JsonArray();
                embeds.add(embed);
                payload.add("embeds", embeds);

                byte[] out = payload.toString().getBytes(StandardCharsets.UTF_8);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(out);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode != 204 && responseCode != 200) {
                    plugin.getLogger().warning("Erreur lors de l'envoi du webhook Discord: " + responseCode);
                }

                connection.disconnect();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Impossible d'envoyer le webhook Discord", e);
            }
        });
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
