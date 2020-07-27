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

package com.quasistellar.shatteredpixeldungeon.items.scrolls.exotic;

import com.quasistellar.shatteredpixeldungeon.Assets;
import com.quasistellar.shatteredpixeldungeon.Dungeon;
import com.quasistellar.shatteredpixeldungeon.actors.Char;
import com.quasistellar.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.quasistellar.shatteredpixeldungeon.effects.CellEmitter;
import com.quasistellar.shatteredpixeldungeon.effects.Flare;
import com.quasistellar.shatteredpixeldungeon.effects.Speck;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.scenes.GameScene;
import com.quasistellar.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.quasistellar.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.quasistellar.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class ScrollOfPolymorph extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_POLYMORPH;
	}
	
	@Override
	public void doRead() {
		
		new Flare( 5, 32 ).color( 0xFFFFFF, true ).show( Item.curUser.sprite, 2f );
		Sample.INSTANCE.play( Assets.Sounds.READ );
		Invisibility.dispel();
		
		for (Mob mob : com.quasistellar.shatteredpixeldungeon.Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (mob.alignment != com.quasistellar.shatteredpixeldungeon.actors.Char.Alignment.ALLY && com.quasistellar.shatteredpixeldungeon.Dungeon.level.heroFOV[mob.pos]) {
				if (!mob.properties().contains(com.quasistellar.shatteredpixeldungeon.actors.Char.Property.BOSS)
						&& !mob.properties().contains(Char.Property.MINIBOSS)){
					com.quasistellar.shatteredpixeldungeon.actors.mobs.npcs.Sheep sheep = new Sheep();
					sheep.lifespan = 10;
					sheep.pos = mob.pos;
					
					//awards half exp for each sheep-ified mob
					//50% chance to round up, 50% to round down
					if (mob.EXP % 2 == 1) mob.EXP += Random.Int(2);
					mob.EXP /= 2;
					
					mob.destroy();
					mob.sprite.killAndErase();
					Dungeon.level.mobs.remove(mob);
					TargetHealthIndicator.instance.target(null);
					GameScene.add(sheep);
					CellEmitter.get(sheep.pos).burst(com.quasistellar.shatteredpixeldungeon.effects.Speck.factory(Speck.WOOL), 4);
				}
			}
		}
		setKnown();
		
		readAnimation();
		
	}
	
}
