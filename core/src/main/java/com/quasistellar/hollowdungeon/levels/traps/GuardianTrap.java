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

package com.quasistellar.hollowdungeon.levels.traps;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.mobs.Gnoll;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.StatueSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

public class GuardianTrap extends Trap {

	{
		color = RED;
		shape = STARS;
	}

	@Override
	public void activate() {

		for (Mob mob : Dungeon.level.mobs) {
			mob.beckon( pos );
		}

		if (Dungeon.level.heroFOV[pos]) {
			GLog.w( Messages.get(this, "alarm") );
			CellEmitter.center(pos).start( Speck.factory(com.quasistellar.hollowdungeon.effects.Speck.SCREAM), 0.3f, 3 );
		}

		Sample.INSTANCE.play( Assets.Sounds.ALERT );

//		for (int i = 0; i < (Dungeon.depth - 5)/5; i++){
//			Guardian guardian = new Guardian();
//			guardian.state = guardian.WANDERING;
//			guardian.pos = Dungeon.level.randomRespawnCell( guardian );
//			GameScene.add(guardian);
//			guardian.beckon(com.quasistellar.hollowdungeon.Dungeon.hero.pos );
//		}

	}

	public static class Guardian extends Gnoll {

		{
			spriteClass = GuardianSprite.class;

			state = WANDERING;
		}

	}

	public static class GuardianSprite extends StatueSprite {

		public GuardianSprite(){
			super();
			tint(0, 0, 1, 0.2f);
		}

		@Override
		public void resetColor() {
			super.resetColor();
			tint(0, 0, 1, 0.2f);
		}
	}
}
