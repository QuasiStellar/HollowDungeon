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

package com.quasistellar.hollowdungeon.windows;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.GamesInProgress;
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.actors.hero.HeroClass;
import com.quasistellar.hollowdungeon.journal.Journal;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.InterlevelScene;
import com.quasistellar.hollowdungeon.scenes.IntroScene;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.ui.ActionIndicator;
import com.quasistellar.hollowdungeon.ui.IconButton;
import com.quasistellar.hollowdungeon.ui.Icons;
import com.quasistellar.hollowdungeon.ui.RedButton;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

public class WndStartGame extends Window {
	
	private static final int WIDTH    = 120;
	private static final int HEIGHT   = 140;

	public WndStartGame(final int slot){
		
		Badges.loadGlobal();
		Journal.loadGlobal();
		
		RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 12 );
		title.hardlight(Window.TITLE_COLOR);
		title.setPos( (WIDTH - title.width())/2f, 3);
		PixelScene.align(title);
		add(title);
		
		float heroBtnSpacing = (WIDTH - 4*HeroBtn.WIDTH)/5f;
		
		float curX = heroBtnSpacing;
		for (HeroClass cl : HeroClass.values()){
			HeroBtn button = new HeroBtn(cl);
			button.setRect(curX, title.height() + 7, HeroBtn.WIDTH, HeroBtn.HEIGHT);
			curX += HeroBtn.WIDTH + heroBtnSpacing;
			add(button);
		}
		
		ColorBlock separator = new ColorBlock(1, 1, 0xFF222222);
		separator.size(WIDTH, 1);
		separator.x = 0;
		separator.y = title.bottom() + 6 + HeroBtn.HEIGHT;
		add(separator);
		
		HeroPane ava = new HeroPane();
		ava.setRect(20, separator.y + 2, WIDTH-30, 80);
		add(ava);
		
		RedButton start = new RedButton(Messages.get(this, "start")){
			@Override
			protected void onClick() {
				if (GamesInProgress.selectedClass == null) return;
				
				super.onClick();
				
				GamesInProgress.curSlot = slot;
				Dungeon.hero = null;
				ActionIndicator.action = null;
				InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
				
				if (HDSettings.intro()) {
					HDSettings.intro( false );
					Game.switchScene( IntroScene.class );
				} else {
					Game.switchScene( InterlevelScene.class );
				}
			}
			
			@Override
			public void update() {
				if( !visible && GamesInProgress.selectedClass != null){
					visible = true;
				}
				super.update();
			}
		};
		start.visible = false;
		start.setRect(0, HEIGHT - 20, WIDTH, 20);
		add(start);
		
