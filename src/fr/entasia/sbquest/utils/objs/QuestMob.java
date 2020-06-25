package fr.entasia.sbquest.utils.objs;

import org.bukkit.entity.EntityType;

public class QuestMob {

	public EntityType type;
	public int number;
	public String name;

	public QuestMob(EntityType type, int number, String name){
		this.type = type;
		this.number = number;
		this.name = name;
	}
}
