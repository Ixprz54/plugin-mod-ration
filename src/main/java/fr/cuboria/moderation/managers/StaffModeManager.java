package fr.cuboria.moderation.managers;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.utils.StaffItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class StaffModeManager {

    private final ModerationPlugin plugin;
    private final Map<UUID, Boolean> staffModePlayers;
    private final Map<UUID, Boolean> vanishPlayers;

    public StaffModeManager(ModerationPlugin plugin) {
        this.plugin = plugin;
        this.staffModePlayers = new HashMap<>();
        this.vanishPlayers = new HashMap<>();
    }

    public void enableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();

        if (isInStaffMode(player)) {
            player.sendMessage(plugin.getConfigurationManager().getPrefix() + "§cVous êtes déjà en mode staff!");
            return;
        }

        // Sauvegarder l'inventaire
        if (plugin.getConfig().getBoolean("staff-mode.save-inventory", true)) {
            ItemStack[] inventory = player.getInventory().getContents();
            ItemStack[] armor = player.getInventory().getArmorContents();
            plugin.getDatabaseManager().saveStaffInventory(uuid, inventory, armor);
        }

        // Vider l'inventaire
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        // Donner les outils staff
        giveStaffItems(player);

        // Activer le vanish si configuré
        if (plugin.getConfig().getBoolean("staff-mode.auto-vanish", true)) {
            enableVanish(player);
        }

        // Activer le god mode si configuré
        if (plugin.getConfig().getBoolean("staff-mode.auto-god-mode", true)) {
            player.setInvulnerable(true);
        }

        // Passer en creative pour pouvoir voler
        player.setAllowFlight(true);
        player.setFlying(false);

        staffModePlayers.put(uuid, true);

        player.sendMessage(plugin.getConfigurationManager().getPrefix() +
                plugin.getConfigurationManager().getMessage("staff-mode-enabled"));

        // Log l'action
        plugin.getDiscordWebhookManager().sendStaffLog(
                "Mode Staff Activé",
                player.getName() + " est passé en mode staff",
                0x00FF00 // Vert
        );
    }

    public void disableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();

        if (!isInStaffMode(player)) {
            player.sendMessage(plugin.getConfigurationManager().getPrefix() + "§cVous n'êtes pas en mode staff!");
            return;
        }

        // Restaurer l'inventaire
        ItemStack[][] savedInventory = plugin.getDatabaseManager().loadStaffInventory(uuid);
        if (savedInventory != null) {
            player.getInventory().setContents(savedInventory[0]);
            player.getInventory().setArmorContents(savedInventory[1]);
            plugin.getDatabaseManager().deleteStaffInventory(uuid);
        } else {
            player.getInventory().clear();
        }

        // Désactiver le vanish
        if (isVanished(player)) {
            disableVanish(player);
        }

        // Désactiver le god mode
        player.setInvulnerable(false);

        // Désactiver le vol
        player.setAllowFlight(false);
        player.setFlying(false);

        staffModePlayers.remove(uuid);

        player.sendMessage(plugin.getConfigurationManager().getPrefix() +
                plugin.getConfigurationManager().getMessage("staff-mode-disabled"));

        // Log l'action
        plugin.getDiscordWebhookManager().sendStaffLog(
                "Mode Staff Désactivé",
                player.getName() + " a quitté le mode staff",
                0xFF0000 // Rouge
        );
    }

    private void giveStaffItems(Player player) {
        ConfigurationManager config = plugin.getConfigurationManager();

        // Item Vanish
        player.getInventory().setItem(
                config.getStaffItemSlot("vanish"),
                StaffItemBuilder.createStaffItem(
                        config.getStaffItemMaterial("vanish"),
                        config.getStaffItemName("vanish"),
                        config.getStaffItemLore("vanish"),
                        "VANISH"
                )
        );

        // Item Freeze
        player.getInventory().setItem(
                config.getStaffItemSlot("freeze"),
                StaffItemBuilder.createStaffItem(
                        config.getStaffItemMaterial("freeze"),
                        config.getStaffItemName("freeze"),
                        config.getStaffItemLore("freeze"),
                        "FREEZE"
                )
        );

        // Item Téléportation
        player.getInventory().setItem(
                config.getStaffItemSlot("teleport"),
                StaffItemBuilder.createStaffItem(
                        config.getStaffItemMaterial("teleport"),
                        config.getStaffItemName("teleport"),
                        config.getStaffItemLore("teleport"),
                        "TELEPORT"
                )
        );

        // Item Inspection
        player.getInventory().setItem(
                config.getStaffItemSlot("inspect"),
                StaffItemBuilder.createStaffItem(
                        config.getStaffItemMaterial("inspect"),
                        config.getStaffItemName("inspect"),
                        config.getStaffItemLore("inspect"),
                        "INSPECT"
                )
        );

        // Item Rapports
        int pendingReports = plugin.getReportManager().getPendingReportsCount();
        List<String> reportLore = config.getStaffItemLore("reports").stream()
                .map(line -> line.replace("{count}", String.valueOf(pendingReports)))
                .collect(Collectors.toList());
        player.getInventory().setItem(
                config.getStaffItemSlot("reports"),
                StaffItemBuilder.createStaffItem(
                        config.getStaffItemMaterial("reports"),
                        config.getStaffItemName("reports"),
                        reportLore,
                        "REPORTS"
                )
        );
    }

    public void enableVanish(Player player) {
        vanishPlayers.put(player.getUniqueId(), true);

        // Cacher le joueur de tous les non-staff
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.hasPermission("cuboria.staff")) {
                online.hidePlayer(plugin, player);
            }
        }

        // Effet de particules pour indiquer qu'on est vanish
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                0,
                false,
                false,
                false
        ));

        player.sendMessage(plugin.getConfigurationManager().getPrefix() + "§aVanish activé!");
    }

    public void disableVanish(Player player) {
        vanishPlayers.remove(player.getUniqueId());

        // Montrer le joueur à tous
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }

        // Retirer l'effet d'invisibilité
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        player.sendMessage(plugin.getConfigurationManager().getPrefix() + "§cVanish désactivé!");
    }

    public void toggleVanish(Player player) {
        if (isVanished(player)) {
            disableVanish(player);
        } else {
            enableVanish(player);
        }
    }

    public boolean isInStaffMode(Player player) {
        return staffModePlayers.getOrDefault(player.getUniqueId(), false);
    }

    public boolean isVanished(Player player) {
        return vanishPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public void disableAllStaffModes() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInStaffMode(player)) {
                disableStaffMode(player);
            }
        }
    }
}
