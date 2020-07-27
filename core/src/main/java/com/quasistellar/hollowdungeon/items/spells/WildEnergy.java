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

package com.quasistellar.hollowdungeon.items.spells;

import com.quasistellar.hollowdungeon.actors.buffs.ArtifactRecharge;
import com.quasistellar.hollowdungeon.actors.buffs.Recharging;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.artifacts.Artifact;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRecharging;
import com.quasistellar.hollowdungeon.items.scrolls.exotic.ScrollOfMysticalEnergy;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.quest.MetalShard;
import com.quasistellar.hollowdungeon.items.wands.CursedWand;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class WildEnergy extends TargetedSpell {
	
	{
		image = ItemSpriteSheet.WILD_ENERGY;
	}
	
	//we rely on cursedWand to do fx instead
	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		affectTarget(bolt, com.quasistellar.hollowdungeon.items.Item.curUser);
	}
	
	@Override
	protected void affectTarget(Ballistica bolt, final Hero hero) {
		CursedWand.cursedZap(this, hero, bolt, new Callback() {
			@Override
			public void call() {
				Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.CHARGEUP );
				ScrollOfRecharging.charge(hero);

				hero.belongings.charge(1f);
				for (int i = 0; i < 4; i++){
					if (hero.belongings.misc1 instanceof com.quasistellar.hollowdungeon.items.artifacts.Artifact) ((com.quasistellar.hollowdungeon.items.artifacts.Artifact) hero.belongings.misc1).charge(hero);
					if (hero.belongings.misc2 instanceof com.quasistellar.hollowdungeon.items.artifacts.Artifact) ((Artifact) hero.belongings.misc2).charge(hero);
				}

				Buff.affect(hero, Recharging.class, 8f);
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(hero, ArtifactRecharge.class).prolong( 8 );
				
				detach( com.quasistellar.hollowdungeon.items.Item.curUser.belongings.backpack );
				com.quasistellar.hollowdungeon.items.Item.updateQuickslot();
				Item.curUser.spendAndNext( 1f );
			}
		});
	}
	
	@Override
	public int price() {
		//prices of ingredients, divided by output quantity
		return Math.round(quantity * ((50 + 100) / 5f));
	}
	
	public static class Recipe extends com.quasistellar.hollowdungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{ScrollOfMysticalEnergy.class, MetalShard.class};
			inQuantity = new int[]{1, 1};
			
			cost = 8;
			
			output = WildEnergy.class;
			outQuantity = 5;
		}
		
	}
}