package fr.cuboria.moderation.managers;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Sanction;
import fr.cuboria.moderation.models.SanctionTemplate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationManager {

    private final ModerationPlugin plugin;
    private FileConfiguration sanctionsConfig;
    private Map<Sanction.SanctionType, List<SanctionTemplate>> sanctionTemplates;

    public ConfigurationManager(ModerationPlugin plugin) {
        this.plugin = plugin;
        loadSanctionsConfig();
        loadSanctionTemplates();
    }

    private void loadSanctionsConfig() {
        File sanctionsFile = new File(plugin.getDataFolder(), "sanctions.yml");
        if (!sanctionsFile.exists()) {
            plugin.saveResource("sanctions.yml", false);
        }
        sanctionsConfig = YamlConfiguration.loadConfiguration(sanctionsFile);
    }

    private void loadSanctionTemplates() {
        sanctionTemplates = new HashMap<>();

        ConfigurationSection sanctionsSection = sanctionsConfig.getConfigurationSection("sanctions");
        if (sanctionsSection == null) {
            plugin.getLogger().warning("Section 'sanctions' introuvable dans sanctions.yml");
            return;
        }

        // Charger les templates pour chaque type
        loadTemplatesForType(sanctionsSection, "warn", Sanction.SanctionType.WARN);
        loadTemplatesForType(sanctionsSection, "mute", Sanction.SanctionType.MUTE);
        loadTemplatesForType(sanctionsSection, "kick", Sanction.SanctionType.KICK);
        loadTemplatesForType(sanctionsSection, "ban", Sanction.SanctionType.BAN);
    }

    private void loadTemplatesForType(ConfigurationSection sanctionsSection, String key, Sanction.SanctionType type) {
        ConfigurationSection typeSection = sanctionsSection.getConfigurationSection(key);
        if (typeSection == null) {
            plugin.getLogger().warning("Section '" + key + "' introuvable dans sanctions.yml");
            return;
        }

        List<?> templatesList = typeSection.getList("templates");
        if (templatesList == null) {
            plugin.getLogger().warning("Templates introuvables pour '" + key + "'");
            return;
        }

        List<SanctionTemplate> templates = new ArrayList<>();
        for (Object obj : templatesList) {
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> templateMap = (Map<String, Object>) obj;

                String reason = (String) templateMap.get("reason");
                long duration = templateMap.containsKey("duration") ?
                        ((Number) templateMap.get("duration")).longValue() : 0;
                String durationText = (String) templateMap.get("duration-text");
                String message = (String) templateMap.get("message");

                if (reason != null && message != null) {
                    templates.add(new SanctionTemplate(reason, duration, durationText, message));
                }
            }
        }

        sanctionTemplates.put(type, templates);
        plugin.getLogger().info("Chargé " + templates.size() + " templates pour " + type.name());
    }

    public List<SanctionTemplate> getTemplates(Sanction.SanctionType type) {
        return sanctionTemplates.getOrDefault(type, new ArrayList<>());
    }

    public String getMessage(String key) {
        return colorize(plugin.getConfig().getString("messages." + key, key));
    }

    public String getPrefix() {
        return colorize(plugin.getConfig().getString("messages.prefix", "&8[&6Cuboria&8] &r"));
    }

    public String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public Material getStaffItemMaterial(String itemKey) {
        String materialName = plugin.getConfig().getString("staff-items." + itemKey + ".material", "STONE");
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Matériel invalide pour " + itemKey + ": " + materialName);
            return Material.STONE;
        }
    }

    public String getStaffItemName(String itemKey) {
        return colorize(plugin.getConfig().getString("staff-items." + itemKey + ".name", itemKey));
    }

    public List<String> getStaffItemLore(String itemKey) {
        List<String> lore = plugin.getConfig().getStringList("staff-items." + itemKey + ".lore");
        List<String> colorizedLore = new ArrayList<>();
        for (String line : lore) {
            colorizedLore.add(colorize(line));
        }
        return colorizedLore;
    }

    public int getStaffItemSlot(String itemKey) {
        return plugin.getConfig().getInt("staff-items." + itemKey + ".slot", 0);
    }

    public String getSanctionName(Sanction.SanctionType type) {
        return colorize(sanctionsConfig.getString("sanctions." + type.name().toLowerCase() + ".name", type.getDisplayName()));
    }

    public Material getSanctionIcon(Sanction.SanctionType type) {
        String materialName = sanctionsConfig.getString("sanctions." + type.name().toLowerCase() + ".icon", "PAPER");
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return Material.PAPER;
        }
    }

    public String getDiscordSummonMessage() {
        return colorize(String.join("\n", plugin.getConfig().getStringList("discord.summon-message")));
    }

    public String getWebhookUrl(String type) {
        return plugin.getConfig().getString("discord.webhook-" + type, "");
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        loadSanctionsConfig();
        loadSanctionTemplates();
    }
}
