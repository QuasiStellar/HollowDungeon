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
import com.quasistellar.hollowdungeon.actors.buffs.Roots;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.particles.EarthParticle;
import com.quasistellar.hollowdungeon.plants.Earthroot;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;

public class SandalsOfNature extends com.quasistellar.hollowdungeon.items.artifacts.Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_SANDALS;

		levelCap = 3;

		charge = 0;

		defaultAction = AC_ROOT;
	}

	public static final String AC_FEED = "FEED";
	public static final String AC_ROOT = "ROOT";

	protected com.quasistellar.hollowdungeon.windows.WndBag.Mode mode = WndBag.Mode.SEED;

	public ArrayList<Class> seeds = new ArrayList<>();

	@Override
	public ArrayList<String> actions( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && level() < 3 && !cursed)
			actions.add(AC_FEED);
		if (isEquipped( hero ) && charge > 0)
			actions.add(AC_ROOT);
		return actions;
	}

	@Override
	public void execute(com.quasistellar.hollowdungeon.actors.hero.Hero hero, String action ) {
		super.execute(hero, action);

		if (action.equals(AC_FEED)){

			GameScene.selectItem(itemSelector, mode, Messages.get(this, "prompt"));

		} else if (action.equals(AC_ROOT) && level() > 0){

			if (!isEquipped( hero )) GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (charge == 0)    GLog.i( Messages.get(this, "no_charge") );
			else {
				Buff.prolong(hero, com.quasistellar.hollowdungeon.actors.buffs.Roots.class, Roots.DURATION);
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(hero, Earthroot.Armor.class).level(charge);
				CellEmitter.bottom(hero.pos).start(EarthParticle.FACTORY, 0.05f, 8);
				Camera.main.shake(1, 0.4f);
				charge = 0;
				Item.updateQuickslot();
			}
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new Naturalism();
	}
	
	@Override
	public void charge(com.quasistellar.hollowdungeon.actors.hero.Hero target) {
		target.buff(Naturalism.class).charge();
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc_" + (level()+1));

		if ( isEquipped ( Dungeon.hero ) ){
			desc += "\n\n";

			if (!cursed)
				desc += Messages.get(this, "desc_hint");
			else
				desc += Messages.get(this, "desc_cursed");

			if (level() > 0)
				desc += "\n\n" + Messages.get(this, "desc_ability");
		}

		if (!seeds.isEmpty()){
			desc += "\n\n" + Messages.get(this, "desc_seeds", seeds.size());
		}

		return desc;
	}

	@Override
	public com.quasistellar.hollowdungeon.items.Item upgrade() {
		if (level() < 0)        image = ItemSpriteSheet.ARTIFACT_SANDALS;
		else if (level() == 0)  image = ItemSpriteSheet.ARTIFACT_SHOES;
		else if (level() == 1)  image = ItemSpriteSheet.ARTIFACT_BOOTS;
		else if (level() >= 2)  image = ItemSpriteSheet.ARTIFACT_GREAVES;
		name = Messages.get(this, "name_" + (level()+1));
		return super.upgrade();
	}

	public static boolean canUseSeed(com.quasistellar.hollowdungeon.items.Item item){
		if (item instanceof com.quasistellar.hollowdungeon.plants.Plant.Seed){
			return !(Item.curItem instanceof SandalsOfNature) ||
					!((SandalsOfNature) Item.curItem).seeds.contains(item.getClass());
		}
		return false;
	}


	private static final String SEEDS = "seeds";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(SEEDS, seeds.toArray(new Class[seeds.size()]));
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (level() > 0) name = Messages.get(this, "name_" + level());
		if (bundle.contains(SEEDS))
			Collections.addAll(seeds , bundle.getClassArray(SEEDS));
		if (level() == 1)  image = ItemSpriteSheet.ARTIFACT_SHOES;
		else if (level() == 2)  image = ItemSpriteSheet.ARTIFACT_BOOTS;
		else if (level() >= 3)  image = com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet.ARTIFACT_GREAVES;
	}

	public class Naturalism extends ArtifactBuff{
		public void charge() {
			if (level() > 0 && charge < target.HT){
				//gain 1+(1*level)% of the difference between current charge and max HP.
				charge+= (Math.round( (target.HT-charge) * (.01+ level()*0.01) ));
				Item.updateQuickslot();
			}
		}
	}

	protected com.quasistellar.hollowdungeon.windows.WndBag.Listener itemSelector = new com.quasistellar.hollowdungeon.windows.WndBag.Listener() {
		@Override
		public void onSelect( com.quasistellar.hollowdungeon.items.Item item ) {
			if (item != null && item instanceof Plant.Seed) {
				if (seeds.contains(item.getClass())){
					GLog.w( Messages.get(SandalsOfNature.class, "already_fed") );
				} else {
					seeds.add(item.getClass());

					Hero hero = com.quasistellar.hollowdungeon.Dungeon.hero;
					hero.sprite.operate( hero.pos );
					Sample.INSTANCE.play( Assets.Sounds.PLANT );
					hero.busy();
					hero.spend( 2f );
					if (seeds.size() >= 3+(level()*3)){
						seeds.clear();
						upgrade();
						if (level() >= 1 && level() <= 3) {
							GLog.p( Messages.get(SandalsOfNature.class, "levelup") );
						}

					} else {
						com.quasistellar.hollowdungeon.utils.GLog.i( Messages.get(SandalsOfNature.class, "absorb_seed") );
					}
					item.detach(hero.belongings.backpack);
				}
			}
		}
	};

}
