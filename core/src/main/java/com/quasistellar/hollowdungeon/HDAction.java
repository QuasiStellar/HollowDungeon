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

package com.quasistellar.hollowdungeon;

import com.badlogic.gdx.Input;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

public class HDAction extends GameAction {

	protected HDAction(String name ){
		super( name );
	}

	//--New references to existing actions from GameAction
	public static final GameAction NONE  = GameAction.NONE;
	public static final GameAction BACK  = GameAction.BACK;
	//--

//	public static final GameAction HERO_INFO   = new HDAction("hero_info");
//	public static final GameAction JOURNAL     = new HDAction("journal");

	public static final GameAction WAIT        = new HDAction("wait");
	public static final GameAction SEARCH      = new HDAction("search");
//	public static final GameAction REST        = new HDAction("rest");

	public static final GameAction INVENTORY   = new HDAction("inventory");

	public static final GameAction FOCUS   = new HDAction("focus");
	public static final GameAction VENGEFUL_SPIRIT   = new HDAction("vengeful_spirit");
	public static final GameAction DESOLATE_DIVE   = new HDAction("desolate_dive");
	public static final GameAction HOWLING_WRAITHS   = new HDAction("howling_wraiths");
	public static final GameAction MOTHWING_CLOAK   = new HDAction("mothwing_cloak");
	public static final GameAction MONARCH_WINGS   = new HDAction("monarch_wings");
	public static final GameAction CRYSTAL_HEART   = new HDAction("crystal_heart");
	public static final GameAction DREAM_NAIL   = new HDAction("dream_nail");
	public static final GameAction DREAMGATE   = new HDAction("dreamgate");

//	public static final GameAction QUICKSLOT_1 = new HDAction("quickslot_1");
//	public static final GameAction QUICKSLOT_2 = new HDAction("quickslot_2");
//	public static final GameAction QUICKSLOT_3 = new HDAction("quickslot_3");
//	public static final GameAction QUICKSLOT_4 = new HDAction("quickslot_4");

	public static final GameAction TAG_ATTACK  = new HDAction("tag_attack");
	public static final GameAction TAG_DANGER  = new HDAction("tag_danger");
	public static final GameAction TAG_ACTION  = new HDAction("tag_action");
	public static final GameAction TAG_LOOT    = new HDAction("tag_loot");
	public static final GameAction TAG_RESUME  = new HDAction("tag_resume");

	public static final GameAction ZOOM_IN     = new HDAction("zoom_in");
	public static final GameAction ZOOM_OUT    = new HDAction("zoom_out");

	public static final GameAction N           = new HDAction("n");
	public static final GameAction E           = new HDAction("e");
	public static final GameAction S           = new HDAction("s");
	public static final GameAction W           = new HDAction("w");
	public static final GameAction NE          = new HDAction("ne");
	public static final GameAction SE          = new HDAction("se");
	public static final GameAction SW          = new HDAction("sw");
	public static final GameAction NW          = new HDAction("nw");

	private static final LinkedHashMap<Integer, GameAction> defaultBindings = new LinkedHashMap<>();
	static {
		defaultBindings.put( Input.Keys.ESCAPE,      HDAction.BACK );
		defaultBindings.put( Input.Keys.BACKSPACE,   HDAction.BACK );

//		defaultBindings.put( Input.Keys.H,           HDAction.HERO_INFO );
//		defaultBindings.put( Input.Keys.J,           HDAction.JOURNAL );

		defaultBindings.put( Input.Keys.SPACE,       HDAction.WAIT );
		defaultBindings.put( Input.Keys.S,           HDAction.SEARCH );
//		defaultBindings.put( Input.Keys.Z,           HDAction.REST );

		defaultBindings.put( Input.Keys.I,           HDAction.INVENTORY );

		defaultBindings.put( Input.Keys.Q,           HDAction.FOCUS );
		defaultBindings.put( Input.Keys.W,           HDAction.VENGEFUL_SPIRIT );
		defaultBindings.put( Input.Keys.E,           HDAction.DESOLATE_DIVE );
		defaultBindings.put( Input.Keys.R,           HDAction.HOWLING_WRAITHS );
		defaultBindings.put( Input.Keys.T,           HDAction.MOTHWING_CLOAK );
		defaultBindings.put( Input.Keys.Y,           HDAction.MONARCH_WINGS );
		defaultBindings.put( Input.Keys.U,           HDAction.CRYSTAL_HEART );
		defaultBindings.put( Input.Keys.O,           HDAction.DREAM_NAIL );
		defaultBindings.put( Input.Keys.P,           HDAction.DREAMGATE );

//		defaultBindings.put( Input.Keys.Q,           HDAction.QUICKSLOT_1 );
//		defaultBindings.put( Input.Keys.W,           HDAction.QUICKSLOT_2 );
//		defaultBindings.put( Input.Keys.E,           HDAction.QUICKSLOT_3 );
//		defaultBindings.put( Input.Keys.R,           HDAction.QUICKSLOT_4 );

		defaultBindings.put( Input.Keys.A,           HDAction.TAG_ATTACK );
		defaultBindings.put( Input.Keys.TAB,         HDAction.TAG_DANGER );
		defaultBindings.put( Input.Keys.D,           HDAction.TAG_ACTION );
		defaultBindings.put( Input.Keys.ENTER,       HDAction.TAG_LOOT );
		defaultBindings.put( Input.Keys.G,           HDAction.TAG_RESUME );

		defaultBindings.put( Input.Keys.PLUS,        HDAction.ZOOM_IN );
		defaultBindings.put( Input.Keys.EQUALS,      HDAction.ZOOM_IN );
		defaultBindings.put( Input.Keys.MINUS,       HDAction.ZOOM_OUT );

		defaultBindings.put( Input.Keys.UP,          HDAction.N );
		defaultBindings.put( Input.Keys.RIGHT,       HDAction.E );
		defaultBindings.put( Input.Keys.DOWN,        HDAction.S );
		defaultBindings.put( Input.Keys.LEFT,        HDAction.W );

		defaultBindings.put( Input.Keys.NUMPAD_5,    HDAction.WAIT );
		defaultBindings.put( Input.Keys.NUMPAD_8,    HDAction.N );
		defaultBindings.put( Input.Keys.NUMPAD_9,    HDAction.NE );
		defaultBindings.put( Input.Keys.NUMPAD_6,    HDAction.E );
		defaultBindings.put( Input.Keys.NUMPAD_3,    HDAction.SE );
		defaultBindings.put( Input.Keys.NUMPAD_2,    HDAction.S );
		defaultBindings.put( Input.Keys.NUMPAD_1,    HDAction.SW );
		defaultBindings.put( Input.Keys.NUMPAD_4,    HDAction.W );
		defaultBindings.put( Input.Keys.NUMPAD_7,    HDAction.NW );
	}

