package fr.cuboria.moderation.gui;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Sanction;
import fr.cuboria.moderation.models.SanctionTemplate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TemplateSelectionGUI {

    private final ModerationPlugin plugin;
    private final Player target;
    private final Player staff;
    private final Sanction.SanctionType type;

    public TemplateSelectionGUI(ModerationPlugin plugin, Player target, Player staff, Sanction.SanctionType type) {
        this.plugin = plugin;
        this.target = target;
        this.staff = staff;
        this.type = type;
    }

    public void open() {
        String title = plugin.getConfigurationManager().colorize("&8&l" + type.getDisplayName() + " - Raisons");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<SanctionTemplate> templates = plugin.getConfigurationManager().getTemplates(type);

        int slot = 10;
        for (SanctionTemplate template : templates) {
            if (slot >= 44) break; // Limite de slots

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + template.getReason());

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Durée: §f" + template.getDurationText());
            lore.add("");
            lore.add("§7Cliquez pour appliquer");
            meta.setLore(lore);

            item.setItemMeta(meta);
            inv.setItem(slot, item);

            slot++;
            // Sauter les bordures
            if (slot % 9 == 8) slot += 2;
        }

        // Bouton retour
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§c← Retour");
        backItem.setItemMeta(backMeta);
        inv.setItem(45, backItem);

        // Remplir les slots vides avec du verre gris
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        staff.openInventory(inv);
    }

    public Player getTarget() {
        return target;
    }

    public Sanction.SanctionType getType() {
        return type;
    }
}
