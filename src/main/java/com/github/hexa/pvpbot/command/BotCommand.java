package com.github.hexa.pvpbot.command;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.BotAI;
import com.github.hexa.pvpbot.util.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.FORWARD;

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

        com.github.hexa.pvpbot.BotManager botManager = PvpBotPlugin.getManager();

        if (args[0].equalsIgnoreCase("create") && args.length > 1) {
            Player player = (Player) sender;
            Location location = player.getLocation();
            World world = location.getWorld();
            String name = args[1];
            sender.sendMessage("[PvpBot] Bot " + name + " created");
            Bot bot = botManager.createBot(name, world, location, player);
            bot.getControllable().getAI().setEnabled(false);
            sender.sendMessage("[PvpBot] Bot " + name + " spawned");
            return true;
        }

        if (args[0].equalsIgnoreCase("delete") && args.length > 1) {
            String name = args[1];

            if (!botManager.botExists(name)) {
                sender.sendMessage("[PvpBot] Bot does not exist!");
                return true;
            }

            Bot bot = botManager.getBotByName(name);

            botManager.removeBot(bot);
            sender.sendMessage("[PvpBot] Bot " + name + " deleted!");
            return true;
        }

        if (args[0].equalsIgnoreCase("list") && args.length == 1) {
            if (botManager.bots.size() == 0) {
                sender.sendMessage("[PvpBot] There are no bots.");
                return true;
            }
            String botList = "";
            for(Map.Entry<String, Bot> entry : botManager.bots.entrySet()) {
                botList = String.join(botList, entry.getKey());
            }
            sender.sendMessage("[PvpBot] Bots: " + botList);
            return true;
        }

        if (args[0].equalsIgnoreCase("attack") && args.length > 1) {
            if (!botManager.botExists(args[1])) {
                sender.sendMessage("[PvpBot] Bot " + args[1] + " does not exist!");
                return true;
            }
            Bot bot = botManager.getBotByName(args[1]);
            bot.getControllable().getAI().setEnabled(true);
            // TODO why like that...
            return true;
        }

        if (args[0].equalsIgnoreCase("set") && args.length > 3) {
            if (!botManager.botExists(args[1])) {
                sender.sendMessage("[PvpBot] Bot " + args[1] + " does not exist!");
                return true;
            }
            Bot bot = botManager.getBotByName(args[1]);
            String property = args[2];
            String value = args[3];
            boolean done = bot.getControllable().getAI().getProperties().set(property, value);
            if (done) {
                sender.sendMessage("[PvpBot] Bot property '" + property + "' set to '" + value + "'.");
            } else {
                sender.sendMessage("[PvpBot] Bot property '" + property + "' does not exist or is incorrect!");
            }
            return true;
        }

        return false;
    }
}