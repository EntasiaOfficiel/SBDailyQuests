package fr.entasia.sbquest;

import fr.entasia.apis.utils.TextUtils;
import fr.entasia.sbquest.utils.InvsManager;
import fr.entasia.sbquest.utils.objs.QuestMob;
import fr.entasia.sbquest.utils.objs.Quests;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.CooManager;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Listeners implements Listener {

	public static final int DAY = 24 * 60 * 60 * 1000;

	private static void msg(Player p){
		p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 3.5f, 1.1f);
		p.sendMessage("§3Tu veux une quête tu dit ?");
	}

	@EventHandler
	public static void onEntityClick(PlayerInteractEntityEvent e) {
		if (e.getHand() == EquipmentSlot.HAND) {
			Entity entity = e.getRightClicked();
			if (entity.getType() == EntityType.VILLAGER && entity.getCustomName().equals("Bob")) {
				e.setCancelled(true);

				Player p = e.getPlayer();
				BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(p.getLocation()));

				long timestamp = Main.main.getConfig().getLong("quests." + is.isid.str());
				if (timestamp > 0) {
					long tdone = System.currentTimeMillis() - timestamp;
					if (tdone < DAY) {
						p.sendMessage("§cTu as déjà fini une quête aujourd'hui ! Reviens dans " + TextUtils.secondsToTime((int) (DAY - tdone) / 1000));
						p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
						return;
					} else Main.main.getConfig().set("quests." + is.isid.str(), null);
				}
				List<MetadataValue> data = p.getMetadata("quest");
				if(data.size()==0) {
					msg(p);
					p.setMetadata("quest", new FixedMetadataValue(Main.main, 1));
					new BukkitRunnable() {
						@Override
						public void run() {
							try {
								p.sendMessage("§3Hmmm... Laisse moi reflechir...");
								p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 3.5f, 1.1f);
								Thread.sleep(1500);
								p.sendMessage("§3J'ai quelque chose pour toi !");
								p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 3.5f, 1.1f);
								Thread.sleep(1500);
								p.setMetadata("quest", new FixedMetadataValue(Main.main, 2));
								InvsManager.openQuestMenu(p);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}.runTaskLaterAsynchronously(Main.main, 30);
				}else if (data.get(0).asInt()==2){
					msg(p);
					InvsManager.openQuestMenu(p);
				}
			}
		}
	}

	@EventHandler
	public static void onEntityKill(EntityDeathEvent e) {
		Entity entity = e.getEntity();
		if (e.getEntity().getKiller() == null) return;
		Player killer = e.getEntity().getKiller();
		if (Main.main.getConfig().getConfigurationSection("quests." + killer.getName()) != null) {
			Quests current_quest = Quests.getByID(Main.main.getConfig().getInt("quests." + killer.getName() + ".id"));
			if(current_quest==null)return;
			int i = 0;
			for (QuestMob qm : current_quest.content.mobs) {
				if (Main.main.getConfig().getInt("quests." + killer.getName() + ".mobs." + i) >= qm.number){i++;continue;}
				if (qm.type == entity.getType()) {
					ConfigurationSection cs = Main.main.getConfig().getConfigurationSection("quests." + killer.getName() + ".mobs");
					int mobs_number = cs.getInt("" + i);
					mobs_number++;
					Main.main.getConfig().set("quests." + killer.getName() + ".mobs." + i, mobs_number);
					if (mobs_number == qm.number) {
						killer.sendMessage("§2Félicitation, vous avez tué §3" + qm.number + " " + qm.name + "§2, vous avez fini un objectif pour votre quête journaliere !");
						killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3.5f, 1.1f);
					} else {
						killer.sendActionBar("§2Vous avez tué un(e) §3" + qm.name + "§2, plus que §3" + (qm.number - mobs_number) + "§2!");
						killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.5f, 1.1f);						}
					break;
				}
				i++;
			}
		}
	}
}