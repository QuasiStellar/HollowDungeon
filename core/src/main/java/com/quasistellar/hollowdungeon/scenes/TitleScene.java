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

package com.quasistellar.hollowdungeon.scenes;

import com.quasistellar.hollowdungeon.effects.Fireball;
import com.quasistellar.hollowdungeon.ui.DiscordButton;
import com.quasistellar.hollowdungeon.ui.IconButton;
import com.quasistellar.hollowdungeon.effects.BannerSprites;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Chrome;
import com.quasistellar.hollowdungeon.GamesInProgress;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.ui.Archs;
import com.quasistellar.hollowdungeon.ui.ExitButton;
import com.quasistellar.hollowdungeon.ui.PrefsButton;
import com.quasistellar.hollowdungeon.ui.StyledButton;
import com.quasistellar.hollowdungeon.ui.UpdateNotification;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.DeviceCompat;

public class TitleScene extends PixelScene {
	
	@Override
	public void create() {
		
		super.create();

		Music.INSTANCE.play( Assets.Music.THEME, true );

		uiCamera.visible = false;
		
		int w = Camera.main.width;
		int h = Camera.main.height;

		Image background = new Image(Assets.Splashes.THEME){
			@Override
			public void update() {
				if (rm > 1f){
					rm -= Game.elapsed;
					gm = bm = rm;
				} else {
					rm = gm = bm = 1;
				}
			}
		};
		background.scale.set(Camera.main.height/background.height);

		background.x = (Camera.main.width - background.width())/2f;
		background.y = (Camera.main.height - background.height())/2f;
		//background.visible = false;
		PixelScene.align(background);
		add(background);

		Archs archs = new Archs();
		archs.setSize( w, h );
		add( archs );

		if (background.x > 0){
			Image fadeLeft = new Image(TextureCache.createGradient(0xFF000000, 0x00000000));
			fadeLeft.x = background.x-2;
			fadeLeft.scale.set(4, background.height());
			add(fadeLeft);

			Image fadeRight = new Image(fadeLeft);
			fadeRight.x = background.x + background.width() + 2;
			fadeRight.y = background.y + background.height();
			fadeRight.angle = 180;
			add(fadeRight);
		}
		
		Image title = BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON );
		add( title );

		float topRegion = Math.max(title.height, h*0.45f);

		title.x = (w - title.width()) / 2f;
		if (landscape()) {
			title.y = (topRegion - title.height()) / 2f;
		} else {
			title.y = 20 + (topRegion - title.height() - 20) / 2f;
		}

		align(title);

//		placeTorch(title.x + 22, title.y + 46);
//		placeTorch(title.x + title.width - 22, title.y + 46);

//		Image signs = new Image( BannerSprites.get( com.quasistellar.hollowdungeon.effects.BannerSprites.Type.PIXEL_DUNGEON_SIGNS ) ) {
//			private float time = 0;
//			@Override
//			public void update() {
//				super.update();
//				am = Math.max(0f, (float)Math.sin( time += Game.elapsed ));
//				if (time >= 1.5f*Math.PI) time = 0;
//			}
//			@Override
//			public void draw() {
//				Blending.setLightMode();
//				super.draw();
//				Blending.setNormalMode();
//			}
//		};
//		signs.x = title.x + (title.width() - signs.width())/2f;
//		signs.y = title.y;
//		add( signs );
		
		IconButton btnPlay = new IconButton(){
			@Override
			protected void onClick() {
				if (GamesInProgress.checkAll().size() == 0){
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					HollowDungeon.switchScene(com.quasistellar.hollowdungeon.scenes.HeroSelectScene.class);
				} else {
					HollowDungeon.switchNoFade( StartScene.class );
				}
			}
			
			@Override
			protected boolean onLongClick() {
				//making it easier to start runs quickly while debugging
				if (DeviceCompat.isDebug()) {
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					HollowDungeon.switchScene(HeroSelectScene.class);
					//TitleScene.this.add( new WndStartGame(1) );
					return true;
				}
				return super.onLongClick();
			}
		};
		btnPlay.icon(BannerSprites.get( BannerSprites.Type.START_GAME ));
		add(btnPlay);

		IconButton btnAbout = new IconButton(){
			@Override
			protected void onClick() {
				HollowDungeon.switchScene( AboutScene.class );
			}
		};
		btnAbout.icon(BannerSprites.get( BannerSprites.Type.ABOUT ));
		add(btnAbout);

		btnPlay.setRect((w - 96) / 2f, topRegion + 5, 96, 16);
		align(btnPlay);
		btnAbout.setRect((w - 96) / 2f, btnPlay.bottom() + 10, 96, 16);

		BitmapText version = new BitmapText( "DEMO-3", pixelFont); //FIXME
		version.measure();
		version.hardlight( 0x888888 );
		version.x = w - version.width() - 4;
		version.y = h - version.height() - 2;
		add( version );
		
		int pos = 2;
		
		PrefsButton btnPrefs = new PrefsButton();
		btnPrefs.setRect( pos, 0, 16, 20 );
		add( btnPrefs );
		
		pos += btnPrefs.width();

		DiscordButton btnDiscord = new DiscordButton();
		btnDiscord.setRect( pos, 0, 16, 20 );
		add( btnDiscord );

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( w - btnExit.width(), 0 );
		add( btnExit );

//		UpdateNotification updInfo = new UpdateNotification();
//		updInfo.setRect(4, h-16, updInfo.reqWidth() + 6, 16-4);
//		add(updInfo);

		fadeIn();
	}
	
	private void placeTorch( float x, float y ) {
		com.quasistellar.hollowdungeon.effects.Fireball fb = new Fireball();
		fb.setPos( x, y );
		add( fb );
	}
	
	private static class TitleButton extends StyledButton {
		
		public TitleButton( String label ){
			this(label, 9);
		}
		
		public TitleButton( String label, int size ){
			super(Chrome.Type.GREY_BUTTON_TR, label, size);
		}
		
	}
}
