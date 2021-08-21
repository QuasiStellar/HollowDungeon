/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Hollow Dungeon
 * Copyright (C) 2020-2021 Pierre Schrodinger
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

package com.quasistellar.hollowdungeon.sprites;

import com.quasistellar.hollowdungeon.Assets;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class FalseKnightSprite extends MobSprite {

	private Animation stab;
	private Animation prep;
	private Animation leap;
	private Animation charging;

	private boolean alt = Random.Int(2) == 0;

	public FalseKnightSprite() {
		super();

		texture( Assets.Sprites.FALSE_KNIGHT );

		TextureFilm frames = new TextureFilm( texture, 92, 72 );

		idle = new Animation( 4, true );
		idle.frames( frames, 1, 0, 1, 0 );

		run = new Animation( 15, true );
		run.frames( frames, 0, 0, 0, 1, 1, 1 );

		attack = new Animation( 12, false );
		attack.frames( frames, 2, 3, 4, 5, 6 );

		charging = new Animation( 12, true);
		charging.frames( frames, 2 );

		stab = new Animation( 12, false );
		stab.frames( frames, 2, 3, 4, 5, 6 );

		prep = new Animation( 1, true );
		prep.frames( frames, 2 );

		leap = new Animation( 1, true );
		leap.frames( frames, 3 );

		die = new Animation( 15, false );
		die.frames( frames, 7, 8 );

		play( idle );
	}

	public void leapPrep( int cell ){
		turnTo( ch.pos, cell );
		play( prep );
	}

	@Override
	public void jump(int from, int to, Callback callback) {
		super.jump(from, to, callback);
		play( leap );
	}

	public void charge( int pos ){
		turnTo(ch.pos, pos);
		play(charging);
		if (visible) Sample.INSTANCE.play( Assets.Sounds.ATK_CROSSBOW );
	}

	@Override
	public void attack( int cell ) {
		super.attack( cell );
		if (alt) {
			play( stab );
		}
		alt = !alt;
	}

	@Override
	public void onComplete( Animation anim ) {
		super.onComplete( anim == stab ? attack : anim );
	}

}
