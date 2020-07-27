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

package com.quasistellar.hollowdungeon.items.food;

import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class MeatPie extends com.quasistellar.hollowdungeon.items.food.Food {
	
	{
		image = ItemSpriteSheet.MEAT_PIE;
	}
	
	@Override
	protected void satisfy(Hero hero) {
		super.satisfy( hero );
	}
	
	@Override
	public int price() {
		return 40 * quantity;
	}
	
	public static class Recipe extends com.quasistellar.hollowdungeon.items.Recipe {
		
		@Override
		public boolean testIngredients(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			boolean pasty = false;
			boolean ration = false;
			boolean meat = false;
			
			for (com.quasistellar.hollowdungeon.items.Item ingredient : ingredients){
				if (ingredient.quantity() > 0) {
					if (ingredient instanceof Pasty) {
						pasty = true;
					} else if (ingredient.getClass() == Food.class) {
						ration = true;
					} else if (ingredient instanceof MysteryMeat
							|| ingredient instanceof StewedMeat
							|| ingredient instanceof ChargrilledMeat
							|| ingredient instanceof FrozenCarpaccio) {
						meat = true;
					}
				}
			}
			
			return pasty && ration && meat;
		}
		
		@Override
		public int cost(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			return 6;
		}
		
		@Override
		public com.quasistellar.hollowdungeon.items.Item brew(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			for (com.quasistellar.hollowdungeon.items.Item ingredient : ingredients){
				ingredient.quantity(ingredient.quantity() - 1);
			}
			
			return sampleOutput(null);
		}
		
		@Override
		public com.quasistellar.hollowdungeon.items.Item sampleOutput(ArrayList<Item> ingredients) {
			return new MeatPie();
		}
	}
}
