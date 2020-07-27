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

package com.quasistellar.shatteredpixeldungeon.actors.mobs;

import com.quasistellar.shatteredpixeldungeon.Assets;
import com.quasistellar.shatteredpixeldungeon.Dungeon;
import com.quasistellar.shatteredpixeldungeon.actors.Actor;
import com.quasistellar.shatteredpixeldungeon.actors.Char;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Corruption;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Haste;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Terror;
import com.quasistellar.shatteredpixeldungeon.effects.CellEmitter;
import com.quasistellar.shatteredpixeldungeon.effects.Speck;
import com.quasistellar.shatteredpixeldungeon.items.Heap;
import com.quasistellar.shatteredpixeldungeon.items.Honeypot;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.sprites.CharSprite;
import com.quasistellar.shatteredpixeldungeon.sprites.MimicSprite;
import com.quasistellar.shatteredpixeldungeon.utils.GLog;
import com.quasistellar.shatteredpixeldungeon.actors.hero.Hero;
import com.quasistellar.shatteredpixeldungeon.items.artifacts.Artifact;
import com.quasistellar.shatteredpixeldungeon.items.rings.Ring;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.shatteredpixeldungeon.items.wands.Wand;
import com.quasistellar.shatteredpixeldungeon.messages.Messages;
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
		if (alignment == com.quasistellar.shatteredpixeldungeon.actors.Char.Alignment.NEUTRAL){
			return Messages.get(com.quasistellar.shatteredpixeldungeon.items.Heap.class, "crystal_chest");
		} else {
			return super.name();
		}
	}

	@Override
	public String description() {
		if (alignment == com.quasistellar.shatteredpixeldungeon.actors.Char.Alignment.NEUTRAL){
			String desc = null;
			for (com.quasistellar.shatteredpixeldungeon.items.Item i : items){
				if (i instanceof Artifact){
					desc = Messages.get(com.quasistellar.shatteredpixeldungeon.items.Heap.class, "crystal_chest_desc", Messages.get(com.quasistellar.shatteredpixeldungeon.items.Heap.class, "artifact"));
					break;
				} else if (i instanceof Ring){
					desc = Messages.get(com.quasistellar.shatteredpixeldungeon.items.Heap.class, "crystal_chest_desc", Messages.get(com.quasistellar.shatteredpixeldungeon.items.Heap.class, "ring"));
					break;
				} else if (i instanceof Wand){
					desc = Messages.get(com.quasistellar.shatteredpixeldungeon.items.Heap.class, "crystal_chest_desc", Messages.get(com.quasistellar.shatteredpixeldungeon.items.Heap.class, "wand"));
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
		if (alignment == com.quasistellar.shatteredpixeldungeon.actors.Char.Alignment.NEUTRAL) {
			alignment = com.quasistellar.shatteredpixeldungeon.actors.Char.Alignment.ENEMY;
			int dmg = super.damageRoll();
			alignment = com.quasistellar.shatteredpixeldungeon.actors.Char.Alignment.NEUTRAL;
			return dmg;
		} else {
			return super.damageRoll();
		}
	}

	public void stopHiding(){
		state = FLEEING;
		//haste for 2 turns if attacking
		if (alignment == com.quasistellar.shatteredpixeldungeon.actors.Char.Alignment.NEUTRAL){
			com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.affect(this, com.quasistellar.shatteredpixeldungeon.actors.buffs.Haste.class, 2f);
		} else {
			Buff.affect(this, Haste.class, 1f);
		}
		if (com.quasistellar.shatteredpixeldungeon.actors.Actor.chars().contains(this) && com.quasistellar.shatteredpixeldungeon.Dungeon.level.heroFOV[pos]) {
			enemy = com.quasistellar.shatteredpixeldungeon.Dungeon.hero;
			target = com.quasistellar.shatteredpixeldungeon.Dungeon.hero.pos;
			enemySeen = true;
			com.quasistellar.shatteredpixeldungeon.utils.GLog.w(Messages.get(this, "reveal") );
			com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.get(pos).burst(com.quasistellar.shatteredpixeldungeon.effects.Speck.factory(com.quasistellar.shatteredpixeldungeon.effects.Speck.STAR), 10);
			Sample.INSTANCE.play(Assets.Sounds.MIMIC, 1, 1.25f);
		}
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		if (alignment == Char.Alignment.NEUTRAL && enemy == com.quasistellar.shatteredpixeldungeon.Dungeon.hero){
			steal( com.quasistellar.shatteredpixeldungeon.Dungeon.hero );

		} else {
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.passable[pos+i] && Actor.findChar(pos+i) == null){
					candidates.add(pos + i);
				}
			}

			if (!candidates.isEmpty()){
				ScrollOfTeleportation.appear(enemy, Random.element(candidates));
			}

			if (alignment == Char.Alignment.ENEMY) state = FLEEING;
		}
		return super.attackProc(enemy, damage);
	}

	protected void steal( Hero hero ) {

		int tries = 10;
		com.quasistellar.shatteredpixeldungeon.items.Item item;
		do {
			item = hero.belongings.randomUnequipped();
		} while (tries-- > 0 && (item == null || item.unique || item.level() > 0));

		if (item != null && !item.unique && item.level() < 1 ) {

			com.quasistellar.shatteredpixeldungeon.utils.GLog.w( Messages.get(this, "ate", item.name()) );
			if (!item.stackable) {
				com.quasistellar.shatteredpixeldungeon.Dungeon.quickslot.convertToPlaceholder(item);
			}
			item.updateQuickslot();

			if (item instanceof com.quasistellar.shatteredpixeldungeon.items.Honeypot){
				items.add(((com.quasistellar.shatteredpixeldungeon.items.Honeypot)item).shatter(this, this.pos));
				item.detach( hero.belongings.backpack );
			} else {
				items.add(item.detach( hero.belongings.backpack ));
				if ( item instanceof com.quasistellar.shatteredpixeldungeon.items.Honeypot.ShatteredPot)
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
					GLog.n( Messages.get(CrystalMimic.class, "escaped"));
					if (Dungeon.level.heroFOV[pos]) CellEmitter.get(pos).burst(com.quasistellar.shatteredpixeldungeon.effects.Speck.factory(Speck.WOOL), 6);
					destroy();
					sprite.killAndErase();
				}
			} else {
				super.nowhereToRun();
			}
		}
	}

}
