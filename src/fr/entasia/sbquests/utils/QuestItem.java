package fr.entasia.sbquests.utils;

import org.bukkit.Material;

public class QuestItem {

	public Material type;
	public int meta;
	public int number;
	public String name;

	public QuestItem(Material type, int meta, int number, String name){
		this.type = type;
		this.meta = meta;
		this.number = number;
		this.name = name;
	}

	public QuestItem(Material type, int number, String name){
		this.type = type;
		this.meta = 0;
		this.number = number;
		this.name = name;
	}
}
