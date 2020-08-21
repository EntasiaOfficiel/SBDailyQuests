package fr.entasia.sbquests.utils;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public enum Quests {
	A(1, 0, 200,
			new QuestContent()
					.addItem(new QuestItem(Material.PAPER, 0, 64, "Papier"))
					.addItem(new QuestItem(Material.SUGAR_CANE, 0, 64, "Cannes à Sucre"))
					.addMob(new QuestMob(EntityType.ZOMBIE, 5, "Zombies")),
			new QuestReward(100, 2000)
					.addItem(new ItemStack(Material.DIAMOND, 10), "Diamants")
					.addItem(new ItemStack(Material.GOLD_INGOT, 20), "Lingots d'Or")),

	B(2, 100, 200,
			new QuestContent()
					.addItem(new QuestItem(Material.PAPER, 0, 234, "Papier"))
					.addItem(new QuestItem(Material.SUGAR_CANE, 0, 66, "Cannes à Sucre"))
					.addMob(new QuestMob(EntityType.COW, 35, "Vaches"))
					.addMob(new QuestMob(EntityType.ZOMBIE, 10, "Zombies")),
			new QuestReward(1000, 20000)
					.addItem(new ItemStack(Material.DIAMOND, 23), "Diamants")
					.addItem(new ItemStack(Material.GOLD_INGOT, 42), "Lingots d'Or")),


	;

	public final int id;
	public final int minlevel;
	public final int maxlevel;
	public final QuestContent content;
	public final QuestReward reward;

	Quests(int id, int minlevel, int maxlevel, QuestContent content, QuestReward reward){
		this.id = id;
		this.minlevel = minlevel;
		this.maxlevel = maxlevel;
		this.content = content;
		this.reward = reward;
	}

	public static Quests getByID(int id){
		for(Quests qs : values()){
			if(qs.id==id)return qs;
		}
		return null;
	}

	public static class QuestContent {

		public final ArrayList<QuestItem> items = new ArrayList<>();
		public final ArrayList<QuestMob> mobs = new ArrayList<>();

		public QuestContent addMob(QuestMob mob){
			mobs.add(mob);
			return this;
		}

		public QuestContent addItem(QuestItem item){
			items.add(item);
			return this;
		}
	}

	public static class QuestReward {
		public int money;
		public int exp;
		public HashMap<ItemStack, String> items = new HashMap<ItemStack, String>();

		public QuestReward addItem(ItemStack item, String name) {
			items.put(item, name);
			return this;
		}

		QuestReward(int money, int exp) {
			this.money = money;
			this.exp = exp;
		}
	}
}
