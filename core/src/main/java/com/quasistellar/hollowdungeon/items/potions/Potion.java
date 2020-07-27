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

package com.quasistellar.hollowdungeon.items.potions;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.blobs.Fire;
import com.quasistellar.hollowdungeon.actors.buffs.Burning;
import com.quasistellar.hollowdungeon.actors.buffs.Ooze;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.ItemStatusHandler;
import com.quasistellar.hollowdungeon.items.Recipe;
import com.quasistellar.hollowdungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion;
import com.quasistellar.hollowdungeon.journal.Catalog;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.windows.WndUseItem;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.effects.Splash;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.items.bags.Bag;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Potion extends com.quasistellar.hollowdungeon.items.Item {

	public static final String AC_DRINK = "DRINK";
	
	//used internally for potions that can be drunk or thrown
	public static final String AC_CHOOSE = "CHOOSE";

	private static final float TIME_TO_DRINK = 1f;

	private static final Class<?>[] potions = {
			PotionOfHealing.class,
			com.quasistellar.hollowdungeon.items.potions.PotionOfExperience.class,
			PotionOfToxicGas.class,
			PotionOfLiquidFlame.class,
			PotionOfStrength.class,
			PotionOfParalyticGas.class,
			PotionOfLevitation.class,
			PotionOfMindVision.class,
			PotionOfPurity.class,
			PotionOfInvisibility.class,
			PotionOfHaste.class,
			PotionOfFrost.class
	};

	private static final HashMap<String, Integer> colors = new HashMap<String, Integer>() {
		{
			put("crimson", ItemSpriteSheet.POTION_CRIMSON);
			put("amber", ItemSpriteSheet.POTION_AMBER);
			put("golden", ItemSpriteSheet.POTION_GOLDEN);
			put("jade", ItemSpriteSheet.POTION_JADE);
			put("turquoise", ItemSpriteSheet.POTION_TURQUOISE);
			put("azure", ItemSpriteSheet.POTION_AZURE);
			put("indigo", ItemSpriteSheet.POTION_INDIGO);
			put("magenta", ItemSpriteSheet.POTION_MAGENTA);
			put("bistre", ItemSpriteSheet.POTION_BISTRE);
			put("charcoal", ItemSpriteSheet.POTION_CHARCOAL);
			put("silver", ItemSpriteSheet.POTION_SILVER);
			put("ivory", ItemSpriteSheet.POTION_IVORY);
		}
	};
	
	private static final HashSet<Class<?extends Potion>> mustThrowPots = new HashSet<>();
	static{
		mustThrowPots.add(PotionOfToxicGas.class);
		mustThrowPots.add(PotionOfLiquidFlame.class);
		mustThrowPots.add(PotionOfParalyticGas.class);
		mustThrowPots.add(PotionOfFrost.class);
		
		//exotic
		mustThrowPots.add(com.quasistellar.hollowdungeon.items.potions.exotic.PotionOfCorrosiveGas.class);
		mustThrowPots.add(com.quasistellar.hollowdungeon.items.potions.exotic.PotionOfSnapFreeze.class);
		mustThrowPots.add(com.quasistellar.hollowdungeon.items.potions.exotic.PotionOfShroudingFog.class);
		mustThrowPots.add(com.quasistellar.hollowdungeon.items.potions.exotic.PotionOfStormClouds.class);
		
		//also all brews, hardcoded
	}
	
	private static final HashSet<Class<?extends Potion>> canThrowPots = new HashSet<>();
	static{
		canThrowPots.add(AlchemicalCatalyst.class);
		
		canThrowPots.add(PotionOfPurity.class);
		canThrowPots.add(PotionOfLevitation.class);
		
		//exotic
		canThrowPots.add(com.quasistellar.hollowdungeon.items.potions.exotic.PotionOfCleansing.class);
		
		//elixirs
		canThrowPots.add(ElixirOfHoneyedHealing.class);
	}
	
	protected static com.quasistellar.hollowdungeon.items.ItemStatusHandler<Potion> handler;
	
	protected String color;
	
	{
		stackable = true;
		defaultAction = AC_DRINK;
	}
	
	@SuppressWarnings("unchecked")
	public static void initColors() {
		handler = new com.quasistellar.hollowdungeon.items.ItemStatusHandler<>( (Class<? extends Potion>[])potions, colors );
	}
	
	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}

	public static void saveSelectively( Bundle bundle, ArrayList<com.quasistellar.hollowdungeon.items.Item> items ) {
		ArrayList<Class<?extends com.quasistellar.hollowdungeon.items.Item>> classes = new ArrayList<>();
		for (com.quasistellar.hollowdungeon.items.Item i : items){
			if (i instanceof com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion){
				if (!classes.contains(com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion.exoToReg.get(i.getClass()))){
					classes.add(com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion.exoToReg.get(i.getClass()));
				}
			} else if (i instanceof Potion){
				if (!classes.contains(i.getClass())){
					classes.add(i.getClass());
				}
			}
		}
		handler.saveClassesSelectively( bundle, classes );
	}
	
	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>( (Class<? extends Potion>[])potions, colors, bundle );
	}
	
	public Potion() {
		super();
		reset();
	}
	
	//anonymous potions are always IDed, do not affect ID status,
	//and their sprite is replaced by a placeholder if they are not known,
	//useful for items that appear in UIs, or which are only spawned for their effects
	protected boolean anonymous = false;
	public void anonymize(){
		if (!isKnown()) image = ItemSpriteSheet.POTION_HOLDER;
		anonymous = true;
	}

	@Override
	public void reset(){
		super.reset();
		if (handler != null && handler.contains(this)) {
			image = handler.image(this);
			color = handler.label(this);
		}
		setAction();
	}
	
	@Override
	public boolean collect( Bag container ) {
		if (super.collect( container )){
			setAction();
			return true;
		} else {
			return false;
		}
	}
	
	public void setAction(){
		if (isKnown() && mustThrowPots.contains(this.getClass())) {
			defaultAction = AC_THROW;
		} else if (isKnown() &&canThrowPots.contains(this.getClass())){
			defaultAction = AC_CHOOSE;
		} else {
			defaultAction = AC_DRINK;
		}
	}
	
	@Override
	public ArrayList<String> actions( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_DRINK );
		return actions;
	}
	
	@Override
	public void execute(final com.quasistellar.hollowdungeon.actors.hero.Hero hero, String action ) {

		super.execute( hero, action );
		
		if (action.equals( AC_CHOOSE )){
			
			GameScene.show(new WndUseItem(null, this) );
			
		} else if (action.equals( AC_DRINK )) {
			
			if (isKnown() && mustThrowPots.contains(getClass())) {
				
					GameScene.show(
						new com.quasistellar.hollowdungeon.windows.WndOptions( Messages.get(Potion.class, "harmful"),
								Messages.get(Potion.class, "sure_drink"),
								Messages.get(Potion.class, "yes"), Messages.get(Potion.class, "no") ) {
							@Override
							protected void onSelect(int index) {
								if (index == 0) {
									drink( hero );
								}
							}
						}
					);
					
				} else {
					drink( hero );
				}
			
		}
	}
	
	@Override
	public void doThrow( final com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {

		if (isKnown()
				&& !mustThrowPots.contains(this.getClass())
				&& !canThrowPots.contains(this.getClass())) {
		
			com.quasistellar.hollowdungeon.scenes.GameScene.show(
				new WndOptions( Messages.get(Potion.class, "beneficial"),
						Messages.get(Potion.class, "sure_throw"),
						Messages.get(Potion.class, "yes"), Messages.get(Potion.class, "no") ) {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							Potion.super.doThrow( hero );
						}
					}
				}
			);
			
		} else {
			super.doThrow( hero );
		}
	}
	
	protected void drink( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		
		detach( hero.belongings.backpack );
		
		hero.spend( TIME_TO_DRINK );
		hero.busy();
		apply( hero );
		
		Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.DRINK );
		
		hero.sprite.operate( hero.pos );
	}
	
	@Override
	protected void onThrow( int cell ) {
		if (com.quasistellar.hollowdungeon.Dungeon.level.map[cell] == Terrain.WELL || com.quasistellar.hollowdungeon.Dungeon.level.pit[cell]) {
			
			super.onThrow( cell );
			
		} else  {

			com.quasistellar.hollowdungeon.Dungeon.level.pressCell( cell );
			shatter( cell );
			
		}
	}
	
	public void apply( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		shatter( hero.pos );
	}
	
	public void shatter( int cell ) {
		if (com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[cell]) {
			GLog.i( Messages.get(Potion.class, "shatter") );
			Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.SHATTER );
			splash( cell );
		}
	}

	@Override
	public void cast(final Hero user, int dst ) {
			super.cast(user, dst);
	}
	
	public boolean isKnown() {
		return anonymous || (handler != null && handler.isKnown( this ));
	}
	
	public void setKnown() {
		if (!anonymous) {
			if (!isKnown()) {
				handler.know(this);
				updateQuickslot();
				Potion p = com.quasistellar.hollowdungeon.Dungeon.hero.belongings.getItem(getClass());
				if (p != null)  p.setAction();
				if (com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion.regToExo.get(getClass()) != null) {
					p = com.quasistellar.hollowdungeon.Dungeon.hero.belongings.getItem(com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion.regToExo.get(getClass()));
					if (p != null) p.setAction();
				}
			}
			
			if (com.quasistellar.hollowdungeon.Dungeon.hero.isAlive()) {
				Catalog.setSeen(getClass());
			}
		}
	}
	
	@Override
	public com.quasistellar.hollowdungeon.items.Item identify() {

		setKnown();
		return super.identify();
	}
	
	@Override
	public String name() {
		return isKnown() ? super.name() : Messages.get(this, color);
	}
	
	@Override
	public String info() {
		return isKnown() ? desc() : Messages.get(this, "unknown_desc");
	}
	
	@Override
	public boolean isIdentified() {
		return isKnown();
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	public static HashSet<Class<? extends Potion>> getKnown() {
		return handler.known();
	}
	
	public static HashSet<Class<? extends Potion>> getUnknown() {
		return handler.unknown();
	}
	
	public static boolean allKnown() {
		return handler.known().size() == potions.length;
	}
	
	protected int splashColor(){
		return anonymous ? 0x00AAFF : ItemSprite.pick( image, 5, 9 );
	}
	
	protected void splash( int cell ) {

		com.quasistellar.hollowdungeon.actors.blobs.Fire fire = (com.quasistellar.hollowdungeon.actors.blobs.Fire) com.quasistellar.hollowdungeon.Dungeon.level.blobs.get( Fire.class );
		if (fire != null)
			fire.clear( cell );

		final int color = splashColor();

		Char ch = Actor.findChar(cell);
		if (ch != null) {
			Buff.detach(ch, Burning.class);
			com.quasistellar.hollowdungeon.actors.buffs.Buff.detach(ch, Ooze.class);
			Splash.at( ch.sprite.center(), color, 5 );
		} else {
			com.quasistellar.hollowdungeon.effects.Splash.at( cell, color, 5 );
		}
	}
	
	@Override
	public int price() {
		return 30 * quantity;
	}
	
	public static class PlaceHolder extends Potion {
		
		{
			image = ItemSpriteSheet.POTION_HOLDER;
		}
		
		@Override
		public boolean isSimilar(com.quasistellar.hollowdungeon.items.Item item) {
			return ExoticPotion.regToExo.containsKey(item.getClass())
					|| com.quasistellar.hollowdungeon.items.potions.exotic.ExoticPotion.regToExo.containsValue(item.getClass());
		}
		
		@Override
		public String info() {
			return "";
		}
	}
	
	public static class SeedToPotion extends Recipe {
		
		public static HashMap<Class<?extends com.quasistellar.hollowdungeon.plants.Plant.Seed>, Class<?extends Potion>> types = new HashMap<>();
		static {
			types.put(com.quasistellar.hollowdungeon.plants.Blindweed.Seed.class,     PotionOfInvisibility.class);
			types.put(com.quasistellar.hollowdungeon.plants.Dreamfoil.Seed.class,     PotionOfPurity.class);
			types.put(com.quasistellar.hollowdungeon.plants.Earthroot.Seed.class,     PotionOfParalyticGas.class);
			types.put(com.quasistellar.hollowdungeon.plants.Fadeleaf.Seed.class,      PotionOfMindVision.class);
			types.put(com.quasistellar.hollowdungeon.plants.Firebloom.Seed.class,     PotionOfLiquidFlame.class);
			types.put(com.quasistellar.hollowdungeon.plants.Icecap.Seed.class,        PotionOfFrost.class);
			types.put(com.quasistellar.hollowdungeon.plants.Rotberry.Seed.class,      PotionOfStrength.class);
			types.put(com.quasistellar.hollowdungeon.plants.Sorrowmoss.Seed.class,    PotionOfToxicGas.class);
			types.put(com.quasistellar.hollowdungeon.plants.Starflower.Seed.class,    PotionOfExperience.class);
			types.put(com.quasistellar.hollowdungeon.plants.Stormvine.Seed.class,     PotionOfLevitation.class);
			types.put(com.quasistellar.hollowdungeon.plants.Sungrass.Seed.class,      PotionOfHealing.class);
			types.put(com.quasistellar.hollowdungeon.plants.Swiftthistle.Seed.class,  PotionOfHaste.class);
		}
		
		@Override
		public boolean testIngredients(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			if (ingredients.size() != 3) {
				return false;
			}
			
			for (com.quasistellar.hollowdungeon.items.Item ingredient : ingredients){
				if (!(ingredient instanceof com.quasistellar.hollowdungeon.plants.Plant.Seed
						&& ingredient.quantity() >= 1
						&& types.containsKey(ingredient.getClass()))){
					return false;
				}
			}
			return true;
		}
		
		@Override
		public int cost(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			return 0;
		}
		
		@Override
		public com.quasistellar.hollowdungeon.items.Item brew(ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			
			for (com.quasistellar.hollowdungeon.items.Item ingredient : ingredients){
				ingredient.quantity(ingredient.quantity() - 1);
			}
			
			ArrayList<Class<?extends com.quasistellar.hollowdungeon.plants.Plant.Seed>> seeds = new ArrayList<>();
			for (com.quasistellar.hollowdungeon.items.Item i : ingredients) {
				if (!seeds.contains(i.getClass())) {
					seeds.add((Class<? extends Plant.Seed>) i.getClass());
				}
			}
			
			com.quasistellar.hollowdungeon.items.Item result;
			
			if ( (seeds.size() == 2 && Random.Int(4) == 0)
					|| (seeds.size() == 3 && Random.Int(2) == 0)) {
				
				result = Generator.randomUsingDefaults( Generator.Category.POTION );
				
			} else {
				result = Reflection.newInstance(types.get(Random.element(ingredients).getClass()));
				
			}
			
			if (seeds.size() == 1){
				result.identify();
			}

			while (result instanceof PotionOfHealing
					&& (com.quasistellar.hollowdungeon.Dungeon.isChallenged(com.quasistellar.hollowdungeon.Challenges.NO_HEALING)
					|| Random.Int(10) < Dungeon.LimitedDrops.COOKING_HP.count)) {

				result = Generator.randomUsingDefaults(com.quasistellar.hollowdungeon.items.Generator.Category.POTION);
			}
			
			if (result instanceof PotionOfHealing) {
				com.quasistellar.hollowdungeon.Dungeon.LimitedDrops.COOKING_HP.count++;
			}
			
			com.quasistellar.hollowdungeon.Statistics.potionsCooked++;
			com.quasistellar.hollowdungeon.Badges.validatePotionsCooked();
			
			return result;
		}
		
		@Override
		public com.quasistellar.hollowdungeon.items.Item sampleOutput(ArrayList<Item> ingredients) {
			return new WndBag.Placeholder(com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet.POTION_HOLDER){
				{
					name = Messages.get(SeedToPotion.class, "name");
				}
				
				@Override
				public String info() {
					return "";
				}
			};
		}
	}
}
