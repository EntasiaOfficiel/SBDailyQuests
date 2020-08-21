package fr.entasia.sbquests;

import fr.entasia.sbquests.tools.Listeners;
import fr.entasia.sbquests.utils.SaveTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class Main extends JavaPlugin {


	public static Main main;
	public static Random r = new Random();

	@Override
	public void onEnable() {
		try{

			main = this;
			getLogger().info("Activation du plugin en cours...");
			new SaveTask().runTaskTimerAsynchronously(this,0, 20*60*2); // 2m

			getServer().getPluginManager().registerEvents(new Listeners(), this);

			saveDefaultConfig();

			getLogger().info("Activé !");
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
