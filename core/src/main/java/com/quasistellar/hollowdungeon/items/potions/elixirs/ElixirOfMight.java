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

package com.quasistellar.hollowdungeon.items.potions.elixirs;

import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.potions.AlchemicalCatalyst;
import com.quasistellar.hollowdungeon.items.potions.PotionOfStrength;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class ElixirOfMight extends Elixir {

	{
		image = ItemSpriteSheet.ELIXIR_MIGHT;
	}
	
	@Override
	public void apply( Hero hero ) {
		setKnown();

		Buff.affect(hero, HTBoost.class).reset();
		HTBoost boost = Buff.affect(hero, HTBoost.class);
		boost.reset();
		
		hero.updateHT( true );
		hero.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "msg_1", boost.boost() ));
		GLog.p( Messages.get(this, "msg_2") );
	}
	
	public String desc() {
		return Messages.get(this, "desc", HTBoost.boost(Dungeon.hero.HT));
	}
	
	@Override
	public int price() {
		//prices of ingredients
		return quantity * (50 + 40);
	}
	
	public static class Recipe extends com.quasistellar.hollowdungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfStrength.class, AlchemicalCatalyst.class};
			inQuantity = new int[]{1, 1};
			
			cost = 5;
			
			output = ElixirOfMight.class;
			outQuantity = 1;
		}
		
	}
	
	public static class HTBoost extends Buff {
		
		{
			type = buffType.POSITIVE;
		}
		
		private int left;
		
		public void reset(){
			left = 5;
		}
		
		public int boost(){
			return Math.round(left*boost(target.HT)/5f);
		}
		
		public static int boost(int HT){
			return Math.round(4 + HT/20f);
		}
		
		public void onLevelUp(){
			left --;
			if (left <= 0){
				detach();
			}
		}
		
		@Override
		public int icon() {
			return BuffIndicator.HEALING;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0.5f, 0f);
		}

		@Override
		public float iconFadePercent() {
			return (5f - left) / 5f;
		}
		
		@Override
		public String toString() {
			return Messages.get(this, "name");
		}
		
		@Override
		public String desc() {
			return Messages.get(this, "desc", boost(), left);
		}
		
		private static String LEFT = "left";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( LEFT, left );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			left = bundle.getInt(LEFT);
		}
	}
}
