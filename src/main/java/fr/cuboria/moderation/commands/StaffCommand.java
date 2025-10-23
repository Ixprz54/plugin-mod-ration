package fr.cuboria.moderation.commands;

import fr.cuboria.moderation.ModerationPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffCommand implements CommandExecutor {

    private final ModerationPlugin plugin;

    public StaffCommand(ModerationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        if (!player.hasPermission("cuboria.staff")) {
            player.sendMessage(plugin.getConfigurationManager().getPrefix() +
                    plugin.getConfigurationManager().getMessage("no-permission"));
            return true;
        }

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "staffco", "staff" -> {
                plugin.getStaffModeManager().enableStaffMode(player);
            }
            case "staffdeco" -> {
                plugin.getStaffModeManager().disableStaffMode(player);
            }
        }

        return true;
    }
}
