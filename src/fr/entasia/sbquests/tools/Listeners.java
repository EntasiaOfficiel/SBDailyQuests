package fr.entasia.sbquests.tools;

import fr.entasia.sbquests.Main;
import fr.entasia.sbquests.utils.QuestMob;
import fr.entasia.sbquests.utils.Quests;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.CooManager;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class Listeners implements Listener {

	@EventHandler
	public static void onEntityKill(EntityDeathEvent e) {
		Entity entity = e.getEntity();
		Player killer = e.getEntity().getKiller();
		if (killer==null) return;
		BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(killer.getLocation()));
		if(is==null||is.getMember(killer.getUniqueId())==null)return;
		String id = is.isid.str();
		if (Main.main.getConfig().getConfigurationSection("quests."+id) != null) {
			Quests current_quest = Quests.getByID(Main.main.getConfig().getInt("quests."+id+".id"));
			if(current_quest==null)return;
			int i = 0;
			for (QuestMob qm : current_quest.content.mobs) {
				if (qm.type == entity.getType()) {
					if (Main.main.getConfig().getInt("quests." + id + ".mobs." + i) >= qm.number)break;
					ConfigurationSection cs = Main.main.getConfig().getConfigurationSection("quests." + id + ".mobs");

					int mobs_number = cs.getInt(String.valueOf(i));
					mobs_number++;
					Main.main.getConfig().set("quests." + id + ".mobs." + i, mobs_number);

					if (mobs_number == qm.number) {
						killer.sendMessage("§2Félicitation, tu as tué §3" + qm.number + " " + qm.name + "§2, tu as fini un objectif pour ta quête d'île journaliere !");
						killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3.5f, 1.1f);
					} else {
						killer.sendActionBar("§2Tu as tué un(e) §3" + qm.name + "§2, plus que §3" + (qm.number - mobs_number) + "§2!");
						killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.5f, 1.1f);						}
					break;
				}
				i++;
			}
		}
	}
}
