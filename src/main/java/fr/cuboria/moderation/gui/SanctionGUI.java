package fr.cuboria.moderation.gui;

import fr.cuboria.moderation.ModerationPlugin;
import fr.cuboria.moderation.models.Sanction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SanctionGUI {

    private final ModerationPlugin plugin;
    private final Player target;
    private final Player staff;

    public SanctionGUI(ModerationPlugin plugin, Player target, Player staff) {
        this.plugin = plugin;
        this.target = target;
        this.staff = staff;
    }

    public void open() {
        String title = plugin.getConfigurationManager().colorize("&8&lSanctions - " + target.getName());
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Item Warn
        ItemStack warnItem = createSanctionItem(
                Sanction.SanctionType.WARN,
                plugin.getConfigurationManager().getSanctionIcon(Sanction.SanctionType.WARN),
                plugin.getConfigurationManager().getSanctionName(Sanction.SanctionType.WARN),
                List.of("§7Cliquez pour avertir", "§7le joueur")
        );
        inv.setItem(10, warnItem);

        // Item Kick
        ItemStack kickItem = createSanctionItem(
                Sanction.SanctionType.KICK,
                plugin.getConfigurationManager().getSanctionIcon(Sanction.SanctionType.KICK),
                plugin.getConfigurationManager().getSanctionName(Sanction.SanctionType.KICK),
                List.of("§7Cliquez pour expulser", "§7le joueur")
        );
        inv.setItem(12, kickItem);

        // Item Mute
        ItemStack muteItem = createSanctionItem(
                Sanction.SanctionType.MUTE,
                plugin.getConfigurationManager().getSanctionIcon(Sanction.SanctionType.MUTE),
                plugin.getConfigurationManager().getSanctionName(Sanction.SanctionType.MUTE),
                List.of("§7Cliquez pour mute", "§7le joueur")
        );
        inv.setItem(14, muteItem);

        // Item Ban
        ItemStack banItem = createSanctionItem(
                Sanction.SanctionType.BAN,
                plugin.getConfigurationManager().getSanctionIcon(Sanction.SanctionType.BAN),
                plugin.getConfigurationManager().getSanctionName(Sanction.SanctionType.BAN),
                List.of("§7Cliquez pour bannir", "§7le joueur")
        );
        inv.setItem(16, banItem);

        // Item Convocation (dernier slot)
        ItemStack summonItem = new ItemStack(Material.BELL);
        ItemMeta summonMeta = summonItem.getItemMeta();
        summonMeta.setDisplayName(plugin.getConfigurationManager().colorize("&6&lConvocation"));
        List<String> summonLore = new ArrayList<>();
        summonLore.add("§7Cliquez pour convoquer le joueur");
        summonLore.add("");

        if (plugin.getFreezeManager().isFrozen(target)) {
            summonLore.add("§aStatut: §eFREEZE");
            summonLore.add("§7Cliquez pour UNFREEZE");
        } else {
            summonLore.add("§cStatut: §7Normal");
            summonLore.add("§7Cliquez pour FREEZE");
        }

        summonMeta.setLore(summonLore);
        summonItem.setItemMeta(summonMeta);
        inv.setItem(26, summonItem);

        // Remplir les slots vides avec du verre gris
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }

        staff.openInventory(inv);
    }

    private ItemStack createSanctionItem(Sanction.SanctionType type, Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
