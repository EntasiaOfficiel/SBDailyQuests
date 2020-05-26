package fr.entasia.sbquest.utils;

import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.sbquest.Main;
import fr.entasia.sbquest.utils.quests.QuestItem;
import fr.entasia.sbquest.utils.quests.QuestMob;
import fr.entasia.sbquest.utils.quests.QuestStruct;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.CooManager;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

public class MenuManager {

	public static MenuCreator questMenu = new MenuCreator(null, null) {
		@Override
		public void onMenuClick(MenuClickEvent e) {
			Player p = e.player;
			BaseIsland is = (BaseIsland) e.data;
			if (e.slot == 0) {
				if (Main.main.getConfig().getConfigurationSection("quests." + is.isid.str()) != null) { // si il a une quête
					QuestStruct current_quest = QuestStruct.getByID(Main.main.getConfig().getInt("quests." + is.isid.str() + ".id"));
					if (current_quest == null){
						p.sendMessage("§cUne erreur est survenue ! (ID de quête invalide)");
						return;
					}
					int qitem_number = 0;
					for (QuestItem qitem : current_quest.content.items) {
						int iterator = 0;
						int max = (qitem.number - Main.main.getConfig().getInt("quests." + is.isid.str() + ".items." + qitem_number));

						for (ItemStack item : p.getInventory().all(qitem.type).values()) {
							if(item.getDurability()==qitem.meta){
								int amount = item.getAmount();
								if(item.getAmount()>max-iterator){
									iterator+=max-iterator;
									item.setAmount(amount-max);
									break;
								}else{
									iterator+=amount;
									item.setAmount(0);
								}
							}
						}
					int actual_number = Main.main.getConfig().getInt("quests." + is.isid.str() + ".items." + qitem_number);
					Main.main.getConfig().set("quests." + is.isid.str() + ".items." + qitem_number, actual_number + iterator);
					qitem_number++;
					Main.main.saveConfig();
					openQuestMenu(p);
					}
				}
			} else if (e.slot == 4) {
				QuestStruct current_quest = QuestStruct.getByID(Main.main.getConfig().getInt("quests." + is.isid.str() + ".id"));
				if (current_quest == null){
					p.sendMessage("§cUne erreur est survenue ! (ID de quête invalide)");
					return;
				}
				p.closeInventory();
				StringBuilder sb = new StringBuilder();
				sb.append("§6Vous avez complété votre quête journalière, vous avez gagné:\n");
				if (current_quest.reward.items.size() > 0) {
					for (Map.Entry<ItemStack, String> item: current_quest.reward.items.entrySet()) {
						ItemStack itemStack = item.getKey();
						sb.append("§6- §c").append(itemStack.getAmount()).append(" §6").append(item.getValue()).append("\n");

						if (e.player.getInventory().firstEmpty() == -1) {
							int possible = 0;
							for (Map.Entry<Integer, ? extends ItemStack> slot : e.player.getInventory().all(itemStack.getType()).entrySet()) {
								if (slot.getValue().getDurability() == itemStack.getDurability()) { // ca passera pas les enchants etc...
									possible += (64 - slot.getValue().getAmount());
									if (possible >= itemStack.getAmount()) break;
								}
							}
							if (possible < itemStack.getAmount()) {
								e.player.sendMessage("§cPas assez de slots libres pour recevoir votre récompense !");
								return;
							}
						}
						p.getInventory().addItem(itemStack);
					}
				}
				if (current_quest.reward.exp > 0) {
					sb.append("§6- §c").append(current_quest.reward.exp).append(" §6Points d'expérience").append("\n");
					p.giveExp(current_quest.reward.exp);
				}
				if (current_quest.reward.money > 0) {
					sb.append("§6- §c").append(current_quest.reward.money).append("§6$").append("\n");
					// SkyPlayer sp = new SkyPlayer(p);
					// sp.addMoney(current_quest.reward.money);
				}
				p.sendMessage(sb.toString());
				p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
				Long timestamp = Main.main.getConfig().getLong("quests." + is.isid.str() + ".time");
				Main.main.getConfig().set("quests." + is.isid.str(), timestamp);
				Main.main.saveConfig();
			}
		}
	};

	public static void openQuestMenu(Player p) {

		// CODE DE CHECK DE QUETE

		boolean finished = false;
		QuestStruct current_quest;
		BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(p.getLocation()));
		if(is==null){
			p.sendMessage("§cUne erreur est survenue ! (No island)");
			return;
		}

