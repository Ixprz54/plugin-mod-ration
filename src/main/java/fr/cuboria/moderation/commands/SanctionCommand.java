package fr.cuboria.moderation.commands;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.gui.SanctionGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SanctionCommand implements CommandExecutor {

    private final ModerationPlugin plugin;

    public SanctionCommand(ModerationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player staff)) {
            sender.sendMessage("Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        if (!staff.hasPermission("cuboria.staff.sanction")) {
            staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                    plugin.getConfigurationManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            staff.sendMessage(plugin.getConfigurationManager().getPrefix() + "§cUtilisation: /sanction <joueur>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            staff.sendMessage(plugin.getConfigurationManager().getPrefix() +
                    plugin.getConfigurationManager().getMessage("player-not-found"));
            return true;
        }

        // Ouvrir la GUI de sanction
        SanctionGUI gui = new SanctionGUI(plugin, target, staff);
        gui.open();

        return true;
    }
}
