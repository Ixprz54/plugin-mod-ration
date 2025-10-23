package fr.cuboria.moderation.listeners;

import fr.cuboria.moderation.ModerationPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class FreezeListener implements Listener {

    private final ModerationPlugin plugin;

    public FreezeListener(ModerationPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            // Empêcher tout mouvement (même rotation de la tête)
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
                player.teleport(event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigurationManager().getPrefix() +
                    plugin.getConfigurationManager().getMessage("you-are-frozen"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigurationManager().getPrefix() +
                    plugin.getConfigurationManager().getMessage("you-are-frozen"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (plugin.getFreezeManager().isFrozen(player)) {
            // Autoriser seulement certaines commandes
            String command = event.getMessage().toLowerCase();
            if (!command.startsWith("/msg") && !command.startsWith("/r") && !command.startsWith("/reply")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigurationManager().getPrefix() +
                        plugin.getConfigurationManager().getMessage("you-are-frozen"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Le joueur freeze est invulnérable
            if (plugin.getFreezeManager().isFrozen(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Empêcher le joueur freeze d'attaquer
        if (event.getDamager() instanceof Player attacker) {
            if (plugin.getFreezeManager().isFrozen(attacker)) {
                event.setCancelled(true);
                attacker.sendMessage(plugin.getConfigurationManager().getPrefix() +
                        plugin.getConfigurationManager().getMessage("you-are-frozen"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Gérer la déconnexion d'un joueur convoqué
        plugin.getFreezeManager().handleSummonedPlayerQuit(player);
    }
}
