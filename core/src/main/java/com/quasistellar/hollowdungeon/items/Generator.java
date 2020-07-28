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

package com.quasistellar.hollowdungeon.items;

import com.quasistellar.hollowdungeon.plants.Plant.Seed;
import com.quasistellar.hollowdungeon.plants.Starflower;
import com.quasistellar.hollowdungeon.items.bags.Bag;
import com.quasistellar.hollowdungeon.items.potions.Potion;
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
import com.quasistellar.hollowdungeon.items.scrolls.Scroll;
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
import com.quasistellar.hollowdungeon.items.stones.Runestone;
import com.quasistellar.hollowdungeon.items.stones.StoneOfAffection;
import com.quasistellar.hollowdungeon.items.stones.StoneOfAggression;
import com.quasistellar.hollowdungeon.items.stones.StoneOfBlast;
import com.quasistellar.hollowdungeon.items.stones.StoneOfBlink;
import com.quasistellar.hollowdungeon.items.stones.StoneOfClairvoyance;
import com.quasistellar.hollowdungeon.items.stones.StoneOfDeepenedSleep;
import com.quasistellar.hollowdungeon.items.stones.StoneOfDisarming;
import com.quasistellar.hollowdungeon.items.stones.StoneOfFlock;
import com.quasistellar.hollowdungeon.items.stones.StoneOfIntuition;
import com.quasistellar.hollowdungeon.items.stones.StoneOfShock;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Generator {

	public enum Category {
		POTION	( 16,   Potion.class ),
		SEED	( 2,    Seed.class ),
		
		SCROLL	( 16,   Scroll.class ),
		STONE   ( 2,    Runestone.class),
		
		GOLD	( 20,   com.quasistellar.hollowdungeon.items.Gold.class );
		
		public Class<?>[] classes;

		//some item types use a deck-based system, where the probs decrement as items are picked
		// until they are all 0, and then they reset. Those generator classes should define
		// defaultProbs. If defaultProbs is null then a deck system isn't used.
		//Artifacts in particular don't reset, no duplicates!
		public float[] probs;
		public float[] defaultProbs = null;
		
		public float prob;
		public Class<? extends Item> superClass;
		
		private Category( float prob, Class<? extends Item> superClass ) {
			this.prob = prob;
			this.superClass = superClass;
		}
		
		public static int order( Item item ) {
			for (int i=0; i < values().length; i++) {
				if (values()[i].superClass.isInstance( item )) {
					return i;
				}
			}
			
			return item instanceof Bag ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
		}

		static {
			GOLD.classes = new Class<?>[]{
					Gold.class };
			GOLD.probs = new float[]{ 1 };
			
			POTION.classes = new Class<?>[]{
					PotionOfStrength.class, //2 drop every chapter, see Dungeon.posNeeded()
					PotionOfHealing.class,
					PotionOfMindVision.class,
					PotionOfFrost.class,
					PotionOfLiquidFlame.class,
					PotionOfToxicGas.class,
					PotionOfHaste.class,
					PotionOfInvisibility.class,
					PotionOfLevitation.class,
					PotionOfParalyticGas.class,
					PotionOfPurity.class,
					PotionOfExperience.class};
			POTION.defaultProbs = new float[]{ 0, 6, 4, 3, 3, 3, 2, 2, 2, 2, 2, 1 };
			POTION.probs = POTION.defaultProbs.clone();
			
			SEED.classes = new Class<?>[]{
					com.quasistellar.hollowdungeon.plants.Rotberry.Seed.class, //quest item
					com.quasistellar.hollowdungeon.plants.Sungrass.Seed.class,
					com.quasistellar.hollowdungeon.plants.Fadeleaf.Seed.class,
					com.quasistellar.hollowdungeon.plants.Icecap.Seed.class,
					com.quasistellar.hollowdungeon.plants.Firebloom.Seed.class,
					com.quasistellar.hollowdungeon.plants.Sorrowmoss.Seed.class,
					com.quasistellar.hollowdungeon.plants.Swiftthistle.Seed.class,
					com.quasistellar.hollowdungeon.plants.Blindweed.Seed.class,
					com.quasistellar.hollowdungeon.plants.Stormvine.Seed.class,
					com.quasistellar.hollowdungeon.plants.Earthroot.Seed.class,
					com.quasistellar.hollowdungeon.plants.Dreamfoil.Seed.class,
					Starflower.Seed.class};
			SEED.defaultProbs = new float[]{ 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 2 };
			SEED.probs = SEED.defaultProbs.clone();
			
			SCROLL.classes = new Class<?>[]{
					ScrollOfUpgrade.class, //3 drop every chapter, see Dungeon.souNeeded()
					ScrollOfIdentify.class,
					ScrollOfRemoveCurse.class,
					ScrollOfMirrorImage.class,
					ScrollOfRecharging.class,
					ScrollOfTeleportation.class,
					ScrollOfLullaby.class,
					ScrollOfMagicMapping.class,
					ScrollOfRage.class,
					ScrollOfRetribution.class,
					ScrollOfTerror.class,
					ScrollOfTransmutation.class
			};
			SCROLL.defaultProbs = new float[]{ 0, 6, 4, 3, 3, 3, 2, 2, 2, 2, 2, 1 };
			SCROLL.probs = SCROLL.defaultProbs.clone();
			
			STONE.classes = new Class<?>[]{
					StoneOfIntuition.class,     //1 additional stone is also dropped on floors 1-3
					StoneOfDisarming.class,
					StoneOfFlock.class,
					StoneOfShock.class,
					StoneOfBlink.class,
					StoneOfDeepenedSleep.class,
					StoneOfClairvoyance.class,
					StoneOfAggression.class,
					StoneOfBlast.class,
					StoneOfAffection.class
			};
			STONE.defaultProbs = new float[]{ 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 };
			STONE.probs = STONE.defaultProbs.clone();
		}
	}

	private static final float[][] floorSetTierProbs = new float[][] {
			{0, 70, 20,  8,  2},
			{0, 25, 50, 20,  5},
			{0,  0, 40, 50, 10},
			{0,  0, 20, 40, 40},
			{0,  0,  0, 20, 80}
	};
	
	private static HashMap<Category,Float> categoryProbs = new LinkedHashMap<>();
	
	public static void reset() {
		for (Category cat : Category.values()) {
			categoryProbs.put( cat, cat.prob );
			if (cat.defaultProbs != null) cat.probs = cat.defaultProbs.clone();
		}
	}

	public static void reset(Category cat){
		cat.probs = cat.defaultProbs.clone();
	}
	
	public static Item random() {
		Category cat = Random.chances( categoryProbs );
		if (cat == null){
			reset();
			cat = Random.chances( categoryProbs );
		}
		categoryProbs.put( cat, categoryProbs.get( cat ) - 1);
		return random( cat );
	}
	
	public static Item random( Category cat ) {
		int i = Random.chances(cat.probs);
		if (i == -1) {
			reset(cat);
			i = Random.chances(cat.probs);
		}
		if (cat.defaultProbs != null) cat.probs[i]--;
		return ((Item) Reflection.newInstance(cat.classes[i])).random();
	}

	//overrides any deck systems and always uses default probs
	public static Item randomUsingDefaults( Category cat ){
		if (cat.defaultProbs == null) {
			return random(cat); //currently covers weapons/armor/missiles
		} else {
			return ((Item) Reflection.newInstance(cat.classes[Random.chances(cat.defaultProbs)])).random();
		}
	}
	
	public static Item random( Class<? extends Item> cl ) {
		return Reflection.newInstance(cl).random();
	}

	private static final String GENERAL_PROBS = "general_probs";
	private static final String CATEGORY_PROBS = "_probs";
	
	public static void storeInBundle(Bundle bundle) {
		Float[] genProbs = categoryProbs.values().toArray(new Float[0]);
		float[] storeProbs = new float[genProbs.length];
		for (int i = 0; i < storeProbs.length; i++){
			storeProbs[i] = genProbs[i];
		}
		bundle.put( GENERAL_PROBS, storeProbs);

		for (Category cat : Category.values()){
			if (cat.defaultProbs == null) continue;
			boolean needsStore = false;
			for (int i = 0; i < cat.probs.length; i++){
				if (cat.probs[i] != cat.defaultProbs[i]){
					needsStore = true;
					break;
				}
			}

			if (needsStore){
				bundle.put(cat.name().toLowerCase() + CATEGORY_PROBS, cat.probs);
			}
		}
	}

	public static void restoreFromBundle(Bundle bundle) {
		reset();

		if (bundle.contains(GENERAL_PROBS)){
			float[] probs = bundle.getFloatArray(GENERAL_PROBS);
			for (int i = 0; i < probs.length; i++){
				categoryProbs.put(Category.values()[i], probs[i]);
			}
		}

		for (Category cat : Category.values()){
			if (bundle.contains(cat.name().toLowerCase() + CATEGORY_PROBS)){
				float[] probs = bundle.getFloatArray(cat.name().toLowerCase() + CATEGORY_PROBS);
				if (cat.defaultProbs != null && probs.length == cat.defaultProbs.length){
					cat.probs = probs;
				}
			}
		}
	}
}
