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

import com.quasistellar.hollowdungeon.actors.blobs.WaterOfAwareness;
import com.quasistellar.hollowdungeon.actors.mobs.Shade;
import com.quasistellar.hollowdungeon.items.FCMap;
import com.quasistellar.hollowdungeon.windows.WndError;
import com.quasistellar.hollowdungeon.windows.WndStory;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.GamesInProgress;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.ui.GameLog;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.utils.DeviceCompat;

import java.io.FileNotFoundException;
import java.io.IOException;

public class InterlevelScene extends PixelScene {
	
	//slow fade on entering a new region
	private static final float SLOW_FADE = 1f; //.33 in, 1.33 steady, .33 out, 2 seconds total
	//norm fade when loading, returning, or descending to a new floor
	private static final float NORM_FADE = 0.67f; //.33 in, .67 steady, .33 out, 1.33 seconds total
	//fast fade when ascending, or descending to a floor you've been on
	private static final float FAST_FADE = 0.50f; //.33 in, .33 steady, .33 out, 1 second total
	
	private static float fadeTime;
	
	public enum Mode {
		DESCEND, TRANSIT, ASCEND, CONTINUE, RESURRECT, RETURN, RESET, NONE
	}
	public static Mode mode;
	
	public static String returnLocation;
	public static int returnPos;
	
	public static boolean noStory = false;
	
	private enum Phase {
		FADE_IN, STATIC, FADE_OUT
	}
	private Phase phase;
	private float timeLeft;
	
	private RenderedTextBlock message;
	
	private static Thread thread;
	private static Exception error = null;
	private float waitingTime;
	
	@Override
	public void create() {
		super.create();
		
		String loadingAsset;
		String loadingLocation;
		final float scrollSpeed;
		fadeTime = NORM_FADE;
		switch (mode){
			default:
				loadingLocation = Dungeon.location;
				scrollSpeed = 0;
				break;
			case CONTINUE:
				loadingLocation = GamesInProgress.check(GamesInProgress.curSlot).location;
				scrollSpeed = 5;
				break;
			case DESCEND:
				if (Dungeon.hero == null){
					loadingLocation = "King's Pass";
					fadeTime = SLOW_FADE;
				} else {
					loadingLocation = Dungeon.exitDestination;
					//TODO: utilize it
					if (false) {
						fadeTime = FAST_FADE;
					} else if (false) {
						fadeTime = SLOW_FADE;
					}
				}
				scrollSpeed = 5;
				break;
			case TRANSIT:
				scrollSpeed = 5;
				break;
			case RETURN:
				loadingLocation = returnLocation;
				scrollSpeed = 15;
				break;
		}
		loadingAsset = Assets.Interfaces.SHADOW;
		
		//speed up transition when debugging
		if (DeviceCompat.isDebug()){
			fadeTime /= 2;
		}
		
		SkinnedBlock bg = new SkinnedBlock(Camera.main.width, Camera.main.height, loadingAsset ){
			@Override
			protected NoosaScript script() {
				return NoosaScriptNoLighting.get();
			}
			
			@Override
			public void draw() {
				Blending.disable();
				super.draw();
				Blending.enable();
			}
			
			@Override
			public void update() {
				super.update();
				offset(0, Game.elapsed * scrollSpeed);
			}
		};
		bg.scale(4, 4);
		add(bg);
		
		Image im = new Image(TextureCache.createGradient(0xAA000000, 0xBB000000, 0xCC000000, 0xDD000000, 0xFF000000)){
			@Override
			public void update() {
				super.update();
				if (phase == Phase.FADE_IN)         aa = Math.max( 0, (timeLeft - (fadeTime - 0.333f)));
				else if (phase == Phase.FADE_OUT)   aa = Math.max( 0, (0.333f - timeLeft));
				else                                aa = 0;
			}
		};
		im.angle = 90;
		im.x = Camera.main.width;
		im.scale.x = Camera.main.height/5f;
		im.scale.y = Camera.main.width;
		add(im);

		String text = Messages.get(Mode.class, mode.name());
		
		message = PixelScene.renderTextBlock( text, 9 );
		message.setPos(
				(Camera.main.width - message.width()) / 2,
				(Camera.main.height - message.height()) / 2
		);
		align(message);
		add( message );
		
		phase = Phase.FADE_IN;
		timeLeft = fadeTime;
		
		if (thread == null) {
			thread = new Thread() {
				@Override
				public void run() {
					
					try {

						if (Dungeon.hero != null){
							Dungeon.hero.spendToWhole();
						}
						Actor.fixTime();

						switch (mode) {
							case DESCEND:
								descend();
								break;
							case TRANSIT:
								transit();
								break;
							case ASCEND:
								ascend();
								break;
							case CONTINUE:
								restore();
								break;
							case RESURRECT:
								resurrect();
								break;
							case RETURN:
								returnTo();
								break;
							case RESET:
								reset();
								break;
						}
						
					} catch (Exception e) {
						
						error = e;
						
					}
					
					if (phase == Phase.STATIC && error == null) {
						phase = Phase.FADE_OUT;
						timeLeft = fadeTime;
					}
				}
			};
			thread.start();
		}
		waitingTime = 0f;
	}
	
