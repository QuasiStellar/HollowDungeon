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

import com.quasistellar.hollowdungeon.effects.BannerSprites;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.DiscardedItemSprite;
import com.quasistellar.hollowdungeon.sprites.HeroSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.ui.SkillIndicator;
import com.quasistellar.hollowdungeon.ui.SpellIndicator;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.journal.Journal;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.SPDSettings;
import com.quasistellar.hollowdungeon.ShatteredPixelDungeon;
import com.quasistellar.hollowdungeon.Statistics;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.blobs.Blob;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.bags.VelvetPouch;
import com.quasistellar.hollowdungeon.levels.RegularLevel;
import com.quasistellar.hollowdungeon.levels.traps.Trap;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.tiles.CustomTilemap;
import com.quasistellar.hollowdungeon.tiles.DungeonTerrainTilemap;
import com.quasistellar.hollowdungeon.tiles.DungeonTileSheet;
import com.quasistellar.hollowdungeon.tiles.DungeonTilemap;
import com.quasistellar.hollowdungeon.tiles.DungeonWallsTilemap;
import com.quasistellar.hollowdungeon.tiles.FogOfWar;
import com.quasistellar.hollowdungeon.tiles.GridTileMap;
import com.quasistellar.hollowdungeon.tiles.RaisedTerrainTilemap;
import com.quasistellar.hollowdungeon.tiles.TerrainFeaturesTilemap;
import com.quasistellar.hollowdungeon.tiles.WallBlockingTilemap;
import com.quasistellar.hollowdungeon.ui.ActionIndicator;
import com.quasistellar.hollowdungeon.ui.AttackIndicator;
import com.quasistellar.hollowdungeon.ui.Banner;
import com.quasistellar.hollowdungeon.ui.BusyIndicator;
import com.quasistellar.hollowdungeon.ui.CharHealthIndicator;
import com.quasistellar.hollowdungeon.ui.GameLog;
import com.quasistellar.hollowdungeon.ui.LootIndicator;
import com.quasistellar.hollowdungeon.ui.QuickSlotButton;
import com.quasistellar.hollowdungeon.ui.ResumeIndicator;
import com.quasistellar.hollowdungeon.ui.StatusPane;
import com.quasistellar.hollowdungeon.ui.TargetHealthIndicator;
import com.quasistellar.hollowdungeon.ui.Toast;
import com.quasistellar.hollowdungeon.ui.Toolbar;
import com.quasistellar.hollowdungeon.ui.Window;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.glwrap.Blending;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Group;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class GameScene extends PixelScene {

	static GameScene scene;

	private SkinnedBlock water;
	private DungeonTerrainTilemap tiles;
	private GridTileMap visualGrid;
	private TerrainFeaturesTilemap terrainFeatures;
	private RaisedTerrainTilemap raisedTerrain;
	private DungeonWallsTilemap walls;
	private WallBlockingTilemap wallBlocking;
	private FogOfWar fog;
	private com.quasistellar.hollowdungeon.sprites.HeroSprite hero;

	private StatusPane pane;
	
	private GameLog log;
	
	private BusyIndicator busy;
	private com.quasistellar.hollowdungeon.effects.CircleArc counter;
	
	private static com.quasistellar.hollowdungeon.scenes.CellSelector cellSelector;
	
	private Group terrain;
	private Group customTiles;
	private Group levelVisuals;
	private Group customWalls;
	private Group ripples;
	private Group plants;
	private Group traps;
	private Group heaps;
	private Group mobs;
	private Group floorEmitters;
	private Group emitters;
	private Group effects;
	private Group gases;
	private Group spells;
	private Group statuses;
	private Group emoicons;
	private Group overFogEffects;
	private Group healthIndicators;
	
	private Toolbar toolbar;
	private Toast prompt;

	private SkillIndicator mothwingCloak;
	private SkillIndicator crystalHeart;
	private SkillIndicator monarchWings;
	private SkillIndicator dreamNail;
	private SkillIndicator dreamgate;
	private SpellIndicator focus;
	private SpellIndicator vengefulSpirit;
	private AttackIndicator attack;
	private LootIndicator loot;
	private ActionIndicator action;
	private ResumeIndicator resume;
	
	@Override
	public void create() {
		
		if (Dungeon.hero == null){
			ShatteredPixelDungeon.switchNoFade(TitleScene.class);
			return;
		}
		
		Music.INSTANCE.play( Assets.Music.GAME, true );

		SPDSettings.lastClass(Dungeon.hero.heroClass.ordinal());
		
		super.create();
		Camera.main.zoom( GameMath.gate(minZoom, defaultZoom + SPDSettings.zoom(), maxZoom));

		scene = this;

		terrain = new Group();
		add( terrain );

		water = new SkinnedBlock(
			Dungeon.level.width() * DungeonTilemap.SIZE,
			Dungeon.level.height() * DungeonTilemap.SIZE,
			Dungeon.level.waterTex() ){

			@Override
			protected NoosaScript script() {
				return NoosaScriptNoLighting.get();
			}

			@Override
			public void draw() {
				//water has no alpha component, this improves performance
				Blending.disable();
				super.draw();
				Blending.enable();
			}
		};
		terrain.add( water );

		ripples = new Group();
		terrain.add( ripples );

		DungeonTileSheet.setupVariance(Dungeon.level.map.length, Dungeon.seedCurDepth());
		
		tiles = new DungeonTerrainTilemap();
		terrain.add( tiles );

		customTiles = new Group();
		terrain.add(customTiles);

		for( CustomTilemap visual : Dungeon.level.customTiles){
			addCustomTile(visual);
		}

		visualGrid = new GridTileMap();
		terrain.add( visualGrid );

		terrainFeatures = new TerrainFeaturesTilemap(Dungeon.level.plants, Dungeon.level.traps);
		terrain.add(terrainFeatures);
		
		levelVisuals = Dungeon.level.addVisuals();
		add(levelVisuals);

		floorEmitters = new Group();
		add(floorEmitters);

		heaps = new Group();
		add( heaps );
		
		for ( Heap heap : Dungeon.level.heaps.valueList() ) {
			addHeapSprite( heap );
		}

		emitters = new Group();
		effects = new Group();
		healthIndicators = new Group();
		emoicons = new Group();
		overFogEffects = new Group();
		
		mobs = new Group();
		add( mobs );

		hero = new HeroSprite();
		hero.place( Dungeon.hero.pos );
		mobs.add( hero );
		
		for (Mob mob : Dungeon.level.mobs) {
			addMobSprite( mob );
			if (Statistics.amuletObtained) {
				mob.beckon( Dungeon.hero.pos );
			}
		}
		
		raisedTerrain = new RaisedTerrainTilemap();
		add( raisedTerrain );

		walls = new DungeonWallsTilemap();
		add(walls);

		customWalls = new Group();
		add(customWalls);

		for( CustomTilemap visual : Dungeon.level.customWalls){
			addCustomWall(visual);
		}

		wallBlocking = new WallBlockingTilemap();
		add (wallBlocking);

		add( emitters );
		add( effects );

		gases = new Group();
		add( gases );

		for (Blob blob : Dungeon.level.blobs.values()) {
			blob.emitter = null;
			addBlobSprite( blob );
		}


		fog = new FogOfWar( Dungeon.level.width(), Dungeon.level.height() );
		add( fog );

		spells = new Group();
		add( spells );

		add(overFogEffects);
		
		statuses = new Group();
		add( statuses );
		
		add( healthIndicators );
		//always appears ontop of other health indicators
		add( new TargetHealthIndicator() );
		
		add( emoicons );
		
		add( cellSelector = new com.quasistellar.hollowdungeon.scenes.CellSelector( tiles ) );

		pane = new StatusPane();
		pane.camera = uiCamera;
		pane.setSize( uiCamera.width, 0 );
		add( pane );
		
		toolbar = new Toolbar();
		toolbar.camera = uiCamera;
		toolbar.setRect( 0,uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height() );
		add( toolbar );

		mothwingCloak = new SkillIndicator(Dungeon.hero.mothwingCloak);
		mothwingCloak.camera = uiCamera;
		add(mothwingCloak);

		crystalHeart = new SkillIndicator(Dungeon.hero.crystalHeart);
		crystalHeart.camera = uiCamera;
		add(crystalHeart);

		monarchWings = new SkillIndicator(Dungeon.hero.monarchWings);
		monarchWings.camera = uiCamera;
		add(monarchWings);

		dreamNail = new SkillIndicator(Dungeon.hero.dreamNail);
		dreamNail.camera = uiCamera;
		add(dreamNail);

		dreamgate = new SkillIndicator(Dungeon.hero.dreamgate);
		dreamgate.camera = uiCamera;
		add(dreamgate);

		focus = new SpellIndicator(Dungeon.hero.focus);
		focus.camera = uiCamera;
		add(focus);

		vengefulSpirit = new SpellIndicator(Dungeon.hero.vengefulSpirit);
		vengefulSpirit.camera = uiCamera;
		add(vengefulSpirit);

		attack = new AttackIndicator();
		attack.camera = uiCamera;
		add( attack );

		loot = new LootIndicator();
		loot.camera = uiCamera;
		add( loot );

		action = new ActionIndicator();
		action.camera = uiCamera;
		add( action );

		resume = new ResumeIndicator();
		resume.camera = uiCamera;
		add( resume );

		log = new GameLog();
		log.camera = uiCamera;
		log.newLine();
		add( log );

		layoutTags();

		busy = new BusyIndicator();
		busy.camera = uiCamera;
		busy.x = 1;
		busy.y = pane.bottom() + 1;
		add( busy );
		
		counter = new com.quasistellar.hollowdungeon.effects.CircleArc(18, 4.25f);
		counter.color( 0x808080, true );
		counter.camera = uiCamera;
		counter.show(this, busy.center(), 0f);
		
		switch (InterlevelScene.mode) {
//		case RESURRECT:
//			ScrollOfTeleportation.appear( Dungeon.hero, Dungeon.level.entrance );
//			new com.quasistellar.hollowdungeon.effects.Flare( 8, 32 ).color( 0xFFFF66, true ).show( hero, 2f ) ;
//			break;
//		case RETURN:
//			ScrollOfTeleportation.appear(  Dungeon.hero, Dungeon.hero.pos );
//			break;
		case DESCEND:
//			switch (Dungeon.depth) {
//			case 1:
//				com.quasistellar.hollowdungeon.windows.WndStory.showChapter( com.quasistellar.hollowdungeon.windows.WndStory.ID_SEWERS );
//				break;
//			case 6:
//				com.quasistellar.hollowdungeon.windows.WndStory.showChapter( com.quasistellar.hollowdungeon.windows.WndStory.ID_PRISON );
//				break;
//			case 11:
//				com.quasistellar.hollowdungeon.windows.WndStory.showChapter( com.quasistellar.hollowdungeon.windows.WndStory.ID_CAVES );
//				break;
//			case 16:
//				com.quasistellar.hollowdungeon.windows.WndStory.showChapter( com.quasistellar.hollowdungeon.windows.WndStory.ID_CITY );
//				break;
//			case 21:
//				com.quasistellar.hollowdungeon.windows.WndStory.showChapter( com.quasistellar.hollowdungeon.windows.WndStory.ID_HALLS );
//				break;
//			}
//			if (Dungeon.hero.isAlive()) {
//				Badges.validateNoKilling();
//			}
			break;
		default:
		}

		Dungeon.hero.next();

		switch (InterlevelScene.mode){
			case FALL: case DESCEND: case CONTINUE:
				Camera.main.snapTo(hero.center().x, hero.center().y - DungeonTilemap.SIZE * (defaultZoom/Camera.main.zoom));
				break;
			case ASCEND:
				Camera.main.snapTo(hero.center().x, hero.center().y + DungeonTilemap.SIZE * (defaultZoom/Camera.main.zoom));
				break;
			default:
				Camera.main.snapTo(hero.center().x, hero.center().y);
		}
		Camera.main.panTo(hero.center(), 2.5f);

		if (InterlevelScene.mode != InterlevelScene.Mode.NONE) {
			if (//Dungeon.depth == Statistics.deepestFloor &&
					(InterlevelScene.mode == InterlevelScene.Mode.DESCEND || InterlevelScene.mode == InterlevelScene.Mode.FALL || InterlevelScene.mode == InterlevelScene.Mode.TRANSIT)) {
				//GLog.h(Messages.get(this, "descend"), Dungeon.location);
				Sample.INSTANCE.play(Assets.Sounds.DESCEND);

//				int spawnersAbove = Statistics.spawnersAlive;
//				if (spawnersAbove > 0 && Dungeon.depth <= 25) {
//					for (Mob m : Dungeon.level.mobs) {
//						if (m instanceof DemonSpawner && ((DemonSpawner) m).spawnRecorded) {
//							spawnersAbove--;
//						}
//					}
//
//					if (spawnersAbove > 0) {
////						if (Dungeon.bossLevel()) {
////							GLog.n(Messages.get(this, "spawner_warn_final"));
////						} else {
//							GLog.n(Messages.get(this, "spawner_warn"));
////						}
//					}
//				}
				
			} else if (InterlevelScene.mode == InterlevelScene.Mode.RESET) {
				GLog.h(Messages.get(this, "warp"));
			} else {
				//GLog.h(Messages.get(this, "return"), Dungeon.location);
			}

			switch (Dungeon.level.feeling) {
				case CHASM:
					GLog.w(Messages.get(this, "chasm"));
					break;
				case WATER:
					GLog.w(Messages.get(this, "water"));
					break;
				case GRASS:
					GLog.w(Messages.get(this, "grass"));
					break;
				case DARK:
					GLog.w(Messages.get(this, "dark"));
					break;
				default:
			}
			if (Dungeon.level instanceof RegularLevel &&
					((RegularLevel) Dungeon.level).secretDoors > Random.IntRange(3, 4)) {
				GLog.w(Messages.get(this, "secrets"));
			}

			InterlevelScene.mode = InterlevelScene.Mode.NONE;

			
		}
		
		fadeIn();

	}
	
	public void destroy() {
		
		//tell the actor thread to finish, then wait for it to complete any actions it may be doing.
		if (actorThread != null && actorThread.isAlive()){
			synchronized (GameScene.class){
				synchronized (actorThread) {
					actorThread.interrupt();
				}
				try {
					GameScene.class.wait(5000);
				} catch (InterruptedException e) {
					ShatteredPixelDungeon.reportException(e);
				}
				synchronized (actorThread) {
					if (Actor.processing()) {
						Throwable t = new Throwable();
						t.setStackTrace(actorThread.getStackTrace());
						throw new RuntimeException("timeout waiting for actor thread! ", t);
					}
				}
			}
		}
		
		freezeEmitters = false;
		
		scene = null;
		Badges.saveGlobal();
		Journal.saveGlobal();
		
		super.destroy();
	}
	
	public static void endActorThread(){
		if (actorThread != null && actorThread.isAlive()){
			Actor.keepActorThreadAlive = false;
			actorThread.interrupt();
		}
	}
	
	@Override
	public synchronized void onPause() {
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
			com.quasistellar.hollowdungeon.journal.Journal.saveGlobal();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}

	private static Thread actorThread;
	
	//sometimes UI changes can be prompted by the actor thread.
	// We queue any removed element destruction, rather than destroying them in the actor thread.
	private ArrayList<Gizmo> toDestroy = new ArrayList<>();
	
	@Override
	public synchronized void update() {
		if (Dungeon.hero == null || scene == null) {
			return;
		}

		super.update();
		
		if (!freezeEmitters) water.offset( 0, -5 * Game.elapsed );

		if (!Actor.processing() && Dungeon.hero.isAlive()) {
			if (actorThread == null || !actorThread.isAlive()) {
				
				actorThread = new Thread() {
					@Override
					public void run() {
						Actor.process();
					}
				};
				
				//if cpu cores are limited, game should prefer drawing the current frame
				if (Runtime.getRuntime().availableProcessors() == 1) {
					actorThread.setPriority(Thread.NORM_PRIORITY - 1);
				}
				actorThread.setName("SHPD Actor Thread");
				Thread.currentThread().setName("SHPD Render Thread");
				Actor.keepActorThreadAlive = true;
				actorThread.start();
			} else {
				synchronized (actorThread) {
					actorThread.notify();
				}
			}
		}

		counter.setSweep((1f - Actor.now()%1f)%1f);
		
		if (Dungeon.hero.ready && Dungeon.hero.paralysed == 0) {
			log.newLine();
		}

		if (tagAttack != attack.active ||
				tagLoot != loot.visible ||
				tagAction != action.visible ||
				tagResume != resume.visible) {

			//we only want to change the layout when new tags pop in, not when existing ones leave.
			boolean tagAppearing = (attack.active && !tagAttack) ||
									(loot.visible && !tagLoot) ||
									(action.visible && !tagAction) ||
									(resume.visible && !tagResume);

			tagAttack = attack.active;
			tagLoot = loot.visible;
			tagAction = action.visible;
			tagResume = resume.visible;

			if (tagAppearing) layoutTags();
		}

		if (tagMothwingCloak != mothwingCloak.visible) {

			tagMothwingCloak = mothwingCloak.visible;

			layoutSkillTags();
		}

		if (tagCrystalHeart != crystalHeart.visible) {

			tagCrystalHeart = crystalHeart.visible;

			layoutSkillTags();
		}

		if (tagMonarchWings != monarchWings.visible) {

			tagMonarchWings = monarchWings.visible;

			layoutSkillTags();
		}

		if (tagDreamNail != dreamNail.visible) {

			tagDreamNail = dreamNail.visible;

			layoutSkillTags();
		}

		if (tagDreamgate != dreamgate.visible) {

			tagDreamgate = dreamgate.visible;

			layoutSkillTags();
		}

		if (tagFocus != focus.visible) {

			tagFocus = focus.visible;

			layoutSkillTags();
		}

		if (tagVengefulSpirit != vengefulSpirit.visible) {

			tagVengefulSpirit = vengefulSpirit.visible;

			layoutSkillTags();
		}

		cellSelector.enable(Dungeon.hero.ready);
		
		for (Gizmo g : toDestroy){
			g.destroy();
		}
		toDestroy.clear();
	}

	private boolean tagMothwingCloak    = false;
	private boolean tagCrystalHeart     = false;
	private boolean tagMonarchWings     = false;
	private boolean tagDreamNail        = false;
	private boolean tagDreamgate        = false;
	private boolean tagFocus            = false;
	private boolean tagVengefulSpirit   = false;
	private boolean tagAttack    = false;
	private boolean tagLoot      = false;
	private boolean tagAction    = false;
	private boolean tagResume    = false;

	public static void layoutTags() {

		if (scene == null) return;

		float tagLeft = SPDSettings.flipTags() ? 0 : uiCamera.width - scene.attack.width();

		if (SPDSettings.flipTags()) {
			scene.log.setRect(scene.attack.width(), scene.toolbar.top()-2, uiCamera.width - scene.attack.width(), 0);
		} else {
			scene.log.setRect(0, scene.toolbar.top()-2, uiCamera.width - scene.attack.width(),  0 );
		}

		float pos = scene.toolbar.top();

		if (scene.tagAttack){
			scene.attack.setPos( tagLeft, pos - scene.attack.height());
			scene.attack.flip(tagLeft == 0);
			pos = scene.attack.top();
		}

		if (scene.tagLoot) {
			scene.loot.setPos( tagLeft, pos - scene.loot.height() );
			scene.loot.flip(tagLeft == 0);
			pos = scene.loot.top();
		}

		if (scene.tagAction) {
			scene.action.setPos( tagLeft, pos - scene.action.height() );
			scene.action.flip(tagLeft == 0);
			pos = scene.action.top();
		}

		if (scene.tagResume) {
			scene.resume.setPos( tagLeft, pos - scene.resume.height() );
			scene.resume.flip(tagLeft == 0);
		}
	}

	public static void layoutSkillTags() {

		if (scene == null) return;

		float tagLeft = SPDSettings.flipTags() ? uiCamera.width - scene.attack.width() : 0;

		if (SPDSettings.flipTags()) {
			scene.log.setRect(scene.attack.width(), scene.toolbar.top(), uiCamera.width - scene.attack.width(), 0);
		} else {
			scene.log.setRect(0, scene.toolbar.top(), uiCamera.width - scene.attack.width(),  0 );
		}

		float upper_pos = scene.pane.bottom();

		if (scene.tagMothwingCloak) {
			scene.mothwingCloak.setPos( tagLeft, upper_pos + 10);
			scene.mothwingCloak.flip(tagLeft == 0);
		}

		if (scene.tagCrystalHeart) {
			scene.crystalHeart.setPos( tagLeft, upper_pos + 10 + scene.mothwingCloak.height());
			scene.crystalHeart.flip(tagLeft == 0);
		}

		if (scene.tagMonarchWings) {
			scene.monarchWings.setPos( tagLeft, upper_pos + 10 + scene.mothwingCloak.height() + scene.crystalHeart.height());
			scene.monarchWings.flip(tagLeft == 0);
		}

		if (scene.tagDreamNail) {
			scene.dreamNail.setPos( tagLeft, upper_pos + 10 + scene.mothwingCloak.height() + scene.crystalHeart.height() + scene.monarchWings.height());
			scene.dreamNail.flip(tagLeft == 0);
		}

		if (scene.tagDreamgate) {
			scene.dreamgate.setPos( tagLeft, upper_pos + 10 + scene.mothwingCloak.height() + scene.crystalHeart.height() + scene.monarchWings.height() + scene.dreamNail.height());
			scene.dreamgate.flip(tagLeft == 0);
		}

		if (scene.tagFocus) {
			float xPos = 0;
			switch(Toolbar.Mode.valueOf(SPDSettings.toolbarMode())){
				case SPLIT:
					xPos = uiCamera.width - scene.focus.width() - 25;
					break;
				case CENTER:
					xPos = uiCamera.width / 2f - scene.focus.width() - 33;
					break;
				case GROUP:
					xPos = uiCamera.width - scene.focus.width() - 65;
					break;
			}
			if (SPDSettings.flipToolbar()) {
				xPos = uiCamera.width - xPos - scene.focus.width();
			}
			scene.focus.setPos( xPos, uiCamera.height - scene.focus.height());
		}

		if (scene.tagVengefulSpirit) {
			float xPos = 0;
			switch(Toolbar.Mode.valueOf(SPDSettings.toolbarMode())){
				case SPLIT:
					xPos = uiCamera.width - scene.vengefulSpirit.width() * 2 - 25;
					break;
				case CENTER:
					xPos = uiCamera.width / 2f - scene.vengefulSpirit.width() * 2 - 33;
					break;
				case GROUP:
					xPos = uiCamera.width - scene.vengefulSpirit.width() * 2 - 65;
					break;
			}
			if (SPDSettings.flipToolbar()) {
				xPos = uiCamera.width - xPos - scene.vengefulSpirit.width();
			}
			scene.vengefulSpirit.setPos( xPos, uiCamera.height - scene.vengefulSpirit.height());
		}
	}
	
	@Override
	protected void onBackPressed() {
		if (!cancel()) {
			add( new com.quasistellar.hollowdungeon.windows.WndGame() );
		}
	}

	public void addCustomTile( CustomTilemap visual){
		customTiles.add( visual.create() );
	}

	public void addCustomWall( CustomTilemap visual){
		customWalls.add( visual.create() );
	}
	
	private void addHeapSprite( Heap heap ) {
		com.quasistellar.hollowdungeon.sprites.ItemSprite sprite = heap.sprite = (com.quasistellar.hollowdungeon.sprites.ItemSprite)heaps.recycle( ItemSprite.class );
		sprite.revive();
		sprite.link( heap );
		heaps.add( sprite );
	}
	
	private void addDiscardedSprite( Heap heap ) {
		heap.sprite = (com.quasistellar.hollowdungeon.sprites.DiscardedItemSprite)heaps.recycle( DiscardedItemSprite.class );
		heap.sprite.revive();
		heap.sprite.link( heap );
		heaps.add( heap.sprite );
	}
	
	private void addPlantSprite( Plant plant ) {

	}

	private void addTrapSprite( Trap trap ) {

	}
	
	private void addBlobSprite( final Blob gas ) {
		if (gas.emitter == null) {
			gases.add( new com.quasistellar.hollowdungeon.effects.BlobEmitter( gas ) );
		}
	}
	
	private void addMobSprite( Mob mob ) {
		CharSprite sprite = mob.sprite();
		sprite.visible = Dungeon.level.heroFOV[mob.pos];
		mobs.add( sprite );
		sprite.link( mob );
	}
	
	private synchronized void prompt( String text ) {
		
		if (prompt != null) {
			prompt.killAndErase();
			toDestroy.add(prompt);
			prompt = null;
		}
		
		if (text != null) {
			prompt = new Toast( text ) {
				@Override
				protected void onClose() {
					cancel();
				}
			};
			prompt.camera = uiCamera;
			prompt.setPos( (uiCamera.width - prompt.width()) / 2, uiCamera.height - 60 );
			add( prompt );
		}
	}
	
	private void showBanner( Banner banner ) {
		banner.camera = uiCamera;
		banner.x = align( uiCamera, (uiCamera.width - banner.width) / 2 );
		banner.y = align( uiCamera, (uiCamera.height - banner.height) / 3 );
		addToFront( banner );
	}
	
	// -------------------------------------------------------

	public static void add( Plant plant ) {
		if (scene != null) {
			scene.addPlantSprite( plant );
		}
	}

	public static void add( Trap trap ) {
		if (scene != null) {
			scene.addTrapSprite( trap );
		}
	}
	
	public static void add( Blob gas ) {
		Actor.add( gas );
		if (scene != null) {
			scene.addBlobSprite( gas );
		}
	}
	
	public static void add( Heap heap ) {
		if (scene != null) {
			scene.addHeapSprite( heap );
		}
	}
	
	public static void discard( Heap heap ) {
		if (scene != null) {
			scene.addDiscardedSprite( heap );
		}
	}
	
	public static void add( Mob mob ) {
		Dungeon.level.mobs.add( mob );
		Actor.add( mob );
		scene.addMobSprite( mob );
	}

	public static void addSprite( Mob mob ) {
		scene.addMobSprite( mob );
	}
	
	public static void add( Mob mob, float delay ) {
		Dungeon.level.mobs.add( mob );
		Actor.addDelayed( mob, delay );
		scene.addMobSprite( mob );
	}
	
	public static void add( com.quasistellar.hollowdungeon.effects.EmoIcon icon ) {
		scene.emoicons.add( icon );
	}
	
	public static void add( CharHealthIndicator indicator ){
		if (scene != null) scene.healthIndicators.add(indicator);
	}
	
	public static void add( CustomTilemap t, boolean wall ){
		if (scene == null) return;
		if (wall){
			scene.addCustomWall(t);
		} else {
			scene.addCustomTile(t);
		}
	}
	
	public static void effect( Visual effect ) {
		scene.effects.add( effect );
	}

	public static void effectOverFog( Visual effect ) {
		scene.overFogEffects.add( effect );
	}
	
	public static com.quasistellar.hollowdungeon.effects.Ripple ripple(int pos ) {
		if (scene != null) {
			com.quasistellar.hollowdungeon.effects.Ripple ripple = (com.quasistellar.hollowdungeon.effects.Ripple) scene.ripples.recycle(com.quasistellar.hollowdungeon.effects.Ripple.class);
			ripple.reset(pos);
			return ripple;
		} else {
			return null;
		}
	}
	
	public static com.quasistellar.hollowdungeon.effects.SpellSprite spellSprite() {
		return (com.quasistellar.hollowdungeon.effects.SpellSprite)scene.spells.recycle( com.quasistellar.hollowdungeon.effects.SpellSprite.class );
	}
	
	public static Emitter emitter() {
		if (scene != null) {
			Emitter emitter = (Emitter)scene.emitters.recycle( Emitter.class );
			emitter.revive();
			return emitter;
		} else {
			return null;
		}
	}

	public static Emitter floorEmitter() {
		if (scene != null) {
			Emitter emitter = (Emitter)scene.floorEmitters.recycle( Emitter.class );
			emitter.revive();
			return emitter;
		} else {
			return null;
		}
	}
	
	public static com.quasistellar.hollowdungeon.effects.FloatingText status() {
		return scene != null ? (com.quasistellar.hollowdungeon.effects.FloatingText)scene.statuses.recycle( com.quasistellar.hollowdungeon.effects.FloatingText.class ) : null;
	}
	
	public static void pickUp( Item item, int pos ) {
		if (scene != null) scene.toolbar.pickup( item, pos );
	}

	public static void pickUpJournal( Item item, int pos ) {
		if (scene != null) scene.pane.pickup( item, pos );
	}
	
	public static void flashJournal(){
		if (scene != null) scene.pane.flash();
	}
	
	public static void updateKeyDisplay(){
		if (scene != null) scene.pane.updateKeys();
	}

	public static void resetMap() {
		if (scene != null) {
			scene.tiles.map(Dungeon.level.map, Dungeon.level.width() );
			scene.visualGrid.map(Dungeon.level.map, Dungeon.level.width() );
			scene.terrainFeatures.map(Dungeon.level.map, Dungeon.level.width() );
			scene.raisedTerrain.map(Dungeon.level.map, Dungeon.level.width() );
			scene.walls.map(Dungeon.level.map, Dungeon.level.width() );
		}
		updateFog();
	}

	//updates the whole map
	public static void updateMap() {
		if (scene != null) {
			scene.tiles.updateMap();
			scene.visualGrid.updateMap();
			scene.terrainFeatures.updateMap();
			scene.raisedTerrain.updateMap();
			scene.walls.updateMap();
			updateFog();
		}
	}
	
	public static void updateMap( int cell ) {
		if (scene != null) {
			scene.tiles.updateMapCell( cell );
			scene.visualGrid.updateMapCell( cell );
			scene.terrainFeatures.updateMapCell( cell );
			scene.raisedTerrain.updateMapCell( cell );
			scene.walls.updateMapCell( cell );
			//update adjacent cells too
			updateFog( cell, 1 );
		}
	}

	public static void plantSeed( int cell ) {
		if (scene != null) {
			scene.terrainFeatures.growPlant( cell );
		}
	}
	
	//todo this doesn't account for walls right now
	public static void discoverTile( int pos, int oldValue ) {
		if (scene != null) {
			scene.tiles.discover( pos, oldValue );
		}
	}
	
	public static void show( Window wnd ) {
		if (scene != null) {
			cancelCellSelector();
			scene.addToFront(wnd);
		}
	}

	public static void updateFog(){
		if (scene != null) {
			scene.fog.updateFog();
			scene.wallBlocking.updateMap();
		}
	}

	public static void updateFog(int x, int y, int w, int h){
		if (scene != null) {
			scene.fog.updateFogArea(x, y, w, h);
			scene.wallBlocking.updateArea(x, y, w, h);
		}
	}
	
	public static void updateFog( int cell, int radius ){
		if (scene != null) {
			scene.fog.updateFog( cell, radius );
			scene.wallBlocking.updateArea( cell, radius );
		}
	}
	
	public static void afterObserve() {
		if (scene != null) {
			for (Mob mob : Dungeon.level.mobs) {
				if (mob.sprite != null)
					mob.sprite.visible = Dungeon.level.heroFOV[mob.pos];
			}
		}
	}

	public static void flash( int color ) {
		flash( color, true);
	}

	public static void flash( int color, boolean lightmode ) {
		scene.fadeIn( 0xFF000000 | color, lightmode );
	}

	public static void gameOver() {
		Banner gameOver = new Banner( com.quasistellar.hollowdungeon.effects.BannerSprites.get( com.quasistellar.hollowdungeon.effects.BannerSprites.Type.GAME_OVER ) );
		gameOver.show( 0x000000, 1f );
		scene.showBanner( gameOver );
		
		Sample.INSTANCE.play( Assets.Sounds.DEATH );
	}
	
	public static void bossSlain() {
		if (Dungeon.hero.isAlive()) {
			Banner bossSlain = new Banner( BannerSprites.get( com.quasistellar.hollowdungeon.effects.BannerSprites.Type.BOSS_SLAIN ) );
			bossSlain.show( 0xFFFFFF, 0.3f, 5f );
			scene.showBanner( bossSlain );
			
			Sample.INSTANCE.play( Assets.Sounds.BOSS );
		}
	}
	
	public static void handleCell( int cell ) {
		cellSelector.select( cell );
	}
	
	public static void selectCell( com.quasistellar.hollowdungeon.scenes.CellSelector.Listener listener ) {
		if (cellSelector.listener != null && cellSelector.listener != defaultCellListener){
			cellSelector.listener.onSelect(null);
		}
		cellSelector.listener = listener;
		if (scene != null)
			scene.prompt( listener.prompt() );
	}
	
	private static boolean cancelCellSelector() {
		cellSelector.resetKeyHold();
		if (cellSelector.listener != null && cellSelector.listener != defaultCellListener) {
			cellSelector.cancel();
			return true;
		} else {
			return false;
		}
	}
	
	public static com.quasistellar.hollowdungeon.windows.WndBag selectItem(com.quasistellar.hollowdungeon.windows.WndBag.Listener listener, com.quasistellar.hollowdungeon.windows.WndBag.Mode mode, String title ) {
		cancelCellSelector();

		com.quasistellar.hollowdungeon.windows.WndBag wnd =	WndBag.lastBag( listener, mode, title );

		if (scene != null) scene.addToFront( wnd );
		
		return wnd;
	}
	
	static boolean cancel() {
		if (Dungeon.hero != null && (Dungeon.hero.curAction != null || Dungeon.hero.resting)) {
			
			Dungeon.hero.curAction = null;
			Dungeon.hero.resting = false;
			return true;
			
		} else {
			
			return cancelCellSelector();
			
		}
	}
	
	public static void ready() {
		selectCell( defaultCellListener );
		QuickSlotButton.cancel();
		if (scene != null && scene.toolbar != null) scene.toolbar.examining = false;
	}
	
	public static void checkKeyHold(){
		cellSelector.processKeyHold();
	}
	
	public static void resetKeyHold(){
		cellSelector.resetKeyHold();
	}

	public static void examineCell( Integer cell ) {
		if (cell == null
				|| cell < 0
				|| cell > Dungeon.level.length()
				|| (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell])) {
			return;
		}

		ArrayList<String> names = new ArrayList<>();
		final ArrayList<Object> objects = new ArrayList<>();

		if (cell == Dungeon.hero.pos) {
			objects.add(Dungeon.hero);
			names.add(Dungeon.hero.className().toUpperCase(Locale.ENGLISH));
		} else {
			if (Dungeon.level.heroFOV[cell]) {
				Mob mob = (Mob) Actor.findChar(cell);
				if (mob != null) {
					objects.add(mob);
					names.add(Messages.titleCase( mob.name() ));
				}
			}
		}

		Heap heap = Dungeon.level.heaps.get(cell);
		if (heap != null && heap.seen) {
			objects.add(heap);
			names.add(Messages.titleCase( heap.toString() ));
		}

		Plant plant = Dungeon.level.plants.get( cell );
		if (plant != null) {
			objects.add(plant);
			names.add(Messages.titleCase( plant.plantName ));
		}

		Trap trap = Dungeon.level.traps.get( cell );
		if (trap != null && trap.visible) {
			objects.add(trap);
			names.add(Messages.titleCase( trap.name ));
		}

		if (objects.isEmpty()) {
			GameScene.show(new com.quasistellar.hollowdungeon.windows.WndInfoCell(cell));
		} else if (objects.size() == 1){
			examineObject(objects.get(0));
		} else {
			GameScene.show(new com.quasistellar.hollowdungeon.windows.WndOptions(Messages.get(GameScene.class, "choose_examine"),
					Messages.get(GameScene.class, "multiple_examine"), names.toArray(new String[names.size()])){
				@Override
				protected void onSelect(int index) {
					examineObject(objects.get(index));
				}
			});

		}
	}

	public static void examineObject(Object o){
		if (o == Dungeon.hero){
			GameScene.show( new com.quasistellar.hollowdungeon.windows.WndHero() );
		} else if ( o instanceof Mob ){
			GameScene.show(new com.quasistellar.hollowdungeon.windows.WndInfoMob((Mob) o));
		} else if ( o instanceof Heap ){
			GameScene.show(new com.quasistellar.hollowdungeon.windows.WndInfoItem((Heap)o));
		} else if ( o instanceof Plant ){
			GameScene.show( new com.quasistellar.hollowdungeon.windows.WndInfoPlant((Plant) o) );
		} else if ( o instanceof Trap ){
			GameScene.show( new com.quasistellar.hollowdungeon.windows.WndInfoTrap((Trap) o));
		} else {
			GameScene.show( new com.quasistellar.hollowdungeon.windows.WndMessage( Messages.get(GameScene.class, "dont_know") ) ) ;
		}
	}

	
	private static final com.quasistellar.hollowdungeon.scenes.CellSelector.Listener defaultCellListener = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			if (Dungeon.hero.handle( cell )) {
				Dungeon.hero.next();
			}
		}
		@Override
		public String prompt() {
			return null;
		}
	};
}
