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
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.scenes.HeroSelectScene;
import com.quasistellar.hollowdungeon.scenes.InterlevelScene;
import com.quasistellar.hollowdungeon.scenes.RankingsScene;
import com.quasistellar.hollowdungeon.scenes.TitleScene;
import com.quasistellar.hollowdungeon.ui.RedButton;
import com.quasistellar.hollowdungeon.ui.Window;
import com.watabou.noosa.Game;

import java.io.IOException;

public class WndGame extends Window {

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 20;
	private static final int GAP		= 2;
	
	private int pos;
	
	public WndGame() {
		
		super();
		
		addButton( new RedButton( Messages.get(this, "settings") ) {
			@Override
			protected void onClick() {
				hide();
				GameScene.show(new WndSettings());
			}
		});

		// Challenges window
		if (Dungeon.challenges > 0) {
			addButton( new RedButton( Messages.get(this, "challenges") ) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndChallenges( Dungeon.challenges, false ) );
				}
			} );
		}

		// Restart
		if (Dungeon.hero == null || !Dungeon.hero.isAlive()) {
			
			RedButton btnStart;
			addButton( btnStart = new RedButton( Messages.get(this, "start") ) {
				@Override
				protected void onClick() {
					InterlevelScene.noStory = true;
					GamesInProgress.selectedClass = Dungeon.hero.heroClass;
					GamesInProgress.curSlot = GamesInProgress.firstEmpty();
					HollowDungeon.switchScene(HeroSelectScene.class);
				}
			} );
			btnStart.textColor(Window.GREEN_COLOR);
			
//			addButton( new RedButton( Messages.get(this, "rankings") ) {
//				@Override
//				protected void onClick() {
//					InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
//					Game.switchScene( RankingsScene.class );
//				}
//			} );
		}

		addButtons(
				// Main menu
				new RedButton( Messages.get(this, "menu") ) {
					@Override
					protected void onClick() {
						try {
							Dungeon.saveAll();
						} catch (IOException e) {
							HollowDungeon.reportException(e);
						}
						Game.switchScene(TitleScene.class);
					}
				},
				// Quit
				new RedButton( Messages.get(this, "exit") ) {
					@Override
					protected void onClick() {
						try {
							Dungeon.saveAll();
						} catch (IOException e) {
							HollowDungeon.reportException(e);
						}
						Game.instance.finish();
					}
				}
		);

		// Cancel
		addButton( new RedButton( Messages.get(this, "return") ) {
			@Override
			protected void onClick() {
				hide();
			}
		} );
		
		resize( WIDTH, pos );
	}
	
	private void addButton( RedButton btn ) {
		add( btn );
		btn.setRect( 0, pos > 0 ? pos += GAP : 0, WIDTH, BTN_HEIGHT );
		pos += BTN_HEIGHT;
	}

	private void addButtons( RedButton btn1, RedButton btn2 ) {
		add( btn1 );
		btn1.setRect( 0, pos > 0 ? pos += GAP : 0, (WIDTH - GAP) / 2, BTN_HEIGHT );
		add( btn2 );
		btn2.setRect( btn1.right() + GAP, btn1.top(), WIDTH - btn1.right() - GAP, BTN_HEIGHT );
		pos += BTN_HEIGHT;
	}
}
