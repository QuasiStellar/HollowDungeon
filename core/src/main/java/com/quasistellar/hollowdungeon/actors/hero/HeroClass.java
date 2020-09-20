/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.quasistellar.hollowdungeon.actors.hero;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.bags.VelvetPouch;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;

public enum HeroClass {

	KNIGHT( "knight" ),
	HORNET( "hornet" );

	private String title;

	HeroClass( String title ) {
		this.title = title;
	}

	public void initHero( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {

		hero.heroClass = this;

		initCommon( hero );

		switch (this) {
			case KNIGHT:
				initKnight( hero );
				break;

			case HORNET:
				initHornet( hero );
				break;
		}

	}

	private static void initCommon( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		new VelvetPouch().collect();
	}

	public com.quasistellar.hollowdungeon.Badges.Badge masteryBadge() {
		switch (this) {
			case KNIGHT:
				return Badges.Badge.MASTERY_KNIGHT;
			case HORNET:
				return Badges.Badge.MASTERY_HORNET;
		}
		return null;
	}

	private static void initKnight(Hero hero ) {

	}

	private static void initHornet(Hero hero ) {

	}

	public String title() {
		return Messages.get(HeroClass.class, title);
	}

	public String spritesheet() {
		switch (this) {
			case KNIGHT: default:
				return Assets.Sprites.KNIGHT;
			case HORNET:
				return Assets.Sprites.HORNET;
		}
	}

	public String splashArt(){
		switch (this) {
			case KNIGHT: default:
				return Assets.Splashes.KNIGHT;
			case HORNET:
				return Assets.Splashes.HORNET;
		}
	}
	
	public String[] perks() {
		switch (this) {
			case KNIGHT: default:
				return new String[]{
						Messages.get(HeroClass.class, "warrior_perk1"),
						Messages.get(HeroClass.class, "warrior_perk2"),
						Messages.get(HeroClass.class, "warrior_perk3"),
						Messages.get(HeroClass.class, "warrior_perk4"),
						Messages.get(HeroClass.class, "warrior_perk5"),
				};
			case HORNET:
				return new String[]{
						Messages.get(HeroClass.class, "huntress_perk1"),
						Messages.get(HeroClass.class, "huntress_perk2"),
						Messages.get(HeroClass.class, "huntress_perk3"),
						Messages.get(HeroClass.class, "huntress_perk4"),
						Messages.get(HeroClass.class, "huntress_perk5"),
				};
		}
	}
	
	public boolean isUnlocked(){
		//always unlock on debug builds
		if (DeviceCompat.isDebug()) return true;
		
		switch (this){
			case KNIGHT: default:
				return true;
			case HORNET:
				return true;
		}
	}
	
	public String unlockMsg() {
		switch (this){
			case KNIGHT: default:
				return "";
			case HORNET:
				return Messages.get(HeroClass.class, "huntress_unlock");
		}
	}

	private static final String CLASS	= "class";
	
	public void storeInBundle( Bundle bundle ) {
		bundle.put( CLASS, toString() );
	}
	
	public static HeroClass restoreInBundle( Bundle bundle ) {
		String value = bundle.getString( CLASS );
		return value.length() > 0 ? valueOf( value ) : KNIGHT;
	}
}
