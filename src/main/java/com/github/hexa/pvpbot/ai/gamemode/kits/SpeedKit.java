package com.github.hexa.pvpbot.ai.gamemode.kits;

import com.github.hexa.pvpbot.ControllableBot;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class SpeedKit extends SwordKit {

    @Override
    public void load(ControllableBot bot) {
        super.load(bot);
        ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        bot.getBukkitEntity().getInventory().setLeggings(leggings); // Its just sword kit with iron leggings
    }

}
