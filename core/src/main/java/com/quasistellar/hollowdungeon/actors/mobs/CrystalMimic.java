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

package com.quasistellar.hollowdungeon.actors.mobs;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.buffs.Corruption;
import com.quasistellar.hollowdungeon.actors.buffs.Haste;
import com.quasistellar.hollowdungeon.actors.buffs.Terror;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Honeypot;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.MimicSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.artifacts.Artifact;
import com.quasistellar.hollowdungeon.items.rings.Ring;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.hollowdungeon.items.wands.Wand;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CrystalMimic extends Mimic {

	{
		spriteClass = MimicSprite.Crystal.class;

		FLEEING = new Fleeing();
	}

	@Override
	public String name() {
		if (alignment == Char.Alignment.NEUTRAL){
			return Messages.get(com.quasistellar.hollowdungeon.items.Heap.class, "crystal_chest");
		} else {
			return super.name();
		}
	}

	@Override
	public String description() {
		if (alignment == Char.Alignment.NEUTRAL){
			String desc = null;
			for (com.quasistellar.hollowdungeon.items.Item i : items){
				if (i instanceof Artifact){
					desc = Messages.get(com.quasistellar.hollowdungeon.items.Heap.class, "crystal_chest_desc", Messages.get(com.quasistellar.hollowdungeon.items.Heap.class, "artifact"));
					break;
				} else if (i instanceof Ring){
					desc = Messages.get(com.quasistellar.hollowdungeon.items.Heap.class, "crystal_chest_desc", Messages.get(com.quasistellar.hollowdungeon.items.Heap.class, "ring"));
					break;
				} else if (i instanceof Wand){
					desc = Messages.get(com.quasistellar.hollowdungeon.items.Heap.class, "crystal_chest_desc", Messages.get(com.quasistellar.hollowdungeon.items.Heap.class, "wand"));
					break;
				}
			}
			if (desc == null) {
				desc = Messages.get(Heap.class, "locked_chest_desc");
			}
			return desc + "\n\n" + Messages.get(this, "hidden_hint");
		} else {
			return super.description();
		}
	}

	//does not deal bonus damage, steals instead. See attackProc
	@Override
	public int damageRoll() {
		if (alignment == Char.Alignment.NEUTRAL) {
			alignment = Char.Alignment.ENEMY;
			int dmg = super.damageRoll();
			alignment = Char.Alignment.NEUTRAL;
			return dmg;
		} else {
			return super.damageRoll();
		}
	}

	public void stopHiding(){
		state = FLEEING;
		//haste for 2 turns if attacking
		if (alignment == Char.Alignment.NEUTRAL){
			Buff.affect(this, com.quasistellar.hollowdungeon.actors.buffs.Haste.class, 2f);
		} else {
			com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(this, Haste.class, 1f);
		}
		if (Actor.chars().contains(this) && Dungeon.level.heroFOV[pos]) {
			enemy = Dungeon.hero;
			target = Dungeon.hero.pos;
			enemySeen = true;
			GLog.w(Messages.get(this, "reveal") );
			CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
			Sample.INSTANCE.play(Assets.Sounds.MIMIC, 1, 1.25f);
		}
	}

	@Override
	public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage) {
		if (alignment == com.quasistellar.hollowdungeon.actors.Char.Alignment.NEUTRAL && enemy == Dungeon.hero){
			steal( Dungeon.hero );

		} else {
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.passable[pos+i] && com.quasistellar.hollowdungeon.actors.Actor.findChar(pos+i) == null){
					candidates.add(pos + i);
				}
			}

			if (!candidates.isEmpty()){
				ScrollOfTeleportation.appear(enemy, Random.element(candidates));
			}

			if (alignment == com.quasistellar.hollowdungeon.actors.Char.Alignment.ENEMY) state = FLEEING;
		}
		return super.attackProc(enemy, damage);
	}

	protected void steal( Hero hero ) {

		int tries = 10;
		com.quasistellar.hollowdungeon.items.Item item;
		do {
			item = hero.belongings.randomUnequipped();
		} while (tries-- > 0 && (item == null || item.unique || item.level() > 0));

		if (item != null && !item.unique && item.level() < 1 ) {

			GLog.w( Messages.get(this, "ate", item.name()) );
			if (!item.stackable) {
				Dungeon.quickslot.convertToPlaceholder(item);
			}
			item.updateQuickslot();

			if (item instanceof com.quasistellar.hollowdungeon.items.Honeypot){
				items.add(((com.quasistellar.hollowdungeon.items.Honeypot)item).shatter(this, this.pos));
				item.detach( hero.belongings.backpack );
			} else {
				items.add(item.detach( hero.belongings.backpack ));
				if ( item instanceof com.quasistellar.hollowdungeon.items.Honeypot.ShatteredPot)
					((Honeypot.ShatteredPot)item).pickupPot(this);
			}

		}
	}

	@Override
	protected void generatePrize() {
		//Crystal mimic already contains a prize item. Just guarantee it isn't cursed.
		for (Item i : items){
			i.cursed = false;
			i.cursedKnown = true;
		}
	}

	private class Fleeing extends Mob.Fleeing{
		@Override
		protected void nowhereToRun() {
			if (buff( Terror.class ) == null && buff( Corruption.class ) == null) {
				if (enemySeen) {
					sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Mob.class, "rage"));
					state = HUNTING;
				} else {
					com.quasistellar.hollowdungeon.utils.GLog.n( Messages.get(CrystalMimic.class, "escaped"));
					if (com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[pos]) com.quasistellar.hollowdungeon.effects.CellEmitter.get(pos).burst(Speck.factory(com.quasistellar.hollowdungeon.effects.Speck.WOOL), 6);
					destroy();
					sprite.killAndErase();
				}
			} else {
				super.nowhereToRun();
			}
		}
	}

}
