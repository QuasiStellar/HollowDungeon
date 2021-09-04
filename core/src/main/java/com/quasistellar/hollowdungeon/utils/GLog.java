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

package com.quasistellar.hollowdungeon.utils;

import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Signal;

public class GLog {

	public static final String TAG = "GAME";

	public static final String POSITIVE		= "++ ";
	public static final String DEBUG   		= "[DEBUG]: ";
	public static final String NEGATIVE		= "-- ";
	public static final String WARNING		= "** ";
	public static final String HIGHLIGHT	= "@@ ";

	public static final String NEW_LINE	    = "\n";

	public static Signal<String> update = new Signal<>();

	public static void newLine(){
		update.dispatch( NEW_LINE );
	}

	public static void i( String text, Object... args ) {

		if (args.length > 0) {
			text = Messages.format( text, args );
		}

		DeviceCompat.log( TAG, text );
		update.dispatch( text );
	}

	public static void p( String text, Object... args ) {
		i( POSITIVE + text, args );
	}

	public static void n( String text, Object... args ) {
		i( NEGATIVE + text, args );
	}

	public static void w( String text, Object... args ) {
		i( WARNING + text, args );
	}

	public static void h( String text, Object... args ) {
		i( HIGHLIGHT + text, args );
	}

	public static void info(String text, Object... args ) {

		if (args.length > 0) {
			text = Messages.format( text, args );
		}

		DeviceCompat.log( TAG, text );
		update.dispatch( text );
	}

	public static void positive(String text, Object... args ) {
		info( POSITIVE + text, args );
	}

	public static void negative(String text, Object... args ) {
		info( NEGATIVE + text, args );
	}

	public static void warning(String text, Object... args ) {
		info( WARNING + text, args );
	}

	public static void highlight(String text, Object... args ) {
		info( HIGHLIGHT + text, args );
	}

	public static void debug(String text, Object... args ) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		StringBuilder addToLog = new StringBuilder(DEBUG + text);
		addToLog.append("\n" + "Trace:\n");
		for (StackTraceElement element : trace) {
			addToLog.append(element.toString()).append("\n");
		}
		HollowDungeon.appendLog(addToLog.toString());

		if (HDSettings.debugReport()) {
			info(DEBUG + text, args);
		}
	}
}
