package fr.entasia.sbquests.commands;

import fr.entasia.sbquests.utils.InvsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) {
		if(sender instanceof Player && sender.hasPermission("*")) {
			InvsManager.openQuestMenu((Player) sender);
		}else sender.sendMessage("ERROR: Vous n'avez pas la permission de faire cela");

		return true;
	}

}
