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

package com.quasistellar.hollowdungeon.levels.traps;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public abstract class Trap implements Bundlable {

	//trap colors
	public static final int RED     = 0;
	public static final int ORANGE  = 1;
	public static final int YELLOW  = 2;
	public static final int GREEN   = 3;
	public static final int TEAL    = 4;
	public static final int VIOLET  = 5;
	public static final int WHITE   = 6;
	public static final int GREY    = 7;
	public static final int BLACK   = 8;

	//trap shapes
	public static final int DOTS        = 0;
	public static final int WAVES       = 1;
	public static final int GRILL       = 2;
	public static final int STARS       = 3;
	public static final int DIAMOND     = 4;
	public static final int CROSSHAIR   = 5;
	public static final int LARGE_DOT   = 6;

	public String name = Messages.get(this, "name");

	public int color;
	public int shape;

	public int pos;

	public boolean visible;
	public boolean active = true;
	
	public boolean canBeHidden = true;
	public boolean canBeSearched = true;

	public Trap set(int pos){
		this.pos = pos;
		return this;
	}

	public Trap reveal() {
		visible = true;
		GameScene.updateMap(pos);
		return this;
	}

	public Trap hide() {
		if (canBeHidden) {
			visible = false;
			com.quasistellar.hollowdungeon.scenes.GameScene.updateMap(pos);
			return this;
		} else {
			return reveal();
		}
	}

	public void trigger() {
		if (active) {
			if (Dungeon.level.heroFOV[pos]) {
				Sample.INSTANCE.play(Assets.Sounds.TRAP);
			}
			disarm();
			reveal();
			activate();
		}
	}

	public abstract void activate();

	public void disarm(){
		active = false;
		com.quasistellar.hollowdungeon.Dungeon.level.disarmTrap(pos);
	}

	private static final String POS	= "pos";
	private static final String VISIBLE	= "visible";
	private static final String ACTIVE = "active";

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		pos = bundle.getInt( POS );
		visible = bundle.getBoolean( VISIBLE );
		if (bundle.contains(ACTIVE)){
			active = bundle.getBoolean(ACTIVE);
		}
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( POS, pos );
		bundle.put( VISIBLE, visible );
		bundle.put( ACTIVE, active );
	}

	public String desc() {
		return Messages.get(this, "desc");
	}
}
