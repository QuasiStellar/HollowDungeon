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

package com.quasistellar.hollowdungeon.journal;

import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.potions.PotionOfExperience;
import com.quasistellar.hollowdungeon.items.potions.PotionOfFrost;
import com.quasistellar.hollowdungeon.items.potions.PotionOfHaste;
import com.quasistellar.hollowdungeon.items.potions.PotionOfHealing;
import com.quasistellar.hollowdungeon.items.potions.PotionOfInvisibility;
import com.quasistellar.hollowdungeon.items.potions.PotionOfLevitation;
import com.quasistellar.hollowdungeon.items.potions.PotionOfLiquidFlame;
import com.quasistellar.hollowdungeon.items.potions.PotionOfMindVision;
import com.quasistellar.hollowdungeon.items.potions.PotionOfParalyticGas;
import com.quasistellar.hollowdungeon.items.potions.PotionOfPurity;
import com.quasistellar.hollowdungeon.items.potions.PotionOfStrength;
import com.quasistellar.hollowdungeon.items.potions.PotionOfToxicGas;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfIdentify;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfLullaby;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfMagicMapping;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfMirrorImage;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRage;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRecharging;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRemoveCurse;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRetribution;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTerror;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTransmutation;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public enum Catalog {

	POTIONS,
	SCROLLS;
	
	private LinkedHashMap<Class<? extends Item>, Boolean> seen = new LinkedHashMap<>();
	
	public Collection<Class<? extends Item>> items(){
		return seen.keySet();
	}
	
	public boolean allSeen(){
		for (Class<?extends Item> item : items()){
			if (!seen.get(item)){
				return false;
			}
		}
		return true;
	}
	
	static {
	
		POTIONS.seen.put( PotionOfHealing.class,            false);
		POTIONS.seen.put( PotionOfStrength.class,           false);
		POTIONS.seen.put( PotionOfLiquidFlame.class,        false);
		POTIONS.seen.put( PotionOfFrost.class,              false);
		POTIONS.seen.put( PotionOfToxicGas.class,           false);
		POTIONS.seen.put( PotionOfParalyticGas.class,       false);
		POTIONS.seen.put( PotionOfPurity.class,             false);
		POTIONS.seen.put( PotionOfLevitation.class,         false);
		POTIONS.seen.put( PotionOfMindVision.class,         false);
		POTIONS.seen.put( PotionOfInvisibility.class,       false);
		POTIONS.seen.put( PotionOfExperience.class,         false);
		POTIONS.seen.put( PotionOfHaste.class,              false);
	
		SCROLLS.seen.put( ScrollOfIdentify.class,           false);
		SCROLLS.seen.put( ScrollOfUpgrade.class,            false);
		SCROLLS.seen.put( ScrollOfRemoveCurse.class,        false);
		SCROLLS.seen.put( ScrollOfMagicMapping.class,       false);
		SCROLLS.seen.put( ScrollOfTeleportation.class,      false);
		SCROLLS.seen.put( ScrollOfRecharging.class,         false);
		SCROLLS.seen.put( ScrollOfMirrorImage.class,        false);
		SCROLLS.seen.put( ScrollOfTerror.class,             false);
		SCROLLS.seen.put( ScrollOfLullaby.class,            false);
		SCROLLS.seen.put( ScrollOfRage.class,               false);
		SCROLLS.seen.put( ScrollOfRetribution.class,        false);
		SCROLLS.seen.put( ScrollOfTransmutation.class,      false);
	}
	
	public static LinkedHashMap<Catalog, Badges.Badge> catalogBadges = new LinkedHashMap<>();
	static {
		catalogBadges.put(POTIONS, Badges.Badge.ALL_POTIONS_IDENTIFIED);
		catalogBadges.put(SCROLLS, Badges.Badge.ALL_SCROLLS_IDENTIFIED);
	}
	
	public static boolean isSeen(Class<? extends Item> itemClass){
		for (Catalog cat : values()) {
			if (cat.seen.containsKey(itemClass)) {
				return cat.seen.get(itemClass);
			}
		}
		return false;
	}
	
	public static void setSeen(Class<? extends Item> itemClass){
		for (Catalog cat : values()) {
			if (cat.seen.containsKey(itemClass) && !cat.seen.get(itemClass)) {
				cat.seen.put(itemClass, true);
				Journal.saveNeeded = true;
			}
		}
	}
	
	private static final String CATALOGS = "catalogs";
	
	public static void store( Bundle bundle ){
		
		Badges.loadGlobal();
		
		ArrayList<String> seen = new ArrayList<>();
		
		//if we have identified all items of a set, we use the badge to keep track instead.
		for (Catalog cat : values()) {
			if (!Badges.isUnlocked(catalogBadges.get(cat))) {
				for (Class<? extends Item> item : cat.items()) {
					if (cat.seen.get(item)) seen.add(item.getSimpleName());
				}
			}
		}

		bundle.put( CATALOGS, seen.toArray(new String[0]) );
		
	}
	
	public static void restore( Bundle bundle ){
		
		Badges.loadGlobal();
		
		//catalog-specific badge logic
		for (Catalog cat : values()){
			if (Badges.isUnlocked(catalogBadges.get(cat))){
				for (Class<? extends Item> item : cat.items()){
					cat.seen.put(item, true);
				}
			}
		}
		
		//general save/load
		if (bundle.contains(CATALOGS)) {
			List<String> seen = Arrays.asList(bundle.getStringArray(CATALOGS));
			
			//TODO should adjust this to tie into the bundling system's class array
			for (Catalog cat : values()) {
				for (Class<? extends Item> item : cat.items()) {
					if (seen.contains(item.getSimpleName())) {
						cat.seen.put(item, true);
					}
				}
			}
		}
	}
	
}
