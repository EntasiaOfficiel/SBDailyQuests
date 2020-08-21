package fr.entasia.sbquests;

import fr.entasia.apis.utils.TextUtils;
import fr.entasia.sbquests.utils.Quests;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Utils {


	public static Quests createQuest(BaseIsland is){ // pass by reference :)
		ArrayList<Quests> okQuests = new ArrayList<>();

		String id = is.isid.str();
		long islevel = is.getLevel();


		// on choisi une quête valide selon le lvl
		for(Quests qs : Quests.values()){
			if(islevel>=qs.minlevel&&islevel<=qs.maxlevel) okQuests.add(qs);
		}
		if(okQuests.size()==0)return null;

		Quests current = okQuests.get(Main.r.nextInt(okQuests.size()));


		// modification de la config
		Main.main.getConfig().set("quests." + id +".id", current.id);
		Main.main.getConfig().set("quests." + id + ".time", System.currentTimeMillis());

		for (int i=0;i<current.content.items.size();i++) {
			Main.main.getConfig().set("quests." + id + ".items." + i, 0);
		}

		for (int i=0;i<current.content.mobs.size();i++) {
			Main.main.getConfig().set("quests." + id + ".mobs." + i, 0);
		}
		return current;
	}

	public static void tryOpen(Player p){
		BaseIsland is = BaseAPI.getIsland(p.getLocation());
		if(is==null){
			p.sendMessage("§cUne erreur est survenue ! (No island)");
		}else if(is.getMember(p.getUniqueId())==null){
			p.sendMessage("§cTu n'es pas membre de cette île !");
		}else{
			long timestamp = Main.main.getConfig().getLong("quests." + is.isid.str());
			if (timestamp == 0) InvsManager.openQuestMenu(is, p);
			else {
				timestamp = System.currentTimeMillis() - timestamp;
				if (timestamp < InvsManager.DAY) {
					p.sendMessage("§cTu as déjà fini une quête aujourd'hui ! Reviens dans " + TextUtils.secondsToTime((int) (InvsManager.DAY - timestamp) / 1000));
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
				} else  InvsManager.openQuestMenu(is, p);
			}
		}
	}
}
