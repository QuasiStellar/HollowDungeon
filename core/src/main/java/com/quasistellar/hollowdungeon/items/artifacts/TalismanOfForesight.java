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

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.actors.buffs.LockedFloor;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.effects.CheckedCell;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfMagicMapping;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.ConeAOE;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class TalismanOfForesight extends com.quasistellar.hollowdungeon.items.artifacts.Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_TALISMAN;

		exp = 0;
		levelCap = 10;

		charge = 0;
		partialCharge = 0;
		chargeCap = 100;

		defaultAction = AC_SCRY;
	}

	public static final String AC_SCRY = "SCRY";

	@Override
	public ArrayList<String> actions( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && !cursed) actions.add(AC_SCRY);
		return actions;
	}

	@Override
	public void execute(com.quasistellar.hollowdungeon.actors.hero.Hero hero, String action ) {
		super.execute(hero, action);

		if (action.equals(AC_SCRY)){
			if (!isEquipped(hero))  GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (charge < 5)    GLog.i( Messages.get(this, "low_charge") );
			else                    GameScene.selectCell(scry);
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new Foresight();
	}
	
	@Override
	public void charge(com.quasistellar.hollowdungeon.actors.hero.Hero target) {
		if (charge < chargeCap){
			charge += 2f;
			if (charge >= chargeCap) {
				charge = chargeCap;
				partialCharge = 0;
				GLog.p( Messages.get(Foresight.class, "full_charge") );
			}
		}
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if ( isEquipped( Dungeon.hero ) ){
			if (!cursed) {
				desc += "\n\n" + Messages.get(this, "desc_worn");

			} else {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			}
		}

		return desc;
	}

	private float maxDist(){
		return Math.min(5 + 2*level(), (charge-3)/1.08f);
	}

	private com.quasistellar.hollowdungeon.scenes.CellSelector.Listener scry = new CellSelector.Listener(){

		@Override
		public void onSelect(Integer target) {
			if (target != null && target != com.quasistellar.hollowdungeon.items.Item.curUser.pos){

				//enforces at least 2 tiles of distance
				if (Dungeon.level.adjacent(target, com.quasistellar.hollowdungeon.items.Item.curUser.pos)){
					target += (target - com.quasistellar.hollowdungeon.items.Item.curUser.pos);
				}

				float dist = Dungeon.level.trueDistance(com.quasistellar.hollowdungeon.items.Item.curUser.pos, target);

				if (dist >= 3 && dist > maxDist()){
					Ballistica trajectory = new Ballistica(com.quasistellar.hollowdungeon.items.Item.curUser.pos, target, Ballistica.STOP_TARGET);
					int i = 0;
					while (i < trajectory.path.size()
							&& Dungeon.level.trueDistance(com.quasistellar.hollowdungeon.items.Item.curUser.pos, trajectory.path.get(i)) <= maxDist()){
						target = trajectory.path.get(i);
						i++;
					}
					dist = Dungeon.level.trueDistance(com.quasistellar.hollowdungeon.items.Item.curUser.pos, target);
				}

				//starts at 200 degrees, loses 8% per tile of distance
				float angle = Math.round(200*(float)Math.pow(0.92, dist));
				ConeAOE cone = new ConeAOE(com.quasistellar.hollowdungeon.items.Item.curUser.pos, target, angle);

				int earnedExp = 0;
				boolean noticed = false;
				for (int cell : cone.cells){
					GameScene.effectOverFog(new CheckedCell( cell, com.quasistellar.hollowdungeon.items.Item.curUser.pos ));
					if (Dungeon.level.discoverable[cell] && !(Dungeon.level.mapped[cell] || Dungeon.level.visited[cell])){
						Dungeon.level.mapped[cell] = true;
						earnedExp++;
					}

					if (Dungeon.level.secret[cell]) {
						Dungeon.level.discover(cell);

						if (Dungeon.level.heroFOV[cell]) {
							int oldValue = Dungeon.level.map[cell];
							GameScene.discoverTile(cell, Dungeon.level.map[cell]);
							Dungeon.level.discover( cell );
							ScrollOfMagicMapping.discover(cell);
							noticed = true;

							if (oldValue == Terrain.SECRET_TRAP){
								earnedExp += 10;
							} else if (oldValue == Terrain.SECRET_DOOR){
								earnedExp += 100;
							}
						}
					}

					com.quasistellar.hollowdungeon.actors.Char ch = com.quasistellar.hollowdungeon.actors.Actor.findChar(cell);
					if (ch != null && ch.alignment != Char.Alignment.NEUTRAL && ch.alignment != com.quasistellar.hollowdungeon.items.Item.curUser.alignment){
						Buff.append(com.quasistellar.hollowdungeon.items.Item.curUser, CharAwareness.class, 5 + 2*level()).charID = ch.id();

						if (!com.quasistellar.hollowdungeon.items.Item.curUser.fieldOfView[ch.pos]){
							earnedExp += 10;
						}
					}

					Heap h = Dungeon.level.heaps.get(cell);
					if (h != null){
						com.quasistellar.hollowdungeon.actors.buffs.Buff.append(com.quasistellar.hollowdungeon.items.Item.curUser, HeapAwareness.class, 5 + 2*level()).pos = h.pos;

						if (!h.seen){
							earnedExp += 10;
						}
					}

				}

				exp += earnedExp;
				if (exp >= 50 + 50*level() && level() < levelCap) {
					exp -= 50 + 50*level();
					upgrade();
					GLog.p( Messages.get(TalismanOfForesight.class, "levelup") );
				}
				com.quasistellar.hollowdungeon.items.Item.updateQuickslot();

				//5 charge at 2 tiles, up to 30 charge at 25 tiles
				charge -= 3 + dist*1.08f;
				partialCharge -= (dist*1.08f)%1f;
				if (partialCharge < 0 && charge > 0){
					partialCharge ++;
					charge --;
				}
				com.quasistellar.hollowdungeon.items.Item.updateQuickslot();
				Dungeon.observe();
				Dungeon.hero.checkVisibleMobs();
				GameScene.updateFog();

				com.quasistellar.hollowdungeon.items.Item.curUser.sprite.zap(target);
				Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
				if (noticed) Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.SECRET);

			}

		}

		@Override
		public String prompt() {
			return Messages.get(TalismanOfForesight.class, "prompt");
		}
	};

	private static final String WARN = "warn";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(WARN, warn);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		warn = bundle.getBoolean(WARN);
	}
	
	private boolean warn = false;
	
	public class Foresight extends ArtifactBuff{

		@Override
		public boolean act() {
			spend( Actor.TICK );

			boolean smthFound = false;

			int distance = 3;

			int cx = target.pos % Dungeon.level.width();
			int cy = target.pos / Dungeon.level.width();
			int ax = cx - distance;
			if (ax < 0) {
				ax = 0;
			}
			int bx = cx + distance;
			if (bx >= Dungeon.level.width()) {
				bx = Dungeon.level.width() - 1;
			}
			int ay = cy - distance;
			if (ay < 0) {
				ay = 0;
			}
			int by = cy + distance;
			if (by >= Dungeon.level.height()) {
				by = Dungeon.level.height() - 1;
			}

			for (int y = ay; y <= by; y++) {
				for (int x = ax, p = ax + y * Dungeon.level.width(); x <= bx; x++, p++) {

					if (Dungeon.level.heroFOV[p]
							&& Dungeon.level.secret[p]
							&& Dungeon.level.map[p] != com.quasistellar.hollowdungeon.levels.Terrain.SECRET_DOOR) {
						if (Dungeon.level.traps.get(p) != null && Dungeon.level.traps.get(p).canBeSearched) {
							smthFound = true;
						}
					}
				}
			}

			if (smthFound && !cursed){
				if (!warn){
					GLog.w( Messages.get(this, "uneasy") );
					if (target instanceof com.quasistellar.hollowdungeon.actors.hero.Hero){
						((Hero)target).interrupt();
					}
					warn = true;
				}
			} else {
				warn = false;
			}

			//fully charges in 2000 turns at lvl=0, scaling to 1000 turns at lvl = 10.
			com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = target.buff(LockedFloor.class);
			if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
				partialCharge += 0.05f+(level()*0.005f);

				if (partialCharge > 1 && charge < chargeCap) {
					partialCharge--;
					charge++;
					Item.updateQuickslot();
				} else if (charge >= chargeCap) {
					partialCharge = 0;
					com.quasistellar.hollowdungeon.utils.GLog.p( Messages.get(TalismanOfForesight.class, "full_charge") );
				}
			}

			return true;
		}

		public void charge(int boost){
			charge = Math.min((charge+boost), chargeCap);
		}

		@Override
		public String toString() {
			return  Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc");
		}

		@Override
		public int icon() {
			if (warn)
				return BuffIndicator.FORESIGHT;
			else
				return com.quasistellar.hollowdungeon.ui.BuffIndicator.NONE;
		}
	}

	public static class CharAwareness extends com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff {

		public int charID;
		public int depth = Dungeon.depth;

		private static final String ID = "id";

		@Override
		public void detach() {
			super.detach();
			Dungeon.observe();
			GameScene.updateFog();
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			charID = bundle.getInt(ID);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(ID, charID);
		}

	}

	public static class HeapAwareness extends FlavourBuff {

		public int pos;
		public int depth = Dungeon.depth;

		private static final String POS = "pos";
		private static final String DEPTH = "depth";

		@Override
		public void detach() {
			super.detach();
			com.quasistellar.hollowdungeon.Dungeon.observe();
			com.quasistellar.hollowdungeon.scenes.GameScene.updateFog();
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			pos = bundle.getInt(POS);
			depth = bundle.getInt(DEPTH);
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(POS, pos);
			bundle.put(DEPTH, depth);
		}
	}

}
