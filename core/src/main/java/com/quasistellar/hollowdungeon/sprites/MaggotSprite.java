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

public class MaggotSprite extends MobSprite {

	public MaggotSprite() {
		super();

		texture( Assets.Sprites.MAGGOT );
		TextureFilm film = new TextureFilm( texture, 50, 47 );

		idle = new Animation( 1, true );
		idle.frames( film, 0 );

		run = new Animation( 15, true );
		run.frames( film, 0 );

		die = new Animation( 60, false );
		die.frames( film, 0 );

		attack = new Animation( 60, false );
		attack.frames( film, 0 );

		idle();
	}
}
