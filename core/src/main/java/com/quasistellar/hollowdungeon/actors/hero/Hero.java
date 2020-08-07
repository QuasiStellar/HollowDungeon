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

package com.quasistellar.hollowdungeon.actors.hero;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.blobs.Alchemy;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.CheckedCell;
import com.quasistellar.hollowdungeon.levels.traps.Trap;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Languages;
import com.quasistellar.hollowdungeon.plants.Swiftthistle;
import com.quasistellar.hollowdungeon.skills.CrystalHeart;
import com.quasistellar.hollowdungeon.skills.DesolateDive;
import com.quasistellar.hollowdungeon.skills.DreamNail;
import com.quasistellar.hollowdungeon.skills.Dreamgate;
import com.quasistellar.hollowdungeon.skills.Focus;
import com.quasistellar.hollowdungeon.skills.HowlingWraiths;
import com.quasistellar.hollowdungeon.skills.MonarchWings;
import com.quasistellar.hollowdungeon.skills.MothwingCloak;
import com.quasistellar.hollowdungeon.skills.Skill;
import com.quasistellar.hollowdungeon.skills.VengefulSpirit;
import com.quasistellar.hollowdungeon.sprites.HeroSprite;
import com.quasistellar.hollowdungeon.ui.HpIndicator;
import com.quasistellar.hollowdungeon.windows.WndTradeItem;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.journal.Notes;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.features.Chasm;
import com.quasistellar.hollowdungeon.scenes.AlchemyScene;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.scenes.InterlevelScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.ui.AttackIndicator;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.ui.QuickSlotButton;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.items.keys.CrystalKey;
import com.quasistellar.hollowdungeon.items.keys.GoldenKey;
import com.quasistellar.hollowdungeon.items.keys.IronKey;
import com.quasistellar.hollowdungeon.items.keys.Key;
import com.quasistellar.hollowdungeon.items.keys.SkeletonKey;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;

public class Hero extends com.quasistellar.hollowdungeon.actors.Char {

	{
		actPriority = Actor.HERO_PRIO;
		
		alignment = Alignment.ALLY;
	}
	
	private static final float TIME_TO_REST		    = 1f;
	private static final float TIME_TO_SEARCH	    = 2f;
	
	public HeroClass heroClass = HeroClass.KNIGHT;

	public boolean ready = false;
	private boolean damageInterrupt = true;
	public HeroAction curAction = null;
	public HeroAction lastAction = null;

	private com.quasistellar.hollowdungeon.actors.Char enemy;
	
	public boolean resting = false;
	
	public Belongings belongings;
	
	public float awareness;

	public Skill mothwingCloak = new MothwingCloak();
	public Skill crystalHeart = new CrystalHeart();
	public Skill monarchWings = new MonarchWings();
	public Skill dreamNail = new DreamNail();
	public Skill dreamgate = new Dreamgate();
	public Skill focus = new Focus();
	public Skill vengefulSpirit = new VengefulSpirit();
	public Skill desolateDive = new DesolateDive();
	public Skill howlingWraiths = new HowlingWraiths();

	public int MM;
	public int MP;
	
	public int HTBoost = 0;

	public int dreamgatePos;
	public String dreamgateLocation = "";
	
	private ArrayList<com.quasistellar.hollowdungeon.actors.mobs.Mob> visibleEnemies;

	//This list is maintained so that some logic checks can be skipped
	// for enemies we know we aren't seeing normally, resultign in better performance
	public ArrayList<com.quasistellar.hollowdungeon.actors.mobs.Mob> mindVisionEnemies = new ArrayList<>();

	public Hero() {
		super();

		MM = 20;
		MP = 0;
		HP = HT = 5;
		
		belongings = new Belongings( this );
		
		visibleEnemies = new ArrayList<>();
	}
	
	public void updateHT( boolean boostHP ){
		int curHT = HT;
		
		HT = 5 + HTBoost;
		
		if (boostHP){
			HP += Math.max(HT - curHT, 0);
		}
		HP = Math.min(HP, HT);
	}


	private static final String HTBOOST     = "htboost";
	private static final String MANA_MAX	= "MM";
	private static final String MANA		= "MP";
	private static final String DREAMGATE_POS= "dreamgatePos";
	private static final String DREAMGATE_LOCATION		= "dreamgateLocation";
	
	@Override
	public void storeInBundle( Bundle bundle ) {

		super.storeInBundle( bundle );
		
		heroClass.storeInBundle( bundle );
		
		bundle.put( HTBOOST, HTBoost );

		bundle.put( MANA_MAX, MM );
		bundle.put( MANA, MP );

		bundle.put( DREAMGATE_POS, dreamgatePos );
		bundle.put( DREAMGATE_LOCATION, dreamgateLocation );

		belongings.storeInBundle( bundle );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		
		heroClass = HeroClass.restoreInBundle( bundle );
		
		HTBoost = bundle.getInt(HTBOOST);

		MM = bundle.getInt( MANA_MAX );
		MP = bundle.getInt( MANA );

		dreamgatePos = bundle.getInt( DREAMGATE_POS );
		dreamgateLocation = bundle.getString( DREAMGATE_LOCATION );
		
		belongings.restoreFromBundle( bundle );
	}
	
