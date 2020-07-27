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

import com.quasistellar.hollowdungeon.ShatteredPixelDungeon;
import com.quasistellar.hollowdungeon.items.artifacts.AlchemistsToolkit;
import com.quasistellar.hollowdungeon.items.bombs.Bomb;
import com.quasistellar.hollowdungeon.items.food.Blandfruit;
import com.quasistellar.hollowdungeon.items.food.MeatPie;
import com.quasistellar.hollowdungeon.items.food.StewedMeat;
import com.quasistellar.hollowdungeon.items.potions.AlchemicalCatalyst;
import com.quasistellar.hollowdungeon.items.potions.Potion;
import com.quasistellar.hollowdungeon.items.potions.brews.BlizzardBrew;
import com.quasistellar.hollowdungeon.items.potions.brews.CausticBrew;
import com.quasistellar.hollowdungeon.items.potions.brews.InfernalBrew;
import com.quasistellar.hollowdungeon.items.potions.brews.ShockingBrew;
import com.quasistellar.hollowdungeon.items.potions.elixirs.ElixirOfAquaticRejuvenation;
import com.quasistellar.hollowdungeon.items.potions.elixirs.ElixirOfArcaneArmor;
import com.quasistellar.hollowdungeon.items.potions.elixirs.ElixirOfDragonsBlood;
import com.quasistellar.hollowdungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.quasistellar.hollowdungeon.items.potions.elixirs.ElixirOfIcyTouch;
import com.quasistellar.hollowdungeon.items.potions.elixirs.ElixirOfMight;
import com.quasistellar.hollowdungeon.items.potions.elixirs.ElixirOfToxicEssence;
import com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion;
import com.quasistellar.hollowdungeon.items.scrolls.Scroll;
import com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll;
import com.quasistellar.hollowdungeon.items.spells.Alchemize;
import com.quasistellar.hollowdungeon.items.spells.AquaBlast;
import com.quasistellar.hollowdungeon.items.spells.ArcaneCatalyst;
import com.quasistellar.hollowdungeon.items.spells.BeaconOfReturning;
import com.quasistellar.hollowdungeon.items.spells.CurseInfusion;
import com.quasistellar.hollowdungeon.items.spells.FeatherFall;
import com.quasistellar.hollowdungeon.items.spells.MagicalInfusion;
import com.quasistellar.hollowdungeon.items.spells.MagicalPorter;
import com.quasistellar.hollowdungeon.items.spells.PhaseShift;
import com.quasistellar.hollowdungeon.items.spells.ReclaimTrap;
import com.quasistellar.hollowdungeon.items.spells.Recycle;
import com.quasistellar.hollowdungeon.items.spells.WildEnergy;
import com.quasistellar.hollowdungeon.items.wands.Wand;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public abstract class Recipe {
	
	public abstract boolean testIngredients(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients);
	
	public abstract int cost(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients);
	
	public abstract com.quasistellar.hollowdungeon.items.Item brew(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients);
	
	public abstract com.quasistellar.hollowdungeon.items.Item sampleOutput(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients);
	
	//subclass for the common situation of a recipe with static inputs and outputs
	public static abstract class SimpleRecipe extends Recipe {
		
		//*** These elements must be filled in by subclasses
		protected Class<?extends com.quasistellar.hollowdungeon.items.Item>[] inputs; //each class should be unique
		protected int[] inQuantity;
		
		protected int cost;
		
		protected Class<?extends com.quasistellar.hollowdungeon.items.Item> output;
		protected int outQuantity;
		//***
		
		//gets a simple list of items based on inputs
		public ArrayList<com.quasistellar.hollowdungeon.items.Item> getIngredients() {
			ArrayList<com.quasistellar.hollowdungeon.items.Item> result = new ArrayList<>();
			for (int i = 0; i < inputs.length; i++) {
				com.quasistellar.hollowdungeon.items.Item ingredient = Reflection.newInstance(inputs[i]);
				ingredient.quantity(inQuantity[i]);
				result.add(ingredient);
			}
			return result;
		}
		
		@Override
		public final boolean testIngredients(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			
			int[] needed = inQuantity.clone();
			
			for (com.quasistellar.hollowdungeon.items.Item ingredient : ingredients){
				if (!ingredient.isIdentified()) return false;
				for (int i = 0; i < inputs.length; i++){
					if (ingredient.getClass() == inputs[i]){
						needed[i] -= ingredient.quantity();
						break;
					}
				}
			}
			
			for (int i : needed){
				if (i > 0){
					return false;
				}
			}
			
			return true;
		}
		
		public final int cost(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients){
			return cost;
		}
		
		@Override
		public final com.quasistellar.hollowdungeon.items.Item brew(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			int[] needed = inQuantity.clone();
			
			for (com.quasistellar.hollowdungeon.items.Item ingredient : ingredients){
				for (int i = 0; i < inputs.length; i++) {
					if (ingredient.getClass() == inputs[i] && needed[i] > 0) {
						if (needed[i] <= ingredient.quantity()) {
							ingredient.quantity(ingredient.quantity() - needed[i]);
							needed[i] = 0;
						} else {
							needed[i] -= ingredient.quantity();
							ingredient.quantity(0);
						}
					}
				}
			}
			
			//sample output and real output are identical in this case.
			return sampleOutput(null);
		}
		
		//ingredients are ignored, as output doesn't vary
		public final com.quasistellar.hollowdungeon.items.Item sampleOutput(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients){
			try {
				com.quasistellar.hollowdungeon.items.Item result = Reflection.newInstance(output);
				result.quantity(outQuantity);
				return result;
			} catch (Exception e) {
				ShatteredPixelDungeon.reportException( e );
				return null;
			}
		}
	}
	
	
	//*******
	// Static members
	//*******
	
	private static Recipe[] oneIngredientRecipes = new Recipe[]{
		new AlchemistsToolkit.upgradeKit(),
		new Scroll.ScrollToStone(),
		new StewedMeat.oneMeat()
	};
	
	private static Recipe[] twoIngredientRecipes = new Recipe[]{
		new Blandfruit.CookFruit(),
		new Bomb.EnhanceBomb(),
		new AlchemicalCatalyst.Recipe(),
		new ArcaneCatalyst.Recipe(),
		new ElixirOfArcaneArmor.Recipe(),
		new ElixirOfAquaticRejuvenation.Recipe(),
		new ElixirOfDragonsBlood.Recipe(),
		new ElixirOfIcyTouch.Recipe(),
		new ElixirOfMight.Recipe(),
		new ElixirOfHoneyedHealing.Recipe(),
		new ElixirOfToxicEssence.Recipe(),
		new BlizzardBrew.Recipe(),
		new InfernalBrew.Recipe(),
		new ShockingBrew.Recipe(),
		new CausticBrew.Recipe(),
		new Alchemize.Recipe(),
		new AquaBlast.Recipe(),
		new BeaconOfReturning.Recipe(),
		new CurseInfusion.Recipe(),
		new FeatherFall.Recipe(),
		new MagicalInfusion.Recipe(),
		new MagicalPorter.Recipe(),
		new PhaseShift.Recipe(),
		new ReclaimTrap.Recipe(),
		new Recycle.Recipe(),
		new WildEnergy.Recipe(),
		new StewedMeat.twoMeat()
	};
	
	private static Recipe[] threeIngredientRecipes = new Recipe[]{
		new Potion.SeedToPotion(),
		new ExoticPotion.PotionToExotic(),
		new ExoticScroll.ScrollToExotic(),
		new StewedMeat.threeMeat(),
		new MeatPie.Recipe()
	};
	
	public static Recipe findRecipe(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients){
		
		if (ingredients.size() == 1){
			for (Recipe recipe : oneIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					return recipe;
				}
			}
			
		} else if (ingredients.size() == 2){
			for (Recipe recipe : twoIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					return recipe;
				}
			}
			
		} else if (ingredients.size() == 3){
			for (Recipe recipe : threeIngredientRecipes){
				if (recipe.testIngredients(ingredients)){
					return recipe;
				}
			}
		}
		
		return null;
	}
	
	public static boolean usableInRecipe(Item item){
		return !item.cursed
				&& (!(item instanceof EquipableItem) || (item instanceof AlchemistsToolkit && item.isIdentified()))
				&& !(item instanceof Wand);
	}
}


