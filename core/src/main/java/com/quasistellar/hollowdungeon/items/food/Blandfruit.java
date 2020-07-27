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

import com.quasistellar.hollowdungeon.Challenges;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.Recipe;
import com.quasistellar.hollowdungeon.items.potions.Potion;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.plants.Sungrass;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Hunger;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Blandfruit extends Food {

	public com.quasistellar.hollowdungeon.items.potions.Potion potionAttrib = null;
	public com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing potionGlow = null;

	{
		stackable = true;
		image = ItemSpriteSheet.BLANDFRUIT;

		//only applies when blandfruit is cooked
		energy = Hunger.STARVING;

		bones = true;
	}

	@Override
	public boolean isSimilar( com.quasistellar.hollowdungeon.items.Item item ) {
		if ( super.isSimilar(item) ){
			Blandfruit other = (Blandfruit) item;
			if (potionAttrib == null && other.potionAttrib == null) {
					return true;
			} else if (potionAttrib != null && other.potionAttrib != null
					&& potionAttrib.isSimilar(other.potionAttrib)){
					return true;
			}
		}
		return false;
	}

	@Override
	public void execute(Hero hero, String action ) {

		if (action.equals( AC_EAT ) && potionAttrib == null) {

			GLog.w( Messages.get(this, "raw"));
			return;

		}

		super.execute(hero, action);

		if (action.equals( AC_EAT ) && potionAttrib != null){

			potionAttrib.apply(hero);

		}
	}

	@Override
	public String desc() {
		if (potionAttrib== null) {
			return super.desc();
		} else {
			String desc = Messages.get(this, "desc_cooked") + "\n\n";
			if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfFrost
				|| potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfLiquidFlame
				|| potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfToxicGas
				|| potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfParalyticGas) {
				desc += Messages.get(this, "desc_throw");
			} else {
				desc += Messages.get(this, "desc_eat");
			}
			return desc;
		}
	}

	@Override
	public int price() {
		return 20 * quantity;
	}

	public com.quasistellar.hollowdungeon.items.Item cook(com.quasistellar.hollowdungeon.plants.Plant.Seed seed){
		return imbuePotion(Reflection.newInstance(Potion.SeedToPotion.types.get(seed.getClass())));
	}

	public com.quasistellar.hollowdungeon.items.Item imbuePotion(com.quasistellar.hollowdungeon.items.potions.Potion potion){

		potionAttrib = potion;
		potionAttrib.anonymize();

		potionAttrib.image = ItemSpriteSheet.BLANDFRUIT;

		if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfHealing){
			name = Messages.get(this, "sunfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0x2EE62E );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfStrength){
			name = Messages.get(this, "rotfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0xCC0022 );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfParalyticGas){
			name = Messages.get(this, "earthfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0x67583D );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfInvisibility){
			name = Messages.get(this, "blindfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0xD9D9D9 );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfLiquidFlame){
			name = Messages.get(this, "firefruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0xFF7F00 );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfFrost){
			name = Messages.get(this, "icefruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0x66B3FF );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfMindVision){
			name = Messages.get(this, "fadefruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0x919999 );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfToxicGas){
			name = Messages.get(this, "sorrowfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0xA15CE5 );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfLevitation) {
			name = Messages.get(this, "stormfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0x1B5F79 );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfPurity) {
			name = Messages.get(this, "dreamfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0xC152AA );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfExperience) {
			name = Messages.get(this, "starfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0x404040 );
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfHaste) {
			name = Messages.get(this, "swiftfruit");
			potionGlow = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0xCCBB00 );
		}

		return this;
	}

	public static final String POTIONATTRIB = "potionattrib";
	
	@Override
	protected void onThrow(int cell) {
		if (Dungeon.level.map[cell] == Terrain.WELL || Dungeon.level.pit[cell]) {
			super.onThrow( cell );
			
		} else if (potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfLiquidFlame ||
				potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfToxicGas ||
				potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfParalyticGas ||
				potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfFrost ||
				potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfLevitation ||
				potionAttrib instanceof com.quasistellar.hollowdungeon.items.potions.PotionOfPurity) {

			potionAttrib.shatter( cell );
			Dungeon.level.drop(new Chunks(), cell).sprite.drop();
			
		} else {
			super.onThrow( cell );
		}
	}
	
	@Override
	public void reset() {
		if (potionAttrib != null)
			imbuePotion(potionAttrib);
		else
			super.reset();
	}
	
	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put( POTIONATTRIB , potionAttrib);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(POTIONATTRIB)) {
			imbuePotion((com.quasistellar.hollowdungeon.items.potions.Potion) bundle.get(POTIONATTRIB));
		}
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return potionGlow;
	}
	
	public static class CookFruit extends Recipe {
		
		@Override
		//also sorts ingredients if it can
		public boolean testIngredients(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			if (ingredients.size() != 2) return false;
			
			if (ingredients.get(0) instanceof Blandfruit){
				if (!(ingredients.get(1) instanceof com.quasistellar.hollowdungeon.plants.Plant.Seed)){
					return false;
				}
			} else if (ingredients.get(0) instanceof com.quasistellar.hollowdungeon.plants.Plant.Seed){
				if (ingredients.get(1) instanceof Blandfruit){
					com.quasistellar.hollowdungeon.items.Item temp = ingredients.get(0);
					ingredients.set(0, ingredients.get(1));
					ingredients.set(1, temp);
				} else {
					return false;
				}
			} else {
				return false;
			}
			
			Blandfruit fruit = (Blandfruit) ingredients.get(0);
			com.quasistellar.hollowdungeon.plants.Plant.Seed seed = (com.quasistellar.hollowdungeon.plants.Plant.Seed) ingredients.get(1);
			
			if (fruit.quantity() >= 1 && fruit.potionAttrib == null
				&& seed.quantity() >= 1){

				if (com.quasistellar.hollowdungeon.Dungeon.isChallenged(Challenges.NO_HEALING)
						&& seed instanceof Sungrass.Seed){
					return false;
				}

				return true;
			}
			
			return false;
		}
		
		@Override
		public int cost(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			return 3;
		}
		
		@Override
		public com.quasistellar.hollowdungeon.items.Item brew(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			ingredients.get(0).quantity(ingredients.get(0).quantity() - 1);
			ingredients.get(1).quantity(ingredients.get(1).quantity() - 1);
			
			
			return new Blandfruit().cook((com.quasistellar.hollowdungeon.plants.Plant.Seed) ingredients.get(1));
		}
		
		@Override
		public com.quasistellar.hollowdungeon.items.Item sampleOutput(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			return new Blandfruit().cook((Plant.Seed) ingredients.get(1));
		}
	}

	public static class Chunks extends Food {

		{
			stackable = true;
			image = com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet.BLAND_CHUNKS;

			energy = com.quasistellar.hollowdungeon.actors.buffs.Hunger.STARVING;

			bones = true;
		}

	}

}
