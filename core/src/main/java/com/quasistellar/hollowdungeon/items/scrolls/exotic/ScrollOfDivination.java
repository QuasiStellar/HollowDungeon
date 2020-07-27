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

package com.quasistellar.hollowdungeon.items.scrolls.exotic;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.effects.Identification;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Window;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.IconTitle;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.items.potions.Potion;
import com.quasistellar.hollowdungeon.items.rings.Ring;
import com.quasistellar.hollowdungeon.items.scrolls.Scroll;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashSet;

public class ScrollOfDivination extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_DIVINATE;
	}
	
	@Override
	public void doRead() {
		
		Item.curUser.sprite.parent.add( new Identification( Item.curUser.sprite.center().offset( 0, -16 ) ) );
		
		readAnimation();
		setKnown();
		
		Sample.INSTANCE.play( Assets.Sounds.READ );
		Invisibility.dispel();
		
		HashSet<Class<? extends Potion>> potions = Potion.getUnknown();
		HashSet<Class<? extends Scroll>> scrolls = Scroll.getUnknown();
		HashSet<Class<? extends Ring>> rings = Ring.getUnknown();
		
		int total = potions.size() + scrolls.size() + rings.size();
		
		if (total == 0){
			GLog.n( Messages.get(this, "nothing_left") );
			return;
		}
		
		ArrayList<com.quasistellar.hollowdungeon.items.Item> IDed = new ArrayList<>();
		int left = 4;
		
		float[] baseProbs = new float[]{3, 3, 3};
		float[] probs = baseProbs.clone();
		
		while (left > 0 && total > 0) {
			switch (Random.chances(probs)) {
				default:
					probs = baseProbs.clone();
					continue;
				case 0:
					if (potions.isEmpty()) {
						probs[0] = 0;
						continue;
					}
					probs[0]--;
					Potion p = Reflection.newInstance(Random.element(potions));
					p.setKnown();
					IDed.add(p);
					potions.remove(p.getClass());
					break;
				case 1:
					if (scrolls.isEmpty()) {
						probs[1] = 0;
						continue;
					}
					probs[1]--;
					Scroll s = Reflection.newInstance(Random.element(scrolls));
					s.setKnown();
					IDed.add(s);
					scrolls.remove(s.getClass());
					break;
				case 2:
					if (rings.isEmpty()) {
						probs[2] = 0;
						continue;
					}
					probs[2]--;
					Ring r = Reflection.newInstance(Random.element(rings));
					r.setKnown();
					IDed.add(r);
					rings.remove(r.getClass());
					break;
			}
			left --;
			total --;
		}
		
		GameScene.show(new WndDivination( IDed ));
	}
	
	private class WndDivination extends Window {
		
		private static final int WIDTH = 120;
		
		WndDivination(ArrayList<com.quasistellar.hollowdungeon.items.Item> IDed ){
			com.quasistellar.hollowdungeon.windows.IconTitle cur = new com.quasistellar.hollowdungeon.windows.IconTitle(new ItemSprite(ScrollOfDivination.this),
					Messages.titleCase(Messages.get(ScrollOfDivination.class, "name")));
			cur.setRect(0, 0, WIDTH, 0);
			add(cur);
			
			RenderedTextBlock msg = PixelScene.renderTextBlock(Messages.get(this, "desc"), 6);
			msg.maxWidth(120);
			msg.setPos(0, cur.bottom() + 2);
			add(msg);
			
			float pos = msg.bottom() + 10;
			
			for (com.quasistellar.hollowdungeon.items.Item i : IDed){
				
				cur = new IconTitle(i);
				cur.setRect(0, pos, WIDTH, 0);
				add(cur);
				pos = cur.bottom() + 2;
				
			}
			
			resize(WIDTH, (int)pos);
		}
		
	}
}
