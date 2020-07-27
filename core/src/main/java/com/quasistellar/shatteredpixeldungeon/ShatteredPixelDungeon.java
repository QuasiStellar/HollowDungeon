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

package com.quasistellar.shatteredpixeldungeon;

import com.quasistellar.shatteredpixeldungeon.actors.mobs.*;
import com.quasistellar.shatteredpixeldungeon.items.bombs.Bomb;
import com.quasistellar.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.quasistellar.shatteredpixeldungeon.items.spells.MagicalInfusion;
import com.quasistellar.shatteredpixeldungeon.items.stones.StoneOfDisarming;
import com.quasistellar.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.quasistellar.shatteredpixeldungeon.items.weapon.enchantments.Chilling;
import com.quasistellar.shatteredpixeldungeon.items.weapon.enchantments.Elastic;
import com.quasistellar.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.quasistellar.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.quasistellar.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.quasistellar.shatteredpixeldungeon.levels.OldCavesBossLevel;
import com.quasistellar.shatteredpixeldungeon.levels.OldCityBossLevel;
import com.quasistellar.shatteredpixeldungeon.levels.OldHallsBossLevel;
import com.quasistellar.shatteredpixeldungeon.levels.OldPrisonBossLevel;
import com.quasistellar.shatteredpixeldungeon.levels.rooms.sewerboss.SewerBossEntranceRoom;
import com.quasistellar.shatteredpixeldungeon.scenes.GameScene;
import com.quasistellar.shatteredpixeldungeon.scenes.PixelScene;
import com.quasistellar.shatteredpixeldungeon.scenes.WelcomeScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PlatformSupport;

public class ShatteredPixelDungeon extends Game {

	//variable constants for specific older versions of shattered, used for data conversion
	//versions older than v0.6.5c are no longer supported, and data from them is ignored
	public static final int v0_6_5c = -1;

	public static final int v0_7_0c = -1;
	public static final int v0_7_1d = -1;
	public static final int v0_7_2d = -1;
	public static final int v0_7_3b = -1;
	public static final int v0_7_4c = -1;
	public static final int v0_7_5e = -1;

	public static final int v0_8_0  = -1;
	public static final int v0_8_1  = 1;
	
	public ShatteredPixelDungeon( PlatformSupport platform ) {
		super( sceneClass == null ? WelcomeScene.class : sceneClass, platform );
	}
	
	@Override
	public void create() {
		super.create();

		updateSystemUI();
		SPDAction.loadBindings();
		
		Music.INSTANCE.enable( SPDSettings.music() );
		Music.INSTANCE.volume( SPDSettings.musicVol()*SPDSettings.musicVol()/100f );
		Sample.INSTANCE.enable( SPDSettings.soundFx() );
		Sample.INSTANCE.volume( SPDSettings.SFXVol()*SPDSettings.SFXVol()/100f );

		Sample.INSTANCE.load( Assets.Sounds.all );
		
	}

	public static void switchNoFade(Class<? extends com.quasistellar.shatteredpixeldungeon.scenes.PixelScene> c){
		switchNoFade(c, null);
	}

	public static void switchNoFade(Class<? extends com.quasistellar.shatteredpixeldungeon.scenes.PixelScene> c, SceneChangeCallback callback) {
		com.quasistellar.shatteredpixeldungeon.scenes.PixelScene.noFade = true;
		switchScene( c, callback );
	}
	
	public static void seamlessResetScene(SceneChangeCallback callback) {
		if (scene() instanceof com.quasistellar.shatteredpixeldungeon.scenes.PixelScene){
			((com.quasistellar.shatteredpixeldungeon.scenes.PixelScene) scene()).saveWindows();
			switchNoFade((Class<? extends com.quasistellar.shatteredpixeldungeon.scenes.PixelScene>) sceneClass, callback );
		} else {
			resetScene();
		}
	}
	
	public static void seamlessResetScene(){
		seamlessResetScene(null);
	}
	
	@Override
	protected void switchScene() {
		super.switchScene();
		if (scene instanceof com.quasistellar.shatteredpixeldungeon.scenes.PixelScene){
			((com.quasistellar.shatteredpixeldungeon.scenes.PixelScene) scene).restoreWindows();
		}
	}
	
	@Override
	public void resize( int width, int height ) {
		if (width == 0 || height == 0){
			return;
		}

		if (scene instanceof com.quasistellar.shatteredpixeldungeon.scenes.PixelScene &&
				(height != Game.height || width != Game.width)) {
			com.quasistellar.shatteredpixeldungeon.scenes.PixelScene.noFade = true;
			((PixelScene) scene).saveWindows();
		}

		super.resize( width, height );

		updateDisplaySize();

	}
	
	@Override
	public void destroy(){
		super.destroy();
		GameScene.endActorThread();
	}
	
	public void updateDisplaySize(){
		platform.updateDisplaySize();
	}

	public static void updateSystemUI() {
		platform.updateSystemUI();
	}
}