package fr.entasia.sbquest.utils;

import fr.entasia.sbquest.Main;
import org.bukkit.scheduler.BukkitRunnable;


public class SaveTask extends BukkitRunnable {

	@Override
	public void run() {
		Main.main.saveConfig();
	}
}
