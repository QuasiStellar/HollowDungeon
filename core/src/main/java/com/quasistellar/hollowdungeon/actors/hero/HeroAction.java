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

package com.quasistellar.hollowdungeon.actors.hero;

import com.quasistellar.hollowdungeon.actors.Char;

public class HeroAction {
	
	public int dst;
	
	public static class Move extends HeroAction {
		public Move( int dst ) {
			this.dst = dst;
		}
	}
	
	public static class PickUp extends HeroAction {
		public PickUp( int dst ) {
			this.dst = dst;
		}
	}
	
	public static class OpenChest extends HeroAction {
		public OpenChest( int dst ) {
			this.dst = dst;
		}
	}
	
	public static class Buy extends HeroAction {
		public Buy( int dst ) {
			this.dst = dst;
		}
	}
	
	public static class Interact extends HeroAction {
		public com.quasistellar.hollowdungeon.actors.Char ch;
		public Interact( com.quasistellar.hollowdungeon.actors.Char ch ) {
			this.ch = ch;
		}
	}
	
	public static class Unlock extends HeroAction {
		public Unlock( int door ) {
			this.dst = door;
		}
	}
	
	public static class Descend extends HeroAction {
		public Descend( int stairs ) {
			this.dst = stairs;
		}
	}
	
	public static class Ascend extends HeroAction {
		public Ascend( int stairs ) {
			this.dst = stairs;
		}
	}

	public static class Transit extends HeroAction {
		public Transit( int stairs ) {
			this.dst = stairs;
		}
	}
	
	public static class Alchemy extends HeroAction {
		public Alchemy( int pot ) {
			this.dst = pot;
		}
	}
	
	public static class Attack extends HeroAction {
		public com.quasistellar.hollowdungeon.actors.Char target;
		public Attack( Char target ) {
			this.target = target;
		}
	}
}
