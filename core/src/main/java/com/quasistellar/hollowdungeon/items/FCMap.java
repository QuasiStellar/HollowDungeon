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

package com.quasistellar.hollowdungeon.items;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Cornifer;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.tiles.FogOfWar;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.DeviceCompat;

public class FCMap extends Item {

	{
		image = ItemSpriteSheet.MAP;
		stackable = true;
	}

	@Override
	public boolean doPickUp(Hero hero) {
		Dungeon.mappedLocations.add("King's Pass");
		Dungeon.mappedLocations.add("Dirtmouth");
		Dungeon.mappedLocations.add("Forgotten Crossroads 1");
		Dungeon.mappedLocations.add("Forgotten Crossroads 2");
		Dungeon.mappedLocations.add("Forgotten Crossroads 3");
		Dungeon.mappedLocations.add("Forgotten Crossroads 4");
		Dungeon.mappedLocations.add("False Knight Arena");
		Dungeon.level.needUpdateFog = null;
		GLog.p(Messages.get(this, "added"));
		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}