	public static void preview(com.quasistellar.hollowdungeon.GamesInProgress.Info info, Bundle bundle ) {
		info.hp = bundle.getInt( Char.TAG_HP );
		info.ht = bundle.getInt( Char.TAG_HT );
		info.mp = bundle.getInt( MANA );
		info.mm = bundle.getInt( MANA_MAX);
		info.shld = bundle.getInt( Char.TAG_SHLD );
		info.heroClass = HeroClass.restoreInBundle( bundle );
		Belongings.preview( info, bundle );
	}
	
	public String className() {
		return heroClass.title();
	}

	@Override
	public String name(){
		return className();
	}

	@Override
	public void hitSound(float pitch) {
		super.hitSound(pitch * 1.1f);
	}

	public void live() {
	}

	@Override
	public int damageRoll() {
		int dmg = 5;
		return buff( com.quasistellar.hollowdungeon.actors.buffs.Fury.class ) != null ? (int)(dmg * 1.5f) : dmg;
	}
	
	@Override
	public float speed() {

		float speed = super.speed();
		
		com.quasistellar.hollowdungeon.actors.buffs.Momentum momentum = buff(com.quasistellar.hollowdungeon.actors.buffs.Momentum.class);
		if (momentum != null){
			((HeroSprite)sprite).sprint( 1f + 0.05f*momentum.stacks());
			speed *= momentum.speedMultiplier();
		}
		
		return speed;
		
	}

	public boolean canAttack(com.quasistellar.hollowdungeon.actors.Char enemy){
		if (enemy == null || pos == enemy.pos) {
			return false;
		}

		//can always attack adjacent enemies
		if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent(pos, enemy.pos)) {
			return true;
		}

