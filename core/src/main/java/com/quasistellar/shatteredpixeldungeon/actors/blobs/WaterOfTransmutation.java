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

package com.quasistellar.shatteredpixeldungeon.actors.blobs;

import com.quasistellar.shatteredpixeldungeon.Challenges;
import com.quasistellar.shatteredpixeldungeon.effects.BlobEmitter;
import com.quasistellar.shatteredpixeldungeon.effects.Speck;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.quasistellar.shatteredpixeldungeon.journal.Catalog;
import com.quasistellar.shatteredpixeldungeon.journal.Notes;
import com.quasistellar.shatteredpixeldungeon.plants.Plant;
import com.quasistellar.shatteredpixeldungeon.actors.hero.Hero;
import com.quasistellar.shatteredpixeldungeon.items.artifacts.Artifact;
import com.quasistellar.shatteredpixeldungeon.items.potions.Potion;
import com.quasistellar.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.quasistellar.shatteredpixeldungeon.items.rings.Ring;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.Scroll;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.quasistellar.shatteredpixeldungeon.items.wands.Wand;
import com.quasistellar.shatteredpixeldungeon.items.weapon.Weapon;
import com.quasistellar.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class WaterOfTransmutation extends WellWater {
	
	@Override
	protected com.quasistellar.shatteredpixeldungeon.items.Item affectItem(Item item, int pos ) {
		
		if (item instanceof com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MagesStaff) {
			item = changeStaff( (com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MagesStaff)item );
		} else if (item instanceof com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon) {
			item = changeWeapon( (com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon)item );
		} else if (item instanceof Scroll) {
			item = changeScroll( (Scroll)item );
		} else if (item instanceof Potion) {
			item = changePotion( (Potion)item );
		} else if (item instanceof Ring) {
			item = changeRing( (Ring)item );
		} else if (item instanceof Wand) {
			item = changeWand( (Wand)item );
		} else if (item instanceof com.quasistellar.shatteredpixeldungeon.plants.Plant.Seed) {
			item = changeSeed( (com.quasistellar.shatteredpixeldungeon.plants.Plant.Seed)item );
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
		emitter.start( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( Speck.CHANGE ), 0.2f, 0 );
	}
	
	@Override
	protected Notes.Landmark record() {
		return Notes.Landmark.WELL_OF_TRANSMUTATION;
	}

	private com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MagesStaff changeStaff(MagesStaff staff ){
		Class<?extends Wand> wandClass = staff.wandClass();

		if (wandClass == null){
			return null;
		} else {
			Wand n;
			do {
				n = (Wand) com.quasistellar.shatteredpixeldungeon.items.Generator.random(com.quasistellar.shatteredpixeldungeon.items.Generator.Category.WAND);
			} while (com.quasistellar.shatteredpixeldungeon.Challenges.isItemBlocked(n) || n.getClass() == wandClass);
			n.level(0);
			n.identify();
			staff.imbueWand(n, null);
		}

		return staff;
	}
	
	private Weapon changeWeapon( com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon w ) {
		
		Weapon n;
		com.quasistellar.shatteredpixeldungeon.items.Generator.Category c = com.quasistellar.shatteredpixeldungeon.items.Generator.wepTiers[w.tier-1];

		do {
			n = (MeleeWeapon)Reflection.newInstance(c.classes[Random.chances(c.probs)]);
		} while (com.quasistellar.shatteredpixeldungeon.Challenges.isItemBlocked(n) || n.getClass() == w.getClass());

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
			n = (Ring) com.quasistellar.shatteredpixeldungeon.items.Generator.random( com.quasistellar.shatteredpixeldungeon.items.Generator.Category.RING );
		} while (com.quasistellar.shatteredpixeldungeon.Challenges.isItemBlocked(n) || n.getClass() == r.getClass());
		
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
		Artifact n = com.quasistellar.shatteredpixeldungeon.items.Generator.randomArtifact();

		if (n != null && !com.quasistellar.shatteredpixeldungeon.Challenges.isItemBlocked(n)){
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
			n = (Wand) com.quasistellar.shatteredpixeldungeon.items.Generator.random( com.quasistellar.shatteredpixeldungeon.items.Generator.Category.WAND );
		} while ( Challenges.isItemBlocked(n) || n.getClass() == w.getClass());
		
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
	
	private com.quasistellar.shatteredpixeldungeon.plants.Plant.Seed changeSeed(com.quasistellar.shatteredpixeldungeon.plants.Plant.Seed s ) {
		
		com.quasistellar.shatteredpixeldungeon.plants.Plant.Seed n;
		
		do {
			n = (Plant.Seed) com.quasistellar.shatteredpixeldungeon.items.Generator.random( com.quasistellar.shatteredpixeldungeon.items.Generator.Category.SEED );
		} while (n.getClass() == s.getClass());
		
		return n;
	}
	
	private Scroll changeScroll( Scroll s ) {
		if (s instanceof ScrollOfUpgrade) {
			
			return null;
			
		} else {
			
			Scroll n;
			do {
				n = (Scroll) com.quasistellar.shatteredpixeldungeon.items.Generator.random( com.quasistellar.shatteredpixeldungeon.items.Generator.Category.SCROLL );
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
				n = (Potion) com.quasistellar.shatteredpixeldungeon.items.Generator.random( com.quasistellar.shatteredpixeldungeon.items.Generator.Category.POTION );
			} while (n.getClass() == p.getClass());
			return n;
		}
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
