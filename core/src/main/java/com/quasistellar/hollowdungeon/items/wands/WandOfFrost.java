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

package com.quasistellar.hollowdungeon.items.wands;

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Chill;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.actors.buffs.Frost;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.effects.MagicMissile;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfFrost extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_FROST;
	}

	public int min(int lvl){
		return 2+lvl;
	}

	public int max(int lvl){
		return 8+5*lvl;
	}

	@Override
	protected void onZap(Ballistica bolt) {

		Heap heap = Dungeon.level.heaps.get(bolt.collisionPos);
		if (heap != null) {
			heap.freeze();
		}

		com.quasistellar.hollowdungeon.actors.Char ch = com.quasistellar.hollowdungeon.actors.Actor.findChar(bolt.collisionPos);
		if (ch != null){

			int damage = damageRoll();

			if (ch.buff(com.quasistellar.hollowdungeon.actors.buffs.Frost.class) != null){
				return; //do nothing, can't affect a frozen target
			}
			if (ch.buff(com.quasistellar.hollowdungeon.actors.buffs.Chill.class) != null){
				//5% less damage per turn of chill remaining
				damage = (int)Math.round(damage * Math.pow(0.95f, ch.buff(com.quasistellar.hollowdungeon.actors.buffs.Chill.class).cooldown()));
			} else {
				ch.sprite.burst( 0xFF99CCFF, buffedLvl() / 2 + 2 );
			}

			processSoulMark(ch, chargesPerCast());
			ch.damage(damage, this);
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.1f * Random.Float(0.87f, 1.15f) );

			if (ch.isAlive()){
				if (Dungeon.level.water[ch.pos])
					Buff.affect(ch, com.quasistellar.hollowdungeon.actors.buffs.Chill.class, 4+buffedLvl());
				else
					Buff.affect(ch, com.quasistellar.hollowdungeon.actors.buffs.Chill.class, 2+buffedLvl());
			}
		} else {
			com.quasistellar.hollowdungeon.Dungeon.level.pressCell(bolt.collisionPos);
		}
	}

	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(com.quasistellar.hollowdungeon.items.Item.curUser.sprite.parent,
				com.quasistellar.hollowdungeon.effects.MagicMissile.FROST,
				Item.curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.ZAP);
	}

	@Override
	public void onHit(com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff staff, com.quasistellar.hollowdungeon.actors.Char attacker, Char defender, int damage) {
		com.quasistellar.hollowdungeon.actors.buffs.Chill chill = defender.buff(com.quasistellar.hollowdungeon.actors.buffs.Chill.class);
		if (chill != null && chill.cooldown() >= Chill.DURATION){
			//need to delay this through an actor so that the freezing isn't broken by taking damage from the staff hit.
			new FlavourBuff(){
				{actPriority = Actor.VFX_PRIO;}
				public boolean act() {
					com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(target, com.quasistellar.hollowdungeon.actors.buffs.Frost.class, Frost.DURATION);
					return super.act();
				}
			}.attachTo(defender);
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0x88CCFF);
		particle.am = 0.6f;
		particle.setLifespan(2f);
		float angle = Random.Float(PointF.PI2);
		particle.speed.polar( angle, 2f);
		particle.acc.set( 0f, 1f);
		particle.setSize( 0f, 1.5f);
		particle.radiateXY(Random.Float(1f));
	}

}
