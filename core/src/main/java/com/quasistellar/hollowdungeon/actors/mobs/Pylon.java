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

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.blobs.Electricity;
import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.actors.buffs.Vertigo;
import com.quasistellar.hollowdungeon.effects.Lightning;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.levels.NewCavesBossLevel;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.PylonSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.particles.SparkParticle;
import com.quasistellar.hollowdungeon.tiles.DungeonTilemap;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Pylon extends Mob {

	{
		spriteClass = com.quasistellar.hollowdungeon.sprites.PylonSprite.class;

		HP = HT = 50;

		properties.add(Char.Property.MINIBOSS);
		properties.add(Char.Property.INORGANIC);
		properties.add(Char.Property.ELECTRIC);
		properties.add(Char.Property.IMMOVABLE);

		state = PASSIVE;
		alignment = Char.Alignment.NEUTRAL;
	}

	private int targetNeighbor = Random.Int(8);

	@Override
	protected boolean act() {
		spend(Actor.TICK);

		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null) {
			int n;
			do {
				n = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			} while (!Dungeon.level.passable[n] && !Dungeon.level.avoid[n]);
			Dungeon.level.drop( heap.pickUp(), n ).sprite.drop( pos );
		}

		if (alignment == Char.Alignment.NEUTRAL){
			return true;
		}

		int cell1 = pos + PathFinder.CIRCLE8[targetNeighbor];
		int cell2 = pos + PathFinder.CIRCLE8[(targetNeighbor+4)%8];

		sprite.flash();
		if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[cell1] || Dungeon.level.heroFOV[cell2]) {
			sprite.parent.add(new Lightning(DungeonTilemap.raisedTileCenterToWorld(cell1),
					com.quasistellar.hollowdungeon.tiles.DungeonTilemap.raisedTileCenterToWorld(cell2), null));
			CellEmitter.get(cell1).burst(SparkParticle.FACTORY, 3);
			com.quasistellar.hollowdungeon.effects.CellEmitter.get(cell2).burst(com.quasistellar.hollowdungeon.effects.particles.SparkParticle.FACTORY, 3);
			Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		}

		shockChar(Actor.findChar(cell1));
		shockChar(com.quasistellar.hollowdungeon.actors.Actor.findChar(cell2));

		targetNeighbor = (targetNeighbor+1)%8;

		return true;
	}

	private void shockChar( com.quasistellar.hollowdungeon.actors.Char ch ){
		if (ch != null && !(ch instanceof com.quasistellar.hollowdungeon.actors.mobs.NewDM300)){
			ch.sprite.flash();
			ch.damage(Random.NormalIntRange(10, 20), new com.quasistellar.hollowdungeon.actors.blobs.Electricity());

			if (ch == Dungeon.hero && !ch.isAlive()){
				Dungeon.fail(NewDM300.class);
				GLog.n( Messages.get(Electricity.class, "ondeath") );
			}
		}
	}

	public void activate(){
		alignment = Char.Alignment.ENEMY;
		((com.quasistellar.hollowdungeon.sprites.PylonSprite) sprite).activate();
	}

	@Override
	public CharSprite sprite() {
		com.quasistellar.hollowdungeon.sprites.PylonSprite p = (PylonSprite) super.sprite();
		if (alignment != Char.Alignment.NEUTRAL) p.activate();
		return p;
	}

	@Override
	public void notice() {
		//do nothing
	}

	@Override
	public String description() {
		if (alignment == Char.Alignment.NEUTRAL){
			return Messages.get(this, "desc_inactive");
		} else {
			return Messages.get(this, "desc_active");
		}
	}

	@Override
	public boolean interact(com.quasistellar.hollowdungeon.actors.Char c) {
		return true;
	}

	@Override
	public void add(com.quasistellar.hollowdungeon.actors.buffs.Buff buff) {
		//immune to all buffs/debuffs when inactive
		if (alignment != com.quasistellar.hollowdungeon.actors.Char.Alignment.NEUTRAL) {
			super.add(buff);
		}
	}

	@Override
	public void damage(int dmg, Object src) {
		//immune to damage when inactive
		if (alignment == com.quasistellar.hollowdungeon.actors.Char.Alignment.NEUTRAL){
			return;
		}
		if (dmg >= 15){
			//takes 15/16/17/18/19/20 dmg at 15/17/20/24/29/36 incoming dmg
			dmg = 14 + (int)(Math.sqrt(8*(dmg - 14) + 1) - 1)/2;
		}
		super.damage(dmg, src);
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		((NewCavesBossLevel) com.quasistellar.hollowdungeon.Dungeon.level).eliminatePylon();
	}

	private static final String ALIGNMENT = "alignment";
	private static final String TARGET_NEIGHBOUR = "target_neighbour";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ALIGNMENT, alignment);
		bundle.put(TARGET_NEIGHBOUR, targetNeighbor);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		alignment = bundle.getEnum(ALIGNMENT, com.quasistellar.hollowdungeon.actors.Char.Alignment.class);
		targetNeighbor = bundle.getInt(TARGET_NEIGHBOUR);
	}

	{
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Paralysis.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Amok.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Sleep.class );
		immunities.add( ToxicGas.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Terror.class );
		immunities.add( Vertigo.class );
	}

}
