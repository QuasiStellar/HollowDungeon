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

package com.quasistellar.hollowdungeon.actors.mobs.npcs;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.levels.rooms.standard.BlacksmithRoom;
import com.quasistellar.hollowdungeon.sprites.BlacksmithSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.WndBlacksmith;
import com.quasistellar.hollowdungeon.windows.WndQuest;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.journal.Notes;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.quest.DarkGold;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfUpgrade;
import com.quasistellar.hollowdungeon.levels.rooms.Room;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Blacksmith extends NPC {
	
	{
		spriteClass = BlacksmithSprite.class;

		properties.add(Char.Property.IMMOVABLE);
	}
	
	@Override
	protected boolean act() {
		throwItem();
		return super.act();
	}
	
	@Override
	public boolean interact(com.quasistellar.hollowdungeon.actors.Char c) {
		
		sprite.turnTo( pos, c.pos );

		if (c != Dungeon.hero){
			return true;
		}
		
		if (!Quest.given) {
			
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show(new com.quasistellar.hollowdungeon.windows.WndQuest( Blacksmith.this,
							Quest.alternative ? Messages.get(Blacksmith.this, "blood_1") : Messages.get(Blacksmith.this, "gold_1") ) {
						
						@Override
						public void onBackPressed() {
							super.onBackPressed();
							
							Quest.given = true;
							Quest.completed = false;
						}
					} );
				}
			});
			
			Notes.add( Notes.Landmark.TROLL );
			
		} else if (!Quest.completed) {
			if (Quest.alternative) {

				
			} else {

				
			}
		} else if (!Quest.reforged) {
			
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show( new WndBlacksmith( Blacksmith.this, Dungeon.hero ) );
				}
			});
			
		} else {
			
			tell( Messages.get(this, "get_lost") );
			
		}

		return true;
	}
	
	private void tell( String text ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				com.quasistellar.hollowdungeon.scenes.GameScene.show( new WndQuest( Blacksmith.this, text ) );
			}
		});
	}
	
	public static String verify(com.quasistellar.hollowdungeon.items.Item item1, com.quasistellar.hollowdungeon.items.Item item2 ) {
		
		if (item1 == item2 && (item1.quantity() == 1 && item2.quantity() == 1)) {
			return Messages.get(Blacksmith.class, "same_item");
		}

		if (item1.getClass() != item2.getClass()) {
			return Messages.get(Blacksmith.class, "diff_type");
		}
		
		if (!item1.isIdentified() || !item2.isIdentified()) {
			return Messages.get(Blacksmith.class, "un_ided");
		}
		
		if (item1.cursed || item2.cursed) {
			return Messages.get(Blacksmith.class, "cursed");
		}
		
		if (item1.level() < 0 || item2.level() < 0) {
			return Messages.get(Blacksmith.class, "degraded");
		}
		
		if (!item1.isUpgradable() || !item2.isUpgradable()) {
			return Messages.get(Blacksmith.class, "cant_reforge");
		}
		
		return null;
	}
	
	public static void upgrade(com.quasistellar.hollowdungeon.items.Item item1, com.quasistellar.hollowdungeon.items.Item item2 ) {
		
		com.quasistellar.hollowdungeon.items.Item first, second;
		if (item2.level() > item1.level()) {
			first = item2;
			second = item1;
		} else {
			first = item1;
			second = item2;
		}

		Sample.INSTANCE.play( Assets.Sounds.EVOKE );
		ScrollOfUpgrade.upgrade( Dungeon.hero );
		Item.evoke( Dungeon.hero );

		second.detach( Dungeon.hero.belongings.backpack );

		int level = first.level();
		//adjust for curse infusion
		first.level(level+1); //prevents on-upgrade effects like enchant/glyph removal

		Dungeon.hero.spendAndNext( 2f );
		com.quasistellar.hollowdungeon.items.Item.updateQuickslot();
		
		Quest.reforged = true;
		
		Notes.remove( com.quasistellar.hollowdungeon.journal.Notes.Landmark.TROLL );
	}
	
	@Override
	public void damage( int dmg, Object src ) {
	}
	
	@Override
	public void add( Buff buff ) {
	}
	
	@Override
	public boolean reset() {
		return true;
	}

	public static class Quest {
		
		private static boolean spawned;
		
		private static boolean alternative;
		private static boolean given;
		private static boolean completed;
		private static boolean reforged;
		
		public static void reset() {
			spawned		= false;
			given		= false;
			completed	= false;
			reforged	= false;
		}
		
		private static final String NODE	= "blacksmith";
		
		private static final String SPAWNED		= "spawned";
		private static final String ALTERNATIVE	= "alternative";
		private static final String GIVEN		= "given";
		private static final String COMPLETED	= "completed";
		private static final String REFORGED	= "reforged";
		
		public static void storeInBundle( Bundle bundle ) {
			
			Bundle node = new Bundle();
			
			node.put( SPAWNED, spawned );
			
			if (spawned) {
				node.put( ALTERNATIVE, alternative );
				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REFORGED, reforged );
			}
			
			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );
			
			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				alternative	=  node.getBoolean( ALTERNATIVE );
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
				reforged = node.getBoolean( REFORGED );
			} else {
				reset();
			}
		}
		
		public static ArrayList<Room> spawn( ArrayList<Room> rooms ) {

			return rooms;
		}
	}
}
