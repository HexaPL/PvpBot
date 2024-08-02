package com.github.hexa.pvpbot.command;

import com.github.hexa.pvpbot.AbstractBotManager;
import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.ai.gamemode.GameMode;
import com.github.hexa.pvpbot.PvpBotPlugin;
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

        AbstractBotManager botManager = PvpBotPlugin.getManager();

        if (args[0].equalsIgnoreCase("create") && args.length > 1) {
            String name = args[1];
            if (botManager.botExists(name)) {
                sender.sendMessage("[PvpBot] Bot " + name + " already exists!");
                return true;
            }
            if (args.length == 2) {
                sender.sendMessage("[PvpBot] Choose the gamemode (NONE, SWORD, SPEED)");
                return true;
            }
            GameMode gameMode = GameMode.valueOf(args[2].toUpperCase());
            Player player = (Player) sender;
            sender.sendMessage("[PvpBot] Bot " + name + " created with gamemode " + gameMode);
            Bot bot = botManager.createBot(name, gameMode, player);
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
            for (Map.Entry<String, Bot> entry : botManager.bots.entrySet()) {
                botList = String.join(", ", botList, entry.getKey());
            }
            sender.sendMessage("[PvpBot] Bots: " + botList);
            return true;
        }

        if (args[0].equalsIgnoreCase("attack") && args.length > 2) {
            if (!botManager.botExists(args[1])) {
                sender.sendMessage("[PvpBot] Bot " + args[1] + " does not exist!");
                return true;
            }
            Bot bot = botManager.getBotByName(args[1]);
            boolean attack = Boolean.parseBoolean(args[2]);
            bot.getControllable().getAI().setEnabled(attack);
            sender.sendMessage("[PvpBot] Attack set to " + bot.getControllable().getAI().isEnabled());
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