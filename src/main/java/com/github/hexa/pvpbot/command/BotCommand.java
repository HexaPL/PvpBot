package com.github.hexa.pvpbot.command;

import com.github.hexa.pvpbot.v1_16_R3.BotManager;
import com.github.hexa.pvpbot.v1_16_R3.EntityPlayerBot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class BotCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("[PvpBot] Usage: /bot <args>");
            return true;
        }

        if (args[0].equalsIgnoreCase("create") && args.length > 1) {
            Player player = (Player) sender;
            Location location = player.getLocation();
            World world = location.getWorld();
            String name = args[1];
            sender.sendMessage("[PvpBot] Creating bot" + name);
            BotManager.createBot(name, world, location, player);
            sender.sendMessage("[PvpBot] Bot " + name + " spawned!");
            return true;
        }

        if (args[0].equalsIgnoreCase("delete") && args.length > 1) {
            String name = args[1];
            EntityPlayerBot bot = BotManager.bots.get(name);

            if (bot == null) {
                sender.sendMessage("[PvpBot] Bot does not exist!");
                return true;
            }

            BotManager.removeBot(bot);
            sender.sendMessage("[PvpBot] Bot " + name + " deleted!");
            return true;
        }

        if (args[0].equalsIgnoreCase("list") && args.length == 1) {
            if (BotManager.bots.size() == 0) {
                sender.sendMessage("[PvpBot] There are no bots.");
                return true;
            }
            String botList = "";
            for(Map.Entry<String, EntityPlayerBot> entry : BotManager.bots.entrySet()) {
                botList = String.join(botList, entry.getKey());
            }
            sender.sendMessage("[PvpBot] Bots: " + botList);
        }

        return false;
    }
}
