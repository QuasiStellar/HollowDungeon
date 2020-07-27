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

package com.quasistellar.hollowdungeon.actors.blobs;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.BlobEmitter;
import com.quasistellar.hollowdungeon.effects.particles.ShadowParticle;
import com.quasistellar.hollowdungeon.effects.particles.ShaftParticle;
import com.quasistellar.hollowdungeon.items.Ankh;
import com.quasistellar.hollowdungeon.items.DewVial;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.journal.Notes;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.potions.PotionOfHealing;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRemoveCurse;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

public class WaterOfHealth extends WellWater {
	
	@Override
	protected boolean affectHero( Hero hero ) {
		
		if (!hero.isAlive()) return false;
		
		Sample.INSTANCE.play( Assets.Sounds.DRINK );

		hero.HP = hero.HT;
		hero.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );

		PotionOfHealing.cure( hero );
		hero.belongings.uncurseEquipped();
		
		CellEmitter.get( hero.pos ).start( ShaftParticle.FACTORY, 0.2f, 3 );

		Dungeon.hero.interrupt();
	
		GLog.p( Messages.get(this, "procced") );
		
		return true;
	}
	
	@Override
	protected com.quasistellar.hollowdungeon.items.Item affectItem(Item item, int pos ) {
		if (item instanceof com.quasistellar.hollowdungeon.items.DewVial && !((com.quasistellar.hollowdungeon.items.DewVial)item).isFull()) {
			((DewVial)item).fill();
			CellEmitter.get( pos ).start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
			Sample.INSTANCE.play( Assets.Sounds.DRINK );
			return item;
		} else if ( item instanceof com.quasistellar.hollowdungeon.items.Ankh && !(((com.quasistellar.hollowdungeon.items.Ankh) item).isBlessed())){
			((Ankh) item).bless();
			CellEmitter.get( pos ).start(Speck.factory(Speck.LIGHT), 0.2f, 3);
			Sample.INSTANCE.play( Assets.Sounds.DRINK );
			return item;
		} else if (ScrollOfRemoveCurse.uncursable(item)) {
			if (ScrollOfRemoveCurse.uncurse( null, item )){
				com.quasistellar.hollowdungeon.effects.CellEmitter.get( pos ).start( ShadowParticle.UP, 0.05f, 10 );
			}
			Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.DRINK );
			return item;
		}
		return null;
	}
	
	@Override
	protected com.quasistellar.hollowdungeon.journal.Notes.Landmark record() {
		return Notes.Landmark.WELL_OF_HEALTH;
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.HEALING ), 0.5f, 0 );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
