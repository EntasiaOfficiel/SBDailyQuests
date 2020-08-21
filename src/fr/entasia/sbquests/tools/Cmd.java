package fr.entasia.sbquests.tools;

import fr.entasia.apis.utils.TextUtils;
import fr.entasia.sbquests.Main;
import fr.entasia.sbquests.Utils;
import fr.entasia.sbquests.InvsManager;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Cmd implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] arg) {
		if (!(sender instanceof Player)) return true;
		Utils.tryOpen((Player)sender);
		return false;
	}
}
