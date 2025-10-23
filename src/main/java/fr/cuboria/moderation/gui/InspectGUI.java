package fr.cuboria.moderation.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class InspectGUI {

    private final Player target;
    private final Player viewer;

    public InspectGUI(Player target, Player viewer) {
        this.target = target;
        this.viewer = viewer;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lInspection - " + target.getName());

        // Copier l'inventaire du joueur
        ItemStack[] targetInventory = target.getInventory().getContents();
        for (int i = 0; i < 36 && i < targetInventory.length; i++) {
            inv.setItem(i, targetInventory[i]);
        }

        // Armure
        ItemStack[] armor = target.getInventory().getArmorContents();
        inv.setItem(36, armor[3]); // Casque
        inv.setItem(37, armor[2]); // Plastron
        inv.setItem(38, armor[1]); // Jambières
        inv.setItem(39, armor[0]); // Bottes

        // Item d'informations
        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e" + target.getName());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Vie: §c" + Math.round(target.getHealth()) + "§7/§c" + Math.round(target.getMaxHealth()));
        lore.add("§7Nourriture: §6" + target.getFoodLevel() + "§7/§620");
        lore.add("§7Niveau: §a" + target.getLevel());
        lore.add("§7GameMode: §b" + target.getGameMode().name());
        lore.add("§7Monde: §d" + target.getWorld().getName());
        lore.add("");
        lore.add("§7Position:");
        lore.add("  §7X: §f" + Math.round(target.getLocation().getX()));
        lore.add("  §7Y: §f" + Math.round(target.getLocation().getY()));
        lore.add("  §7Z: §f" + Math.round(target.getLocation().getZ()));

        infoMeta.setLore(lore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(49, infoItem);

        // Remplir les slots vides avec du verre noir
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 40; i < 54; i++) {
            if (i != 49 && inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        viewer.openInventory(inv);
    }
}
