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

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.sprites.CharSprite;

public class CharHealthIndicator extends HealthBar {
	
	private static final int HEIGHT = 1;
	
	private com.quasistellar.hollowdungeon.actors.Char target;
	
	public CharHealthIndicator( com.quasistellar.hollowdungeon.actors.Char c ){
		target = c;
		add(this);
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		height = HEIGHT;
	}
	
	@Override
	public void update() {
		super.update();
		
		if (target != null && target.isAlive() && target.sprite.visible) {
			CharSprite sprite = target.sprite;
			width = sprite.width()*(4/6f);
			x = sprite.x + sprite.width()/6f;
			y = sprite.y - 2;
			level( target );
			visible = target.HP < target.HT || target.shielding() > 0;
		} else {
			visible = false;
		}
	}
	
	public void target( com.quasistellar.hollowdungeon.actors.Char ch ) {
		if (ch != null && ch.isAlive()) {
			target = ch;
		} else {
			target = null;
		}
	}
	
	public Char target() {
		return target;
	}
}
