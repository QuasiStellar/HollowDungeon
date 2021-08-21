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

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Cornifer;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.NPC;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Shopkeeper;
import com.quasistellar.hollowdungeon.items.Geo;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.ui.RedButton;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Window;
import com.quasistellar.hollowdungeon.utils.GLog;

public class WndQuestTrade extends Window {

	private static final float GAP		= 2;
	private static final int BTN_HEIGHT	= 16;
	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;

	private WndBag owner;

	//buying
	public WndQuestTrade(NPC questgiver, String text, final Item item, int price ) {

		super();

		layoutFields(new IconTitle(item, Messages.capitalize(questgiver.name())), PixelScene.renderTextBlock(text, 6));

		float pos = height;

		RedButton btnBuy = new RedButton( Messages.get(Cornifer.class, "buy") ) {
			@Override
			protected void onClick() {
				hide();
				buy( item, price, questgiver );
			}
		};
		btnBuy.setRect( 0, pos + GAP, width, BTN_HEIGHT );
		btnBuy.enable( price <= Dungeon.geo);
		add( btnBuy );

		pos = btnBuy.bottom();

		resize(width, (int) pos);
	}

	private void layoutFields(IconTitle title, RenderedTextBlock info){
		int width = WIDTH_MIN;

		info.maxWidth(width);

		//window can go out of the screen on landscape, so widen it as appropriate
		while (PixelScene.landscape()
				&& info.height() > 100
				&& width < WIDTH_MAX){
			width += 20;
			info.maxWidth(width);
		}

		title.setRect( 0, 0, width, 0 );
		add( title );

		info.setPos(title.left(), title.bottom() + GAP);
		add( info );

		resize( width, (int)(info.bottom() + 2) );
	}
	
	private void buy( Item item, int price, NPC questgiver ) {

		item.doPickUp(Dungeon.hero);
		Dungeon.geo -= price;
		if (questgiver instanceof Cornifer) {
			((Cornifer) questgiver).bought = true;
		}

	}
}
