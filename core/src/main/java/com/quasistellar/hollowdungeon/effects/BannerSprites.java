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

package com.quasistellar.hollowdungeon.effects;

import com.quasistellar.hollowdungeon.Assets;
import com.watabou.noosa.Image;

public class BannerSprites {

	public enum  Type {
		PIXEL_DUNGEON,
		BOSS_SLAIN,
		GAME_OVER,
		SELECT_YOUR_HERO,
		PIXEL_DUNGEON_SIGNS,
		START_GAME,
		ABOUT
	}

	public static Image get( Type type ) {
		Image icon = new Image( Assets.Interfaces.BANNERS );
		switch (type) {
			case PIXEL_DUNGEON:
				icon.frame( icon.texture.uvRect( 0, 0, 256, 122 ) );
				break;
			case BOSS_SLAIN:
				icon.frame( icon.texture.uvRect( 0, 122, 128, 157 ) );
				break;
			case GAME_OVER:
				icon.frame( icon.texture.uvRect( 0, 157, 128, 192 ) );
				break;
			case SELECT_YOUR_HERO:
				icon.frame( icon.texture.uvRect( 0, 192, 128, 213 ) );
				break;
			case PIXEL_DUNGEON_SIGNS:
				icon.frame( icon.texture.uvRect( 0, 213, 256, 303 ) );
				break;
			case START_GAME:
				icon.frame( icon.texture.uvRect( 0, 303, 96, 319 ) );
				break;
			case ABOUT:
				icon.frame( icon.texture.uvRect( 0, 319, 96, 335 ) );
				break;
		}
		return icon;
	}
}
