package fr.entasia.sbquests;

import fr.entasia.errors.EntasiaException;
import fr.entasia.sbquests.utils.objs.Quests;
import fr.entasia.skycore.apis.BaseIsland;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Utils {


	public static Quests createQuest(BaseIsland is, String id){ // pass by reference :)
		ArrayList<Quests> okQuests = new ArrayList<>();

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

}
