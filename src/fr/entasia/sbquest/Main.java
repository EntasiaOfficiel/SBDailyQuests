package fr.entasia.sbquest;

import fr.entasia.sbquest.commands.OpenQuestMenu;
import fr.entasia.sbquest.utils.EventManager;
import fr.entasia.sbquest.utils.SaveTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {


	public static Main main;
	public static final String CONFIGBASE = "quests";

	@Override
	public void onEnable() {
		try{

			main = this;
			getLogger().info("Activation du plugin en cours...");
			new SaveTask().runTaskTimerAsynchronously(this,0, 20*60*2);

			getCommand("openquestmenu").setExecutor(new OpenQuestMenu());
			getServer().getPluginManager().registerEvents(new EventManager(), this);

			saveDefaultConfig();

			getLogger().info("Activé !");

			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					Main.main.saveConfig();
				}
			}, 20, 2400);
		}catch(Exception e){
			e.printStackTrace();
			getLogger().severe("UNE ERREUR EST SURVENUE ! ARRET DU SERVEUR");
			getServer().shutdown();
		}
	}

	@Override
	public void onDisable() {
		getLogger().info("Désactivation du plugin en cours...");
		saveConfig();
	}
}
