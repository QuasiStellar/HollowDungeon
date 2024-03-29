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

import com.quasistellar.hollowdungeon.journal.Journal;
import com.quasistellar.hollowdungeon.windows.WndChallenges;
import com.quasistellar.hollowdungeon.windows.WndMessage;
import com.quasistellar.hollowdungeon.windows.WndRealtime;
import com.quasistellar.hollowdungeon.windows.WndSettings;
import com.quasistellar.hollowdungeon.windows.WndStartGame;
import com.quasistellar.hollowdungeon.windows.WndTabbed;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Chrome;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.GamesInProgress;
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.actors.hero.HeroClass;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.ui.ActionIndicator;
import com.quasistellar.hollowdungeon.ui.ExitButton;
import com.quasistellar.hollowdungeon.ui.IconButton;
import com.quasistellar.hollowdungeon.ui.Icons;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.StyledButton;
import com.quasistellar.hollowdungeon.ui.Window;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;

import java.util.ArrayList;

public class HeroSelectScene extends PixelScene {

	private Image background;
	private RenderedTextBlock prompt;

	//fading UI elements
	private ArrayList<StyledButton> heroBtns = new ArrayList<>();
	private StyledButton startBtn;
	private IconButton infoButton;
	private IconButton challengeButton;
	private IconButton btnExit;

	@Override
	public void create() {
		super.create();

		Badges.loadGlobal();
		Journal.loadGlobal();

		background = new Image(HeroClass.KNIGHT.splashArt()){
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
		background.visible = false;
		PixelScene.align(background);
		add(background);

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

		prompt = PixelScene.renderTextBlock(Messages.get(WndStartGame.class, "title"), 12);
		prompt.hardlight(Window.TITLE_COLOR);
		prompt.setPos( (Camera.main.width - prompt.width())/2f, (Camera.main.height - HeroBtn.HEIGHT - prompt.height() - 4));
		PixelScene.align(prompt);
		add(prompt);

		startBtn = new StyledButton(Chrome.Type.GREY_BUTTON_TR, ""){
			@Override
			protected void onClick() {
				super.onClick();

				if (GamesInProgress.selectedClass == null) return;

				HollowDungeon.scene().addToFront(new WndRealtime(GamesInProgress.selectedClass));
			}
		};
		startBtn.icon(Icons.get(Icons.ENTER));
		startBtn.setSize(80, 21);
		startBtn.setPos((Camera.main.width - startBtn.width())/2f, (Camera.main.height - HeroBtn.HEIGHT + 2 - startBtn.height()));
		add(startBtn);
		startBtn.visible = false;

//		infoButton = new IconButton(Icons.get(Icons.INFO)){
//			@Override
//			protected void onClick() {
//				super.onClick();
//				HollowDungeon.scene().addToFront(new WndHeroInfo(GamesInProgress.selectedClass));
//			}
//		};
//		infoButton.visible = false;
//		infoButton.setSize(21, 21);
//		add(infoButton);

		HeroClass[] classes = HeroClass.values();

		int btnWidth = HeroBtn.MIN_WIDTH;
		int curX = (Camera.main.width - btnWidth * classes.length)/2;
		if (curX > 0){
			btnWidth += Math.min(curX/(classes.length/2), 15);
			curX = (Camera.main.width - btnWidth * classes.length)/2;
		}

		int heroBtnleft = curX;
		for (HeroClass cl : classes){
			HeroBtn button = new HeroBtn(cl);
			button.setRect(curX, Camera.main.height-HeroBtn.HEIGHT+3, btnWidth, HeroBtn.HEIGHT);
			curX += btnWidth;
			add(button);
			heroBtns.add(button);
		}

		challengeButton = new IconButton(
				Icons.get( HDSettings.challenges() > 0 ? Icons.CHALLENGE_ON :Icons.CHALLENGE_OFF)){
			@Override
			protected void onClick() {
				HollowDungeon.scene().addToFront(new WndChallenges(HDSettings.challenges(), true) {
					public void onBackPressed() {
						super.onBackPressed();
						icon(Icons.get(HDSettings.challenges() > 0 ? Icons.CHALLENGE_ON : Icons.CHALLENGE_OFF));
					}
				} );
			}

			@Override
			public void update() {
				if( !visible && GamesInProgress.selectedClass != null){
					visible = true;
				}
				super.update();
			}
		};
		challengeButton.setRect(heroBtnleft + 16, Camera.main.height-HeroBtn.HEIGHT-16, 21, 21);
		challengeButton.visible = false;

		if (DeviceCompat.isDebug() || Badges.isUnlocked(Badges.Badge.VICTORY)){
			add(challengeButton);
		} else {
			Dungeon.challenges = 0;
			HDSettings.challenges(0);
		}

		btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );

		PointerArea fadeResetter = new PointerArea(0, 0, Camera.main.width, Camera.main.height){
			@Override
			public boolean onSignal(PointerEvent event) {
				resetFade();
				return false;
			}
		};
		add(fadeResetter);
		resetFade();

		if (GamesInProgress.selectedClass != null){
			setSelectedHero(GamesInProgress.selectedClass);
		}

		fadeIn();

	}