		if (Main.main.getConfig().getConfigurationSection("quests."+p.getName()) == null) {
			// Pas de quête
			ArrayList<QuestStruct> a = new ArrayList<>();

			long islevel = is.getLevel();


			// on choisi une quête valide selon le lvl
			for(QuestStruct qs : QuestStruct.values()){
				if(islevel>=qs.minlevel&&islevel<=qs.maxlevel) a.add(qs);
			}
			current_quest = a.get((int) (Math.random() * a.size()));


			// modification de la config
			Main.main.getConfig().set("quests." + is.isid.str() +".id", current_quest.id);
			Main.main.getConfig().set("quests." + is.isid.str() + ".time", System.currentTimeMillis());

			for (int i=0;i<current_quest.content.items.size();i++) {
				Main.main.getConfig().set("quests." + is.isid.str() + ".items." + i, 0);
			}

			for (int i=0;i<current_quest.content.mobs.size();i++) {
				Main.main.getConfig().set("quests." + is.isid.str() + ".mobs." + i, 0);
			}
			Main.main.saveConfig();

		} else {
			if (Main.main.getConfig().getLong("quests." + is.isid.str() + ".time") < (System.currentTimeMillis() - (24 * 60 * 60 * 1000))) {
				Main.main.getConfig().set("quests." + is.isid.str(), null);
				Main.main.saveConfig();
				openQuestMenu(p);
				return;
			}
			current_quest = QuestStruct.getByID(Main.main.getConfig().getInt("quests."+p.getName()+".id"));
			if (current_quest == null){
				p.sendMessage("§cUne erreur est survenue ! (ID de quête invalide)");
				return;
			}
			int iterator = 0;
			int task_number = 0;
			int task_completed = 0;
			for (QuestItem qitem: current_quest.content.items) {
				if (qitem.number == Main.main.getConfig().getInt("quests." + is.isid.str() + ".items." + iterator)) task_completed++;
				task_number++;
				iterator++;
			}
			iterator = 0;
			for (QuestMob qmob: current_quest.content.mobs) {
				if (qmob.number == Main.main.getConfig().getInt("quests." + is.isid.str() + ".mobs." + iterator)) task_completed++;
				task_number++;
				iterator++;
			}

			finished = task_number == task_completed;
		}

		// Ouverture menu
		Inventory inv = questMenu.createInv(3, "§cQuête Journalière", is);

		ItemStack item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§6Quête du jour");
		ArrayList<String> lore = new ArrayList<>();
		lore.add("§6Voici les taches à effectuer:");
		ConfigurationSection cs = Main.main.getConfig().getConfigurationSection("quests." + is.isid.str() + ".");
		int iterator = 0;

		ConfigurationSection tcs = cs.getConfigurationSection("items");
		for (QuestItem qitem : current_quest.content.items) {
			String remaining;
			if ((qitem.number - tcs.getInt("" + iterator)) == 0) remaining = "(Complété ✔)";
			else remaining = "(" + (qitem.number - tcs.getInt("" + iterator)) + " Restant)";
			lore.add("§8- Collecter " + qitem.number + " " + qitem.name + " " + remaining);
			iterator++;
		}
		iterator = 0;
		tcs = cs.getConfigurationSection("mobs");
		for (QuestMob qmob: current_quest.content.mobs) {
			String remaining;
			if ((qmob.number - tcs.getInt("" + iterator)) == 0) remaining = "(Complété ✔)";
			else remaining = "(" + (qmob.number - tcs.getInt("" + iterator)) + " Restant)";
			lore.add("§8- Tuer " + qmob.number + " " + qmob.name + " " + remaining);
			iterator++;
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(13, item);

		item = new ItemStack(Material.CHEST);
		meta = item.getItemMeta();
		meta.setDisplayName("§2Vérifier mon inventaire");
		item.setItemMeta(meta);
		inv.setItem(0, item);

		lore = new ArrayList<>();
		if (finished) {
			item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
			lore.add("§aVous pouvez récupérer votre récompense");
		} else {
			item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
			lore.add("§cVous devez finir la quête avant tout !");
		}

		meta = item.getItemMeta();
		meta.setDisplayName("§3Récupérez votre récompense");
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(22, item);

		p.openInventory(inv);
		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.5f, 1.1f);
	}

}