	public static LinkedHashMap<Integer, GameAction> getDefaults() {
		return new LinkedHashMap<>(defaultBindings);
	}

	//hard bindings for android devices
	static {
		KeyBindings.addHardBinding( Input.Keys.BACK, HDAction.BACK );
		KeyBindings.addHardBinding( Input.Keys.MENU, HDAction.INVENTORY );
	}

	//we only save/loads keys which differ from the default configuration.
	private static final String BINDINGS_FILE = "keybinds.dat";

	public static void loadBindings(){

		try {
			Bundle b = FileUtils.bundleFromFile(BINDINGS_FILE);

			Bundle firstKeys = b.getBundle("first_keys");
			Bundle secondKeys = b.getBundle("second_keys");

			LinkedHashMap<Integer, GameAction> defaults = getDefaults();
			LinkedHashMap<Integer, GameAction> custom = new LinkedHashMap<>();

			for (GameAction a : allActions()) {
				if (firstKeys.contains(a.name())) {
					if (firstKeys.getInt(a.name()) == 0){
						for (int i : defaults.keySet()){
							if (defaults.get(i) == a){
								defaults.remove(i);
								break;
							}
						}
					} else {
						custom.put(firstKeys.getInt(a.name()), a);
						defaults.remove(firstKeys.getInt(a.name()));
					}
				}

				//we store any custom second keys in defaults for the moment to preserve order
				//incase the 2nd key is custom but the first one isn't
				if (secondKeys.contains(a.name())) {
					if (secondKeys.getInt(a.name()) == 0){
						int last = 0;
						for (int i : defaults.keySet()){
							if (defaults.get(i) == a){
								last = i;
							}
						}
						defaults.remove(last);
					} else {
						defaults.remove(secondKeys.getInt(a.name()));
						defaults.put(secondKeys.getInt(a.name()), a);
					}
				}

			}

			//now merge them and store
			for( int i : defaults.keySet()){
				if (i != 0) {
					custom.put(i, defaults.get(i));
				}
			}

			KeyBindings.setAllBindings(custom);

		} catch (Exception e){
			KeyBindings.setAllBindings(getDefaults());
		}

	}

	public static void saveBindings(){

		Bundle b = new Bundle();

		Bundle firstKeys = new Bundle();
		Bundle secondKeys = new Bundle();

		for (GameAction a : allActions()){
			int firstCur = 0;
			int secondCur = 0;
			int firstDef = 0;
			int secondDef = 0;

			for (int i : defaultBindings.keySet()){
				if (defaultBindings.get(i) == a){
					if(firstDef == 0){
						firstDef = i;
					} else {
						secondDef = i;
					}
				}
			}

			LinkedHashMap<Integer, GameAction> curBindings = KeyBindings.getAllBindings();
			for (int i : curBindings.keySet()){
				if (curBindings.get(i) == a){
					if(firstCur == 0){
						firstCur = i;
					} else {
						secondCur = i;
					}
				}
			}

			if (firstCur != firstDef){
				firstKeys.put(a.name(), firstCur);
			}
			if (secondCur != secondDef){
				secondKeys.put(a.name(), secondCur);
			}

		}

		b.put("first_keys", firstKeys);
		b.put("second_keys", secondKeys);

		try {
			FileUtils.bundleToFile(BINDINGS_FILE, b);
		} catch (IOException e) {
			HollowDungeon.reportException(e);
		}

	}

}
