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

import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.watabou.input.GameAction;
import com.watabou.noosa.Image;

public class ResumeIndicator extends Tag {

	private Image icon;

	public ResumeIndicator() {
		super(0xCDD5C0);

		setSize( 24, 24 );

		visible = false;

	}
	
	@Override
	public GameAction keyAction() {
		return HDAction.TAG_RESUME;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		icon = com.quasistellar.hollowdungeon.ui.Icons.get( Icons.ARROW);
		add( icon );
	}

	@Override
	protected void layout() {
		super.layout();

		icon.x = x+1 + (width - icon.width) / 2f;
		icon.y = y + (height - icon.height) / 2f;
		PixelScene.align(icon);
	}

	@Override
	protected void onClick() {
		Dungeon.hero.resume();
	}

	@Override
	public void update() {
		if (!Dungeon.hero.isAlive())
			visible = false;
		else if (visible != (Dungeon.hero.lastAction != null)){
			visible = Dungeon.hero.lastAction != null;
			if (visible)
				flash();
		}
		super.update();
	}
}
