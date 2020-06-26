package fr.entasia.sbquests;

import fr.entasia.apis.utils.TextUtils;
import fr.entasia.sbquests.utils.InvsManager;
import fr.entasia.sbquests.utils.objs.QuestMob;
import fr.entasia.sbquests.utils.objs.Quests;
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

	@EventHandler
	public static void onEntityClick(PlayerInteractEntityEvent e) {
		Entity entity = e.getRightClicked();
		if (entity.getType() == EntityType.VILLAGER && entity.getCustomName().equals("Bob")) {
			e.setCancelled(true);
			if (e.getHand() == EquipmentSlot.HAND) {
				Player p = e.getPlayer();
				BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(p.getLocation()));
				List<MetadataValue> data = p.getMetadata("quest");
				if(data.size()==0) {
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 3.5f, 1.1f);
					p.sendMessage("§3Tu veux une quête tu dit ?");

					long timestamp = Main.main.getConfig().getLong("quests." + is.isid.str());
					if (timestamp == 0) InvsManager.openQuestMenu(p);
					else{
						timestamp = System.currentTimeMillis() - timestamp;
						if (timestamp < DAY) {
							p.sendMessage("§cTu as déjà fini une quête aujourd'hui ! Reviens dans " + TextUtils.secondsToTime((int) (DAY - timestamp) / 1000));
							p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
						} else{
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
										p.removeMetadata("quest", Main.main);
										InvsManager.openQuestMenu(p);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}.runTaskLaterAsynchronously(Main.main, 30);
							Main.main.getConfig().set("quests." + is.isid.str(), null);
						}
					}
				}
			}
		}
	}

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
