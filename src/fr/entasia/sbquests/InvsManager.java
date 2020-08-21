package fr.entasia.sbquests;

import fr.entasia.apis.menus.MenuClickEvent;
import fr.entasia.apis.menus.MenuCreator;
import fr.entasia.apis.utils.ItemUtils;
import fr.entasia.sbquests.utils.QuestItem;
import fr.entasia.sbquests.utils.QuestMob;
import fr.entasia.sbquests.utils.Quests;
import fr.entasia.skycore.apis.BaseAPI;
import fr.entasia.skycore.apis.BaseIsland;
import fr.entasia.skycore.apis.SkyPlayer;
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

	private static MenuCreator questMenu = new MenuCreator(null, null) {
		@Override
		public void onMenuClick(MenuClickEvent e) {
			Player p = e.player;
			BaseIsland is = (BaseIsland) e.data;
			ConfigurationSection cs = Main.main.getConfig().getConfigurationSection("quests." + is.isid.str());
			if(cs==null){
				p.sendMessage("§cErreur lors du chargement de la quête !");
				return;
			}
			if (e.item.getType() == Material.CHEST) {
				Quests current_quest = Quests.getByID(cs.getInt("id"));
				if (current_quest == null){
					p.sendMessage("§cUne erreur est survenue ! (ID de quête invalide)");
					return;
				}
				int i = 0;
				for (QuestItem qitem : current_quest.content.items) {
					int needed = qitem.number - cs.getInt("items." + i);
					i++;
					if(needed==0)continue;

					for (ItemStack item : p.getInventory().all(qitem.type).values()) {
						if(item.getDurability()==qitem.meta){
							int amount = item.getAmount();
							if(amount>needed){
								item.setAmount(amount-needed);
								needed=0;
								break;
							}else{
								needed-=amount;
								item.setAmount(0);
								if(needed==0)break;
							}
						}
					}
					cs.set("items."+i, qitem.number-needed);
				}
				openQuestMenu(is, p);
			} else if (e.item.getType() == Material.STAINED_GLASS_PANE) {
				long timestamp = cs.getLong("time");
				if(timestamp==0){
					p.sendMessage("§cUn membre de ton équipe à déja récupéré la récompense !");
					return;
				}
				Quests current_quest = Quests.getByID(cs.getInt("id"));
				if (current_quest == null){
					p.sendMessage("§cUne erreur est survenue ! (ID de quête invalide)");
					return;
				}

				int i = 0;
				for (QuestItem qitem : current_quest.content.items) {
					if(cs.getInt("items."+i)!=qitem.number)return;
					i++;
				}
				i = 0;
				for (QuestMob qmob : current_quest.content.mobs) {
					if(cs.getInt("mobs."+i)!=qmob.number)return;
					i++;
				}

				System.out.println(timestamp);
				Main.main.getConfig().set("quests." + is.isid.str(), timestamp);

				p.closeInventory();
				StringBuilder sb = new StringBuilder();
				sb.append("§6Tu as complété ta quête journalière, tu as gagné :");
				boolean drop = false;
				if (current_quest.reward.items.size() > 0) {
					ItemStack result;
					for (Map.Entry<ItemStack, String> item: current_quest.reward.items.entrySet()) {
						result = item.getKey();
						sb.append("\n§6- §c").append(result.getAmount()).append(" §6").append(item.getValue());

						if (!ItemUtils.giveOrDrop(p, result))drop = true;
					}
				}
				p.sendMessage(sb.toString());
				if(drop)p.sendMessage("§cTon inventaire n'avait assez de place, une partie de la récompense à été drop");
				p.sendMessage("");
				if (current_quest.reward.exp > 0) {
					p.sendMessage("§6- §c"+current_quest.reward.exp+" §6Points d'expérience");
					p.giveExp(current_quest.reward.exp);
				}
				if (current_quest.reward.money > 0) {
					p.sendMessage("§6- §c"+current_quest.reward.money+"§6$");
					SkyPlayer sp = BaseAPI.getOnlineSP(p.getUniqueId());
					if(sp==null)p.sendMessage("§cErreur lors de l'ajout de la monnaie ! Contacte un membre du Staff");
					else sp.addMoney(current_quest.reward.money);
				}
				p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
			}
		}
	};

	protected static void openQuestMenu(BaseIsland is, Player p) {

		// CODE DE CHECK DE QUETE

		Quests current;

		String id = is.isid.str();
		ConfigurationSection cs = Main.main.getConfig().getConfigurationSection("quests." +id);
		if(cs == null) {
			current = Utils.createQuest(is);
			cs = Main.main.getConfig().getConfigurationSection("quests." + id);
		}else if (cs.getLong("time") < System.currentTimeMillis() - DAY) {
			current = Utils.createQuest(is);
		}else{
			current = Quests.getByID(cs.getInt("id"));
			if (current == null){
				p.sendMessage("§cUne erreur est survenue ! (ID de quête invalide)");
				return;
			}
		}
		if (current == null){
			p.sendMessage("§cErreur : aucune quête n'est disponible ! Contacte un membre du Staff ! (level "+is.getLevel()+")");
			return;
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

	public static final int DAY = 24 * 60 * 60 * 1000;

}
