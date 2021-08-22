/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Hollow Dungeon
 * Copyright (C) 2020-2021 Pierre Schrodinger
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

package com.quasistellar.hollowdungeon;

import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.scenes.WelcomeScene;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PlatformSupport;

public class HollowDungeon extends Game {

	public static final int vSOME_OLD_VERSION  = -1;
	public static final int v0_0  = 1;
	public static final int v0_2  = 4;
	public static final int v0_3  = 5;
	public static final int v0_4  = 6;
	
	public HollowDungeon(PlatformSupport platform ) {
		super( sceneClass == null ? WelcomeScene.class : sceneClass, platform );
	}
	
	@Override
	public void create() {
		super.create();

		updateSystemUI();
		HDAction.loadBindings();
		
		Music.INSTANCE.enable( HDSettings.music() );
		Music.INSTANCE.volume( HDSettings.musicVol()* HDSettings.musicVol()/100f );
		Sample.INSTANCE.enable( HDSettings.soundFx() );
		Sample.INSTANCE.volume( HDSettings.SFXVol()* HDSettings.SFXVol()/100f );

		Sample.INSTANCE.load( Assets.Sounds.all );
		
	}

	public static void switchNoFade(Class<? extends com.quasistellar.hollowdungeon.scenes.PixelScene> c){
		switchNoFade(c, null);
	}

	public static void switchNoFade(Class<? extends com.quasistellar.hollowdungeon.scenes.PixelScene> c, SceneChangeCallback callback) {
		PixelScene.noFade = true;
		switchScene( c, callback );
	}
	
	public static void seamlessResetScene(SceneChangeCallback callback) {
		if (scene() instanceof com.quasistellar.hollowdungeon.scenes.PixelScene){
			((com.quasistellar.hollowdungeon.scenes.PixelScene) scene()).saveWindows();
			switchNoFade((Class<? extends com.quasistellar.hollowdungeon.scenes.PixelScene>) sceneClass, callback );
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
		if (scene instanceof com.quasistellar.hollowdungeon.scenes.PixelScene){
			((com.quasistellar.hollowdungeon.scenes.PixelScene) scene).restoreWindows();
		}
	}
	
	@Override
	public void resize( int width, int height ) {
		if (width == 0 || height == 0){
			return;
		}

		if (scene instanceof com.quasistellar.hollowdungeon.scenes.PixelScene &&
				(height != Game.height || width != Game.width)) {
			PixelScene.noFade = true;
			((com.quasistellar.hollowdungeon.scenes.PixelScene) scene).saveWindows();
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