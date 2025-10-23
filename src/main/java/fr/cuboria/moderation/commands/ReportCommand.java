package fr.cuboria.moderation.commands;

import fr.cuboria.moderation.ModerationPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {

    private final ModerationPlugin plugin;

    public ReportCommand(ModerationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage("Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        if (!reporter.hasPermission("cuboria.report")) {
            reporter.sendMessage(plugin.getConfigurationManager().getPrefix() +
                    plugin.getConfigurationManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            reporter.sendMessage(plugin.getConfigurationManager().getPrefix() + "§cUtilisation: /report <joueur> <raison>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            reporter.sendMessage(plugin.getConfigurationManager().getPrefix() +
                    plugin.getConfigurationManager().getMessage("player-not-found"));
            return true;
        }

        if (target.equals(reporter)) {
            reporter.sendMessage(plugin.getConfigurationManager().getPrefix() + "§cVous ne pouvez pas vous signaler vous-même!");
            return true;
        }

        // Construire la raison
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        String reason = reasonBuilder.toString().trim();

        // Créer le report
        plugin.getReportManager().createReport(reporter, target, reason);

        return true;
    }
}
