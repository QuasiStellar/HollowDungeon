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

package com.quasistellar.hollowdungeon.actors.mobs;

import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class Bestiary {
	
	public static ArrayList<Class<? extends Mob>> getMobRotation( String location ){
		ArrayList<Class<? extends Mob>> mobs = standardMobRotation( location );
		addRareMobs(location, mobs);
		swapMobAlts(mobs);
		Random.shuffle(mobs);
		return mobs;
	}
	
	//returns a rotation of standard mobs, unshuffled.
	private static ArrayList<Class<? extends Mob>> standardMobRotation( String location ){
		switch(location){

			default:
				//3x whusk, 1x vengefly
				return new ArrayList<>(Arrays.asList(
						WanderingHusk.class, WanderingHusk.class, WanderingHusk.class,
						Vengefly.class));
		}
		
	}
	
	//has a chance to add a rarely spawned mobs to the rotation
	public static void addRareMobs( String location, ArrayList<Class<?extends Mob>> rotation ){
		
		switch (location){
			
			// Sewers
			default:
				return;

		}
	}
	
	//switches out regular mobs for their alt versions when appropriate
	private static void swapMobAlts(ArrayList<Class<?extends Mob>> rotation){
//		for (int i = 0; i < rotation.size(); i++){
//			if (Random.Int( 50 ) == 0) {
//				Class<? extends Mob> cl = rotation.get(i);
//				if (cl == Rat.class) {
//					cl = Albino.class;
//				} else if (cl == Slime.class) {
//					cl = CausticSlime.class;
//				} else if (cl == Thief.class) {
//					cl = Bandit.class;
//				} else if (cl == Brute.class) {
//					cl = ArmoredBrute.class;
//				} else if (cl == DM200.class) {
//					cl = DM201.class;
//				} else if (cl == Monk.class) {
//					cl = Senior.class;
//				} else if (cl == Scorpio.class) {
//					cl = Acidic.class;
//				}
//				rotation.set(i, cl);
//			}
//		}
	}
}
