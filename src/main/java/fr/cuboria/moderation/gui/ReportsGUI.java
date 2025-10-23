package fr.cuboria.moderation.gui;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportsGUI {

    private final ModerationPlugin plugin;
    private final Player viewer;

    public ReportsGUI(ModerationPlugin plugin, Player viewer) {
        this.plugin = plugin;
        this.viewer = viewer;
    }

    public void open() {
        String title = plugin.getConfigurationManager().colorize("&c&lRapports en attente");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<Report> reports = plugin.getReportManager().getPendingReports();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        int slot = 0;
        for (Report report : reports) {
            if (slot >= 54) break;

            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + report.getTargetName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Signalé par: §f" + report.getReporterName());
            lore.add("§7Raison: §f" + report.getReason());
            lore.add("§7Date: §f" + dateFormat.format(new Date(report.getTimestamp())));
            lore.add("");
            lore.add("§aCliquez pour sanctionner");
            lore.add("§cShift+Clic pour ignorer");

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);

            slot++;
        }

        if (reports.isEmpty()) {
            ItemStack noReports = new ItemStack(Material.BARRIER);
            ItemMeta meta = noReports.getItemMeta();
            meta.setDisplayName("§cAucun rapport en attente");
            noReports.setItemMeta(meta);
            inv.setItem(22, noReports);
        }

        viewer.openInventory(inv);
    }
}
