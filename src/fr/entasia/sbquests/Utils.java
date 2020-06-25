package fr.entasia.sbquests;

import fr.entasia.sbquests.utils.objs.Quests;
import fr.entasia.skycore.apis.BaseIsland;

import java.util.ArrayList;

public class Utils {


	public static Quests createQuest(BaseIsland is){
		ArrayList<Quests> okQuests = new ArrayList<>();

		long islevel = is.getLevel();


		// on choisi une quête valide selon le lvl
		for(Quests qs : Quests.values()){
			if(islevel>=qs.minlevel&&islevel<=qs.maxlevel) okQuests.add(qs);
		}
		Quests current = okQuests.get(Main.r.nextInt(okQuests.size()));


		// modification de la config
		Main.main.getConfig().set("quests." + is.isid.str() +".id", current.id);
		Main.main.getConfig().set("quests." + is.isid.str() + ".time", System.currentTimeMillis());

		for (int i=0;i<current.content.items.size();i++) {
			Main.main.getConfig().set("quests." + is.isid.str() + ".items." + i, 0);
		}

		for (int i=0;i<current.content.mobs.size();i++) {
			Main.main.getConfig().set("quests." + is.isid.str() + ".mobs." + i, 0);
		}
		return current;
	}

}
