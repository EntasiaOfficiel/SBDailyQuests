package fr.entasia.sbquests.utils;

import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.sbquests.Main;
import fr.entasia.sbquests.Utils;
import fr.entasia.sbquests.utils.objs.QuestItem;
import fr.entasia.sbquests.utils.objs.QuestMob;
import fr.entasia.sbquests.utils.objs.Quests;
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

public class InvsManager {

	public static MenuCreator questMenu = new MenuCreator(null, null) {
		@Override
		public void onMenuClick(MenuClickEvent e) {
			Player p = e.player;
			BaseIsland is = (BaseIsland) e.data;
			ConfigurationSection cs = Main.main.getConfig().getConfigurationSection("quests." + is.isid.str());
			if (e.slot == 0) {
				if (cs != null) { // si il a une quête
					Quests current_quest = Quests.getByID(cs.getInt("id"));
					if (current_quest == null){
						p.sendMessage("§cUne erreur est survenue ! (ID de quête invalide)");
						return;
					}
					int qitem_number = 0;
					for (QuestItem qitem : current_quest.content.items) {
						int iterator = 0;
						int max = (qitem.number - cs.getInt(".items." + qitem_number));

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
					int actual_number = cs.getInt("items." + qitem_number);
					cs.set("items." + qitem_number, actual_number + iterator);
					qitem_number++;
					openQuestMenu(p);
					}
				}
			} else if (e.slot == 4) {
				Quests current_quest = Quests.getByID(cs.getInt("id"));
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
				long timestamp = Main.main.getConfig().getLong("quests." + is.isid.str() + ".time");
				Main.main.getConfig().set("quests." + is.isid.str(), timestamp);
			}
		}
	};

	public static void openQuestMenu(Player p) {

		// CODE DE CHECK DE QUETE

		Quests current;
		BaseIsland is = BaseAPI.getIsland(CooManager.getIslandID(p.getLocation()));
		if(is==null){
			p.sendMessage("§cUne erreur est survenue ! (No island)");
			return;
		}else if(is.getMember(p.getUniqueId())==null){
			p.sendMessage("§cTu n'es pas membre de cette île !");
		}

		String id = is.isid.str();
		ConfigurationSection cs = Main.main.getConfig().getConfigurationSection("quests." +id);
		if(cs == null) {
			current = Utils.createQuest(p, is, id);
			cs = Main.main.getConfig().getConfigurationSection("quests." + id);
		}else if (cs.getLong("time") < (System.currentTimeMillis() - (24 * 60 * 60 * 1000))) {
			current = Utils.createQuest(p, is, id);
		}else{
			current = Quests.getByID(cs.getInt("id"));
			if (current == null){
				p.sendMessage("§cUne erreur est survenue ! (ID de quête invalide)");
				return;
			}
		}

		// Ouverture menu
		Inventory inv = questMenu.createInv(3, "§cQuête Journalière", is);

		boolean finished = true;

		ItemStack item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§6Quête du jour");
		ArrayList<String> lore = new ArrayList<>();
		lore.add("§6Voici les taches à effectuer:");
		int iterator = 0;

		ConfigurationSection tcs = cs.getConfigurationSection("items");
		String remaining;
		for (QuestItem qitem : current.content.items) {
			if ((qitem.number - tcs.getInt("" + iterator)) == 0) remaining = "(Complété ✔)";
			else{
				finished = false;
				remaining = "(" + (qitem.number - tcs.getInt("" + iterator)) + " Restant)";
			}
			lore.add("§8- Collecter " + qitem.number + " " + qitem.name + " " + remaining);
			iterator++;
		}
		iterator = 0;
		tcs = cs.getConfigurationSection("mobs");
		for (QuestMob qmob: current.content.mobs) {
			if ((qmob.number - tcs.getInt("" + iterator)) == 0) remaining = "(Complété ✔)";
			else{
				finished = false;
				remaining = "(" + (qmob.number - tcs.getInt("" + iterator)) + " Restant)";
			}
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
