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

package com.quasistellar.hollowdungeon.items.rings;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.Flare;
import com.quasistellar.hollowdungeon.items.Gold;
import com.quasistellar.hollowdungeon.items.Honeypot;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.potions.AlchemicalCatalyst;
import com.quasistellar.hollowdungeon.items.potions.PotionOfExperience;
import com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTransmutation;
import com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll;
import com.quasistellar.hollowdungeon.items.spells.ArcaneCatalyst;
import com.quasistellar.hollowdungeon.items.stones.StoneOfEnchantment;
import com.quasistellar.hollowdungeon.items.weapon.Weapon;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Challenges;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.bombs.Bomb;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

public class RingOfWealth extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_WEALTH;
	}

	private float triesToDrop = Float.MIN_VALUE;
	private int dropsToRare = Integer.MIN_VALUE;
	
	public String statsInfo() {
		if (isIdentified()){
			return Messages.get(this, "stats", new DecimalFormat("#.##").format(100f * (Math.pow(1.25f, soloBuffedBonus()) - 1f)));
		} else {
			return Messages.get(this, "typical_stats", new DecimalFormat("#.##").format(25f));
		}
	}

	private static final String TRIES_TO_DROP = "tries_to_drop";
	private static final String DROPS_TO_RARE = "drops_to_rare";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TRIES_TO_DROP, triesToDrop);
		bundle.put(DROPS_TO_RARE, dropsToRare);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		triesToDrop = bundle.getFloat(TRIES_TO_DROP);
		dropsToRare = bundle.getInt(DROPS_TO_RARE);
	}

	@Override
	protected RingBuff buff( ) {
		return new Wealth();
	}
	
	public static float dropChanceMultiplier( com.quasistellar.hollowdungeon.actors.Char target ){
		return (float)Math.pow(1.25, getBuffedBonus(target, Wealth.class));
	}
	
	public static ArrayList<com.quasistellar.hollowdungeon.items.Item> tryForBonusDrop(Char target, int tries ){
		int bonus = getBuffedBonus(target, Wealth.class);

		if (bonus <= 0) return null;
		
		HashSet<Wealth> buffs = target.buffs(Wealth.class);
		float triesToDrop = Float.MIN_VALUE;
		int dropsToEquip = Integer.MIN_VALUE;
		
		//find the largest count (if they aren't synced yet)
		for (Wealth w : buffs){
			if (w.triesToDrop() > triesToDrop){
				triesToDrop = w.triesToDrop();
				dropsToEquip = w.dropsToRare();
			}
		}

		//reset (if needed), decrement, and store counts
		if (triesToDrop == Float.MIN_VALUE) {
			triesToDrop = Random.NormalIntRange(0, 30);
			dropsToEquip = Random.NormalIntRange(5, 10);
		}

		//now handle reward logic
		ArrayList<com.quasistellar.hollowdungeon.items.Item> drops = new ArrayList<>();

		triesToDrop -= tries;
		while ( triesToDrop <= 0 ){
			if (dropsToEquip <= 0){
				com.quasistellar.hollowdungeon.items.Item i;
				do {
					i = genEquipmentDrop(bonus - 1);
				} while (Challenges.isItemBlocked(i));
				drops.add(i);
				dropsToEquip = Random.NormalIntRange(5, 10);
			} else {
				com.quasistellar.hollowdungeon.items.Item i;
				do {
					i = genConsumableDrop(bonus - 1);
				} while (com.quasistellar.hollowdungeon.Challenges.isItemBlocked(i));
				drops.add(i);
				dropsToEquip--;
			}
			triesToDrop += Random.NormalIntRange(0, 30);
		}

		//store values back into rings
		for (Wealth w : buffs){
			w.triesToDrop(triesToDrop);
			w.dropsToRare(dropsToEquip);
		}
		
		return drops;
	}

	//used for visuals
	// 1/2/3 used for low/mid/high tier consumables
	// 3 used for +0-1 equips, 4 used for +2 or higher equips
	private static int latestDropTier = 0;

	public static void showFlareForBonusDrop( Visual vis ){
		switch (latestDropTier){
			default:
				break; //do nothing
			case 1:
				new com.quasistellar.hollowdungeon.effects.Flare(6, 20).color(0x00FF00, true).show(vis, 3f);
				break;
			case 2:
				new com.quasistellar.hollowdungeon.effects.Flare(6, 24).color(0x00AAFF, true).show(vis, 3.33f);
				break;
			case 3:
				new com.quasistellar.hollowdungeon.effects.Flare(6, 28).color(0xAA00FF, true).show(vis, 3.67f);
				break;
			case 4:
				new Flare(6, 32).color(0xFFAA00, true).show(vis, 4f);
				break;
		}
		latestDropTier = 0;
	}
	
	public static com.quasistellar.hollowdungeon.items.Item genConsumableDrop(int level) {
		float roll = Random.Float();
		//60% chance - 4% per level. Starting from +15: 0%
		if (roll < (0.6f - 0.04f * level)) {
			latestDropTier = 1;
			return genLowValueConsumable();
		//30% chance + 2% per level. Starting from +15: 60%-2%*(lvl-15)
		} else if (roll < (0.9f - 0.02f * level)) {
			latestDropTier = 2;
			return genMidValueConsumable();
		//10% chance + 2% per level. Starting from +15: 40%+2%*(lvl-15)
		} else {
			latestDropTier = 3;
			return genHighValueConsumable();
		}
	}

	private static com.quasistellar.hollowdungeon.items.Item genLowValueConsumable(){
		switch (Random.Int(4)){
			case 0: default:
				com.quasistellar.hollowdungeon.items.Item i = new Gold().random();
				return i.quantity(i.quantity()/2);
			case 1:
				return Generator.random(Generator.Category.STONE);
			case 2:
				return Generator.random(Generator.Category.POTION);
			case 3:
				return Generator.random(Generator.Category.SCROLL);
		}
	}

	private static com.quasistellar.hollowdungeon.items.Item genMidValueConsumable(){
		switch (Random.Int(6)){
			case 0: default:
				com.quasistellar.hollowdungeon.items.Item i = genLowValueConsumable();
				return i.quantity(i.quantity()*2);
			case 1:
				i = Generator.randomUsingDefaults(Generator.Category.POTION);
				return Reflection.newInstance(ExoticPotion.regToExo.get(i.getClass()));
			case 2:
				i = Generator.randomUsingDefaults(Generator.Category.SCROLL);
				return Reflection.newInstance(ExoticScroll.regToExo.get(i.getClass()));
			case 3:
				return Random.Int(2) == 0 ? new ArcaneCatalyst() : new AlchemicalCatalyst();
			case 4:
				return new Bomb();
			case 5:
				return new Honeypot();
		}
	}

	private static com.quasistellar.hollowdungeon.items.Item genHighValueConsumable(){
		switch (Random.Int(4)){
			case 0: default:
				com.quasistellar.hollowdungeon.items.Item i = genMidValueConsumable();
				if (i instanceof Bomb){
					return new Bomb.DoubleBomb();
				} else {
					return i.quantity(i.quantity()*2);
				}
			case 1:
				return new StoneOfEnchantment();
			case 2:
				return new PotionOfExperience();
			case 3:
				return new ScrollOfTransmutation();
		}
	}

	private static com.quasistellar.hollowdungeon.items.Item genEquipmentDrop(int level ){
		Item result;
		//each upgrade increases depth used for calculating drops by 1
		int floorset = (Dungeon.depth + level)/5;
		switch (Random.Int(5)){
			default: case 0: case 1: case 2:
				Weapon w = Generator.randomWeapon(floorset);
				if (!w.hasGoodEnchant() && Random.Int(10) < level)      w.enchant();
				else if (w.hasCurseEnchant())                           w.enchant(null);
				result = w;
				break;
			case 3:
				result = Generator.random(Generator.Category.RING);
				break;
			case 4:
				result = Generator.random(com.quasistellar.hollowdungeon.items.Generator.Category.ARTIFACT);
				break;
		}
		//minimum level of sqrt(ringLvl)
		if (result.isUpgradable()){
			if (result.level() < Math.floor(Math.sqrt(level))){
				result.level((int)Math.floor(Math.sqrt(level)));
			}
		}
		result.cursed = false;
		result.cursedKnown = true;
		if (result.level() >= 2) {
			latestDropTier = 4;
		} else {
			latestDropTier = 3;
		}
		return result;
	}

	public class Wealth extends RingBuff {
		
		private void triesToDrop( float val ){
			triesToDrop = val;
		}
		
		private float triesToDrop(){
			return triesToDrop;
		}

		private void dropsToRare( int val ) {
			dropsToRare = val;
		}

		private int dropsToRare(){
			return dropsToRare;
		}
		
	}
}
