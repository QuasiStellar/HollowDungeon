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

package com.quasistellar.hollowdungeon.actors.buffs;

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Image;

public class Barrier extends ShieldBuff {
	
	{
		type = Buff.buffType.POSITIVE;
	}
	
	@Override
	public boolean act() {
		
		absorbDamage(1);
		
		if (shielding() <= 0){
			detach();
		}
		
		spend( Actor.TICK );
		
		return true;
	}
	
	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.SHIELDED);
		else target.sprite.remove(com.quasistellar.hollowdungeon.sprites.CharSprite.State.SHIELDED);
	}
	
	@Override
	public int icon() {
		return BuffIndicator.ARMOR;
	}
	
	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.5f, 1f, 2f);
	}
	
	@Override
	public String toString() {
		return Messages.get(this, "name");
	}
	
	@Override
	public String desc() {
		return Messages.get(this, "desc", shielding());
	}
}
