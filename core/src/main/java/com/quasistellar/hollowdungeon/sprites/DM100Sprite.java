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
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.Lightning;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PointF;

public class DM100Sprite extends MobSprite {
	
	public DM100Sprite () {
		super();
		
		texture( Assets.Sprites.DM100 );
		
		TextureFilm frames = new TextureFilm( texture, 16, 14 );
		
		idle = new Animation( 1, true );
		idle.frames( frames, 0, 1 );

		run = new Animation( 12, true );
		run.frames( frames, 6, 7, 8, 9 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 2, 3, 4, 0 );

		zap = new Animation( 8, false );
		zap.frames( frames, 5, 5, 1 );

		die = new Animation( 12, false );
		die.frames( frames, 10, 11, 12, 13, 14, 15 );
		
		play( idle );
	}
	
	public void zap( int pos ) {

		Char enemy = Actor.findChar(pos);

		//shoot lightning from eye, not sprite center.
		PointF origin = center();
		if (flipHorizontal){
			origin.y -= 6*scale.y;
			origin.x -= 1*scale.x;
		} else {
			origin.y -= 8*scale.y;
			origin.x += 1*scale.x;
		}
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		
		turnTo( ch.pos, pos );
		flash();
		play( zap );
	}

	@Override
	public void die() {
		emitter().burst( Speck.factory( Speck.WOOL ), 5 );
		super.die();
	}

	@Override
	public void onComplete( Animation anim ) {
		if (anim == zap) {
			idle();
		}
		super.onComplete( anim );
	}
}