		return false;
	}
	
	public float attackDelay() {
		return 1;
	}

	@Override
	public void spend( float time ) {
		justMoved = false;
		
		com.quasistellar.hollowdungeon.plants.Swiftthistle.TimeBubble bubble = buff(com.quasistellar.hollowdungeon.plants.Swiftthistle.TimeBubble.class);
		if (bubble != null){
			bubble.processTime(time);
			return;
		}
		
		super.spend(time);
	}
	
	public void spendAndNext( float time ) {
		busy();
		spend( time );
		next();
	}
	
	@Override
	public boolean act() {
		
		//calls to dungeon.observe will also update hero's local FOV.
		fieldOfView = com.quasistellar.hollowdungeon.Dungeon.level.heroFOV;
		
		if (!ready) {
			//do a full observe (including fog update) if not resting.
			if (!resting || buff(com.quasistellar.hollowdungeon.actors.buffs.MindVision.class) != null || buff(com.quasistellar.hollowdungeon.actors.buffs.Awareness.class) != null) {
				com.quasistellar.hollowdungeon.Dungeon.observe();
			} else {
				//otherwise just directly re-calculate FOV
				com.quasistellar.hollowdungeon.Dungeon.level.updateFieldOfView(this, fieldOfView);
			}
		}
		
		checkVisibleMobs();
		BuffIndicator.refreshHero();
		
		if (paralysed > 0) {
			
			curAction = null;
			
			spendAndNext( com.quasistellar.hollowdungeon.actors.Actor.TICK );
			return false;
		}
		
		boolean actResult;
		if (curAction == null) {
			
			if (resting) {
				spend( TIME_TO_REST );
				next();
			} else {
				ready();
			}
			
			actResult = false;
			
		} else {
			
			resting = false;
			
			ready = false;
			
			if (curAction instanceof HeroAction.Move) {
				actResult = actMove( (HeroAction.Move)curAction );
				
			} else if (curAction instanceof HeroAction.Interact) {
				actResult = actInteract( (HeroAction.Interact)curAction );
				
			} else if (curAction instanceof HeroAction.Buy) {
				actResult = actBuy( (HeroAction.Buy)curAction );
				
			}else if (curAction instanceof HeroAction.PickUp) {
				actResult = actPickUp( (HeroAction.PickUp)curAction );
				
			} else if (curAction instanceof HeroAction.OpenChest) {
				actResult = actOpenChest( (HeroAction.OpenChest)curAction );
				
			} else if (curAction instanceof HeroAction.Unlock) {
				actResult = actUnlock((HeroAction.Unlock) curAction);
				
			} else if (curAction instanceof HeroAction.Descend) {
				actResult = actDescend( (HeroAction.Descend)curAction );

			} else if (curAction instanceof HeroAction.Transit) {
				actResult = actTransit( (HeroAction.Transit)curAction );
				
			} else if (curAction instanceof HeroAction.Ascend) {
				actResult = actAscend( (HeroAction.Ascend)curAction );
				
			} else if (curAction instanceof HeroAction.Attack) {
				actResult = actAttack( (HeroAction.Attack)curAction );
				
			} else if (curAction instanceof HeroAction.Alchemy) {
				actResult = actAlchemy( (HeroAction.Alchemy)curAction );
				
			} else {
				actResult = false;
			}
		}
		
		return actResult;
	}
	
	public void busy() {
		ready = false;
	}
	
	private void ready() {
		if (sprite.looping()) sprite.idle();
		curAction = null;
		damageInterrupt = true;
		ready = true;

		AttackIndicator.updateState();
		
		GameScene.ready();
	}
	
	public void interrupt() {
		if (isAlive() && curAction != null &&
			((curAction instanceof HeroAction.Move && curAction.dst != pos) ||
			(curAction instanceof HeroAction.Ascend || curAction instanceof HeroAction.Descend))) {
			lastAction = curAction;
		}
		curAction = null;
		GameScene.resetKeyHold();
	}
	
	public void resume() {
		curAction = lastAction;
		lastAction = null;
		damageInterrupt = false;
		next();
	}
	
	private boolean actMove( HeroAction.Move action ) {

		if (getCloser( action.dst )) {
			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actInteract( HeroAction.Interact action ) {
		
		com.quasistellar.hollowdungeon.actors.Char ch = action.ch;

		if (ch.canInteract(this)) {
			
			ready();
			sprite.turnTo( pos, ch.pos );
			return ch.interact(this);
			
		} else {
			
			if (fieldOfView[ch.pos] && getCloser( ch.pos )) {

				return true;

			} else {
				ready();
				return false;
			}
			
		}
	}
	
	private boolean actBuy( HeroAction.Buy action ) {
		int dst = action.dst;
		if (pos == dst) {

			ready();
			
			com.quasistellar.hollowdungeon.items.Heap heap = com.quasistellar.hollowdungeon.Dungeon.level.heaps.get( dst );
			if (heap != null && heap.type == com.quasistellar.hollowdungeon.items.Heap.Type.FOR_SALE && heap.size() == 1) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndTradeItem( heap ) );
					}
				});
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actAlchemy( HeroAction.Alchemy action ) {
		int dst = action.dst;
		if (com.quasistellar.hollowdungeon.Dungeon.level.distance(dst, pos) <= 1) {

			ready();
			
			com.quasistellar.hollowdungeon.actors.blobs.Alchemy alch = (com.quasistellar.hollowdungeon.actors.blobs.Alchemy) com.quasistellar.hollowdungeon.Dungeon.level.blobs.get(Alchemy.class);
			//TODO logic for a well having dried up?
			if (alch != null) {
				alch.alchPos = dst;
				AlchemyScene.setProvider( alch );
			}
			com.quasistellar.hollowdungeon.ShatteredPixelDungeon.switchScene(com.quasistellar.hollowdungeon.scenes.AlchemyScene.class);
			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actPickUp( HeroAction.PickUp action ) {
		int dst = action.dst;
		if (pos == dst) {
			
			com.quasistellar.hollowdungeon.items.Heap heap = com.quasistellar.hollowdungeon.Dungeon.level.heaps.get( pos );
			if (heap != null) {
				com.quasistellar.hollowdungeon.items.Item item = heap.peek();
				if (item.doPickUp( this )) {
					heap.pickUp();

					if (item instanceof com.quasistellar.hollowdungeon.items.Dewdrop || item instanceof Key) {
						//Do Nothing
					}
					
					curAction = null;
				} else {

					if (item instanceof com.quasistellar.hollowdungeon.items.Dewdrop || item instanceof Key) {
						//Do Nothing
					} else {
						//TODO temporary until 0.8.0a, when all languages will get this phrase
						if (Messages.lang() == Languages.ENGLISH) {
							GLog.newLine();
							GLog.n(Messages.get(this, "you_cant_have", item.name()));
						}
					}

					heap.sprite.drop();
					ready();
				}
			} else {
				ready();
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actOpenChest( HeroAction.OpenChest action ) {
		int dst = action.dst;
		if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent( pos, dst ) || pos == dst) {
			
			com.quasistellar.hollowdungeon.items.Heap heap = com.quasistellar.hollowdungeon.Dungeon.level.heaps.get( dst );
			if (heap != null && (heap.type != com.quasistellar.hollowdungeon.items.Heap.Type.HEAP && heap.type != com.quasistellar.hollowdungeon.items.Heap.Type.FOR_SALE)) {
				
				if ((heap.type == com.quasistellar.hollowdungeon.items.Heap.Type.LOCKED_CHEST && Notes.keyCount(new GoldenKey(Dungeon.location)) < 1)
					|| (heap.type == com.quasistellar.hollowdungeon.items.Heap.Type.CRYSTAL_CHEST && Notes.keyCount(new CrystalKey(Dungeon.location)) < 1)){

						GLog.w( Messages.get(this, "locked_chest") );
						ready();
						return false;

				}
				
				switch (heap.type) {
				case TOMB:
					Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.TOMB );
					Camera.main.shake( 1, 0.5f );
					break;
				case SKELETON:
				case REMAINS:
					break;
				default:
					Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.UNLOCK );
				}
				
				sprite.operate( dst );
				
			} else {
				ready();
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actUnlock( HeroAction.Unlock action ) {
		int doorCell = action.dst;
		if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent( pos, doorCell )) {
			
			boolean hasKey = false;
			int door = com.quasistellar.hollowdungeon.Dungeon.level.map[doorCell];
			
			if (door == Terrain.LOCKED_DOOR
					&& Notes.keyCount(new IronKey(Dungeon.location)) > 0) {
				
				hasKey = true;
				
			} else if (door == Terrain.LOCKED_EXIT
					&& Notes.keyCount(new SkeletonKey(Dungeon.location)) > 0) {

				hasKey = true;
				
			}
			
			if (hasKey) {
				
				sprite.operate( doorCell );
				
				Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.UNLOCK );
				
			} else {
				GLog.w( Messages.get(this, "locked_door") );
				ready();
			}

			return false;

		} else if (getCloser( doorCell )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actDescend( HeroAction.Descend action ) {
		int stairs = action.dst;

		if (rooted) {
			Camera.main.shake(1, 1f);
			ready();
			return false;
		//there can be multiple exit tiles, so descend on any of them
		//TODO this is slightly brittle, it assumes there are no disjointed sets of exit tiles
		} else if ((com.quasistellar.hollowdungeon.Dungeon.level.map[pos] == Terrain.EXIT)) {
			
			curAction = null;

			com.quasistellar.hollowdungeon.actors.buffs.Buff buff = com.quasistellar.hollowdungeon.Dungeon.hero.buff(com.quasistellar.hollowdungeon.plants.Swiftthistle.TimeBubble.class);
			if (buff != null) buff.detach();
			
			InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
			Game.switchScene( com.quasistellar.hollowdungeon.scenes.InterlevelScene.class );

			return false;

		} else if (getCloser( stairs )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actTransit( HeroAction.Transit action ) {
		int stairs = action.dst;

		if (rooted) {
			Camera.main.shake(1, 1f);
			ready();
			return false;
			//there can be multiple exit tiles, so descend on any of them
			//TODO this is slightly brittle, it assumes there are no disjointed sets of exit tiles
		} else if ((com.quasistellar.hollowdungeon.Dungeon.level.map[pos] == Terrain.UNLOCKED_EXIT)) {

			curAction = null;

			com.quasistellar.hollowdungeon.actors.buffs.Buff buff = com.quasistellar.hollowdungeon.Dungeon.hero.buff(com.quasistellar.hollowdungeon.plants.Swiftthistle.TimeBubble.class);
			if (buff != null) buff.detach();

			InterlevelScene.mode = InterlevelScene.Mode.TRANSIT;
			Game.switchScene( com.quasistellar.hollowdungeon.scenes.InterlevelScene.class );

			return false;

		} else if (getCloser( stairs )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actAscend( HeroAction.Ascend action ) {
		int stairs = action.dst;


		if (rooted){
			Camera.main.shake( 1, 1f );
			ready();
			return false;
		//there can be multiple entrance tiles, so descend on any of them
		//TODO this is slightly brittle, it assumes there are no disjointed sets of entrance tiles
		} else if (com.quasistellar.hollowdungeon.Dungeon.level.map[pos] == Terrain.ENTRANCE) {
			
//			if (com.quasistellar.hollowdungeon.Dungeon.depth == 1) {
//
//				if (belongings.getItem( com.quasistellar.hollowdungeon.items.Amulet.class ) == null) {
//					Game.runOnRenderThread(new Callback() {
//						@Override
//						public void call() {
//							GameScene.show( new WndMessage( Messages.get(Hero.this, "leave") ) );
//						}
//					});
//					ready();
//				} else {
//					com.quasistellar.hollowdungeon.Badges.silentValidateHappyEnd();
//					com.quasistellar.hollowdungeon.Dungeon.win( com.quasistellar.hollowdungeon.items.Amulet.class );
//					com.quasistellar.hollowdungeon.Dungeon.deleteGame( com.quasistellar.hollowdungeon.GamesInProgress.curSlot, true );
//					Game.switchScene( SurfaceScene.class );
//				}
//
//			} else {
				
				curAction = null;

				Buff buff = com.quasistellar.hollowdungeon.Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
				if (buff != null) buff.detach();

				InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
				Game.switchScene( com.quasistellar.hollowdungeon.scenes.InterlevelScene.class );
//			}

			return false;

		} else if (getCloser( stairs )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actAttack( HeroAction.Attack action ) {

		enemy = action.target;

		if (enemy.isAlive() && canAttack( enemy ) && !isCharmedBy( enemy )) {
			
			sprite.attack( enemy.pos );

			return false;

		} else {

			if (fieldOfView[enemy.pos] && getCloser( enemy.pos )) {

				return true;

			} else {
				ready();
				return false;
			}

		}
	}

	public com.quasistellar.hollowdungeon.actors.Char enemy(){
		return enemy;
	}
	
	public void rest( boolean fullRest ) {
		spendAndNext( TIME_TO_REST );
		if (!fullRest) {
			sprite.showStatus( CharSprite.DEFAULT, Messages.get(this, "wait") );
		}
		resting = fullRest;
	}

	public static class Invulnerable extends FlavourBuff {
		//does nothing
	}

	@Override
	public void damage( int dmg, Object src ) {

		if (this.buff(Invulnerable.class) != null)
			return;

		if (damageInterrupt) {
			interrupt();
			resting = false;
		}

		if (this.buff(com.quasistellar.hollowdungeon.actors.buffs.Drowsy.class) != null){
			com.quasistellar.hollowdungeon.actors.buffs.Buff.detach(this, com.quasistellar.hollowdungeon.actors.buffs.Drowsy.class);
			GLog.w( Messages.get(this, "pain_resist") );
		}

		int preHP = HP + shielding();
		super.damage( dmg, src );
		Buff.prolong(this, Invulnerable.class, 1.9f);
		int postHP = HP + shielding();
		int effectiveDamage = preHP - postHP;

		//flash red when hit for serious damage.
		float percentDMG = effectiveDamage / (float)preHP; //percent of current HP that was taken
		float percentHP = 1 - ((HT - postHP) / (float)HT); //percent health after damage was taken
		// The flash intensity increases primarily based on damage taken and secondarily on missing HP.
		float flashIntensity = 0.25f * (percentDMG * percentDMG) / percentHP;
		//if the intensity is very low don't flash at all
		if (flashIntensity >= 0.05f){
			flashIntensity = Math.min(1/3f, flashIntensity); //cap intensity at 1/3
			GameScene.flash( (int)(0xFF*flashIntensity) << 16 );
			if (isAlive()) {
				if (flashIntensity >= 1/6f) {
					Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.HEALTH_CRITICAL, 1/3f + flashIntensity * 2f);
				} else {
					Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.HEALTH_WARN, 1/3f + flashIntensity * 4f);
				}
			}
		}

		HpIndicator.refreshHero();
	}
	
	public void checkVisibleMobs() {
		ArrayList<com.quasistellar.hollowdungeon.actors.mobs.Mob> visible = new ArrayList<>();

		boolean newMob = false;

		com.quasistellar.hollowdungeon.actors.mobs.Mob target = null;
		for (com.quasistellar.hollowdungeon.actors.mobs.Mob m : com.quasistellar.hollowdungeon.Dungeon.level.mobs.toArray(new com.quasistellar.hollowdungeon.actors.mobs.Mob[0])) {
			if (fieldOfView[ m.pos ] && m.alignment == Alignment.ENEMY) {
				visible.add(m);
				if (!visibleEnemies.contains( m )) {
					newMob = true;
				}

				if (!mindVisionEnemies.contains(m) && QuickSlotButton.autoAim(m) != -1){
					if (target == null){
						target = m;
					} else if (distance(target) > distance(m)) {
						target = m;
					}
				}
			}
		}

		com.quasistellar.hollowdungeon.actors.Char lastTarget = QuickSlotButton.lastTarget;
		if (target != null && (lastTarget == null ||
							!lastTarget.isAlive() ||
							!fieldOfView[lastTarget.pos])){
			com.quasistellar.hollowdungeon.ui.QuickSlotButton.target(target);
		}
		
		if (newMob) {
			interrupt();
			if (resting){
				com.quasistellar.hollowdungeon.Dungeon.observe();
				resting = false;
			}
		}

		visibleEnemies = visible;
	}
	
	public int visibleEnemies() {
		return visibleEnemies.size();
	}
	
	public com.quasistellar.hollowdungeon.actors.mobs.Mob visibleEnemy(int index ) {
		return visibleEnemies.get(index % visibleEnemies.size());
	}
	
	private boolean walkingToVisibleTrapInFog = false;
	
	//FIXME this is a fairly crude way to track this, really it would be nice to have a short
	//history of hero actions
	public boolean justMoved = false;
	
	private boolean getCloser( final int target ) {

		if (target == pos)
			return false;

		if (rooted) {
			Camera.main.shake( 1, 1f );
			return false;
		}
		
		int step = -1;
		
		if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent( pos, target )) {

			path = null;

			if (Actor.findChar( target ) == null) {
				if (com.quasistellar.hollowdungeon.Dungeon.level.pit[target] && !flying && !com.quasistellar.hollowdungeon.Dungeon.level.solid[target]) {
					if (!Chasm.jumpConfirmed){
						Chasm.heroJump(this);
						interrupt();
					} else {
						com.quasistellar.hollowdungeon.levels.features.Chasm.heroFall(target);
					}
					return false;
				}
				if (com.quasistellar.hollowdungeon.Dungeon.level.passable[target] || com.quasistellar.hollowdungeon.Dungeon.level.avoid[target]) {
					step = target;
				}
				if (walkingToVisibleTrapInFog
						&& com.quasistellar.hollowdungeon.Dungeon.level.traps.get(target) != null
						&& com.quasistellar.hollowdungeon.Dungeon.level.traps.get(target).visible){
					return false;
				}
			}
			
		} else {

			boolean newPath = false;
			if (path == null || path.isEmpty() || !com.quasistellar.hollowdungeon.Dungeon.level.adjacent(pos, path.getFirst()))
				newPath = true;
			else if (path.getLast() != target)
				newPath = true;
			else {
				if (!com.quasistellar.hollowdungeon.Dungeon.level.passable[path.get(0)] || Actor.findChar(path.get(0)) != null) {
					newPath = true;
				}
			}

			if (newPath) {

				int len = com.quasistellar.hollowdungeon.Dungeon.level.length();
				boolean[] p = com.quasistellar.hollowdungeon.Dungeon.level.passable;
				boolean[] v = com.quasistellar.hollowdungeon.Dungeon.level.visited;
				boolean[] m = com.quasistellar.hollowdungeon.Dungeon.level.mapped;
				boolean[] passable = new boolean[len];
				for (int i = 0; i < len; i++) {
					passable[i] = p[i] && (v[i] || m[i]);
				}

				PathFinder.Path newpath = com.quasistellar.hollowdungeon.Dungeon.findPath(this, target, passable, fieldOfView, true);
				if (newpath != null && path != null && newpath.size() > 2*path.size()){
					path = null;
				} else {
					path = newpath;
				}
			}

			if (path == null) return false;
			step = path.removeFirst();

		}

		if (step != -1) {
			
			float speed = speed();
			
			sprite.move(pos, step);
			move(step);

			spend( 1 / speed );
			justMoved = true;
			
			search(false);

			//FIXME this is a fairly sloppy fix for a crash involving pitfall traps.
			//really there should be a way for traps to specify whether action should continue or
			//not when they are pressed.
			return InterlevelScene.mode != com.quasistellar.hollowdungeon.scenes.InterlevelScene.Mode.FALL;

		} else {

			return false;
			
		}

	}
	
	public boolean handle( int cell ) {
		
		if (cell == -1) {
			return false;
		}
		
		com.quasistellar.hollowdungeon.actors.Char ch;
		com.quasistellar.hollowdungeon.items.Heap heap;
		
		if (com.quasistellar.hollowdungeon.Dungeon.level.map[cell] == Terrain.ALCHEMY && cell != pos) {
			
			curAction = new HeroAction.Alchemy( cell );
			
		} else if (fieldOfView[cell] && (ch = Actor.findChar( cell )) instanceof Mob) {

			if (ch.alignment != Alignment.ENEMY && ch.buff(com.quasistellar.hollowdungeon.actors.buffs.Amok.class) == null) {
				curAction = new HeroAction.Interact( ch );
			} else {
				curAction = new HeroAction.Attack( ch );
			}

		} else if ((heap = com.quasistellar.hollowdungeon.Dungeon.level.heaps.get( cell )) != null
				//moving to an item doesn't auto-pickup when enemies are near...
				&& (visibleEnemies.size() == 0 || cell == pos ||
				//...but only for standard heaps, chests and similar open as normal.
				(heap.type != com.quasistellar.hollowdungeon.items.Heap.Type.HEAP && heap.type != com.quasistellar.hollowdungeon.items.Heap.Type.FOR_SALE))) {

			switch (heap.type) {
			case HEAP:
				curAction = new HeroAction.PickUp( cell );
				break;
			case FOR_SALE:
				curAction = heap.size() == 1 && heap.peek().price() > 0 ?
					new HeroAction.Buy( cell ) :
					new HeroAction.PickUp( cell );
				break;
			default:
				curAction = new HeroAction.OpenChest( cell );
			}
			
		} else if (com.quasistellar.hollowdungeon.Dungeon.level.map[cell] == Terrain.LOCKED_DOOR || com.quasistellar.hollowdungeon.Dungeon.level.map[cell] == Terrain.LOCKED_EXIT) {
			
			curAction = new HeroAction.Unlock( cell );
			
		} else if ((cell == com.quasistellar.hollowdungeon.Dungeon.level.exit || com.quasistellar.hollowdungeon.Dungeon.level.map[cell] == Terrain.EXIT)) {
			
			curAction = new HeroAction.Descend( cell );
			
		} else if (cell == com.quasistellar.hollowdungeon.Dungeon.level.entrance || com.quasistellar.hollowdungeon.Dungeon.level.map[cell] == Terrain.ENTRANCE) {
			
			curAction = new HeroAction.Ascend( cell );
			
		} else if (cell == Dungeon.level.transition || com.quasistellar.hollowdungeon.Dungeon.level.map[cell] == Terrain.UNLOCKED_EXIT) {

			curAction = new HeroAction.Transit( cell );

		} else  {
			
			if (!com.quasistellar.hollowdungeon.Dungeon.level.visited[cell] && !com.quasistellar.hollowdungeon.Dungeon.level.mapped[cell]
					&& com.quasistellar.hollowdungeon.Dungeon.level.traps.get(cell) != null && com.quasistellar.hollowdungeon.Dungeon.level.traps.get(cell).visible) {
				walkingToVisibleTrapInFog = true;
			} else {
				walkingToVisibleTrapInFog = false;
			}
			
			curAction = new HeroAction.Move( cell );
			lastAction = null;
			
		}

		return true;
	}

	public void earnMana( int mana ) {
		this.MP = Math.max(Math.min(this.MP + mana, this.MM), 0);
	}

	public void loseMana( int mana ) {
		earnMana(-mana);
	}

	@Override
	public void add( Buff buff ) {

		super.add( buff );

		if (sprite != null) {
			String msg = buff.heroMessage();
			if (msg != null){
				GLog.w(msg);
			}

			if (buff instanceof com.quasistellar.hollowdungeon.actors.buffs.Paralysis || buff instanceof com.quasistellar.hollowdungeon.actors.buffs.Vertigo) {
				interrupt();
			}

		}
		
		BuffIndicator.refreshHero();
	}
	
	@Override
	public void remove( com.quasistellar.hollowdungeon.actors.buffs.Buff buff ) {
		super.remove( buff );

		com.quasistellar.hollowdungeon.ui.BuffIndicator.refreshHero();
	}
	
	@Override
	public void die( Object cause  ) {
		
		curAction = null;

		com.quasistellar.hollowdungeon.items.Ankh ankh = null;

		//look for ankhs in player inventory, prioritize ones which are blessed.
		for (com.quasistellar.hollowdungeon.items.Item item : belongings){
			if (item instanceof com.quasistellar.hollowdungeon.items.Ankh) {
				if (ankh == null || ((com.quasistellar.hollowdungeon.items.Ankh) item).isBlessed()) {
					ankh = (com.quasistellar.hollowdungeon.items.Ankh) item;
				}
			}
		}
		
		com.quasistellar.hollowdungeon.actors.Actor.fixTime();
		super.die( cause );

		if (ankh == null) {
			
			reallyDie( cause );
			
		}
	}
	
	public static void reallyDie( Object cause ) {
		
		int length = com.quasistellar.hollowdungeon.Dungeon.level.length();
		int[] map = com.quasistellar.hollowdungeon.Dungeon.level.map;
		boolean[] visited = com.quasistellar.hollowdungeon.Dungeon.level.visited;
		boolean[] discoverable = com.quasistellar.hollowdungeon.Dungeon.level.discoverable;
		
		for (int i=0; i < length; i++) {
			
			int terr = map[i];
			
			if (discoverable[i]) {
				
				visited[i] = true;
				if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {
					com.quasistellar.hollowdungeon.Dungeon.level.discover( i );
				}
			}
		}
		
		com.quasistellar.hollowdungeon.Dungeon.observe();
		GameScene.updateFog();
				
		com.quasistellar.hollowdungeon.Dungeon.hero.belongings.identify();

		int pos = com.quasistellar.hollowdungeon.Dungeon.hero.pos;

		ArrayList<Integer> passable = new ArrayList<>();
		for (Integer ofs : PathFinder.NEIGHBOURS8) {
			int cell = pos + ofs;
			if ((com.quasistellar.hollowdungeon.Dungeon.level.passable[cell] || com.quasistellar.hollowdungeon.Dungeon.level.avoid[cell]) && com.quasistellar.hollowdungeon.Dungeon.level.heaps.get( cell ) == null) {
				passable.add( cell );
			}
		}
		Collections.shuffle( passable );

		GameScene.gameOver();
		
		if (cause instanceof Hero.Doom) {
			((Hero.Doom)cause).onDeath();
		}
		
		com.quasistellar.hollowdungeon.Dungeon.deleteGame( com.quasistellar.hollowdungeon.GamesInProgress.curSlot, true );
	}

	@Override
	public void move( int step ) {
		boolean wasHighGrass = com.quasistellar.hollowdungeon.Dungeon.level.map[step] == Terrain.HIGH_GRASS;

		super.move( step );
		
		if (!flying) {
			if (com.quasistellar.hollowdungeon.Dungeon.level.water[pos]) {
				Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.WATER, 1, Random.Float( 0.8f, 1.25f ) );
			} else if (com.quasistellar.hollowdungeon.Dungeon.level.map[pos] == Terrain.EMPTY_SP) {
				Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.STURDY, 1, Random.Float( 0.96f, 1.05f ) );
			} else if (com.quasistellar.hollowdungeon.Dungeon.level.map[pos] == Terrain.GRASS
					|| com.quasistellar.hollowdungeon.Dungeon.level.map[pos] == Terrain.EMBERS
					|| com.quasistellar.hollowdungeon.Dungeon.level.map[pos] == Terrain.FURROWED_GRASS){
				if (step == pos && wasHighGrass) {
					Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.TRAMPLE, 1, Random.Float( 0.96f, 1.05f ) );
				} else {
					Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.GRASS, 1, Random.Float( 0.96f, 1.05f ) );
				}
			} else {
				Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.STEP, 1, Random.Float( 0.96f, 1.05f ) );
			}
		}
	}
	
	@Override
	public void onAttackComplete() {
		
		com.quasistellar.hollowdungeon.ui.AttackIndicator.target(enemy);
		
		boolean hit = attack( enemy );

		//trace a ballistica to our target (which will also extend past them
		Ballistica trajectory = new Ballistica(this.pos, enemy.pos, Ballistica.STOP_TARGET);
		//trim it to just be the part that goes past them
		trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
		//knock them back along that ballistica
		Utils.throwChar(enemy, trajectory, 1);
		
		Invisibility.dispel();
		spend( attackDelay() );

		curAction = null;

		super.onAttackComplete();
	}
	
	@Override
	public void onMotionComplete() {
		GameScene.checkKeyHold();
	}
	
	@Override
	public void onOperateComplete() {
		
		if (curAction instanceof HeroAction.Unlock) {

			int doorCell = ((HeroAction.Unlock)curAction).dst;
			int door = com.quasistellar.hollowdungeon.Dungeon.level.map[doorCell];
			
			if (com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, doorCell) <= 1) {
				boolean hasKey = true;
				if (door == Terrain.LOCKED_DOOR) {
					hasKey = Notes.remove(new IronKey(Dungeon.location));
					if (hasKey) Level.set(doorCell, Terrain.DOOR);
				} else {
					hasKey = Notes.remove(new SkeletonKey(Dungeon.location));
					if (hasKey) Level.set(doorCell, Terrain.UNLOCKED_EXIT);
				}
				
				if (hasKey) {
					com.quasistellar.hollowdungeon.levels.Level.set(doorCell, door == Terrain.LOCKED_DOOR ? Terrain.DOOR : Terrain.UNLOCKED_EXIT);
					GameScene.updateMap(doorCell);
					spend(Key.TIME_TO_UNLOCK);
				}
			}
			
		} else if (curAction instanceof HeroAction.OpenChest) {
			
			com.quasistellar.hollowdungeon.items.Heap heap = com.quasistellar.hollowdungeon.Dungeon.level.heaps.get( ((HeroAction.OpenChest)curAction).dst );
			
			if (com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, heap.pos) <= 1){
				boolean hasKey = true;
				if (heap.type == com.quasistellar.hollowdungeon.items.Heap.Type.SKELETON || heap.type == com.quasistellar.hollowdungeon.items.Heap.Type.REMAINS) {
					Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.BONES );
				} else if (heap.type == com.quasistellar.hollowdungeon.items.Heap.Type.LOCKED_CHEST){
					hasKey = Notes.remove(new GoldenKey(Dungeon.location));
				} else if (heap.type == com.quasistellar.hollowdungeon.items.Heap.Type.CRYSTAL_CHEST){
					hasKey = com.quasistellar.hollowdungeon.journal.Notes.remove(new CrystalKey(Dungeon.location));
				}
				
				if (hasKey) {
					heap.open(this);
					spend(Key.TIME_TO_UNLOCK);
				}
			}
			
		}
		curAction = null;

		super.onOperateComplete();
	}

	public boolean search( boolean intentional ) {
		
		if (!isAlive()) return false;
		
		boolean smthFound = false;

		int distance = heroClass == HeroClass.HORNET ? 2 : 1;
		
		boolean foresight = buff(com.quasistellar.hollowdungeon.actors.buffs.Foresight.class) != null;
		
		if (foresight) distance++;
		
		int cx = pos % com.quasistellar.hollowdungeon.Dungeon.level.width();
		int cy = pos / com.quasistellar.hollowdungeon.Dungeon.level.width();
		int ax = cx - distance;
		if (ax < 0) {
			ax = 0;
		}
		int bx = cx + distance;
		if (bx >= com.quasistellar.hollowdungeon.Dungeon.level.width()) {
			bx = com.quasistellar.hollowdungeon.Dungeon.level.width() - 1;
		}
		int ay = cy - distance;
		if (ay < 0) {
			ay = 0;
		}
		int by = cy + distance;
		if (by >= com.quasistellar.hollowdungeon.Dungeon.level.height()) {
			by = com.quasistellar.hollowdungeon.Dungeon.level.height() - 1;
		}
		
		for (int y = ay; y <= by; y++) {
			for (int x = ax, p = ax + y * com.quasistellar.hollowdungeon.Dungeon.level.width(); x <= bx; x++, p++) {
				
				if (fieldOfView[p] && p != pos) {
					
					if (intentional) {
						GameScene.effectOverFog(new CheckedCell(p, pos));
					}
					
					if (com.quasistellar.hollowdungeon.Dungeon.level.secret[p]){
						
						Trap trap = com.quasistellar.hollowdungeon.Dungeon.level.traps.get( p );
						float chance;

						//searches aided by foresight always succeed, even if trap isn't searchable
						if (foresight){
							chance = 1f;

						//otherwise if the trap isn't searchable, searching always fails
						} else if (trap != null && !trap.canBeSearched){
							chance = 0f;

						//intentional searches always succeed against regular traps and doors
						} else if (intentional){
							chance = 1f;
							
						//unintentional trap detection scales from 40% at floor 0 to 30% at floor 25
						} else if (com.quasistellar.hollowdungeon.Dungeon.level.map[p] == Terrain.SECRET_TRAP) {
							chance = 0.4f - (10 / 250f); //TODO: do smth with it
							
						//unintentional door detection scales from 20% at floor 0 to 0% at floor 20
						} else {
							chance = 0.2f - (10 / 100f);
						}
						
						if (Random.Float() < chance) {
						
							int oldValue = com.quasistellar.hollowdungeon.Dungeon.level.map[p];
							
							com.quasistellar.hollowdungeon.scenes.GameScene.discoverTile( p, oldValue );
							
							com.quasistellar.hollowdungeon.Dungeon.level.discover( p );

							CellEmitter.get( p ).start( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.DISCOVER ), 0.1f, 4 );
							
							smthFound = true;
						}
					}
				}
			}
		}

		
		if (intentional) {
			sprite.showStatus( com.quasistellar.hollowdungeon.sprites.CharSprite.DEFAULT, Messages.get(this, "search") );
			sprite.operate( pos );
			spendAndNext(TIME_TO_SEARCH);
			
		}
		
		if (smthFound) {
			com.quasistellar.hollowdungeon.utils.GLog.w( Messages.get(this, "noticed_smth") );
			Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.SECRET );
			interrupt();
		}
		
		return smthFound;
	}
	
	public void resurrect( int resetLevel ) {
		
		HP = HT;
		com.quasistellar.hollowdungeon.Dungeon.geo = 0;
		
		//belongings.resurrect( resetLevel );

		live();
	}

	@Override
	public void next() {
		if (isAlive())
			super.next();
	}

	public static interface Doom {
		public void onDeath();
	}
}