	@Override
	public void update() {
		super.update();

		waitingTime += Game.elapsed;
		
		float p = timeLeft / fadeTime;
		
		switch (phase) {
		
		case FADE_IN:
			message.alpha( 1 - p );
			if ((timeLeft -= Game.elapsed) <= 0) {
				if (!thread.isAlive() && error == null) {
					phase = Phase.FADE_OUT;
					timeLeft = fadeTime;
				} else {
					phase = Phase.STATIC;
				}
			}
			break;
			
		case FADE_OUT:
			message.alpha( p );
			
			if ((timeLeft -= Game.elapsed) <= 0) {
				Game.switchScene( GameScene.class );
				thread = null;
				error = null;
			}
			break;
			
		case STATIC:
			if (error != null) {
				String errorMsg;
				if (error instanceof FileNotFoundException)     errorMsg = Messages.get(this, "file_not_found");
				else if (error instanceof IOException)          errorMsg = Messages.get(this, "io_error");
				else if (error.getMessage() != null &&
						error.getMessage().equals("old save")) errorMsg = Messages.get(this, "io_error");

				else throw new RuntimeException("fatal error occured while moving between floors. " +
							"Seed:" + Dungeon.seed + " location:" + Dungeon.location, error);

				add( new WndError( errorMsg ) {
					public void onBackPressed() {
						super.onBackPressed();
						Game.switchScene( StartScene.class );
					}
				} );
				thread = null;
				error = null;
			} else if (thread != null && (int)waitingTime == 10){
				waitingTime = 11f;
				String s = "";
				for (StackTraceElement t : thread.getStackTrace()){
					s += "\n";
					s += t.toString();
				}
				HollowDungeon.reportException(
						new RuntimeException("waited more than 10 seconds on levelgen. " +
								"Seed:" + Dungeon.seed + " location:" + Dungeon.location + " trace:" +
								s)
				);
			}
			break;
		}
	}

	private void descend() throws IOException {

		boolean starting = false;
		if (Dungeon.hero == null) {
			starting = true;
			Mob.clearHeldAllies();
			Dungeon.init();
			if (noStory) {
				Dungeon.chapters.add( WndStory.ID_SEWERS );
				noStory = false;
			}
			GameLog.wipe();
		} else {
			Mob.holdAllies( Dungeon.level );
			Dungeon.saveAll();
		}

		Level level;
		Dungeon.location = Dungeon.exitDestination;
		if (Dungeon.levelsToRebuild.contains(Dungeon.location)) {
			level = Dungeon.newLevel();
		} else {
			try {
				level = Dungeon.loadLevel( GamesInProgress.curSlot );
			} catch (IOException e) {
				level = Dungeon.newLevel();
			}
		}
		Dungeon.changeConnections(Dungeon.location);
		// FIXME: magical number
		Dungeon.switchLevel( level, starting ? 233 : level.entrance );
		if (Dungeon.hero.belongings.getSimilar(new FCMap()) != null) {
			WaterOfAwareness.affectHeroAnyway(new WaterOfAwareness());
		}
		removeOldShades();
		checkForLevelReset(level, starting ? 233 : level.entrance);
	}

	private void transit() throws IOException {

		Mob.holdAllies( Dungeon.level );
		Dungeon.saveAll();

		Level level;
		Dungeon.location = Dungeon.transitionDestination;
		if (Dungeon.levelsToRebuild.contains(Dungeon.location)) {
			level = Dungeon.newLevel();
			Dungeon.levelsToRebuild.remove(Dungeon.location);
		} else {
			try {
				level = Dungeon.loadLevel(GamesInProgress.curSlot);
			} catch (IOException e) {
				level = Dungeon.newLevel();
			}
		}
		Dungeon.changeConnections(Dungeon.location);
		Dungeon.switchLevel( level, level.transition );
		if (Dungeon.hero.belongings.getSimilar(new FCMap()) != null) {
			WaterOfAwareness.affectHeroAnyway(new WaterOfAwareness());
		}
		removeOldShades();
		checkForLevelReset(level, level.transition);
	}
	
