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

package com.quasistellar.hollowdungeon.actors.buffs;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.messages.Messages;

public class Drowsy extends com.quasistellar.hollowdungeon.actors.buffs.Buff {

	public static final float DURATION = 5f;

	{
		type = buffType.NEUTRAL;
		announced = true;
	}

	@Override
	public int icon() {
		return BuffIndicator.DROWSY;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	public boolean attachTo(Char target ) {
		if (!target.isImmune(Sleep.class) && super.attachTo(target)) {
			if (cooldown() == 0) {
				spend(DURATION);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean act(){
		Buff.affect(target, MagicalSleep.class);

		detach();
		return true;
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(visualcooldown()));
	}
}