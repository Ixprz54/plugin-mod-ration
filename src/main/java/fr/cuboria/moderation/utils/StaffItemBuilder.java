package fr.cuboria.moderation.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.List;

public class StaffItemBuilder {

    private static final String STAFF_ITEM_KEY = "cuboria_staff_item";

    public static ItemStack createStaffItem(Material material, String name, List<String> lore, String itemId) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);

            // Ajouter un tag personnalisé pour identifier l'item
            NamespacedKey key = new NamespacedKey("cuboria", STAFF_ITEM_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);

            item.setItemMeta(meta);
        }

        return item;
    }

    public static String getStaffItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey("cuboria", STAFF_ITEM_KEY);

        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }

        return null;
    }

    public static boolean isStaffItem(ItemStack item) {
        return getStaffItemId(item) != null;
    }
}
