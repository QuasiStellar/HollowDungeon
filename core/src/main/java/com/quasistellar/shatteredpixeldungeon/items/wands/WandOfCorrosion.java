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

package com.quasistellar.shatteredpixeldungeon.items.wands;

import com.quasistellar.shatteredpixeldungeon.Assets;
import com.quasistellar.shatteredpixeldungeon.Dungeon;
import com.quasistellar.shatteredpixeldungeon.actors.Actor;
import com.quasistellar.shatteredpixeldungeon.actors.Char;
import com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob;
import com.quasistellar.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Ooze;
import com.quasistellar.shatteredpixeldungeon.effects.CellEmitter;
import com.quasistellar.shatteredpixeldungeon.effects.MagicMissile;
import com.quasistellar.shatteredpixeldungeon.effects.Speck;
import com.quasistellar.shatteredpixeldungeon.effects.particles.CorrosionParticle;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.shatteredpixeldungeon.scenes.GameScene;
import com.quasistellar.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.quasistellar.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class WandOfCorrosion extends Wand {

	{
		image = ItemSpriteSheet.WAND_CORROSION;

		collisionProperties = Ballistica.STOP_TARGET | Ballistica.STOP_TERRAIN;
	}

	@Override
	protected void onZap(Ballistica bolt) {
		com.quasistellar.shatteredpixeldungeon.actors.blobs.CorrosiveGas gas = Blob.seed(bolt.collisionPos, 50 + 10 * buffedLvl(), CorrosiveGas.class);
		com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.get(bolt.collisionPos).burst(com.quasistellar.shatteredpixeldungeon.effects.Speck.factory(Speck.CORROSION), 10 );
		gas.setStrength(2 + buffedLvl());
		GameScene.add(gas);
		Sample.INSTANCE.play(com.quasistellar.shatteredpixeldungeon.Assets.Sounds.GAS);

		for (int i : PathFinder.NEIGHBOURS9) {
			com.quasistellar.shatteredpixeldungeon.actors.Char ch = com.quasistellar.shatteredpixeldungeon.actors.Actor.findChar(bolt.collisionPos + i);
			if (ch != null) {
				processSoulMark(ch, chargesPerCast());
			}
		}
		
		if (Actor.findChar(bolt.collisionPos) == null){
			Dungeon.level.pressCell(bolt.collisionPos);
		}
	}

	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		com.quasistellar.shatteredpixeldungeon.effects.MagicMissile.boltFromChar(
				Item.curUser.sprite.parent,
				MagicMissile.CORROSION,
				Item.curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void onHit(com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff, com.quasistellar.shatteredpixeldungeon.actors.Char attacker, Char defender, int damage) {
		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		if (Random.Int( buffedLvl() + 3 ) >= 2) {
			
			Buff.affect( defender, com.quasistellar.shatteredpixeldungeon.actors.buffs.Ooze.class ).set( Ooze.DURATION );
			CellEmitter.center(defender.pos).burst( CorrosionParticle.SPLASH, 5 );
			
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( ColorMath.random( 0xAAAAAA, 0xFF8800) );
		particle.am = 0.6f;
		particle.setLifespan( 1f );
		particle.acc.set(0, 20);
		particle.setSize( 0.5f, 3f );
		particle.shuffleXY( 1f );
	}

}
