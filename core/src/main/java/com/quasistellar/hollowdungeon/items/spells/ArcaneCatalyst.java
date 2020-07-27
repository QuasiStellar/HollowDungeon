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

package com.quasistellar.hollowdungeon.items.spells;

import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.scrolls.Scroll;
import com.quasistellar.hollowdungeon.items.stones.Runestone;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;

public class ArcaneCatalyst extends Spell {
	
	{
		image = ItemSpriteSheet.SCROLL_CATALYST;
	}
	
	private static HashMap<Class<? extends com.quasistellar.hollowdungeon.items.scrolls.Scroll>, Float> scrollChances = new HashMap<>();
	static{
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfIdentify.class,      3f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRemoveCurse.class,   2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfMagicMapping.class,  2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfMirrorImage.class,   2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRecharging.class,    2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfLullaby.class,       2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRetribution.class,   2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRage.class,          2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation.class, 2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTerror.class,        2f );
		scrollChances.put( com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTransmutation.class, 1f );
	}
	
	@Override
	protected void onCast(Hero hero) {
		
		detach( Item.curUser.belongings.backpack );
		Item.updateQuickslot();
		
		Scroll s = Reflection.newInstance(Random.chances(scrollChances));
		s.anonymize();
		Item.curItem = s;
		s.doRead();
	}
	
	@Override
	public int price() {
		return 40 * quantity;
	}
	
	public static class Recipe extends com.quasistellar.hollowdungeon.items.Recipe {
		
		@Override
		public boolean testIngredients(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			boolean scroll = false;
			boolean secondary = false;
			
			for (com.quasistellar.hollowdungeon.items.Item i : ingredients){
				if (i instanceof com.quasistellar.hollowdungeon.plants.Plant.Seed || i instanceof com.quasistellar.hollowdungeon.items.stones.Runestone){
					secondary = true;
					//if it is a regular or exotic potion
				} else if (ExoticScroll.regToExo.containsKey(i.getClass())
						|| com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll.regToExo.containsValue(i.getClass())) {
					scroll = true;
				}
			}
			
			return scroll && secondary;
		}
		
		@Override
		public int cost(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			for (com.quasistellar.hollowdungeon.items.Item i : ingredients){
				if (i instanceof Plant.Seed){
					return 2;
				} else if (i instanceof Runestone){
					return 1;
				}
			}
			return 1;
		}
		
		@Override
		public com.quasistellar.hollowdungeon.items.Item brew(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			
			for (com.quasistellar.hollowdungeon.items.Item i : ingredients){
				i.quantity(i.quantity()-1);
			}
			
			return sampleOutput(null);
		}
		
		@Override
		public com.quasistellar.hollowdungeon.items.Item sampleOutput(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			return new ArcaneCatalyst();
		}
	}
}
