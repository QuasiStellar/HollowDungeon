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

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.GamesInProgress;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.InterlevelScene;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.scenes.StartScene;
import com.quasistellar.hollowdungeon.sprites.HeroSprite;
import com.quasistellar.hollowdungeon.ui.ActionIndicator;
import com.quasistellar.hollowdungeon.ui.RedButton;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Window;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Button;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.Locale;

public class WndGameInProgress extends Window {
	
	private static final int WIDTH    = 120;
	private static final int HEIGHT   = 120;
	
	private int GAP	  = 6;
	
	private float pos;
	
	public WndGameInProgress(final int slot){
		
		final GamesInProgress.Info info = GamesInProgress.check(slot);

		String className = info.heroClass.title();
		
		com.quasistellar.hollowdungeon.windows.IconTitle title = new IconTitle();
		title.icon( HeroSprite.avatar(info.heroClass, info.armorTier) );
		title.label((Messages.get(this, "title", className)).toUpperCase(Locale.ENGLISH));
		title.color(Window.SHPX_COLOR);
		title.setRect( 0, 0, WIDTH, 0 );
		add(title);
		
		//manually produces debug information about a run, mainly useful for levelgen errors
		Button debug = new Button(){
			@Override
			protected boolean onLongClick() {
				try {
					Bundle bundle = FileUtils.bundleFromFile(GamesInProgress.gameFile(slot));
					HollowDungeon.scene().addToFront(new WndMessage("_Debug Info:_\n\n" +
							"Version: " + Game.version + " (" + Game.versionCode + ")\n" +
							"Seed: " + bundle.getLong("seed") + "\n" +
							"Challenge Mask: " + info.challenges));
				} catch (IOException ignored) { }
				return true;
			}
		};
		debug.setRect(0, 0, title.imIcon.width(), title.imIcon.height);
		add(debug);
		
		if (info.challenges > 0) GAP -= 2;
		
		pos = title.bottom() + GAP;
		
		if (info.challenges > 0) {
			RedButton btnChallenges = new RedButton( Messages.get(this, "challenges") ) {
				@Override
				protected void onClick() {
					Game.scene().add( new WndChallenges( info.challenges, false ) );
				}
			};
			float btnW = btnChallenges.reqWidth() + 2;
			btnChallenges.setRect( (WIDTH - btnW)/2, pos, btnW , btnChallenges.reqHeight() + 2 );
			add( btnChallenges );
			
			pos = btnChallenges.bottom() + GAP;
		}
		
		pos += GAP;

		if (info.shld > 0) statSlot( Messages.get(this, "health"), info.hp + "+" + info.shld + "/" + info.ht );
		else statSlot( Messages.get(this, "health"), (info.hp) + "/" + info.ht );
		statSlot( Messages.get(this, "soul"), (info.mp) + "/" + info.mm );
		
		pos += GAP;
		statSlot( Messages.get(this, "gold"), info.goldCollected );
		
		RedButton cont = new RedButton(Messages.get(this, "continue")){
			@Override
			protected void onClick() {
				super.onClick();
				
				GamesInProgress.curSlot = slot;
				
				Dungeon.hero = null;
				ActionIndicator.action = null;
				InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
				HollowDungeon.switchScene(InterlevelScene.class);
			}
		};
		
		RedButton erase = new RedButton( Messages.get(this, "erase")){
			@Override
			protected void onClick() {
				super.onClick();
				
				HollowDungeon.scene().add(new WndOptions(
						Messages.get(WndGameInProgress.class, "erase_warn_title"),
						Messages.get(WndGameInProgress.class, "erase_warn_body"),
						Messages.get(WndGameInProgress.class, "erase_warn_yes"),
						Messages.get(WndGameInProgress.class, "erase_warn_no") ) {
					@Override
					protected void onSelect( int index ) {
						if (index == 0) {
							FileUtils.deleteDir(GamesInProgress.gameFolder(slot));
							GamesInProgress.setUnknown(slot);
							HollowDungeon.switchNoFade(StartScene.class);
						}
					}
				} );
			}
		};
		
		cont.setRect(0, HEIGHT - 20, WIDTH/2 -1, 20);
		add(cont);
		
		erase.setRect(WIDTH/2 + 1, HEIGHT-20, WIDTH/2 - 1, 20);
		add(erase);
		
		resize(WIDTH, HEIGHT);
	}
	
	private void statSlot( String label, String value ) {
		
		RenderedTextBlock txt = PixelScene.renderTextBlock( label, 8 );
		txt.setPos(0, pos);
		add( txt );
		
		txt = PixelScene.renderTextBlock( value, 8 );
		txt.setPos(WIDTH * 0.6f, pos);
		PixelScene.align(txt);
		add( txt );
		
		pos += GAP + txt.height();
	}
	
	private void statSlot( String label, int value ) {
		statSlot( label, Integer.toString( value ) );
	}
}
