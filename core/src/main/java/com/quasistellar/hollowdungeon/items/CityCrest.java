/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.Statistics;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.ui.Icons;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.windows.WndOptionsGreen;
import com.quasistellar.hollowdungeon.windows.WndTitledMessage;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;

public class CityCrest extends Item {

	{
		image = ItemSpriteSheet.CREST;
		stackable = true;
	}

	@Override
	public boolean doPickUp( Hero hero ) {

		Game.runOnRenderThread(() -> GameScene.show(new WndOptionsGreen(Messages.get(CityCrest.class, "title"),
				Messages.get(CityCrest.class, "text"),
				Messages.get(CityCrest.class, "invite"),
				Messages.get(CityCrest.class, "nah")){
			@Override
			protected void onSelect(int index) {
				if (index == 0) {
					DeviceCompat.openURI("https://discord.gg/JWnHK6R");
				}
			}
		} )
		);
		HDSettings.skills(true);
		return super.doPickUp(hero);
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