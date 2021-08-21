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

package com.quasistellar.hollowdungeon.ui;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.windows.WndSettings;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.utils.DeviceCompat;

public class DiscordButton extends Button {
	
	private Image image;
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		image = Icons.DISCORD.get();
		add( image );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		image.x = x + (width - image.width)/2f;
		image.y = y + (height - image.height)/2f;
		PixelScene.align(image);
	}
	
	@Override
	protected void onPointerDown() {
		image.brightness( 1.5f );
		Sample.INSTANCE.play( Assets.Sounds.CLICK );
	}
	
	@Override
	protected void onPointerUp() {
		image.resetColor();
	}
	
	@Override
	protected void onClick() {
		parent.add( new WndOptions(Messages.get(this, "name"),
				Messages.get(this, "wnd_body"),
				Messages.get(this, "wnd_invite")){
			@Override
			protected void onSelect(int index) {
				DeviceCompat.openURI( "https://discord.gg/JWnHK6R" );
			}
		} );
	}
}
