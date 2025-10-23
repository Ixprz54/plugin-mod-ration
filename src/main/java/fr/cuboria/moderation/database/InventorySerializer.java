package fr.cuboria.moderation.database;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InventorySerializer {

    public static byte[] serializeItemStacks(ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sérialisation de l'inventaire", e);
        }
    }

    public static ItemStack[] deserializeItemStacks(byte[] data) {
        if (data == null) {
            return new ItemStack[0];
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];

            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            return items;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erreur lors de la désérialisation de l'inventaire", e);
        }
    }
}
