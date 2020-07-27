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

package com.quasistellar.hollowdungeon.items.weapon.missiles.darts;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.PinCushion;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.hero.HeroSubClass;
import com.quasistellar.hollowdungeon.items.wands.WandOfRegrowth;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class TippedDart extends com.quasistellar.hollowdungeon.items.weapon.missiles.darts.Dart {
	
	{
		tier = 2;
		
		//so that slightly more than 1.5x durability is needed for 2 uses
		baseUses = 0.65f;
	}
	
	private static final String AC_CLEAN = "CLEAN";
	
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		actions.remove( AC_TIP );
		actions.add( AC_CLEAN );
		return actions;
	}
	
	@Override
	public void execute(final Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals( AC_CLEAN )){
			
			GameScene.show(new WndOptions(Messages.get(this, "clean_title"),
					Messages.get(this, "clean_desc"),
					Messages.get(this, "clean_all"),
					Messages.get(this, "clean_one"),
					Messages.get(this, "cancel")){
				@Override
				protected void onSelect(int index) {
					if (index == 0){
						detachAll(hero.belongings.backpack);
						new com.quasistellar.hollowdungeon.items.weapon.missiles.darts.Dart().quantity(quantity).collect();
						
						hero.spend( 1f );
						hero.busy();
						hero.sprite.operate(hero.pos);
					} else if (index == 1){
						detach(hero.belongings.backpack);
						if (!new com.quasistellar.hollowdungeon.items.weapon.missiles.darts.Dart().collect()) Dungeon.level.drop(new com.quasistellar.hollowdungeon.items.weapon.missiles.darts.Dart(), hero.pos).sprite.drop();
						
						hero.spend( 1f );
						hero.busy();
						hero.sprite.operate(hero.pos);
					}
				}
			});
			
		}
	}
	
	//exact same damage as regular darts, despite being higher tier.

	@Override
	protected void rangedHit(com.quasistellar.hollowdungeon.actors.Char enemy, int cell) {
		targetPos = cell;
		super.rangedHit( enemy, cell);
		
		//need to spawn a dart
		if (durability <= 0){
			//attempt to stick the dart to the enemy, just drop it if we can't.
			com.quasistellar.hollowdungeon.items.weapon.missiles.darts.Dart d = new Dart();
			if (enemy.isAlive() && sticky) {
				PinCushion p = Buff.affect(enemy, PinCushion.class);
				if (p.target == enemy){
					p.stick(d);
					return;
				}
			}
			Dungeon.level.drop( d, enemy.pos ).sprite.drop();
		}
	}

	private static int targetPos = -1;

	@Override
	protected float durabilityPerUse() {
		float use = super.durabilityPerUse();
		
		if (Dungeon.hero.subClass == HeroSubClass.WARDEN){
			use /= 2f;
		}

		//checks both destination and source position
		float lotusPreserve = 0f;
		if (targetPos != -1){
			for (com.quasistellar.hollowdungeon.actors.Char ch : Actor.chars()){
				if (ch instanceof WandOfRegrowth.Lotus){
					WandOfRegrowth.Lotus l = (WandOfRegrowth.Lotus) ch;
					if (l.inRange(targetPos)){
						lotusPreserve = Math.max(lotusPreserve, l.seedPreservation());
					}
				}
			}
			targetPos = -1;
		}
		int p = com.quasistellar.hollowdungeon.items.Item.curUser == null ? com.quasistellar.hollowdungeon.Dungeon.hero.pos : Item.curUser.pos;
		for (Char ch : com.quasistellar.hollowdungeon.actors.Actor.chars()){
			if (ch instanceof WandOfRegrowth.Lotus){
				WandOfRegrowth.Lotus l = (WandOfRegrowth.Lotus) ch;
				if (l.inRange(p)){
					lotusPreserve = Math.max(lotusPreserve, l.seedPreservation());
				}
			}
		}
		use *= (1f - lotusPreserve);
		
		return use;
	}
	
	@Override
	public int price() {
		//value of regular dart plus half of the seed
		return 8 * quantity;
	}
	
	private static HashMap<Class<?extends com.quasistellar.hollowdungeon.plants.Plant.Seed>, Class<?extends TippedDart>> types = new HashMap<>();
	static {
		types.put(com.quasistellar.hollowdungeon.plants.Blindweed.Seed.class,     BlindingDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Dreamfoil.Seed.class,     SleepDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Earthroot.Seed.class,     ParalyticDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Fadeleaf.Seed.class,      DisplacingDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Firebloom.Seed.class,     IncendiaryDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Icecap.Seed.class,        ChillingDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Rotberry.Seed.class,      RotDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Sorrowmoss.Seed.class,    PoisonDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Starflower.Seed.class,    HolyDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Stormvine.Seed.class,     ShockingDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Sungrass.Seed.class,      HealingDart.class);
		types.put(com.quasistellar.hollowdungeon.plants.Swiftthistle.Seed.class,  AdrenalineDart.class);
	}
	
	public static TippedDart getTipped(com.quasistellar.hollowdungeon.plants.Plant.Seed s, int quantity ){
		return (TippedDart) Reflection.newInstance(types.get(s.getClass())).quantity(quantity);
	}
	
	public static TippedDart randomTipped( int quantity ){
		com.quasistellar.hollowdungeon.plants.Plant.Seed s;
		do{
			s = (Plant.Seed) Generator.randomUsingDefaults(com.quasistellar.hollowdungeon.items.Generator.Category.SEED);
		} while (!types.containsKey(s.getClass()));
		
		return getTipped(s, quantity );
		
	}
	
}