		if (DeviceCompat.isDebug() || Badges.isUnlocked(Badges.Badge.VICTORY)){
			IconButton challengeButton = new IconButton(
					Icons.get( HDSettings.challenges() > 0 ? Icons.CHALLENGE_ON :Icons.CHALLENGE_OFF)){
				@Override
				protected void onClick() {
					HollowDungeon.scene().addToFront(new WndChallenges(HDSettings.challenges(), true) {
						public void onBackPressed() {
							super.onBackPressed();
							if (parent != null) {
								icon(Icons.get(HDSettings.challenges() > 0 ?
										Icons.CHALLENGE_ON : Icons.CHALLENGE_OFF));
							}
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
			challengeButton.setRect(WIDTH - 20, HEIGHT - 20, 20, 20);
			challengeButton.visible = false;
			add(challengeButton);
			
		} else {
			Dungeon.challenges = 0;
			HDSettings.challenges(0);
		}
		
		resize(WIDTH, HEIGHT);
		
	}
	
	private static class HeroBtn extends Button {
		
		private HeroClass cl;
		
		private Image hero;
		
		private static final int WIDTH = 24;
		private static final int HEIGHT = 16;
		
		HeroBtn ( HeroClass cl ){
			super();
			
			this.cl = cl;

			add(hero = new Image(cl.spritesheet(), 0, 90, 12, 15));
			
		}
		
		@Override
		protected void layout() {
			super.layout();
			if (hero != null){
				hero.x = x + (width - hero.width()) / 2f;
				hero.y = y + (height - hero.height()) / 2f;
				PixelScene.align(hero);
			}
		}
		
		@Override
		public void update() {
			super.update();
			if (cl != GamesInProgress.selectedClass){
				if (!cl.isUnlocked()){
					hero.brightness(0.3f);
				} else {
					hero.brightness(0.6f);
				}
			} else {
				hero.brightness(1f);
			}
		}
		
		@Override
		protected void onClick() {
			super.onClick();
			
			if( !cl.isUnlocked() ){
				HollowDungeon.scene().addToFront( new com.quasistellar.hollowdungeon.windows.WndMessage(cl.unlockMsg()));
			} else {
				GamesInProgress.selectedClass = cl;
			}
		}
	}
	
	private class HeroPane extends Component {
		
		private HeroClass cl;
		
		private Image avatar;
		
		private IconButton heroItem;
		private IconButton heroLoadout;
		private IconButton heroMisc;
		private IconButton heroSubclass;
		
		private RenderedTextBlock name;
		
		private static final int BTN_SIZE = 20;
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			avatar = new Image(Assets.Sprites.AVATARS);
			avatar.scale.set(2f);
			add(avatar);
			
			heroItem = new IconButton(){
				@Override
				protected void onClick() {
					if (cl == null) return;
					HollowDungeon.scene().addToFront(new com.quasistellar.hollowdungeon.windows.WndMessage(Messages.get(cl, cl.name() + "_desc_item")));
				}
			};
			heroItem.setSize(BTN_SIZE, BTN_SIZE);
			add(heroItem);
			
			heroLoadout = new IconButton(){
				@Override
				protected void onClick() {
					if (cl == null) return;
					HollowDungeon.scene().addToFront(new com.quasistellar.hollowdungeon.windows.WndMessage(Messages.get(cl, cl.name() + "_desc_loadout")));
				}
			};
			heroLoadout.setSize(BTN_SIZE, BTN_SIZE);
			add(heroLoadout);
			
			heroMisc = new IconButton(){
				@Override
				protected void onClick() {
					if (cl == null) return;
					HollowDungeon.scene().addToFront(new com.quasistellar.hollowdungeon.windows.WndMessage(Messages.get(cl, cl.name() + "_desc_misc")));
				}
			};
			heroMisc.setSize(BTN_SIZE, BTN_SIZE);
			add(heroMisc);
			
			name = PixelScene.renderTextBlock(12);
			add(name);
			
			visible = false;
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			avatar.x = x;
			avatar.y = y + (height - avatar.height() - name.height() - 4)/2f;
			PixelScene.align(avatar);
			
			name.setPos(
					x + (avatar.width() - name.width())/2f,
					avatar.y + avatar.height() + 3
			);
			PixelScene.align(name);
			
			heroItem.setPos(x + width - BTN_SIZE, y);
			heroLoadout.setPos(x + width - BTN_SIZE, heroItem.bottom());
			heroMisc.setPos(x + width - BTN_SIZE, heroLoadout.bottom());
			heroSubclass.setPos(x + width - BTN_SIZE, heroMisc.bottom());
		}
		
		@Override
		public synchronized void update() {
			super.update();
			if (GamesInProgress.selectedClass != cl){
				cl = GamesInProgress.selectedClass;
				if (cl != null) {
					avatar.frame(cl.ordinal() * 24, 0, 24, 32);
					
					name.text(Messages.capitalize(cl.title()));
					
					switch(cl){
						case KNIGHT:
							heroItem.icon(new ItemSprite(ItemSpriteSheet.SEAL, null));
							heroLoadout.icon(new ItemSprite(ItemSpriteSheet.WORN_SHORTSWORD, null));
							heroMisc.icon(new ItemSprite(ItemSpriteSheet.RATION, null));
							break;
						case HORNET:
							heroItem.icon(new ItemSprite(ItemSpriteSheet.SPIRIT_BOW, null));
							heroLoadout.icon(new ItemSprite(ItemSpriteSheet.GLOVES, null));
							heroMisc.icon(new Image(Assets.Environment.TILES_SEWERS, 112, 96, 16, 16 ));
							break;
					}
					
					layout();
					
					visible = true;
				} else {
					visible = false;
				}
			}
		}
	}

}