	private void setSelectedHero(HeroClass cl){
		GamesInProgress.selectedClass = cl;

		background.texture( cl.splashArt() );
		background.visible = true;
		background.hardlight(1.5f,1.5f,1.5f);

		prompt.visible = false;
		startBtn.visible = true;
		startBtn.text(Messages.titleCase(cl.title()));
		startBtn.textColor(Window.TITLE_COLOR);
		startBtn.setSize(startBtn.reqWidth() + 8, 21);
		startBtn.setPos((Camera.main.width - startBtn.width())/2f, startBtn.top());
		PixelScene.align(startBtn);

//		infoButton.visible = true;
//		infoButton.setPos(startBtn.right(), startBtn.top());

		challengeButton.visible = true;
		challengeButton.setPos(startBtn.left()-challengeButton.width(), startBtn.top());
	}

	private float uiAlpha;

	@Override
	public void update() {
		super.update();
		//do not fade when a window is open
		for (Object v : members){
			if (v instanceof Window) resetFade();
		}
		if (GamesInProgress.selectedClass != null) {
			if (uiAlpha > 0f){
				uiAlpha -= Game.elapsed/4f;
			}
			float alpha = GameMath.gate(0f, uiAlpha, 1f);
			for (StyledButton b : heroBtns){
				b.alpha(alpha);
			}
			startBtn.alpha(alpha);
			btnExit.icon().alpha(alpha);
			challengeButton.icon().alpha(alpha);
//			infoButton.icon().alpha(alpha);
		}
	}

	private void resetFade(){
		//starts fading after 4 seconds, fades over 4 seconds.
		uiAlpha = 2f;
	}

	@Override
	protected void onBackPressed() {
		HollowDungeon.switchScene( TitleScene.class );
	}

	private class HeroBtn extends StyledButton {

		private HeroClass cl;

		private static final int MIN_WIDTH = 20;
		private static final int HEIGHT = 24;

		HeroBtn ( HeroClass cl ){
			super(Chrome.Type.GREY_BUTTON_TR, "");

			this.cl = cl;

			icon(new Image(cl.spritesheet(), 0, cl == HeroClass.KNIGHT ? 4 : 0, 14, cl == HeroClass.KNIGHT ? 18 : 20));

		}

		@Override
		public void update() {
			super.update();
			if (cl != GamesInProgress.selectedClass){
				if (!cl.isUnlocked()){
					icon.brightness(0.1f);
				} else {
					icon.brightness(0.6f);
				}
			} else {
				icon.brightness(1f);
			}
		}

		@Override
		protected void onClick() {
			super.onClick();

			if( !cl.isUnlocked() ){
				HollowDungeon.scene().addToFront( new WndMessage(cl.unlockMsg()));
			} else if (GamesInProgress.selectedClass == cl) {
//				HollowDungeon.scene().add(new WndHeroInfo(cl));
			} else {
				setSelectedHero(cl);
			}
		}
	}

//	private static class WndHeroInfo extends WndTabbed {
//
//		private RenderedTextBlock info;
//
//		private int WIDTH = 120;
//		private int MARGIN = 4;
//		private int INFO_WIDTH = WIDTH - MARGIN*2;
//
//		public WndHeroInfo( HeroClass cl ){
//
//			Tab tab;
//			Image[] tabIcons;
//			switch (cl){
//				case KNIGHT: default:
//					tabIcons = new Image[]{
//							new com.quasistellar.hollowdungeon.sprites.ItemSprite(ItemSpriteSheet.SEAL, null),
//							new com.quasistellar.hollowdungeon.sprites.ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD, null),
//							new com.quasistellar.hollowdungeon.sprites.ItemSprite(ItemSpriteSheet.RATION, null)
//					};
//					break;
//				case HORNET:
//					tabIcons = new Image[]{
//							new com.quasistellar.hollowdungeon.sprites.ItemSprite(ItemSpriteSheet.SPIRIT_BOW, null),
//							new com.quasistellar.hollowdungeon.sprites.ItemSprite(ItemSpriteSheet.GLOVES, null),
//							new Image(Assets.Environment.TILES_SEWERS, 112, 96, 16, 16 )
//					};
//					break;
//			}
//
//			tab = new IconTab( tabIcons[0] ){
//				@Override
//				protected void select(boolean value) {
//					super.select(value);
//					if (value){
//						info.text(Messages.get(cl, cl.name() + "_desc_item"), INFO_WIDTH);
//					}
//				}
//			};
//			add(tab);
//
//			tab = new IconTab( tabIcons[1] ){
//				@Override
//				protected void select(boolean value) {
//					super.select(value);
//					if (value){
//						info.text(Messages.get(cl, cl.name() + "_desc_loadout"), INFO_WIDTH);
//					}
//				}
//			};
//			add(tab);
//
//			tab = new IconTab( tabIcons[2] ){
//				@Override
//				protected void select(boolean value) {
//					super.select(value);
//					if (value){
//						info.text(Messages.get(cl, cl.name() + "_desc_misc"), INFO_WIDTH);
//					}
//				}
//			};
//			add(tab);
//
//			info = PixelScene.renderTextBlock(6);
//			info.setPos(MARGIN, MARGIN);
//			add(info);
//
//			select(0);
//
//		}
//
//		@Override
//		public void select(Tab tab) {
//			super.select(tab);
//			resize(WIDTH, (int)info.bottom()+MARGIN);
//			layoutTabs();
//		}
//	}
}
