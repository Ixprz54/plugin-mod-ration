package fr.cuboria.moderation.managers;

import fr.cuboria.moderation.ModerationPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeManager {

    private final ModerationPlugin plugin;
    private final Map<UUID, Boolean> frozenPlayers;
    private final Map<UUID, UUID> summonedPlayers; // Player UUID -> Staff UUID qui a convoqué

    public FreezeManager(ModerationPlugin plugin) {
        this.plugin = plugin;
        this.frozenPlayers = new HashMap<>();
        this.summonedPlayers = new HashMap<>();
    }

    public void freezePlayer(Player player) {
        frozenPlayers.put(player.getUniqueId(), true);
        player.sendMessage(plugin.getConfigurationManager().getPrefix() +
                plugin.getConfigurationManager().getMessage("you-are-frozen"));
    }

    public void unfreezePlayer(Player player) {
        frozenPlayers.remove(player.getUniqueId());
        summonedPlayers.remove(player.getUniqueId());
    }

    public boolean isFrozen(UUID playerUuid) {
        return frozenPlayers.getOrDefault(playerUuid, false);
    }

    public boolean isFrozen(Player player) {
        return isFrozen(player.getUniqueId());
    }

    public void toggleFreeze(Player player) {
        if (isFrozen(player)) {
            unfreezePlayer(player);
        } else {
            freezePlayer(player);
        }
    }

    public void summonPlayer(Player player, Player staff) {
        freezePlayer(player);
        summonedPlayers.put(player.getUniqueId(), staff.getUniqueId());

        String summonMessage = plugin.getConfigurationManager().getDiscordSummonMessage();
        player.sendMessage(summonMessage);

        // Log l'action
        plugin.getDiscordWebhookManager().sendStaffLog(
                "Convocation",
                staff.getName() + " a convoqué " + player.getName(),
                0xFFA500 // Orange
        );
    }

    public boolean isSummoned(UUID playerUuid) {
        return summonedPlayers.containsKey(playerUuid);
    }

    public UUID getSummoningStaff(UUID playerUuid) {
        return summonedPlayers.get(playerUuid);
    }

    public void handleSummonedPlayerQuit(Player player) {
        if (isSummoned(player.getUniqueId())) {
            // Le joueur a quitté alors qu'il était convoqué
            // Appliquer un ban permanent
            plugin.getSanctionManager().applySummonEscapeBan(player);

            // Retirer de la liste des joueurs convoqués
            summonedPlayers.remove(player.getUniqueId());
            frozenPlayers.remove(player.getUniqueId());

            // Log l'action
            plugin.getDiscordWebhookManager().sendStaffLog(
                    "Fuite de Convocation",
                    player.getName() + " s'est déconnecté pendant une convocation et a été banni.",
                    0xFF0000 // Rouge
            );
        }
    }

    public void clearAll() {
        frozenPlayers.clear();
        summonedPlayers.clear();
    }
}
