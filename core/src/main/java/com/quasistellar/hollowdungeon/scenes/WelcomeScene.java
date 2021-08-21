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

import com.quasistellar.hollowdungeon.effects.Fireball;
import com.quasistellar.hollowdungeon.effects.BannerSprites;
import com.quasistellar.hollowdungeon.Chrome;
import com.quasistellar.hollowdungeon.GamesInProgress;
import com.quasistellar.hollowdungeon.Rankings;
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.ui.IconButton;
import com.quasistellar.hollowdungeon.ui.Icons;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.StyledButton;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.utils.FileUtils;

public class WelcomeScene extends com.quasistellar.hollowdungeon.scenes.PixelScene {

	private static int LATEST_UPDATE = HollowDungeon.v0_3;

	@Override
	public void create() {
		super.create();

		final int previousVersion = HDSettings.version();

		if (HollowDungeon.versionCode == previousVersion) {
			HollowDungeon.switchNoFade(TitleScene.class);
			return;
		}

		uiCamera.visible = false;

		int w = Camera.main.width;
		int h = Camera.main.height;

		Image title = BannerSprites.get( BannerSprites.Type.PIXEL_DUNGEON );
		title.brightness(0.6f);
		add( title );
		
		float topRegion = Math.max(title.height, h*0.45f);
		
		title.x = (w - title.width()) / 2f;
		if (landscape()) {
			title.y = (topRegion - title.height()) / 2f;
		} else {
			title.y = 20 + (topRegion - title.height() - 20) / 2f;
		}

		align(title);

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
		
		IconButton okay = new IconButton(){
			@Override
			protected void onClick() {
				super.onClick();
				if (previousVersion == 0){
					HDSettings.version(HollowDungeon.versionCode);
					GamesInProgress.selectedClass = null;
					GamesInProgress.curSlot = 1;
					HollowDungeon.switchScene(HeroSelectScene.class);
				} else {
					updateVersion(previousVersion);
					HollowDungeon.switchScene(TitleScene.class);
				}
			}
		};
		okay.icon(BannerSprites.get( BannerSprites.Type.START_GAME ));

		//FIXME these buttons are very low on 18:9 devices
//		if (previousVersion != 0){
//			StyledButton changes = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(TitleScene.class, "changes")){
//				@Override
//				protected void onClick() {
//					super.onClick();
//					updateVersion(previousVersion);
//					HollowDungeon.switchScene(ChangesScene.class);
//				}
//			};
//			okay.setRect(title.x, h-25, (title.width()/2)-2, 21);
//			add(okay);
//
//			changes.setRect(okay.right()+2, h-25, (title.width()/2)-2, 21);
//			changes.icon(Icons.get(Icons.CHANGES));
//			add(changes);
//		} else {
			okay.setRect(title.x, h-25, title.width(), 21);
			add(okay);
//		}

		RenderedTextBlock text = PixelScene.renderTextBlock(6);
		String message;
		if (previousVersion == 0) {
			message = Messages.get(this, "welcome_msg");
		} else if (previousVersion <= HollowDungeon.versionCode) {
			if (previousVersion < LATEST_UPDATE){
				message = Messages.get(this, "update_intro");
				message += "\n\n" + Messages.get(this, "update_msg");
			} else {
				//TODO: change the messages here in accordance with the type of patch.
				message = Messages.get(this, "patch_intro");
				message += "\n";
				//message += "\n" + Messages.get(this, "patch_balance");
//				message += "\n" + Messages.get(this, "patch_bugfixes");
//				message += "\n" + Messages.get(this, "patch_translations");

			}
		} else {
			message = Messages.get(this, "what_msg");
		}
		text.text(message, w-20);
		float textSpace = h - title.y - (title.height() - 10) - okay.height() - 2;
		text.setPos((w - text.width()) / 2f, title.y+(title.height() - 10) + ((textSpace - text.height()) / 2));
		add(text);

	}

	private void updateVersion(int previousVersion){
		
		//update rankings, to update any data which may be outdated
		if (previousVersion < LATEST_UPDATE){
			try {
				Rankings.INSTANCE.load();
				for (Rankings.Record rec : Rankings.INSTANCE.records.toArray(new Rankings.Record[0])){
					try {
						Rankings.INSTANCE.loadGameData(rec);
						Rankings.INSTANCE.saveGameData(rec);
					} catch (Exception e) {
						//if we encounter a fatal per-record error, then clear that record
						Rankings.INSTANCE.records.remove(rec);
						HollowDungeon.reportException(e);
					}
				}
				Rankings.INSTANCE.save();
			} catch (Exception e) {
				//if we encounter a fatal error, then just clear the rankings
				FileUtils.deleteFile( Rankings.RANKINGS_FILE );
				HollowDungeon.reportException(e);
			}
		}
		
		HDSettings.version(HollowDungeon.versionCode);
	}

	private void placeTorch( float x, float y ) {
		com.quasistellar.hollowdungeon.effects.Fireball fb = new Fireball();
		fb.setPos( x, y );
		add( fb );
	}
	
}
