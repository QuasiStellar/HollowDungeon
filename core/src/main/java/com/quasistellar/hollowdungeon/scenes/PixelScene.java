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

package com.quasistellar.hollowdungeon.scenes;

import com.quasistellar.hollowdungeon.effects.BadgeBanner;
import com.quasistellar.hollowdungeon.messages.Languages;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Window;
import com.watabou.glwrap.Blending;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.BitmapText.Font;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Scene;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.BitmapCache;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class PixelScene extends Scene {

	// Minimum virtual display size for portrait orientation
	public static final float MIN_WIDTH_P        = 135;
	public static final float MIN_HEIGHT_P        = 225;

	// Minimum virtual display size for landscape orientation
	public static final float MIN_WIDTH_L        = 240;
	public static final float MIN_HEIGHT_L        = 160;

	public static int defaultZoom = 0;
	public static int maxDefaultZoom = 0;
	public static int maxScreenZoom = 0;
	public static float minZoom;
	public static float maxZoom;

	public static Camera uiCamera;

	//stylized 3x5 bitmapped pixel font. Only latin characters supported.
	public static BitmapText.Font pixelFont;

	@Override
	public void create() {

		super.create();

		GameScene.scene = null;

		float minWidth, minHeight;
		if (landscape()) {
			minWidth = MIN_WIDTH_L;
			minHeight = MIN_HEIGHT_L;
		} else {
			minWidth = MIN_WIDTH_P;
			minHeight = MIN_HEIGHT_P;
		}

		maxDefaultZoom = (int)Math.min(Game.width/minWidth, Game.height/minHeight);
		maxScreenZoom = (int)Math.min(Game.dispWidth/minWidth, Game.dispHeight/minHeight);
		defaultZoom = HDSettings.scale();

		if (!DeviceCompat.isDesktop()) {
			defaultZoom = (int) Math.ceil( Game.density * 2 );
		} else {
			if (defaultZoom < Math.ceil( Game.density * 2 ) || defaultZoom > maxDefaultZoom){
				defaultZoom = (int) GameMath.gate(3, (int)Math.ceil( Game.density * 2.5 ), maxDefaultZoom);
			}
		}

		minZoom = 1;
		maxZoom = defaultZoom * 2;

		Camera.reset( new PixelCamera( defaultZoom ) );

		float uiZoom = defaultZoom;
		uiCamera = Camera.createFullscreen( uiZoom );
		Camera.add( uiCamera );

		if (pixelFont == null) {

			// 3x5 (6)
			pixelFont = Font.colorMarked(
				BitmapCache.get( Assets.Fonts.PIXELFONT), 0x00000000, BitmapText.Font.LATIN_FULL );
			pixelFont.baseLine = 6;
			pixelFont.tracking = -1;
			
		}
		
		//set up the texture size which rendered text will use for any new glyphs.
		int renderedTextPageSize;
		if (defaultZoom <= 3){
			renderedTextPageSize = 256;
		} else if (defaultZoom <= 8){
			renderedTextPageSize = 512;
		} else {
			renderedTextPageSize = 1024;
		}
		//asian languages have many more unique characters, so increase texture size to anticipate that
		if (false){
			renderedTextPageSize *= 2;
		}
		Game.platform.setupFontGenerators(renderedTextPageSize, HDSettings.systemFont());
		
	}
	
	//FIXME this system currently only works for a subset of windows
	private static ArrayList<Class<?extends Window>> savedWindows = new ArrayList<>();
	private static Class<?extends PixelScene> savedClass = null;
	
	public void saveWindows(){
		savedWindows.clear();
		savedClass = getClass();
		for (Gizmo g : members.toArray(new Gizmo[0])){
			if (g instanceof Window){
				savedWindows.add((Class<? extends Window>) g.getClass());
			}
		}
	}
	
	public void restoreWindows(){
		if (getClass().equals(savedClass)){
			for (Class<?extends Window> w : savedWindows){
				try{
					add(Reflection.newInstanceUnhandled(w));
				} catch (Exception e){
					//window has no public zero-arg constructor, just eat the exception
				}
			}
		}
		savedWindows.clear();
	}

	@Override
	public void destroy() {
		super.destroy();
		PointerEvent.clearListeners();
	}

	public static RenderedTextBlock renderTextBlock(int size ){
		return renderTextBlock("", size);
	}

	public static RenderedTextBlock renderTextBlock(String text, int size ){
		RenderedTextBlock result = new RenderedTextBlock( text, size*defaultZoom);
		result.zoom(1/(float)defaultZoom);
		return result;
	}

	public static RenderedTextBlock renderTextBlock(String text, int size, int maxWidth ){
		RenderedTextBlock result = new RenderedTextBlock( text, size*defaultZoom, maxWidth);
		result.zoom(1/(float)defaultZoom);
		return result;
	}

	/**
	 * These methods align UI elements to device pixels.
	 * e.g. if we have a scale of 3x then valid positions are #.0, #.33, #.67
	 */

	public static float align( float pos ) {
		return Math.round(pos * defaultZoom) / (float)defaultZoom;
	}

	public static float align( Camera camera, float pos ) {
		return Math.round(pos * camera.zoom) / camera.zoom;
	}

	public static void align( Visual v ) {
		v.x = align( v.x );
		v.y = align( v.y );
	}

	public static void align( Component c ){
		c.setPos(align(c.left()), align(c.top()));
	}

	public static boolean noFade = false;
	protected void fadeIn() {
		if (noFade) {
			noFade = false;
		} else {
			fadeIn( 0xFF000000, false );
		}
	}
	
	protected void fadeIn( int color, boolean light ) {
		add( new Fader( color, light ) );
	}
	
	public static void showBadge( Badges.Badge badge ) {
		com.quasistellar.hollowdungeon.effects.BadgeBanner banner = BadgeBanner.show( badge.image );
		banner.camera = uiCamera;
		banner.x = align( banner.camera, (banner.camera.width - banner.width) / 2 );
		banner.y = align( banner.camera, (banner.camera.height - banner.height) / 3 );
		Game.scene().add( banner );
	}
	
	protected static class Fader extends ColorBlock {
		
		private static float FADE_TIME = 1f;
		
		private boolean light;
		
		private float time;
		
		public Fader( int color, boolean light ) {
			super( uiCamera.width, uiCamera.height, color );
			
			this.light = light;
			
			camera = uiCamera;
			
			alpha( 1f );
			time = FADE_TIME;
		}
		
		@Override
		public void update() {
			
			super.update();
			
			if ((time -= Game.elapsed) <= 0) {
				alpha( 0f );
				parent.remove( this );
			} else {
				alpha( time / FADE_TIME );
			}
		}
		
		@Override
		public void draw() {
			if (light) {
				Blending.setLightMode();
				super.draw();
				Blending.setNormalMode();
			} else {
				super.draw();
			}
		}
	}
	
	private static class PixelCamera extends Camera {
		
		public PixelCamera( float zoom ) {
			super(
				(int)(Game.width - Math.ceil( Game.width / zoom ) * zoom) / 2,
				(int)(Game.height - Math.ceil( Game.height / zoom ) * zoom) / 2,
				(int)Math.ceil( Game.width / zoom ),
				(int)Math.ceil( Game.height / zoom ), zoom );
			fullScreen = true;
		}
		
		@Override
		protected void updateMatrix() {
			float sx = align( this, scroll.x + shakeX );
			float sy = align( this, scroll.y + shakeY );
			
			matrix[0] = +zoom * invW2;
			matrix[5] = -zoom * invH2;
			
			matrix[12] = -1 + x * invW2 - sx * matrix[0];
			matrix[13] = +1 - y * invH2 - sy * matrix[5];
			
		}
	}
}
