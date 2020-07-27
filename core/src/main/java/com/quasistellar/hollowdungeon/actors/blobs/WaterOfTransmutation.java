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

package com.quasistellar.hollowdungeon.actors.blobs;

import com.quasistellar.hollowdungeon.effects.BlobEmitter;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon;
import com.quasistellar.hollowdungeon.journal.Catalog;
import com.quasistellar.hollowdungeon.journal.Notes;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.Challenges;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.artifacts.Artifact;
import com.quasistellar.hollowdungeon.items.potions.Potion;
import com.quasistellar.hollowdungeon.items.potions.PotionOfStrength;
import com.quasistellar.hollowdungeon.items.rings.Ring;
import com.quasistellar.hollowdungeon.items.scrolls.Scroll;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfUpgrade;
import com.quasistellar.hollowdungeon.items.wands.Wand;
import com.quasistellar.hollowdungeon.items.weapon.Weapon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class WaterOfTransmutation extends WellWater {
	
	@Override
	protected com.quasistellar.hollowdungeon.items.Item affectItem(Item item, int pos ) {
		
		if (item instanceof com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff) {
			item = changeStaff( (com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff)item );
		} else if (item instanceof com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon) {
			item = changeWeapon( (com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon)item );
		} else if (item instanceof Scroll) {
			item = changeScroll( (Scroll)item );
		} else if (item instanceof Potion) {
			item = changePotion( (Potion)item );
		} else if (item instanceof Ring) {
			item = changeRing( (Ring)item );
		} else if (item instanceof Wand) {
			item = changeWand( (Wand)item );
		} else if (item instanceof com.quasistellar.hollowdungeon.plants.Plant.Seed) {
			item = changeSeed( (com.quasistellar.hollowdungeon.plants.Plant.Seed)item );
		} else if (item instanceof Artifact) {
			item = changeArtifact( (Artifact)item );
		} else {
			item = null;
		}
		
		//incase a never-seen item pops out
		if (item != null&& item.isIdentified()){
			Catalog.setSeen(item.getClass());
		}

		return item;

	}
	
	@Override
	protected boolean affectHero(Hero hero) {
		return false;
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.CHANGE ), 0.2f, 0 );
	}
	
	@Override
	protected com.quasistellar.hollowdungeon.journal.Notes.Landmark record() {
		return Notes.Landmark.WELL_OF_TRANSMUTATION;
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
	
	private Weapon changeWeapon( com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon w ) {
		
		Weapon n;
		Generator.Category c = Generator.wepTiers[w.tier-1];

		do {
			n = (MeleeWeapon)Reflection.newInstance(c.classes[Random.chances(c.probs)]);
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

	private Artifact changeArtifact( Artifact a ) {
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
	
	private Scroll changeScroll( Scroll s ) {
		if (s instanceof ScrollOfUpgrade) {
			
			return null;
			
		} else {
			
			Scroll n;
			do {
				n = (Scroll) Generator.random( Generator.Category.SCROLL );
			} while (n.getClass() == s.getClass());
			return n;
		}
	}
	
	private Potion changePotion( Potion p ) {
		if (p instanceof PotionOfStrength) {
			
			return null;
			
		} else {
			
			Potion n;
			do {
				n = (Potion) Generator.random( Generator.Category.POTION );
			} while (n.getClass() == p.getClass());
			return n;
		}
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
