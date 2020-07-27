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

package com.quasistellar.hollowdungeon.items.artifacts;

import com.quasistellar.hollowdungeon.actors.blobs.CorrosiveGas;
import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.actors.buffs.Burning;
import com.quasistellar.hollowdungeon.actors.buffs.Corruption;
import com.quasistellar.hollowdungeon.actors.buffs.LockedFloor;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.actors.mobs.Wraith;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.NPC;
import com.quasistellar.hollowdungeon.effects.particles.ShaftParticle;
import com.quasistellar.hollowdungeon.items.EquipableItem;
import com.quasistellar.hollowdungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.sprites.GhostSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Window;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.ShatteredPixelDungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Ghost;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.ui.BossHealthBar;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRetribution;
import com.quasistellar.hollowdungeon.items.weapon.Weapon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DriedRose extends com.quasistellar.hollowdungeon.items.artifacts.Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_ROSE1;

		levelCap = 10;

		charge = 100;
		chargeCap = 100;

		defaultAction = AC_SUMMON;
	}

	private boolean talkedTo = false;
	private boolean firstSummon = false;
	
	private GhostHero ghost = null;
	private int ghostID = 0;
	
	private com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon weapon = null;

	public int droppedPetals = 0;

	public static final String AC_SUMMON = "SUMMON";
	public static final String AC_DIRECT = "DIRECT";
	public static final String AC_OUTFIT = "OUTFIT";

	@Override
	public ArrayList<String> actions( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (!Ghost.Quest.completed()){
			actions.remove(EquipableItem.AC_EQUIP);
			return actions;
		}
		if (isEquipped( hero ) && charge == chargeCap && !cursed && ghostID == 0) {
			actions.add(AC_SUMMON);
		}
		if (ghostID != 0){
			actions.add(AC_DIRECT);
		}
		if (isIdentified() && !cursed){
			actions.add(AC_OUTFIT);
		}
		
		return actions;
	}

	@Override
	public void execute(com.quasistellar.hollowdungeon.actors.hero.Hero hero, String action ) {

		super.execute(hero, action);

		if (action.equals(AC_SUMMON)) {

			if (!Ghost.Quest.completed())   GameScene.show(new com.quasistellar.hollowdungeon.windows.WndUseItem(null, this));
			else if (ghost != null)         GLog.i( Messages.get(this, "spawned") );
			else if (!isEquipped( hero ))   GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (charge != chargeCap)   GLog.i( Messages.get(this, "no_charge") );
			else if (cursed)                GLog.i( Messages.get(this, "cursed") );
			else {
				ArrayList<Integer> spawnPoints = new ArrayList<>();
				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
					int p = hero.pos + PathFinder.NEIGHBOURS8[i];
					if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
						spawnPoints.add(p);
					}
				}

				if (spawnPoints.size() > 0) {
					ghost = new GhostHero( this );
					ghostID = ghost.id();
					ghost.pos = Random.element(spawnPoints);

					GameScene.add(ghost, 1f);
					Dungeon.level.occupyCell(ghost);
					
					CellEmitter.get(ghost.pos).start( ShaftParticle.FACTORY, 0.3f, 4 );
					com.quasistellar.hollowdungeon.effects.CellEmitter.get(ghost.pos).start( Speck.factory(com.quasistellar.hollowdungeon.effects.Speck.LIGHT), 0.2f, 3 );

					hero.spend(1f);
					hero.busy();
					hero.sprite.operate(hero.pos);

					if (!firstSummon) {
						ghost.yell( Messages.get(GhostHero.class, "hello", Dungeon.hero.name()) );
						Sample.INSTANCE.play( Assets.Sounds.GHOST );
						firstSummon = true;
						
					} else {
						if (BossHealthBar.isAssigned()) {
							ghost.sayBoss();
						} else {
							ghost.sayAppeared();
						}
					}
					
					charge = 0;
					partialCharge = 0;
					Item.updateQuickslot();

				} else
					GLog.i( Messages.get(this, "no_space") );
			}

		} else if (action.equals(AC_DIRECT)){
			if (ghost == null && ghostID != 0){
				com.quasistellar.hollowdungeon.actors.Actor a = Actor.findById(ghostID);
				if (a != null){
					ghost = (GhostHero)a;
				} else {
					ghostID = 0;
				}
			}
			if (ghost != null) GameScene.selectCell(ghostDirector);
			
		} else if (action.equals(AC_OUTFIT)){
			GameScene.show( new WndGhostHero(this) );
		}
	}
	
	public int ghostStrength(){
		return 13 + level()/2;
	}

	@Override
	public String desc() {
		if (!com.quasistellar.hollowdungeon.actors.mobs.npcs.Ghost.Quest.completed() && !isIdentified()){
			return Messages.get(this, "desc_no_quest");
		}
		
		String desc = super.desc();

		if (isEquipped( Dungeon.hero )){
			if (!cursed){

				if (level() < levelCap)
					desc+= "\n\n" + Messages.get(this, "desc_hint");

			} else {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			}
		}

		if (weapon != null) {
			desc += "\n";

			if (weapon != null) {
				desc += "\n" + Messages.get(this, "desc_weapon", weapon.toString());
			}
		}
		
		return desc;
	}
	
	@Override
	public String status() {
		if (ghost == null && ghostID != 0){
			try {
				com.quasistellar.hollowdungeon.actors.Actor a = Actor.findById(ghostID);
				if (a != null) {
					ghost = (GhostHero) a;
				} else {
					ghostID = 0;
				}
			} catch ( ClassCastException e ){
				ShatteredPixelDungeon.reportException(e);
				ghostID = 0;
			}
		}
		if (ghost == null){
			return super.status();
		} else {
			return (int)((ghost.HP+partialCharge)*100) / ghost.HT + "%";
		}
	}
	
	@Override
	protected ArtifactBuff passiveBuff() {
		return new roseRecharge();
	}
	
	@Override
	public void charge(com.quasistellar.hollowdungeon.actors.hero.Hero target) {
		if (ghost == null){
			if (charge < chargeCap) {
				charge += 4;
				if (charge >= chargeCap) {
					charge = chargeCap;
					partialCharge = 0;
					GLog.p(Messages.get(DriedRose.class, "charged"));
				}
				Item.updateQuickslot();
			}
		} else {
			ghost.HP = Math.min( ghost.HT, ghost.HP + 1 + level()/3);
			Item.updateQuickslot();
		}
	}
	
	@Override
	public com.quasistellar.hollowdungeon.items.Item upgrade() {
		if (level() >= 9)
			image = ItemSpriteSheet.ARTIFACT_ROSE3;
		else if (level() >= 4)
			image = ItemSpriteSheet.ARTIFACT_ROSE2;

		//For upgrade transferring via well of transmutation
		droppedPetals = Math.max( level(), droppedPetals );
		
		if (ghost != null){
			ghost.updateRose();
		}

		return super.upgrade();
	}
	
	public Weapon ghostWeapon(){
		return weapon;
	}

	private static final String TALKEDTO =      "talkedto";
	private static final String FIRSTSUMMON =   "firstsummon";
	private static final String GHOSTID =       "ghostID";
	private static final String PETALS =        "petals";
	
	private static final String WEAPON =        "weapon";
	private static final String ARMOR =         "armor";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);

		bundle.put( TALKEDTO, talkedTo );
		bundle.put( FIRSTSUMMON, firstSummon );
		bundle.put( GHOSTID, ghostID );
		bundle.put( PETALS, droppedPetals );
		
		if (weapon != null) bundle.put( WEAPON, weapon );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);

		talkedTo = bundle.getBoolean( TALKEDTO );
		firstSummon = bundle.getBoolean( FIRSTSUMMON );
		ghostID = bundle.getInt( GHOSTID );
		droppedPetals = bundle.getInt( PETALS );
		
		if (ghostID != 0) defaultAction = AC_DIRECT;
		
		if (bundle.contains(WEAPON)) weapon = (com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon)bundle.get( WEAPON );
	}

	public class roseRecharge extends ArtifactBuff {

		@Override
		public boolean act() {
			
			spend( Actor.TICK );
			
			if (ghost == null && ghostID != 0){
				com.quasistellar.hollowdungeon.actors.Actor a = Actor.findById(ghostID);
				if (a != null){
					ghost = (GhostHero)a;
				} else {
					ghostID = 0;
				}
			}
			
			//rose does not charge while ghost hero is alive
			if (ghost != null){
				defaultAction = AC_DIRECT;
				
				//heals to full over 1000 turns
				if (ghost.HP < ghost.HT) {
					partialCharge += ghost.HT / 1000f;
					Item.updateQuickslot();
					
					if (partialCharge > 1) {
						ghost.HP++;
						partialCharge--;
					}
				} else {
					partialCharge = 0;
				}
				
				return true;
			} else {
				defaultAction = AC_SUMMON;
			}
			
			com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = target.buff(LockedFloor.class);
			if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
				partialCharge += 1/5f; //500 turns to a full charge
				if (partialCharge > 1){
					charge++;
					partialCharge--;
					if (charge == chargeCap){
						partialCharge = 0f;
						GLog.p( Messages.get(DriedRose.class, "charged") );
					}
				}
			} else if (cursed && Random.Int(100) == 0) {

				ArrayList<Integer> spawnPoints = new ArrayList<>();

				for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
					int p = target.pos + PathFinder.NEIGHBOURS8[i];
					if (Actor.findChar(p) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
						spawnPoints.add(p);
					}
				}

				if (spawnPoints.size() > 0) {
					Wraith.spawnAt(Random.element(spawnPoints));
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
				}

			}

			Item.updateQuickslot();

			return true;
		}
	}
	
	public com.quasistellar.hollowdungeon.scenes.CellSelector.Listener ghostDirector = new CellSelector.Listener(){
		
		@Override
		public void onSelect(Integer cell) {
			if (cell == null) return;
			
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
			
			if (!Dungeon.level.heroFOV[cell]
					|| Actor.findChar(cell) == null
					|| (Actor.findChar(cell) != Dungeon.hero && Actor.findChar(cell).alignment != Char.Alignment.ENEMY)){
				ghost.yell(Messages.get(ghost, "directed_position_" + Random.IntRange(1, 5)));
				ghost.aggro(null);
				ghost.state = ghost.WANDERING;
				ghost.defendingPos = cell;
				ghost.movingToDefendPos = true;
				return;
			}
			
			if (ghost.fieldOfView == null || ghost.fieldOfView.length != Dungeon.level.length()){
				ghost.fieldOfView = new boolean[Dungeon.level.length()];
			}
			Dungeon.level.updateFieldOfView( ghost, ghost.fieldOfView );
			
			if (Actor.findChar(cell) == Dungeon.hero){
				ghost.yell(Messages.get(ghost, "directed_follow_" + Random.IntRange(1, 5)));
				ghost.aggro(null);
				ghost.state = ghost.WANDERING;
				ghost.defendingPos = -1;
				ghost.movingToDefendPos = false;
				
			} else if (Actor.findChar(cell).alignment == Char.Alignment.ENEMY){
				ghost.yell(Messages.get(ghost, "directed_attack_" + Random.IntRange(1, 5)));
				ghost.aggro(com.quasistellar.hollowdungeon.actors.Actor.findChar(cell));
				ghost.setTarget(cell);
				ghost.movingToDefendPos = false;
				
			}
		}
		
		@Override
		public String prompt() {
			return  "\"" + Messages.get(GhostHero.class, "direct_prompt") + "\"";
		}
	};

	public static class Petal extends com.quasistellar.hollowdungeon.items.Item {

		{
			stackable = true;
			dropsDownHeap = true;
			
			image = ItemSpriteSheet.PETAL;
		}

		@Override
		public boolean doPickUp( Hero hero ) {
			DriedRose rose = hero.belongings.getItem( DriedRose.class );

			if (rose == null){
				GLog.w( Messages.get(this, "no_rose") );
				return false;
			} if ( rose.level() >= rose.levelCap ){
				GLog.i( Messages.get(this, "no_room") );
				hero.spendAndNext(TIME_TO_PICK_UP);
				return true;
			} else {

				rose.upgrade();
				if (rose.level() == rose.levelCap) {
					GLog.p( Messages.get(this, "maxlevel") );
				} else
					GLog.i( Messages.get(this, "levelup") );

				Sample.INSTANCE.play( Assets.Sounds.DEWDROP );
				hero.spendAndNext(TIME_TO_PICK_UP);
				return true;

			}
		}

	}

	public static class GhostHero extends NPC {

		{
			spriteClass = GhostSprite.class;

			flying = true;

			alignment = Char.Alignment.ALLY;
			intelligentAlly = true;
			WANDERING = new Wandering();
			
			state = HUNTING;
			
			//before other mobs
			actPriority = com.quasistellar.hollowdungeon.actors.Actor.MOB_PRIO + 1;
			
			properties.add(Char.Property.UNDEAD);
		}
		
		private DriedRose rose = null;
		
		public GhostHero(){
			super();
		}

		public GhostHero(DriedRose rose){
			super();
			this.rose = rose;
			updateRose();
			HP = HT;
		}
		
		private void updateRose(){
			if (rose == null) {
				rose = Dungeon.hero.belongings.getItem(DriedRose.class);
			}
			
			//same dodge as the hero
			if (rose == null) return;
			HT = 20 + 8*rose.level();
		}
		
		private int defendingPos = -1;
		private boolean movingToDefendPos = false;
		
		public void clearDefensingPos(){
			defendingPos = -1;
			movingToDefendPos = false;
		}

		@Override
		protected boolean act() {
			updateRose();
			if (rose == null || !rose.isEquipped(Dungeon.hero)){
				damage(1, this);
			}
			
			if (!isAlive())
				return true;
			if (!Dungeon.hero.isAlive()){
				sayHeroKilled();
				sprite.die();
				destroy();
				return true;
			}
			return super.act();
		}
		
		@Override
		protected com.quasistellar.hollowdungeon.actors.Char chooseEnemy() {
			com.quasistellar.hollowdungeon.actors.Char enemy = super.chooseEnemy();
			
			int targetPos = defendingPos != -1 ? defendingPos : Dungeon.hero.pos;
			
			//will never attack something far from their target
			if (enemy != null
					&& Dungeon.level.mobs.contains(enemy)
					&& (Dungeon.level.distance(enemy.pos, targetPos) <= 8)){
				return enemy;
			}
			
			return null;
		}
		
		@Override
		protected float attackDelay() {
			float delay = super.attackDelay();
			if (rose != null && rose.weapon != null){
				delay *= rose.weapon.speedFactor(this);
			}
			return delay;
		}
		
		@Override
		protected boolean canAttack(com.quasistellar.hollowdungeon.actors.Char enemy) {
			return super.canAttack(enemy) || (rose != null && rose.weapon != null && rose.weapon.canReach(this, enemy.pos));
		}
		
		@Override
		public int damageRoll() {
			int dmg = 0;
			if (rose != null && rose.weapon != null){
				dmg += rose.weapon.damageRoll(this);
			} else {
				dmg += Random.NormalIntRange(0, 5);
			}
			
			return dmg;
		}
		
		@Override
		public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage) {
			damage = super.attackProc(enemy, damage);
			if (rose != null && rose.weapon != null) {
				damage = rose.weapon.proc( this, enemy, damage );
				if (!enemy.isAlive() && enemy == Dungeon.hero){
					Dungeon.fail(getClass());
					GLog.n( Messages.capitalize(Messages.get(com.quasistellar.hollowdungeon.actors.Char.class, "kill", name())) );
				}
			}
			return damage;
		}
		
		@Override
		public void damage(int dmg, Object src) {
			super.damage( dmg, src );

			//for the rose status indicator
			Item.updateQuickslot();
		}

		private void setTarget(int cell) {
			target = cell;
		}

		@Override
		public boolean interact(com.quasistellar.hollowdungeon.actors.Char c) {
			updateRose();
			if (c == Dungeon.hero && rose != null && !rose.talkedTo){
				rose.talkedTo = true;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show(new com.quasistellar.hollowdungeon.windows.WndQuest(GhostHero.this, Messages.get(GhostHero.this, "introduce") ));
					}
				});
				return true;
			} else {
				return super.interact(c);
			}
		}

		@Override
		public void die(Object cause) {
			sayDefeated();
			super.die(cause);
		}

		@Override
		public void destroy() {
			updateRose();
			if (rose != null) {
				rose.ghost = null;
				rose.charge = 0;
				rose.partialCharge = 0;
				rose.ghostID = -1;
				rose.defaultAction = AC_SUMMON;
			}
			super.destroy();
		}
		
		public void sayAppeared(){
			int depth = (Dungeon.depth - 1) / 5;
			
			//only some lines are said on the first floor of a depth
			int variant = Dungeon.depth % 5 == 1 ? Random.IntRange(1, 3) : Random.IntRange(1, 6);
			
			switch(depth){
				case 0:
					yell( Messages.get( this, "dialogue_sewers_" + variant ));
					break;
				case 1:
					yell( Messages.get( this, "dialogue_prison_" + variant ));
					break;
				case 2:
					yell( Messages.get( this, "dialogue_caves_" + variant ));
					break;
				case 3:
					yell( Messages.get( this, "dialogue_city_" + variant ));
					break;
				case 4: default:
					yell( Messages.get( this, "dialogue_halls_" + variant ));
					break;
			}
			if (com.quasistellar.hollowdungeon.ShatteredPixelDungeon.scene() instanceof com.quasistellar.hollowdungeon.scenes.GameScene) {
				Sample.INSTANCE.play( Assets.Sounds.GHOST );
			}
		}
		
		public void sayBoss(){
			int depth = (Dungeon.depth - 1) / 5;
			
			switch(depth){
				case 0:
					yell( Messages.get( this, "seen_goo_" + Random.IntRange(1, 3) ));
					break;
				case 1:
					yell( Messages.get( this, "seen_tengu_" + Random.IntRange(1, 3) ));
					break;
				case 2:
					yell( Messages.get( this, "seen_dm300_" + Random.IntRange(1, 3) ));
					break;
				case 3:
					yell( Messages.get( this, "seen_king_" + Random.IntRange(1, 3) ));
					break;
				case 4: default:
					yell( Messages.get( this, "seen_yog_" + Random.IntRange(1, 3) ));
					break;
			}
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayDefeated(){
			if (com.quasistellar.hollowdungeon.ui.BossHealthBar.isAssigned()){
				yell( Messages.get( this, "defeated_by_boss_" + Random.IntRange(1, 3) ));
			} else {
				yell( Messages.get( this, "defeated_by_enemy_" + Random.IntRange(1, 3) ));
			}
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayHeroKilled(){
			if (Dungeon.bossLevel()){
				yell( Messages.get( this, "hero_killed_boss_" + Random.IntRange(1, 3) ));
			} else {
				yell( Messages.get( this, "hero_killed_" + Random.IntRange(1, 3) ));
			}
			Sample.INSTANCE.play( Assets.Sounds.GHOST );
		}
		
		public void sayAnhk(){
			yell( Messages.get( this, "blessed_ankh_" + Random.IntRange(1, 3) ));
			Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.GHOST );
		}
		
		private static final String DEFEND_POS = "defend_pos";
		private static final String MOVING_TO_DEFEND = "moving_to_defend";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(DEFEND_POS, defendingPos);
			bundle.put(MOVING_TO_DEFEND, movingToDefendPos);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			if (bundle.contains(DEFEND_POS)) defendingPos = bundle.getInt(DEFEND_POS);
			movingToDefendPos = bundle.getBoolean(MOVING_TO_DEFEND);
		}
		
		{
			immunities.add( ToxicGas.class );
			immunities.add( CorrosiveGas.class );
			immunities.add( Burning.class );
			immunities.add( ScrollOfRetribution.class );
			immunities.add( ScrollOfPsionicBlast.class );
			immunities.add( Corruption.class );
		}
		
		private class Wandering extends Mob.Wandering {
			
			@Override
			public boolean act( boolean enemyInFOV, boolean justAlerted ) {
				if ( enemyInFOV && !movingToDefendPos ) {
					
					enemySeen = true;
					
					notice();
					alerted = true;
					state = HUNTING;
					target = enemy.pos;
					
				} else {
					
					enemySeen = false;
					
					int oldPos = pos;
					target = defendingPos != -1 ? defendingPos : Dungeon.hero.pos;
					//always move towards the hero when wandering
					if (getCloser( target )) {
						//moves 2 tiles at a time when returning to the hero
						if (defendingPos == -1 && !Dungeon.level.adjacent(target, pos)){
							getCloser( target );
						}
						spend( 1 / speed() );
						if (pos == defendingPos) movingToDefendPos = false;
						return moveSprite( oldPos, pos );
					} else {
						spend( com.quasistellar.hollowdungeon.actors.Actor.TICK );
					}
					
				}
				return true;
			}
			
		}

	}
	
	private static class WndGhostHero extends Window {
		
		private static final int BTN_SIZE	= 32;
		private static final float GAP		= 2;
		private static final float BTN_GAP	= 12;
		private static final int WIDTH		= 116;
		
		private com.quasistellar.hollowdungeon.windows.WndBlacksmith.ItemButton btnWeapon;
		private com.quasistellar.hollowdungeon.windows.WndBlacksmith.ItemButton btnArmor;
		
		WndGhostHero(final DriedRose rose){
			
			com.quasistellar.hollowdungeon.windows.IconTitle titlebar = new com.quasistellar.hollowdungeon.windows.IconTitle();
			titlebar.icon( new ItemSprite(rose) );
			titlebar.label( Messages.get(this, "title") );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );
			
			RenderedTextBlock message =
					PixelScene.renderTextBlock(Messages.get(this, "desc", rose.ghostStrength()), 6);
			message.maxWidth( WIDTH );
			message.setPos(0, titlebar.bottom() + GAP);
			add( message );
			
			btnWeapon = new com.quasistellar.hollowdungeon.windows.WndBlacksmith.ItemButton(){
				@Override
				protected void onClick() {
					if (rose.weapon != null){
						item(new com.quasistellar.hollowdungeon.windows.WndBag.Placeholder(ItemSpriteSheet.WEAPON_HOLDER));
						if (!rose.weapon.doPickUp(Dungeon.hero)){
							Dungeon.level.drop( rose.weapon, Dungeon.hero.pos);
						}
						rose.weapon = null;
					} else {
						GameScene.selectItem(new com.quasistellar.hollowdungeon.windows.WndBag.Listener() {
							@Override
							public void onSelect(com.quasistellar.hollowdungeon.items.Item item) {
								if (!(item instanceof com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon)) {
									//do nothing, should only happen when window is cancelled
								} else if (item.unique) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_unique"));
									hide();
								} else if (!item.isIdentified()) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_unidentified"));
									hide();
								} else if (item.cursed) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_cursed"));
									hide();
								} else if (((com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon)item).STRReq() > rose.ghostStrength()) {
									GLog.w( Messages.get(WndGhostHero.class, "cant_strength"));
									hide();
								} else {
									if (item.isEquipped(Dungeon.hero)){
										((com.quasistellar.hollowdungeon.items.weapon.melee.MeleeWeapon) item).doUnequip(Dungeon.hero, false, false);
									} else {
										item.detach(Dungeon.hero.belongings.backpack);
									}
									rose.weapon = (MeleeWeapon) item;
									item(rose.weapon);
								}
								
							}
						}, com.quasistellar.hollowdungeon.windows.WndBag.Mode.WEAPON, Messages.get(WndGhostHero.class, "weapon_prompt"));
					}
				}
			};
			btnWeapon.setRect( (WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.top() + message.height() + GAP, BTN_SIZE, BTN_SIZE );
			if (rose.weapon != null) {
				btnWeapon.item(rose.weapon);
			} else {
				btnWeapon.item(new com.quasistellar.hollowdungeon.windows.WndBag.Placeholder(ItemSpriteSheet.WEAPON_HOLDER));
			}
			add( btnWeapon );
			
			resize(WIDTH, (int)(btnArmor.bottom() + GAP));
		}
	
	}
}
