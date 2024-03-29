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

package com.quasistellar.hollowdungeon.levels;

import com.quasistellar.hollowdungeon.journal.Document;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.levels.builders.Builder;
import com.quasistellar.hollowdungeon.levels.builders.LoopBuilder;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.quasistellar.hollowdungeon.levels.rooms.Room;
import com.quasistellar.hollowdungeon.levels.rooms.special.PitRoom;
import com.quasistellar.hollowdungeon.levels.rooms.special.SpecialRoom;
import com.quasistellar.hollowdungeon.levels.rooms.standard.BenchRoom;
import com.quasistellar.hollowdungeon.levels.rooms.standard.EntranceRoom;
import com.quasistellar.hollowdungeon.levels.rooms.standard.ExitRoom;
import com.quasistellar.hollowdungeon.levels.rooms.standard.StandardRoom;
import com.quasistellar.hollowdungeon.levels.rooms.standard.TransitionRoom;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class RegularLevel extends Level {
	
	protected ArrayList<Room> rooms;
	
	protected Builder builder;
	
	protected Room roomEntrance;
	protected Room roomExit;
	
	public int secretDoors;
	
	@Override
	protected boolean build() {
		
		builder = builder();
		
		ArrayList<Room> initRooms = initRooms();
		Random.shuffle(initRooms);
		
		do {
			for (Room r : initRooms){
				r.neigbours.clear();
				r.connected.clear();
			}
			rooms = builder.build((ArrayList<Room>)initRooms.clone());
		} while (rooms == null);
		
		return painter().paint(this, rooms);
		
	}
	
	protected ArrayList<Room> initRooms() {
		ArrayList<Room> initRooms = new ArrayList<>();
		initRooms.add ( roomEntrance = new EntranceRoom());
		initRooms.add( roomExit = new ExitRoom());
		
		int standards = standardRooms();
		for (int i = 0; i < standards; i++) {
			StandardRoom s;
			do {
				s = StandardRoom.createRoom();
			} while (!s.setSizeCat( standards-i ));
			i += s.sizeCat.roomValue-1;
			initRooms.add(s);
		}

		if (!Dungeon.transitionDestination.equals("")) {
			initRooms.add(new TransitionRoom());
		}

		int specials = specialRooms();
		SpecialRoom.initForFloor();
		for (int i = 0; i < specials; i++) {
			SpecialRoom s = SpecialRoom.createRoom();
			if (s instanceof PitRoom) specials++;
			initRooms.add(s);
		}

		if (Dungeon.location.equals("Forgotten Crossroads 4")) {
			initRooms.add(new BenchRoom());
		}
		
		//int secrets = SecretRoom.secretsForFloor(Dungeon.depth);
//		for (int i = 0; i < secrets; i++)
//			initRooms.add(SecretRoom.createRoom());
		
		return initRooms;
	}
	
	protected int standardRooms(){
		return 0;
	}
	
	protected int specialRooms(){
		return 0;
	}
	
	protected Builder builder(){
		return new LoopBuilder()
				.setLoopShape( 2 ,
						Random.Float(0.4f, 0.7f),
						Random.Float(0f, 0.5f));
	}
	
	protected abstract Painter painter();
	
	protected int nTraps() {
		return 0;
	}
	
	protected Class<?>[] trapClasses(){
		return new Class<?>[]{};
	}

	protected float[] trapChances() {
		return new float[]{};
	}
	
	@Override
	public int nMobs() {
		switch(Dungeon.location) {
			case "King's Pass":
				//mobs are not randomly spawned on floor 1.
				return 0;
			default:
				return 3 + Random.Int(3);
		}
	}
	
	@Override
	protected void createMobs() {
		int mobsToSpawn = nMobs();

		ArrayList<Room> stdRooms = new ArrayList<>();
		for (Room room : rooms) {
			if (room instanceof StandardRoom && room != roomEntrance) {
				for (int i = 0; i < ((StandardRoom) room).sizeCat.roomValue; i++) {
					stdRooms.add(room);
				}
			}
		}
		Random.shuffle(stdRooms);
		Iterator<Room> stdRoomIter = stdRooms.iterator();

		while (mobsToSpawn > 0) {
			Mob mob = createMob();
			Room roomToSpawn;
			
			if (!stdRoomIter.hasNext()) {
				stdRoomIter = stdRooms.iterator();
			}
			roomToSpawn = stdRoomIter.next();

			int tries = 30;
			do {
				mob.pos = pointToCell(roomToSpawn.random());
				tries--;
			} while (tries >= 0 && (findMob(mob.pos) != null || !passable[mob.pos] || mob.pos == exit));

			if (tries >= 0) {
				mobsToSpawn--;
				mobs.add(mob);

				//add a second mob to this room
				if (mobsToSpawn > 0 && Random.Int(4) == 0){
					mob = createMob();

					tries = 30;
					do {
						mob.pos = pointToCell(roomToSpawn.random());
						tries--;
					} while (tries >= 0 && findMob(mob.pos) != null || !passable[mob.pos] || mob.pos == exit);

					if (tries >= 0) {
						mobsToSpawn--;
						mobs.add(mob);
					}
				}
			}
		}

		for (Mob m : mobs){
			if (map[m.pos] == Terrain.HIGH_GRASS || map[m.pos] == Terrain.FURROWED_GRASS) {
				map[m.pos] = Terrain.GRASS;
				losBlocking[m.pos] = false;
			}

		}

	}
	
	@Override
	public int randomRespawnCell( Char ch ) {
		int count = 0;
		int cell = -1;
		
		while (true) {
			
			if (++count > 30) {
				return -1;
			}
			
			Room room = randomRoom( StandardRoom.class );
			if (room == null || room == roomEntrance) {
				continue;
			}

			cell = pointToCell(room.random(1));
			if (!heroFOV[cell]
					&& Actor.findChar( cell ) == null
					&& passable[cell]
					&& (!Char.hasProp(ch, Char.Property.LARGE) || openSpace[cell])
					&& room.canPlaceCharacter(cellToPoint(cell), this)
					&& cell != exit) {
				return cell;
			}
			
		}
	}
	
	@Override
	public int randomDestination( Char ch ) {
		
		int count = 0;
		int cell = -1;
		
		while (true) {
			
			if (++count > 30) {
				return -1;
			}
			
			Room room = Random.element( rooms );
			if (room == null) {
				continue;
			}
			
			cell = pointToCell(room.random());
			if (passable[cell] && (!Char.hasProp(ch, Char.Property.LARGE) || openSpace[cell])) {
				return cell;
			}
			
		}
	}
	
	@Override
	protected void createItems() {
		
		// drops 3/4/5 items 60%/30%/10% of the time
		int nItems = 3 + Random.chances(new float[]{6, 3, 1});
		
		for (int i=0; i < nItems; i++) {

			Item toDrop = Generator.random();
			if (toDrop == null) continue;

			int cell = randomDropCell();
			if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
				map[cell] = Terrain.GRASS;
				losBlocking[cell] = false;
			}

			Heap.Type type = null;
			switch (Random.Int( 20 )) {
			case 0:
				type = Heap.Type.SKELETON;
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				type = Heap.Type.CHEST;
				break;
			default:
				type = Heap.Type.HEAP;
				break;
			}

			Heap dropped = drop( toDrop, cell );
			dropped.type = type;
			if (type == Heap.Type.SKELETON){
				dropped.setHauntedIfCursed();
			}
		}

		for (Item item : itemsToSpawn) {
			int cell = randomDropCell();
			drop( item, cell ).type = Heap.Type.HEAP;
			if (map[cell] == Terrain.HIGH_GRASS || map[cell] == Terrain.FURROWED_GRASS) {
				map[cell] = Terrain.GRASS;
				losBlocking[cell] = false;
			}
		}

		//use a separate generator for this to prevent held items and meta progress from affecting levelgen
		Random.pushGenerator( Dungeon.seedCurDepth() );

		//guide pages
		Collection<String> allPages = Document.ADVENTURERS_GUIDE.pages();
		ArrayList<String> missingPages = new ArrayList<>();
		for ( String page : allPages){
			if (!Document.ADVENTURERS_GUIDE.hasPage(page)){
				missingPages.add(page);
			}
		}

		//these are dropped specially
		missingPages.remove(Document.GUIDE_INTRO_PAGE);
		missingPages.remove(com.quasistellar.hollowdungeon.journal.Document.GUIDE_SEARCH_PAGE);

		Random.popGenerator();

	}
	
	public ArrayList<Room> rooms() {
		return new ArrayList<>(rooms);
	}
	
	//FIXME pit rooms shouldn't be problematic enough to warrant this
	public boolean hasPitRoom(){
		for (Room r : rooms) {
			if (r instanceof PitRoom) {
				return true;
			}
		}
		return false;
	}
	
	protected Room randomRoom( Class<?extends Room> type ) {
		Random.shuffle( rooms );
		for (Room r : rooms) {
			if (type.isInstance(r)) {
				return r;
			}
		}
		return null;
	}
	
	public Room room( int pos ) {
		for (Room room : rooms) {
			if (room.inside( cellToPoint(pos) )) {
				return room;
			}
		}
		
		return null;
	}
	
	protected int randomDropCell() {
		while (true) {
			Room room = randomRoom( StandardRoom.class );
			if (room != null && room != roomEntrance) {
				int pos = pointToCell(room.random());
				if (passable[pos]
						&& pos != exit
						&& heaps.get(pos) == null
						&& findMob(pos) == null) {
					
//					Trap t = traps.get(pos);
					
					//items cannot spawn on traps which destroy items
//					if (t == null ||
//							! (t instanceof BurningTrap || t instanceof BlazingTrap
//							|| t instanceof ChillingTrap || t instanceof FrostTrap
//							|| t instanceof ExplosiveTrap || t instanceof DisintegrationTrap)) {
//
						return pos;
//					}
				}
			}
		}
	}
	
	@Override
	public int fallCell( boolean fallIntoPit ) {
		if (fallIntoPit) {
			for (Room room : rooms) {
				if (room instanceof PitRoom) {
					int result;
					do {
						result = pointToCell(room.random());
					} while (traps.get(result) != null
							|| findMob(result) != null
							|| heaps.get(result) != null);
					return result;
				}
			}
		}
		
		return super.fallCell( false );
	}
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( "rooms", rooms );
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		
		rooms = new ArrayList<>( (Collection<Room>) ((Collection<?>) bundle.getCollection( "rooms" )) );
		for (Room r : rooms) {
			r.onLevelLoad( this );
			if (r instanceof EntranceRoom ){
				roomEntrance = r;
			} else if (r instanceof ExitRoom ){
				roomExit = r;
			}
		}
	}
	
}
