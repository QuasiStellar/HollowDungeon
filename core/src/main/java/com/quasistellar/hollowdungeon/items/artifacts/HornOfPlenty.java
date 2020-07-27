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

package com.quasistellar.hollowdungeon.items.artifacts;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Statistics;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.SpellSprite;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.items.food.Blandfruit;
import com.quasistellar.hollowdungeon.items.food.Food;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class HornOfPlenty extends com.quasistellar.hollowdungeon.items.artifacts.Artifact {


	{
		image = ItemSpriteSheet.ARTIFACT_HORN1;

		levelCap = 10;

		charge = 0;
		partialCharge = 0;
		chargeCap = 10 + level();

		defaultAction = AC_EAT;
	}
	
	private int storedFoodEnergy = 0;

	public static final String AC_EAT = "EAT";
	public static final String AC_STORE = "STORE";

	protected com.quasistellar.hollowdungeon.windows.WndBag.Mode mode = WndBag.Mode.FOOD;

	@Override
	public ArrayList<String> actions( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && charge > 0)
			actions.add(AC_EAT);
		if (isEquipped( hero ) && level() < levelCap && !cursed)
			actions.add(AC_STORE);
		return actions;
	}

	@Override
	public void execute(com.quasistellar.hollowdungeon.actors.hero.Hero hero, String action ) {

		super.execute(hero, action);

	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new hornRecharge();
	}
	
	@Override
	public void charge(com.quasistellar.hollowdungeon.actors.hero.Hero target) {
		if (charge < chargeCap){
			partialCharge += 0.25f;
			if (partialCharge >= 1){
				partialCharge--;
				charge++;
				
				if (charge == chargeCap){
					GLog.p( Messages.get(HornOfPlenty.class, "full") );
					partialCharge = 0;
				}
				
				if (charge >= 15)       image = ItemSpriteSheet.ARTIFACT_HORN4;
				else if (charge >= 10)  image = ItemSpriteSheet.ARTIFACT_HORN3;
				else if (charge >= 5)   image = ItemSpriteSheet.ARTIFACT_HORN2;
				
				Item.updateQuickslot();
			}
		}
	}
	
	@Override
	public String desc() {
		String desc = super.desc();

		if ( isEquipped( Dungeon.hero ) ){
			if (!cursed) {
				if (level() < levelCap)
					desc += "\n\n" +Messages.get(this, "desc_hint");
			} else {
				desc += "\n\n" +Messages.get(this, "desc_cursed");
			}
		}

		return desc;
	}

	@Override
	public void level(int value) {
		super.level(value);
		chargeCap = 10 + level();
	}

	@Override
	public com.quasistellar.hollowdungeon.items.Item upgrade() {
		super.upgrade();
		chargeCap = 10 + level();
		return this;
	}
	
	public void gainFoodValue( Food food ){
		if (level() >= 10) return;

		GLog.i( Messages.get(this, "feed") );
	}
	
	private static final String STORED = "stored";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( STORED, storedFoodEnergy );
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		storedFoodEnergy = bundle.getInt(STORED);
		
		if (charge >= 15)       image = ItemSpriteSheet.ARTIFACT_HORN4;
		else if (charge >= 10)  image = ItemSpriteSheet.ARTIFACT_HORN3;
		else if (charge >= 5)   image = ItemSpriteSheet.ARTIFACT_HORN2;
	}

	public class hornRecharge extends ArtifactBuff{

	}

	protected static com.quasistellar.hollowdungeon.windows.WndBag.Listener itemSelector = new com.quasistellar.hollowdungeon.windows.WndBag.Listener() {
		@Override
		public void onSelect( com.quasistellar.hollowdungeon.items.Item item ) {
			if (item != null && item instanceof Food) {
				if (item instanceof Blandfruit && ((Blandfruit) item).potionAttrib == null){
					com.quasistellar.hollowdungeon.utils.GLog.w( Messages.get(HornOfPlenty.class, "reject") );
				} else {
					Hero hero = com.quasistellar.hollowdungeon.Dungeon.hero;
					hero.sprite.operate( hero.pos );
					hero.busy();
					hero.spend( Food.TIME_TO_EAT );

					((HornOfPlenty) com.quasistellar.hollowdungeon.items.Item.curItem).gainFoodValue(((Food)item));
					item.detach(hero.belongings.backpack);
				}

			}
		}
	};
}
