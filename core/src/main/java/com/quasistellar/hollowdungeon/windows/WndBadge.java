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

package com.quasistellar.hollowdungeon.windows;

import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.effects.BadgeBanner;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Window;
import com.watabou.noosa.Image;

public class WndBadge extends Window {
	
	private static final int WIDTH = 120;
	private static final int MARGIN = 4;
	
	public WndBadge( Badges.Badge badge ) {
		
		super();
		
		Image icon = BadgeBanner.image( badge.image );
		icon.scale.set( 2 );
		add( icon );

		RenderedTextBlock info = PixelScene.renderTextBlock( badge.desc(), 8 );
		info.maxWidth(WIDTH - MARGIN * 2);
		info.align(RenderedTextBlock.CENTER_ALIGN);
		PixelScene.align(info);
		add(info);
		
		float w = Math.max( icon.width(), info.width() ) + MARGIN * 2;
		
		icon.x = (w - icon.width()) / 2f;
		icon.y = MARGIN;
		PixelScene.align(icon);

		info.setPos((w - info.width()) / 2, icon.y + icon.height() + MARGIN);
		resize( (int)w, (int)(info.bottom() + MARGIN) );
		
		BadgeBanner.highlight( icon, badge.image );
	}
}
