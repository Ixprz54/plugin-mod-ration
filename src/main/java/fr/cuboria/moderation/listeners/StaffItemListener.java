package fr.cuboria.moderation.listeners;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.gui.InspectGUI;
import fr.cuboria.moderation.gui.ReportsGUI;
import fr.cuboria.moderation.gui.SanctionGUI;
import fr.cuboria.moderation.gui.TemplateSelectionGUI;
import fr.cuboria.moderation.models.Report;
import fr.cuboria.moderation.models.Sanction;
import fr.cuboria.moderation.models.SanctionTemplate;
import fr.cuboria.moderation.utils.StaffItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class StaffItemListener implements Listener {

    private final ModerationPlugin plugin;

    public StaffItemListener(ModerationPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !StaffItemBuilder.isStaffItem(item)) {
            return;
        }

        if (!plugin.getStaffModeManager().isInStaffMode(player)) {
            return;
        }

        event.setCancelled(true);

        String itemId = StaffItemBuilder.getStaffItemId(item);

        switch (itemId) {
            case "VANISH" -> {
                plugin.getStaffModeManager().toggleVanish(player);
            }
            case "REPORTS" -> {
                ReportsGUI gui = new ReportsGUI(plugin, player);
                gui.open();
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player staff = event.getPlayer();
        ItemStack item = staff.getInventory().getItemInMainHand();

        if (!StaffItemBuilder.isStaffItem(item)) {
            return;
        }

        if (!plugin.getStaffModeManager().isInStaffMode(staff)) {
            return;
        }

        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        event.setCancelled(true);

        String itemId = StaffItemBuilder.getStaffItemId(item);

        switch (itemId) {
            case "FREEZE" -> {
                plugin.getFreezeManager().toggleFreeze(target);

                String message = plugin.getFreezeManager().isFrozen(target) ?
                        plugin.getConfigurationManager().getMessage("player-frozen") :
                        plugin.getConfigurationManager().getMessage("player-unfrozen");

                staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                        message.replace("{player}", target.getName()));
            }
            case "TELEPORT" -> {
                staff.teleport(target.getLocation());
                staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                        "§aTéléporté à §e" + target.getName());
            }
            case "INSPECT" -> {
                InspectGUI gui = new InspectGUI(target, staff);
                gui.open();
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player staff)) {
            return;
        }

        String title = event.getView().getTitle();

        // GUI de sanction principale
        if (title.startsWith("§8§lSanctions - ")) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) {
                return;
            }

            String displayName = clicked.getItemMeta().getDisplayName();
            String targetName = title.replace("§8§lSanctions - ", "");
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                        plugin.getConfigurationManager().getMessage("player-not-found"));
                staff.closeInventory();
                return;
            }

            // Détecter le type de sanction cliqué
            Sanction.SanctionType type = null;
            if (displayName.contains("Avertissement")) {
                type = Sanction.SanctionType.WARN;
            } else if (displayName.contains("Expulsion") || displayName.contains("Kick")) {
                type = Sanction.SanctionType.KICK;
            } else if (displayName.contains("Mute")) {
                type = Sanction.SanctionType.MUTE;
            } else if (displayName.contains("Ban")) {
                type = Sanction.SanctionType.BAN;
            } else if (displayName.contains("Convocation")) {
                // Gestion de la convocation
                if (plugin.getFreezeManager().isFrozen(target)) {
                    plugin.getFreezeManager().unfreezePlayer(target);
                    staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                            "§a" + target.getName() + " a été unfreeze.");
                } else {
                    plugin.getFreezeManager().summonPlayer(target, staff);
                    staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                            "§e" + target.getName() + " a été convoqué et freeze.");
                }

                // Rafraîchir la GUI
                staff.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    SanctionGUI gui = new SanctionGUI(plugin, target, staff);
                    gui.open();
                }, 1L);
                return;
            }

            if (type != null) {
                // Stocker le contexte
                plugin.getGUIManager().setContext(staff.getUniqueId(), target, type);
                // Ouvrir le menu de sélection de template
                TemplateSelectionGUI templateGUI = new TemplateSelectionGUI(plugin, target, staff, type);
                templateGUI.open();
            }
        }

        // GUI de sélection de template
        else if (title.contains(" - Raisons")) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) {
                return;
            }

            String displayName = clicked.getItemMeta().getDisplayName();

            // Bouton retour
            if (displayName.equals("§c← Retour")) {
                // Récupérer le contexte
                var context = plugin.getGUIManager().getContext(staff.getUniqueId());
                if (context != null) {
                    Player target = context.getTarget();
                    if (target != null && target.isOnline()) {
                        staff.closeInventory();
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            SanctionGUI gui = new SanctionGUI(plugin, target, staff);
                            gui.open();
                        }, 1L);
                    } else {
                        staff.closeInventory();
                        plugin.getGUIManager().clearContext(staff.getUniqueId());
                    }
                } else {
                    staff.closeInventory();
                }
                return;
            }

            // Récupérer le contexte
            var context = plugin.getGUIManager().getContext(staff.getUniqueId());
            if (context == null) {
                staff.sendMessage(plugin.getConfigurationManager().getPrefix() + "§cContexte perdu, veuillez recommencer.");
                staff.closeInventory();
                return;
            }

            Player target = context.getTarget();
            Sanction.SanctionType type = context.getType();

            if (target == null || !target.isOnline()) {
                staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                        plugin.getConfigurationManager().getMessage("player-not-found"));
                staff.closeInventory();
                plugin.getGUIManager().clearContext(staff.getUniqueId());
                return;
            }

            // Trouver le template sélectionné
            String reason = displayName.replace("§e", "");

            // Trouver le template exact
            List<SanctionTemplate> templates = plugin.getConfigurationManager().getTemplates(type);
            SanctionTemplate selectedTemplate = null;

            for (SanctionTemplate template : templates) {
                if (template.getReason().equals(reason)) {
                    selectedTemplate = template;
                    break;
                }
            }

            if (selectedTemplate == null) {
                staff.sendMessage(plugin.getConfigurationManager().getPrefix() + "§cTemplate introuvable.");
                staff.closeInventory();
                return;
            }

            // Appliquer la sanction
            plugin.getSanctionManager().applySanction(
                    target,
                    staff,
                    type,
                    selectedTemplate.getReason(),
                    selectedTemplate.getDuration(),
                    selectedTemplate.getMessage()
            );

            staff.closeInventory();
            plugin.getGUIManager().clearContext(staff.getUniqueId());
        }

        // GUI de rapports
        else if (title.equals("§c§lRapports en attente")) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) {
                return;
            }

            String targetName = clicked.getItemMeta().getDisplayName().replace("§e", "");
            Player target = Bukkit.getPlayer(targetName);

            if (target == null) {
                staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                        "§cCe joueur n'est plus en ligne.");
                return;
            }

            if (event.isShiftClick()) {
                // Marquer comme traité
                List<Report> reports = plugin.getReportManager().getPendingReports();
                for (Report report : reports) {
                    if (report.getTargetName().equals(targetName)) {
                        plugin.getReportManager().markReportAsHandled(report, staff);
                        staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                                "§aReport ignoré.");
                        break;
                    }
                }

                // Rafraîchir la GUI
                staff.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    ReportsGUI gui = new ReportsGUI(plugin, staff);
                    gui.open();
                }, 1L);
            } else {
                // Ouvrir le menu de sanction
                staff.closeInventory();
                SanctionGUI gui = new SanctionGUI(plugin, target, staff);
                gui.open();
            }
        }

        // GUI d'inspection (empêcher la modification)
        else if (title.startsWith("§8§lInspection - ")) {
            event.setCancelled(true);
        }
    }
}
