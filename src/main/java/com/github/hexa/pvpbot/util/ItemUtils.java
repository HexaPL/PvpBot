package com.github.hexa.pvpbot.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemUtils {

    public static ItemStack[] getFullArmor(Material material) {
        ItemStack[] armors;
        switch (material) {
            case DIAMOND -> armors = new ItemStack[]{new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_HELMET)};
            case GOLD_INGOT -> armors = new ItemStack[]{new ItemStack(Material.GOLDEN_BOOTS), new ItemStack(Material.GOLDEN_LEGGINGS), new ItemStack(Material.GOLDEN_CHESTPLATE), new ItemStack(Material.GOLDEN_HELMET)};
            case IRON_INGOT -> armors = new ItemStack[]{new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.IRON_HELMET)};
            case CHAIN -> armors = new ItemStack[]{new ItemStack(Material.CHAINMAIL_BOOTS), new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.CHAINMAIL_HELMET)};
            case LEATHER -> armors = new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)};
            default -> {return null;}
        }
        return armors;
    }

    public static ItemStack[] getFullArmor(Material material, Map<Enchantment, Integer> enchantments) {
        ItemStack[] armors = getFullArmor(material);
        return enchantItems(armors, enchantments);
    }

    public static ItemStack[] enchantItems(ItemStack[] items, Map<Enchantment, Integer> enchantments) {
        for (ItemStack item : items) {
            item.addEnchantments(enchantments);
        }
        return items;
    }

}
