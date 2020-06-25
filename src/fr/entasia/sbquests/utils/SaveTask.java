package fr.entasia.sbquests.utils;

import fr.entasia.sbquests.Main;
import org.bukkit.scheduler.BukkitRunnable;


public class SaveTask extends BukkitRunnable {

	@Override
	public void run() {
		Main.main.saveConfig();
	}
}
