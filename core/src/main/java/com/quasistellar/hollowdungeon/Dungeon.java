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

package com.quasistellar.hollowdungeon;

import com.quasistellar.hollowdungeon.items.Ankh;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.journal.Notes;
import com.quasistellar.hollowdungeon.levels.SewerLevel;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.ui.QuickSlotButton;
import com.quasistellar.hollowdungeon.utils.BArray;
import com.quasistellar.hollowdungeon.utils.DungeonSeed;
import com.quasistellar.hollowdungeon.actors.buffs.Amok;
import com.quasistellar.hollowdungeon.actors.buffs.Awareness;
import com.quasistellar.hollowdungeon.actors.buffs.Light;
import com.quasistellar.hollowdungeon.actors.buffs.MindVision;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Blacksmith;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Imp;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Wandmaker;
import com.quasistellar.hollowdungeon.items.potions.Potion;
import com.quasistellar.hollowdungeon.items.scrolls.Scroll;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfUpgrade;
import com.quasistellar.hollowdungeon.levels.rooms.secret.SecretRoom;
import com.quasistellar.hollowdungeon.levels.rooms.special.SpecialRoom;
import com.quasistellar.hollowdungeon.mechanics.ShadowCaster;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Dungeon {

	//enum of items which have limited spawns, records how many have spawned
	//could all be their own separate numbers, but this allows iterating, much nicer for bundling/initializing.
	public static enum LimitedDrops {
		//limited world drops
		STRENGTH_POTIONS,
		UPGRADE_SCROLLS,
		ARCANE_STYLI,

		//Health potion sources
		//enemies
		SWARM_HP,
		NECRO_HP,
		BAT_HP,
		WARLOCK_HP,
		//Demon spawners are already limited in their spawnrate, no need to limit their health drops
		//alchemy
		COOKING_HP,
		BLANDFRUIT_SEED,

		//Other limited enemy drops
		SLIME_WEP,
		SKELE_WEP,
		THEIF_MISC,
		GUARD_ARM,
		SHAMAN_WAND,
		DM200_EQUIP,
		GOLEM_EQUIP,

		//containers
		DEW_VIAL,
		VELVET_POUCH,
		SCROLL_HOLDER,
		POTION_BANDOLIER,
		MAGICAL_HOLSTER;

		public int count = 0;

		//for items which can only be dropped once, should directly access count otherwise.
		public boolean dropped(){
			return count != 0;
		}
		public void drop(){
			count = 1;
		}

		public static void reset(){
			for (LimitedDrops lim : values()){
				lim.count = 0;
			}
		}

		public static void store( Bundle bundle ){
			for (LimitedDrops lim : values()){
				bundle.put(lim.name(), lim.count);
			}
		}

		public static void restore( Bundle bundle ){
			for (LimitedDrops lim : values()){
				if (bundle.contains(lim.name())){
					lim.count = bundle.getInt(lim.name());
				} else {
					lim.count = 0;
				}
				
			}
		}

	}

	public static int challenges;

	public static Hero hero;
	public static com.quasistellar.hollowdungeon.levels.Level level;

	public static QuickSlot quickslot = new QuickSlot();
	
	public static String location;
	public static int gold;

	public static String entranceDestination;
	public static String exitDestination;
	public static String transitionDestination;
	
	public static HashSet<Integer> chapters;

	public static int version;

	public static long seed;
	
	public static void init() {

		version = Game.versionCode;
		challenges = SPDSettings.challenges();

		seed = DungeonSeed.randomSeed();

		Actor.clear();
		Actor.resetNextID();
		
		Random.pushGenerator( seed );

			Scroll.initLabels();
			Potion.initColors();

			SpecialRoom.initForRun();
			SecretRoom.initForRun();

		Random.resetGenerators();
		
		Statistics.reset();
		Notes.reset();

		quickslot.reset();
		QuickSlotButton.reset();
		
		location = "King's Pass";
		exitDestination = "King's Pass";
		gold = 0;

		for (LimitedDrops a : LimitedDrops.values())
			a.count = 0;
		
		chapters = new HashSet<>();

		Wandmaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();

		Generator.reset();
		hero = new Hero();
		hero.live();
		
		com.quasistellar.hollowdungeon.Badges.reset();
		
		GamesInProgress.selectedClass.initHero( hero );
	}

	public static boolean isChallenged( int mask ) {
		return (challenges & mask) != 0;
	}

	public static Level switchLocation(String location) {
		Level level;
		switch (location) {
			case "King's Pass":
				level = new SewerLevel();
				break;
			case "Dirtmouth":
				level = new SewerLevel();
				break;
			case "Forgotten Crossroads 1":
				level = new SewerLevel();
				break;
			case "Forgotten Crossroads 2":
				level = new SewerLevel();
				break;
			case "Forgotten Crossroads 3":
				level = new SewerLevel();
				break;
			case "Forgotten Crossroads 4":
				level = new SewerLevel();
				break;
			case "Forgotten Crossroads 5":
				level = new SewerLevel();
				break;
			case "Ancestral Mound":
				level = new SewerLevel();
				break;
			case "Temple of the Black Egg":
				level = new SewerLevel();
				break;
			case "Greenpath 1":
				level = new SewerLevel();
				break;
			case "Greenpath 2":
				level = new SewerLevel();
				break;
			case "Greenpath 3":
				level = new SewerLevel();
				break;
			case "Greenpath 4":
				level = new SewerLevel();
				break;
			case "Greenpath 5":
				level = new SewerLevel();
				break;
			case "Lake of Unn":
				level = new SewerLevel();
				break;
			case "Fog Canyon 1":
				level = new SewerLevel();
				break;
			case "Fog Canyon 2":
				level = new SewerLevel();
				break;
			case "Fog Canyon 3":
				level = new SewerLevel();
				break;
			case "Fog Canyon 4":
				level = new SewerLevel();
				break;
			case "Overgrown Mound":
				level = new SewerLevel();
				break;
			case "Teacher's Archives":
				level = new SewerLevel();
				break;
			case "Fungal Wastes 1":
				level = new SewerLevel();
				break;
			case "Fungal Wastes 2":
				level = new SewerLevel();
				break;
			case "Fungal Wastes 3":
				level = new SewerLevel();
				break;
			case "Fungal Wastes 4":
				level = new SewerLevel();
				break;
			case "Fungal Wastes 5":
				level = new SewerLevel();
				break;
			case "City of Tears 1":
				level = new SewerLevel();
				break;
			case "City of Tears 2":
				level = new SewerLevel();
				break;
			case "City of Tears 3":
				level = new SewerLevel();
				break;
			case "City of Tears 4":
				level = new SewerLevel();
				break;
			case "City of Tears 5":
				level = new SewerLevel();
				break;
			case "Soul Sanctum 1":
				level = new SewerLevel();
				break;
			case "Soul Sanctum 2":
				level = new SewerLevel();
				break;
			case "Soul Sanctum 3":
				level = new SewerLevel();
				break;
			case "Watcher's Spire 1":
				level = new SewerLevel();
				break;
			case "Watcher's Spire 2":
				level = new SewerLevel();
				break;
			case "Watcher's Spire 3":
				level = new SewerLevel();
				break;
			case "Resting Grounds":
				level = new SewerLevel();
				break;
			case "Crystal Peak 1":
				level = new SewerLevel();
				break;
			case "Crystal Peak 2":
				level = new SewerLevel();
				break;
			default:
				level = new com.quasistellar.hollowdungeon.levels.DeadEndLevel();
		}
		return level;
	}

	public static void changeConnections(String location) {
		switch (location) {
			case "King's Pass":
				entranceDestination = "Howling Cliffs 1";
				exitDestination = "Dirtmouth";
				transitionDestination = "";
				break;
			case "Dirtmouth":
				entranceDestination = "King's Pass";
				exitDestination = "Forgotten Crossroads 1";
				transitionDestination = "Crystal Peak 1";
				break;
			case "Forgotten Crossroads 1":
				entranceDestination = "Dirtmouth";
				exitDestination = "Forgotten Crossroads 2";
				transitionDestination = "Crystal Peak 2";
				break;
			case "Forgotten Crossroads 2":
				entranceDestination = "Forgotten Crossroads 1";
				exitDestination = "Forgotten Crossroads 3";
				transitionDestination = "Resting Grounds";
				break;
			case "Forgotten Crossroads 3":
				entranceDestination = "Forgotten Crossroads 2";
				exitDestination = "Forgotten Crossroads 4";
				transitionDestination = "Greenpath 1";
				break;
			case "Forgotten Crossroads 4":
				entranceDestination = "Forgotten Crossroads 3";
				exitDestination = "Forgotten Crossroads 5";
				transitionDestination = "Fungal Wastes 1";
				break;
			case "Forgotten Crossroads 5":
				entranceDestination = "Forgotten Crossroads 4";
				exitDestination = "Ancestral Mound";
				transitionDestination = "";
				break;
			case "Ancestral Mound":
				entranceDestination = "Forgotten Crossroads 5";
				exitDestination = "";
				transitionDestination = "";
				break;
			case "Temple of the Black Egg":
				entranceDestination = "";
				exitDestination = "";
				transitionDestination = "";
				break;
			case "Greenpath 1":
				entranceDestination = "";
				exitDestination = "Greenpath 2";
				transitionDestination = "Forgotten Crossroads 3";
				break;
			case "Greenpath 2":
				entranceDestination = "Greenpath 1";
				exitDestination = "Greenpath 3";
				transitionDestination = "Howling Cliffs 3";
				break;
			case "Greenpath 3":
				entranceDestination = "Greenpath 2";
				exitDestination = "Greenpath 4";
				transitionDestination = "Queen's Gardens 1";
				break;
			case "Greenpath 4":
				entranceDestination = "Greenpath 3";
				exitDestination = "Greenpath 5";
				transitionDestination = "Fog Canyon 2";
				break;
			case "Greenpath 5":
				entranceDestination = "Greenpath 4";
				exitDestination = "Lake of Unn";
				transitionDestination = "";
				break;
			case "Lake of Unn":
				entranceDestination = "Greenpath 5";
				exitDestination = "";
				transitionDestination = "";
				break;
			case "Fog Canyon 1":
				entranceDestination = "";
				exitDestination = "Fog Canyon 2";
				transitionDestination = "Fungal Wastes 2";
				break;
			case "Fog Canyon 2":
				entranceDestination = "Fog Canyon 1";
				exitDestination = "Fog Canyon 3";
				transitionDestination = "Greenpath 4";
				break;
			case "Fog Canyon 3":
				entranceDestination = "Fog Canyon 2";
				exitDestination = "Fog Canyon 4";
				transitionDestination = "Teacher's Archives";
				break;
			case "Fog Canyon 4":
				entranceDestination = "Fog Canyon 3";
				exitDestination = "Overgrown Mound";
				transitionDestination = "Queen's Gardens 3";
				break;
			case "Overgrown Mound":
				entranceDestination = "Fog Canyon 4";
				exitDestination = "";
				transitionDestination = "";
				break;
			case "Teacher's Archives":
				entranceDestination = "";
				exitDestination = "";
				transitionDestination = "Fog Canyon 3";
				break;
			case "Fungal Wastes 1":
				entranceDestination = "";
				exitDestination = "Fungal Wastes 2";
				transitionDestination = "Forgotten Crossroads 4";
				break;
			case "Fungal Wastes 2":
				entranceDestination = "Fungal Wastes 1";
				exitDestination = "Fungal Wastes 3";
				transitionDestination = "Fog Canyon 1";
				break;
			case "Fungal Wastes 3":
				entranceDestination = "Fungal Wastes 2";
				exitDestination = "Fungal Wastes 4";
				transitionDestination = "Fog Canyon 3";
				break;
			case "Fungal Wastes 4":
				entranceDestination = "Fungal Wastes 3";
				exitDestination = "Fungal Wastes 5";
				transitionDestination = "City of Tears 1";
				break;
			case "Fungal Wastes 5":
				entranceDestination = "Fungal Wastes 4";
				exitDestination = "Deepnest 1";
				transitionDestination = "";
				break;
			case "City of Tears 1":
				entranceDestination = "";
				exitDestination = "City of Tears 2";
				transitionDestination = "Fungal Wastes 4";
				break;
			case "City of Tears 2":
				entranceDestination = "City of Tears 1";
				exitDestination = "City of Tears 3";
				transitionDestination = "Soul Sanctum 1";
				break;
			case "City of Tears 3":
				entranceDestination = "City of Tears 2";
				exitDestination = "City of Tears 4";
				transitionDestination = "Resting Grounds";
				break;
			case "City of Tears 4":
				entranceDestination = "City of Tears 3";
				exitDestination = "City of Tears 5";
				transitionDestination = "Watcher's Spire 1";
				break;
			case "City of Tears 5":
				entranceDestination = "City of Tears 4";
				exitDestination = "Royal Waterways 1";
				transitionDestination = "Tower of Love";
				break;
			case "Soul Sanctum 1":
				entranceDestination = "";
				exitDestination = "Soul Sanctum 2";
				transitionDestination = "City of Tears 2";
				break;
			case "Soul Sanctum 2":
				entranceDestination = "Soul Sanctum 1";
				exitDestination = "Soul Sanctum 3";
				transitionDestination = "";
				break;
			case "Soul Sanctum 3":
				entranceDestination = "Soul Sanctum 2";
				exitDestination = "";
				transitionDestination = "";
				break;
			case "Watcher's Spire 1":
				entranceDestination = "";
				exitDestination = "Watcher's Spire 2";
				transitionDestination = "City of Tears 4";
				break;
			case "Watcher's Spire 2":
				entranceDestination = "Watcher's Spire 1";
				exitDestination = "Watcher's Spire 3";
				transitionDestination = "";
				break;
			case "Watcher's Spire 3":
				entranceDestination = "Watcher's Spire 2";
				exitDestination = "";
				transitionDestination = "";
				break;
			case "Resting Grounds":
				entranceDestination = "";
				exitDestination = "";
				transitionDestination = "";
				break;
			case "Crystal Peak 1":
				entranceDestination = "";
				exitDestination = "Crystal Peak 2";
				transitionDestination = "Dirtmouth";
				break;
			case "Crystal Peak 2":
				entranceDestination = "Crystal Peak 1";
				exitDestination = "Crystal Peak 3";
				transitionDestination = "Forgotten Crossroads 1";
				break;
		}
	}

	public static com.quasistellar.hollowdungeon.levels.Level newLevel() {
		
		Dungeon.level = null;
		Actor.clear();
		
		Level level = switchLocation(location);
		changeConnections(location);

		GLog.h(entranceDestination);
		GLog.h(exitDestination);
		GLog.h(transitionDestination);
		level.create();
		
		return level;
	}
	
	public static void resetLevel() {
		
		Actor.clear();
		
		level.reset();
		switchLevel( level, level.entrance );
	}

	public static long seedCurDepth(){
		return seedForDepth(location);
	}

	public static long seedForDepth(String location){
		Random.pushGenerator( seed );

			for (int i = 0; i < location.length(); i ++) {
				Random.Long(); //we don't care about these values, just need to go through them
			}
			long result = Random.Long();

		Random.popGenerator();
		return result;
	}

	public static void switchLevel(final com.quasistellar.hollowdungeon.levels.Level level, int pos ) {
		
		if (pos == -2){
			pos = level.exit;
		} else if (pos < 0 || pos >= level.length()){
			pos = level.entrance;
		}
		
		PathFinder.setMapSize(level.width(), level.height());
		
		Dungeon.level = level;
		Mob.restoreAllies( level, pos );
		Actor.init();
		
		com.quasistellar.hollowdungeon.actors.Actor respawner = level.respawner();
		if (respawner != null) {
			Actor.addDelayed( respawner, level.respawnTime() );
		}

		hero.pos = pos;
		
		for(Mob m : level.mobs){
			if (m.pos == hero.pos){
				//displace mob
				for(int i : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(m.pos+i) == null && level.passable[m.pos + i]){
						m.pos += i;
						break;
					}
				}
			}
		}
		
		Light light = hero.buff( Light.class );
		hero.viewDistance = light == null ? level.viewDistance : Math.max( Light.DISTANCE, level.viewDistance );
		
		hero.curAction = hero.lastAction = null;
		
		observe();
		try {
			saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
			/*This only catches IO errors. Yes, this means things can go wrong, and they can go wrong catastrophically.
			But when they do the user will get a nice 'report this issue' dialogue, and I can fix the bug.*/
		}
	}

	public static void dropToChasm( com.quasistellar.hollowdungeon.items.Item item ) {

	}
	
	private static final String VERSION		= "version";
	private static final String SEED		= "seed";
	private static final String CHALLENGES	= "challenges";
	private static final String HERO		= "hero";
	private static final String GOLD		= "gold";
	private static final String LOCATION	= "location";
	private static final String DROPPED     = "dropped%d";
	private static final String PORTED      = "ported%d";
	private static final String LEVEL		= "level";
	private static final String LIMDROPS    = "limited_drops";
	private static final String CHAPTERS	= "chapters";
	private static final String QUESTS		= "quests";
	private static final String BADGES		= "badges";
	
	public static void saveGame( int save ) {
		try {
			Bundle bundle = new Bundle();

			version = Game.versionCode;
			bundle.put( VERSION, version );
			bundle.put( SEED, seed );
			bundle.put( CHALLENGES, challenges );
			bundle.put( HERO, hero );
			bundle.put( GOLD, gold );
			bundle.put( LOCATION, location );

			quickslot.storePlaceholders( bundle );

			Bundle limDrops = new Bundle();
			LimitedDrops.store( limDrops );
			bundle.put ( LIMDROPS, limDrops );
			
			int count = 0;
			int ids[] = new int[chapters.size()];
			for (Integer id : chapters) {
				ids[count++] = id;
			}
			bundle.put( CHAPTERS, ids );
			
			Bundle quests = new Bundle();
			Wandmaker	.Quest.storeInBundle( quests );
			Blacksmith	.Quest.storeInBundle( quests );
			Imp			.Quest.storeInBundle( quests );
			bundle.put( QUESTS, quests );
			
			SpecialRoom.storeRoomsInBundle( bundle );
			SecretRoom.storeRoomsInBundle( bundle );
			
			Statistics.storeInBundle( bundle );
			Notes.storeInBundle( bundle );
			Generator.storeInBundle( bundle );
			
			Scroll.save( bundle );
			Potion.save( bundle );

			Actor.storeNextID( bundle );
			
			Bundle badges = new Bundle();
			com.quasistellar.hollowdungeon.Badges.saveLocal( badges );
			bundle.put( BADGES, badges );
			
			FileUtils.bundleToFile( GamesInProgress.gameFile(save), bundle);
			
		} catch (IOException e) {
			GamesInProgress.setUnknown( save );
			ShatteredPixelDungeon.reportException(e);
		}
	}
	
	public static void saveLevel( int save ) throws IOException {
		Bundle bundle = new Bundle();
		bundle.put( LEVEL, level );
		
		FileUtils.bundleToFile(GamesInProgress.depthFile( save, location), bundle);
	}
	
	public static void saveAll() throws IOException {
		if (hero != null && hero.isAlive()) {
			
			Actor.fixTime();
			saveGame( GamesInProgress.curSlot );
			saveLevel( GamesInProgress.curSlot );

			GamesInProgress.set( GamesInProgress.curSlot, location, challenges, hero );

		}
	}
	
	public static void loadGame( int save ) throws IOException {
		loadGame( save, true );
	}
	
	public static void loadGame( int save, boolean fullLoad ) throws IOException {
		
		Bundle bundle = FileUtils.bundleFromFile( GamesInProgress.gameFile( save ) );

		version = bundle.getInt( VERSION );

		seed = bundle.contains( SEED ) ? bundle.getLong( SEED ) : com.quasistellar.hollowdungeon.utils.DungeonSeed.randomSeed();

		Actor.restoreNextID( bundle );

		quickslot.reset();
		com.quasistellar.hollowdungeon.ui.QuickSlotButton.reset();

		Dungeon.challenges = bundle.getInt( CHALLENGES );
		
		Dungeon.level = null;
		
		Scroll.restore( bundle );
		Potion.restore( bundle );

		quickslot.restorePlaceholders( bundle );
		
		if (fullLoad) {
			
			LimitedDrops.restore( bundle.getBundle(LIMDROPS) );

			chapters = new HashSet<>();
			int ids[] = bundle.getIntArray( CHAPTERS );
			if (ids != null) {
				for (int id : ids) {
					chapters.add( id );
				}
			}
			
			Bundle quests = bundle.getBundle( QUESTS );
			if (!quests.isNull()) {
				Wandmaker.Quest.restoreFromBundle( quests );
				Blacksmith.Quest.restoreFromBundle( quests );
				Imp.Quest.restoreFromBundle( quests );
			} else {
				Wandmaker.Quest.reset();
				Blacksmith.Quest.reset();
				Imp.Quest.reset();
			}
			
			SpecialRoom.restoreRoomsFromBundle(bundle);
			SecretRoom.restoreRoomsFromBundle(bundle);
		}
		
		Bundle badges = bundle.getBundle(BADGES);
		if (!badges.isNull()) {
			com.quasistellar.hollowdungeon.Badges.loadLocal( badges );
		} else {
			com.quasistellar.hollowdungeon.Badges.reset();
		}
		
		com.quasistellar.hollowdungeon.journal.Notes.restoreFromBundle( bundle );
		
		hero = null;
		hero = (Hero)bundle.get( HERO );
		
		gold = bundle.getInt( GOLD );
		location = bundle.getString( LOCATION );
		
		Statistics.restoreFromBundle( bundle );
		com.quasistellar.hollowdungeon.items.Generator.restoreFromBundle( bundle );
	}
	
	public static com.quasistellar.hollowdungeon.levels.Level loadLevel(int save ) throws IOException {
		
		Dungeon.level = null;
		Actor.clear();
		
		Bundle bundle = FileUtils.bundleFromFile( GamesInProgress.depthFile( save, location)) ;
		
		com.quasistellar.hollowdungeon.levels.Level level = (Level)bundle.get( LEVEL );
		
		if (level == null){
			throw new IOException();
		} else {
			return level;
		}
	}
	
	public static void deleteGame( int save, boolean deleteLevels ) {
		
		FileUtils.deleteFile(GamesInProgress.gameFile(save));
		
		if (deleteLevels) {
			FileUtils.deleteDir(GamesInProgress.gameFolder(save));
		}
		
		GamesInProgress.delete( save );
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.location = bundle.getString( LOCATION );
		info.version = bundle.getInt( VERSION );
		info.challenges = bundle.getInt( CHALLENGES );
		Hero.preview( info, bundle.getBundle( HERO ) );
		Statistics.preview( info, bundle );
	}
	
	public static void fail( Class cause ) {
		if (hero.belongings.getItem( Ankh.class ) == null) {
			Rankings.INSTANCE.submit( false, cause );
		}
	}
	
	public static void win( Class cause ) {

		hero.belongings.identify();

		int chCount = 0;
		for (int ch : Challenges.MASKS){
			if ((challenges & ch) != 0) chCount++;
		}
		
		if (chCount != 0) {
			Badges.validateChampion(chCount);
		}

		Rankings.INSTANCE.submit( true, cause );
	}

	//TODO hero max vision is now separate from shadowcaster max vision. Might want to adjust.
	public static void observe(){
		observe( ShadowCaster.MAX_DISTANCE+1 );
	}
	
	public static void observe( int dist ) {

		if (level == null) {
			return;
		}
		
		level.updateFieldOfView(hero, level.heroFOV);

		int x = hero.pos % level.width();
		int y = hero.pos / level.width();
	
		//left, right, top, bottom
		int l = Math.max( 0, x - dist );
		int r = Math.min( x + dist, level.width() - 1 );
		int t = Math.max( 0, y - dist );
		int b = Math.min( y + dist, level.height() - 1 );
	
		int width = r - l + 1;
		int height = b - t + 1;
		
		int pos = l + t * level.width();
	
		for (int i = t; i <= b; i++) {
			BArray.or( level.visited, level.heroFOV, pos, width, level.visited );
			pos+=level.width();
		}
	
		GameScene.updateFog(l, t, width, height);
		
		if (hero.buff(MindVision.class) != null){
			for (Mob m : level.mobs.toArray(new Mob[0])){
				BArray.or( level.visited, level.heroFOV, m.pos - 1 - level.width(), 3, level.visited );
				BArray.or( level.visited, level.heroFOV, m.pos, 3, level.visited );
				BArray.or( level.visited, level.heroFOV, m.pos - 1 + level.width(), 3, level.visited );
				//updates adjacent cells too
				GameScene.updateFog(m.pos, 2);
			}
		}
		
		if (hero.buff(Awareness.class) != null){
			for (Heap h : level.heaps.valueList()){
				BArray.or( level.visited, level.heroFOV, h.pos - 1 - level.width(), 3, level.visited );
				BArray.or( level.visited, level.heroFOV, h.pos - 1, 3, level.visited );
				BArray.or( level.visited, level.heroFOV, h.pos - 1 + level.width(), 3, level.visited );
				GameScene.updateFog(h.pos, 2);
			}
		}

		com.quasistellar.hollowdungeon.scenes.GameScene.afterObserve();
	}

	//we store this to avoid having to re-allocate the array with each pathfind
	private static boolean[] passable;

	private static void setupPassable(){
		if (passable == null || passable.length != Dungeon.level.length())
			passable = new boolean[Dungeon.level.length()];
		else
			BArray.setFalse(passable);
	}

	public static PathFinder.Path findPath(com.quasistellar.hollowdungeon.actors.Char ch, int to, boolean[] pass, boolean[] vis, boolean chars) {

		setupPassable();
		if (ch.flying || ch.buff( Amok.class ) != null) {
			BArray.or( pass, Dungeon.level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Dungeon.level.length() );
		}

		if (Char.hasProp(ch, Char.Property.LARGE)){
			BArray.and( pass, Dungeon.level.openSpace, passable );
		}

		if (chars) {
			for (com.quasistellar.hollowdungeon.actors.Char c : Actor.chars()) {
				if (vis[c.pos]) {
					passable[c.pos] = false;
				}
			}
		}

		return PathFinder.find( ch.pos, to, passable );

	}
	
	public static int findStep(com.quasistellar.hollowdungeon.actors.Char ch, int to, boolean[] pass, boolean[] visible, boolean chars ) {

		if (Dungeon.level.adjacent( ch.pos, to )) {
			return Actor.findChar( to ) == null && (pass[to] || Dungeon.level.avoid[to]) ? to : -1;
		}

		setupPassable();
		if (ch.flying || ch.buff( Amok.class ) != null) {
			BArray.or( pass, Dungeon.level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Dungeon.level.length() );
		}

		if (Char.hasProp(ch, Char.Property.LARGE)){
			BArray.and( pass, Dungeon.level.openSpace, passable );
		}

		if (chars){
			for (com.quasistellar.hollowdungeon.actors.Char c : Actor.chars()) {
				if (visible[c.pos]) {
					passable[c.pos] = false;
				}
			}
		}
		
		return PathFinder.getStep( ch.pos, to, passable );

	}
	
	public static int flee(com.quasistellar.hollowdungeon.actors.Char ch, int from, boolean[] pass, boolean[] visible, boolean chars ) {

		setupPassable();
		if (ch.flying) {
			BArray.or( pass, Dungeon.level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Dungeon.level.length() );
		}

		if (Char.hasProp(ch, Char.Property.LARGE)){
			com.quasistellar.hollowdungeon.utils.BArray.and( pass, Dungeon.level.openSpace, passable );
		}

		if (chars) {
			for (com.quasistellar.hollowdungeon.actors.Char c : com.quasistellar.hollowdungeon.actors.Actor.chars()) {
				if (visible[c.pos]) {
					passable[c.pos] = false;
				}
			}
		}
		passable[ch.pos] = true;
		
		return PathFinder.getStepBack( ch.pos, from, passable );
		
	}

}