	private void ascend() throws IOException {
		
		Mob.holdAllies( Dungeon.level );

		Dungeon.saveAll();
		Dungeon.location = Dungeon.entranceDestination;

		Level level;
		if (Dungeon.levelsToRebuild.contains(Dungeon.location)) {
			level = Dungeon.newLevel();
			Dungeon.levelsToRebuild.remove(Dungeon.location);
		} else {
			try {
				level = Dungeon.loadLevel(GamesInProgress.curSlot);
			} catch (IOException e) {
				level = Dungeon.newLevel();
			}
		}
		Dungeon.changeConnections(Dungeon.location);
		Dungeon.switchLevel( level, level.exit );
		if (Dungeon.hero.belongings.getSimilar(new FCMap()) != null) {
			WaterOfAwareness.affectHeroAnyway(new WaterOfAwareness());
		}
		removeOldShades();
		checkForLevelReset(level, level.exit);
	}
	
	private void returnTo() throws IOException {
		
		Mob.holdAllies( Dungeon.level );

		Dungeon.saveAll();
		Dungeon.location = returnLocation;
		Level level;
		if (Dungeon.levelsToRebuild.contains(Dungeon.location)) {
			level = Dungeon.newLevel();
			Dungeon.levelsToRebuild.remove(Dungeon.location);
		} else {
			level = Dungeon.loadLevel(GamesInProgress.curSlot);
		}
		Dungeon.changeConnections(Dungeon.location);
		Dungeon.switchLevel( level, returnPos );
		if (Dungeon.hero.belongings.getSimilar(new FCMap()) != null) {
			WaterOfAwareness.affectHeroAnyway(new WaterOfAwareness());
		}
		removeOldShades();
		checkForLevelReset(level, returnPos);
	}
	
	private void restore() throws IOException {
		
		Mob.clearHeldAllies();

		GameLog.wipe();

		Dungeon.loadGame( GamesInProgress.curSlot );
		if (Dungeon.location.equals("")) {
			Dungeon.switchLevel( Dungeon.loadLevel( GamesInProgress.curSlot ), -1 );
		} else {
			Level level = Dungeon.loadLevel( GamesInProgress.curSlot );
			Dungeon.changeConnections(Dungeon.location);
			Dungeon.switchLevel( level, Dungeon.hero.pos );
			removeOldShades();
			checkForLevelReset(level, Dungeon.hero.pos);
		}
	}
	
	private void resurrect() throws IOException {
		
		Mob.holdAllies( Dungeon.level );
		
//		if (Dungeon.level.locked) {
//			Dungeon.hero.resurrect( Dungeon.depth );
//			Dungeon.depth--;
//			Level level = Dungeon.newLevel();
//			Dungeon.switchLevel( level, level.entrance );
//		} else {
			Dungeon.hero.resurrect( -1 );
			Dungeon.saveAll();
			Dungeon.location = returnLocation;
			Level level;
			if (Dungeon.levelsToRebuild.contains(Dungeon.location)) {
				level = Dungeon.newLevel();
				Dungeon.levelsToRebuild.remove(Dungeon.location);
			} else {
				level = Dungeon.loadLevel(GamesInProgress.curSlot);
			}
			Dungeon.changeConnections(Dungeon.location);
			Dungeon.switchLevel( level, returnPos );
			if (Dungeon.hero.belongings.getSimilar(new FCMap()) != null) {
				WaterOfAwareness.affectHeroAnyway(new WaterOfAwareness());
			}
			removeOldShades();
			checkForLevelReset(level, returnPos);
//		}
	}

	private void reset() throws IOException {
		
		Mob.holdAllies( Dungeon.level );

		//SpecialRoom.resetPitRoom(Dungeon.depth+1);

		//Dungeon.depth--;
		Level level = Dungeon.newLevel();
		Dungeon.changeConnections(Dungeon.location);
		Dungeon.switchLevel( level, level.entrance );
		removeOldShades();
		checkForLevelReset(level, level.entrance);
	}

	private void removeOldShades() {
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (mob instanceof Shade && mob.id() != Dungeon.hero.shadeID) {
				Dungeon.level.mobs.remove(mob);
				Actor.remove(mob);
			}
		}
	}

	private void checkForLevelReset(Level level, int pos) {
		if (!Dungeon.levelsToNotReset.contains(Dungeon.location)) {
			Actor.clear();
			level.reset();
			Dungeon.switchLevel(level, pos);
			Dungeon.levelsToNotReset.add(Dungeon.location);
		}
	}
	
	@Override
	protected void onBackPressed() {
		//Do nothing
	}
}
