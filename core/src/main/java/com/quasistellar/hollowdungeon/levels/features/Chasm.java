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

package com.quasistellar.hollowdungeon.levels.features;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.levels.RegularLevel;
import com.quasistellar.hollowdungeon.levels.rooms.special.WeakFloorRoom;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.plants.Swiftthistle;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.HeroSprite;
import com.quasistellar.hollowdungeon.sprites.MobSprite;
import com.quasistellar.hollowdungeon.ui.Icons;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.scenes.InterlevelScene;
import com.quasistellar.hollowdungeon.actors.buffs.Bleeding;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Cripple;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.levels.rooms.Room;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.windows.WndTitledMessage;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Chasm {

	public static boolean jumpConfirmed = false;
	
	public static void heroJump( final Hero hero ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show(
						new WndTitledMessage(HeroSprite.avatar(hero.heroClass, 1), Messages.get(Chasm.class, "chasm"), Messages.get(Chasm.class, "jump"))
				);
			}
		});
	}
	
	public static void heroFall( int pos ) {
		
		jumpConfirmed = false;
				
		Sample.INSTANCE.play( Assets.Sounds.FALLING );

		Buff buff = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
		if (buff != null) buff.detach();
		
		if (Dungeon.hero.isAlive()) {
			Dungeon.hero.interrupt();
			if (Dungeon.hero.pos != Dungeon.hero.lastFloor) {
				Utils.appear(Dungeon.hero, Dungeon.hero.lastFloor);
			}
			Dungeon.hero.damage(1, Chasm.class);
			if (!Dungeon.hero.isAlive()) {
				Dungeon.fail( Chasm.class );
				GLog.n( Messages.get(Chasm.class, "ondeath") );
			}
			Camera.main.shake(1, 1f);
			Dungeon.hero.spendAndNext(1);
		} else {
			Dungeon.hero.sprite.visible = false;
		}
	}



	public static void mobFall( Mob mob ) {
		if (mob.isAlive()) mob.die( Chasm.class );
		
		((MobSprite)mob.sprite).fall();
	}
}
