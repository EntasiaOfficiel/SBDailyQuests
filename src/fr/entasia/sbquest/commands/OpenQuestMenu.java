package fr.entasia.sbquest.commands;

import fr.entasia.sbquest.utils.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenQuestMenu implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) {
		if (!(sender instanceof Player) || !sender.hasPermission("*")) {
			sender.sendMessage("ERROR: Vous n'avez pas la permission de faire cela");
			return false;
		}
		Player p = (Player) sender;
		MenuManager.openQuestMenu(p);

		return true;
	}

}
