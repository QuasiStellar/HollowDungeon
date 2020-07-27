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

package com.quasistellar.hollowdungeon.items.scrolls;

import com.quasistellar.hollowdungeon.items.EquipableItem;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.artifacts.Artifact;
import com.quasistellar.hollowdungeon.items.potions.brews.Brew;
import com.quasistellar.hollowdungeon.items.potions.elixirs.Elixir;
import com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon;
import com.quasistellar.hollowdungeon.items.weapon.missiles.MissileWeapon;
import com.quasistellar.hollowdungeon.items.weapon.missiles.darts.Dart;
import com.quasistellar.hollowdungeon.journal.Catalog;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.Challenges;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion;
import com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.items.potions.AlchemicalCatalyst;
import com.quasistellar.hollowdungeon.items.potions.Potion;
import com.quasistellar.hollowdungeon.items.rings.Ring;
import com.quasistellar.hollowdungeon.items.stones.Runestone;
import com.quasistellar.hollowdungeon.items.wands.Wand;
import com.quasistellar.hollowdungeon.items.weapon.Weapon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class ScrollOfTransmutation extends InventoryScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_TRANSMUTE;
		mode = WndBag.Mode.TRANMSUTABLE;
		
		bones = true;
	}
	
	public static boolean canTransmute(com.quasistellar.hollowdungeon.items.Item item){
		return item instanceof com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon ||
				(item instanceof com.quasistellar.hollowdungeon.items.weapon.missiles.MissileWeapon && !(item instanceof Dart)) ||
				(item instanceof Potion && !(item instanceof Elixir || item instanceof Brew || item instanceof AlchemicalCatalyst)) ||
				item instanceof com.quasistellar.hollowdungeon.items.scrolls.Scroll ||
				item instanceof Ring ||
				item instanceof Wand ||
				item instanceof com.quasistellar.hollowdungeon.plants.Plant.Seed ||
				item instanceof Runestone ||
				item instanceof com.quasistellar.hollowdungeon.items.artifacts.Artifact;
	}
	
	@Override
	protected void onItemSelected(com.quasistellar.hollowdungeon.items.Item item) {
		
		com.quasistellar.hollowdungeon.items.Item result;
		
		if (item instanceof com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff) {
			result = changeStaff( (com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff)item );
		} else if (item instanceof com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon || item instanceof com.quasistellar.hollowdungeon.items.weapon.missiles.MissileWeapon) {
			result = changeWeapon( (Weapon)item );
		} else if (item instanceof com.quasistellar.hollowdungeon.items.scrolls.Scroll) {
			result = changeScroll( (com.quasistellar.hollowdungeon.items.scrolls.Scroll)item );
		} else if (item instanceof Potion) {
			result = changePotion( (Potion)item );
		} else if (item instanceof Ring) {
			result = changeRing( (Ring)item );
		} else if (item instanceof Wand) {
			result = changeWand( (Wand)item );
		} else if (item instanceof com.quasistellar.hollowdungeon.plants.Plant.Seed) {
			result = changeSeed((com.quasistellar.hollowdungeon.plants.Plant.Seed) item);
		} else if (item instanceof Runestone) {
			result = changeStone((Runestone) item);
		} else if (item instanceof com.quasistellar.hollowdungeon.items.artifacts.Artifact) {
			result = changeArtifact( (com.quasistellar.hollowdungeon.items.artifacts.Artifact)item );
		} else {
			result = null;
		}
		
		if (result == null){
			//This shouldn't ever trigger
			GLog.n( Messages.get(this, "nothing") );
			com.quasistellar.hollowdungeon.items.Item.curItem.collect( com.quasistellar.hollowdungeon.items.Item.curUser.belongings.backpack );
		} else {
			if (item.isEquipped(Dungeon.hero)){
				item.cursed = false; //to allow it to be unequipped
				((com.quasistellar.hollowdungeon.items.EquipableItem)item).doUnequip(Dungeon.hero, false);
				((EquipableItem)result).doEquip(Dungeon.hero);
			} else {
				item.detach(Dungeon.hero.belongings.backpack);
				if (!result.collect()){
					com.quasistellar.hollowdungeon.Dungeon.level.drop(result, Item.curUser.pos).sprite.drop();
				}
			}
			if (result.isIdentified()){
				Catalog.setSeen(result.getClass());
			}
			//TODO visuals
			com.quasistellar.hollowdungeon.utils.GLog.p( Messages.get(this, "morph") );
		}
		
	}
	
	private com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff changeStaff(MagesStaff staff ){
		Class<?extends Wand> wandClass = staff.wandClass();
		
		if (wandClass == null){
			return null;
		} else {
			Wand n;
			do {
				n = (Wand) Generator.random(Generator.Category.WAND);
			} while (Challenges.isItemBlocked(n) || n.getClass() == wandClass);
			n.level(0);
			n.identify();
			staff.imbueWand(n, null);
		}
		
		return staff;
	}
	
	private Weapon changeWeapon( Weapon w ) {
		
		Weapon n;
		com.quasistellar.hollowdungeon.items.Generator.Category c;
		if (w instanceof com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon) {
			c = Generator.wepTiers[((MeleeWeapon)w).tier - 1];
		} else {
			c = Generator.misTiers[((MissileWeapon)w).tier - 1];
		}
		
		do {
			n = (Weapon) Reflection.newInstance(c.classes[Random.chances(c.probs)]);
		} while (Challenges.isItemBlocked(n) || n.getClass() == w.getClass());
		
		int level = w.level();
		if (w.curseInfusionBonus) level--;
		if (level > 0) {
			n.upgrade( level );
		} else if (level < 0) {
			n.degrade( -level );
		}
		
		n.enchantment = w.enchantment;
		n.curseInfusionBonus = w.curseInfusionBonus;
		n.levelKnown = w.levelKnown;
		n.cursedKnown = w.cursedKnown;
		n.cursed = w.cursed;
		n.augment = w.augment;
		
		return n;
		
	}
	
	private Ring changeRing( Ring r ) {
		Ring n;
		do {
			n = (Ring) Generator.random( Generator.Category.RING );
		} while (Challenges.isItemBlocked(n) || n.getClass() == r.getClass());
		
		n.level(0);
		
		int level = r.level();
		if (level > 0) {
			n.upgrade( level );
		} else if (level < 0) {
			n.degrade( -level );
		}
		
		n.levelKnown = r.levelKnown;
		n.cursedKnown = r.cursedKnown;
		n.cursed = r.cursed;
		
		return n;
	}
	
	private com.quasistellar.hollowdungeon.items.artifacts.Artifact changeArtifact(com.quasistellar.hollowdungeon.items.artifacts.Artifact a ) {
		Artifact n = Generator.randomArtifact();
		
		if (n != null && !Challenges.isItemBlocked(n)){
			n.cursedKnown = a.cursedKnown;
			n.cursed = a.cursed;
			n.levelKnown = a.levelKnown;
			n.transferUpgrade(a.visiblyUpgraded());
			return n;
		}
		
		return null;
	}
	
	private Wand changeWand( Wand w ) {
		
		Wand n;
		do {
			n = (Wand) Generator.random( Generator.Category.WAND );
		} while ( com.quasistellar.hollowdungeon.Challenges.isItemBlocked(n) || n.getClass() == w.getClass());
		
		n.level( 0 );
		int level = w.level();
		if (w.curseInfusionBonus) level--;
		n.upgrade( level );
		
		n.levelKnown = w.levelKnown;
		n.cursedKnown = w.cursedKnown;
		n.cursed = w.cursed;
		n.curseInfusionBonus = w.curseInfusionBonus;
		
		return n;
	}
	
	private com.quasistellar.hollowdungeon.plants.Plant.Seed changeSeed(com.quasistellar.hollowdungeon.plants.Plant.Seed s ) {
		
		com.quasistellar.hollowdungeon.plants.Plant.Seed n;
		
		do {
			n = (Plant.Seed) Generator.random( Generator.Category.SEED );
		} while (n.getClass() == s.getClass());
		
		return n;
	}
	
	private Runestone changeStone( Runestone r ) {
		
		Runestone n;
		
		do {
			n = (Runestone) Generator.random( com.quasistellar.hollowdungeon.items.Generator.Category.STONE );
		} while (n.getClass() == r.getClass());
		
		return n;
	}
	
	private com.quasistellar.hollowdungeon.items.scrolls.Scroll changeScroll(Scroll s ) {
		if (s instanceof com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll) {
			return Reflection.newInstance(ExoticScroll.exoToReg.get(s.getClass()));
		} else {
			return Reflection.newInstance(com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll.regToExo.get(s.getClass()));
		}
	}
	
	private Potion changePotion( Potion p ) {
		if	(p instanceof com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion) {
			return Reflection.newInstance(ExoticPotion.exoToReg.get(p.getClass()));
		} else {
			return Reflection.newInstance(com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion.regToExo.get(p.getClass()));
		}
	}
	
	@Override
	public void empoweredRead() {
		//does nothing, this shouldn't happen
	}
	
	@Override
	public int price() {
		return isKnown() ? 50 * quantity : super.price();
	}
}
