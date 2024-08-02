package com.github.hexa.pvpbot.ai.gamemode.kits;

import com.github.hexa.pvpbot.ControllableBot;
import com.github.hexa.pvpbot.ai.gamemode.Kit;
import com.github.hexa.pvpbot.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class SwordKit implements Kit {

    @Override
    public void load(ControllableBot bot) {
        PlayerInventory inventory = bot.getBukkitEntity().getInventory();
        inventory.setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
        ItemStack[] armor = ItemUtils.getFullArmor(Material.DIAMOND, Map.of(Enchantment.PROTECTION_ENVIRONMENTAL, 3));
        inventory.setArmorContents(armor);
    }

}
