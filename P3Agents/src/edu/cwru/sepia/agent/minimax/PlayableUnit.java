package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.environment.model.state.*;


/**
 * This class is used to represent any playable unit in game such as footman, archer etc.
 * It has all the required fields to store necessary attributes of the playable units such as hp,range,damage etc
 * All the fields have been marked private and respective getter and setter methods are generated based on usage.
 */
public class PlayableUnit {
    private int id;
	private int x, y;
	private int hp;
	private int damage;
	private int range;

	//Constructor that initializes a playable unit from its unitView Object.
	public PlayableUnit(Unit.UnitView unit_view) {
		
		UnitTemplate.UnitTemplateView unit_template_view = unit_view.getTemplateView();
        id = unit_view.getID();
		x = unit_view.getXPosition();
		y = unit_view.getYPosition();
		hp = unit_view.getHP();
		damage = unit_template_view.getBasicAttack();
		range = unit_template_view.getRange();
	}

	//Used to initialize a unit from other playable unit by copying its attributes.
	public PlayableUnit(PlayableUnit otherPlayableUnit){
        this.id = otherPlayableUnit.id;
		this.x = otherPlayableUnit.x;
		this.y = otherPlayableUnit.y;
		this.hp = otherPlayableUnit.hp;
		this.damage = otherPlayableUnit.damage;
		this.range = otherPlayableUnit.range;
	}
	
	public int getHp() {
		return hp;
	}


	public int getId() {
		return id;
	}


	public void setHp(int hp) {
		this.hp = hp;
	}


	public int getX() {
		return x;
	}


	public int getY() {
		return y;
	}
	
	public void setXY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getDamage() {
		return damage;
	}

	
	public int getRange() {
		return range;
	}




}
