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

import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.actors.buffs.LockedFloor;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.plants.Swiftthistle;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.MagicMissile;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.scenes.InterlevelScene;
import com.quasistellar.hollowdungeon.ui.QuickSlotButton;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class LloydsBeacon extends com.quasistellar.hollowdungeon.items.artifacts.Artifact {

	public static final float TIME_TO_USE = 1;

	public static final String AC_ZAP       = "ZAP";
	public static final String AC_SET		= "SET";
	public static final String AC_RETURN	= "RETURN";
	
	public int returnDepth	= -1;
	public int returnPos;
	
	{
		image = ItemSpriteSheet.ARTIFACT_BEACON;

		levelCap = 3;

		charge = 0;
		chargeCap = 3+level();

		defaultAction = AC_ZAP;
		usesTargeting = true;
	}
	
	private static final String DEPTH	= "depth";
	private static final String POS		= "pos";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DEPTH, returnDepth );
		if (returnDepth != -1) {
			bundle.put( POS, returnPos );
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		returnDepth	= bundle.getInt( DEPTH );
		returnPos	= bundle.getInt( POS );
	}
	
	@Override
	public ArrayList<String> actions( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_ZAP );
		actions.add( AC_SET );
		if (returnDepth != -1) {
			actions.add( AC_RETURN );
		}
		return actions;
	}
	
	@Override
	public void execute(com.quasistellar.hollowdungeon.actors.hero.Hero hero, String action ) {

		super.execute( hero, action );

		if (action == AC_SET || action == AC_RETURN) {
			
			if (Dungeon.bossLevel()) {
				hero.spend( LloydsBeacon.TIME_TO_USE );
				GLog.w( Messages.get(this, "preventing") );
				return;
			}
			
			for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
				com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar(hero.pos + PathFinder.NEIGHBOURS8[i]);
				if (ch != null && ch.alignment == Char.Alignment.ENEMY) {
					GLog.w( Messages.get(this, "creatures") );
					return;
				}
			}
		}

		if (action == AC_ZAP ){

			Item.curUser = hero;
			int chargesToUse = Dungeon.depth > 20 ? 2 : 1;

			if (!isEquipped( hero )) {
				GLog.i( Messages.get(Artifact.class, "need_to_equip") );
				QuickSlotButton.cancel();

			} else if (charge < chargesToUse) {
				GLog.i( Messages.get(this, "no_charge") );
				com.quasistellar.hollowdungeon.ui.QuickSlotButton.cancel();

			} else {
				GameScene.selectCell(zapper);
			}

		} else if (action == AC_SET) {
			
			returnDepth = Dungeon.depth;
			returnPos = hero.pos;
			
			hero.spend( LloydsBeacon.TIME_TO_USE );
			hero.busy();
			
			hero.sprite.operate( hero.pos );
			Sample.INSTANCE.play( Assets.Sounds.BEACON );
			
			GLog.i( Messages.get(this, "return") );
			
		} else if (action == AC_RETURN) {
			
			if (returnDepth == Dungeon.depth) {
				ScrollOfTeleportation.appear( hero, returnPos );
				for(com.quasistellar.hollowdungeon.actors.mobs.Mob m : Dungeon.level.mobs){
					if (m.pos == hero.pos){
						//displace mob
						for(int i : PathFinder.NEIGHBOURS8){
							if (Actor.findChar(m.pos+i) == null && Dungeon.level.passable[m.pos + i]){
								m.pos += i;
								m.sprite.point(m.sprite.worldToCamera(m.pos));
								break;
							}
						}
					}
				}
				Dungeon.level.occupyCell(hero );
				Dungeon.observe();
				com.quasistellar.hollowdungeon.scenes.GameScene.updateFog();
			} else {

				Buff buff = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
				if (buff != null) buff.detach();
				buff = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
				if (buff != null) buff.detach();

				InterlevelScene.mode = InterlevelScene.Mode.RETURN;
				InterlevelScene.returnDepth = returnDepth;
				InterlevelScene.returnPos = returnPos;
				Game.switchScene( com.quasistellar.hollowdungeon.scenes.InterlevelScene.class );
			}
			
			
		}
	}

	protected com.quasistellar.hollowdungeon.scenes.CellSelector.Listener zapper = new  CellSelector.Listener() {

		@Override
		public void onSelect(Integer target) {

			if (target == null) return;

			Invisibility.dispel();
			charge -= Dungeon.depth > 20 ? 2 : 1;
			Item.updateQuickslot();

			if (Actor.findChar(target) == Item.curUser){
				ScrollOfTeleportation.teleportHero(Item.curUser);
				Item.curUser.spendAndNext(1f);
			} else {
				final Ballistica bolt = new Ballistica( Item.curUser.pos, target, Ballistica.MAGIC_BOLT );
				final com.quasistellar.hollowdungeon.actors.Char ch = com.quasistellar.hollowdungeon.actors.Actor.findChar(bolt.collisionPos);

				if (ch == Item.curUser){
					ScrollOfTeleportation.teleportHero(Item.curUser);
					Item.curUser.spendAndNext( 1f );
				} else {
					Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.ZAP );
					Item.curUser.sprite.zap(bolt.collisionPos);
					Item.curUser.busy();

					MagicMissile.boltFromChar(Item.curUser.sprite.parent,
							com.quasistellar.hollowdungeon.effects.MagicMissile.BEACON,
							Item.curUser.sprite,
							bolt.collisionPos,
							new Callback() {
								@Override
								public void call() {
									if (ch != null) {

										int count = 10;
										int pos;
										do {
											pos = Dungeon.level.randomRespawnCell( ch );
											if (count-- <= 0) {
												break;
											}
										} while (pos == -1);

										if (pos == -1 || Dungeon.bossLevel()) {

											GLog.w( Messages.get(ScrollOfTeleportation.class, "no_tele") );

										} else if (ch.properties().contains(com.quasistellar.hollowdungeon.actors.Char.Property.IMMOVABLE)) {

											GLog.w( Messages.get(LloydsBeacon.class, "tele_fail") );

										} else  {

											ch.pos = pos;
											if (ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Mob && ((com.quasistellar.hollowdungeon.actors.mobs.Mob) ch).state == ((com.quasistellar.hollowdungeon.actors.mobs.Mob) ch).HUNTING){
												((com.quasistellar.hollowdungeon.actors.mobs.Mob) ch).state = ((Mob) ch).WANDERING;
											}
											ch.sprite.place(ch.pos);
											ch.sprite.visible = com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[pos];

										}
									}
									Item.curUser.spendAndNext(1f);
								}
							});

				}


			}

		}

		@Override
		public String prompt() {
			return Messages.get(LloydsBeacon.class, "prompt");
		}
	};

	@Override
	protected ArtifactBuff passiveBuff() {
		return new beaconRecharge();
	}
	
	@Override
	public void charge(Hero target) {
		if (charge < chargeCap){
			partialCharge += 0.25f;
			if (partialCharge >= 1){
				partialCharge--;
				charge++;
				Item.updateQuickslot();
			}
		}
	}

	@Override
	public com.quasistellar.hollowdungeon.items.Item upgrade() {
		if (level() == levelCap) return this;
		chargeCap ++;
		com.quasistellar.hollowdungeon.utils.GLog.p( Messages.get(this, "levelup") );
		return super.upgrade();
	}

	@Override
	public String desc() {
		String desc = super.desc();
		if (returnDepth != -1){
			desc += "\n\n" + Messages.get(this, "desc_set", returnDepth);
		}
		return desc;
	}
	
	private static final com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing WHITE = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0xFFFFFF );
	
	@Override
	public ItemSprite.Glowing glowing() {
		return returnDepth != -1 ? WHITE : null;
	}

	public class beaconRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
			com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = target.buff(LockedFloor.class);
			if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
				partialCharge += 1 / (100f - (chargeCap - charge)*10f);

				if (partialCharge >= 1) {
					partialCharge --;
					charge ++;

					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			}

			com.quasistellar.hollowdungeon.items.Item.updateQuickslot();
			spend( com.quasistellar.hollowdungeon.actors.Actor.TICK );
			return true;
		}
	}
}
