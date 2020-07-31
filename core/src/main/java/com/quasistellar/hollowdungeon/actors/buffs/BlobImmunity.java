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

package com.quasistellar.hollowdungeon.actors.buffs;

import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.actors.blobs.Blizzard;
import com.quasistellar.hollowdungeon.actors.blobs.ConfusionGas;
import com.quasistellar.hollowdungeon.actors.blobs.CorrosiveGas;
import com.quasistellar.hollowdungeon.actors.blobs.Electricity;
import com.quasistellar.hollowdungeon.actors.blobs.Fire;
import com.quasistellar.hollowdungeon.actors.blobs.Freezing;
import com.quasistellar.hollowdungeon.actors.blobs.Inferno;
import com.quasistellar.hollowdungeon.actors.blobs.ParalyticGas;
import com.quasistellar.hollowdungeon.actors.blobs.Regrowth;
import com.quasistellar.hollowdungeon.actors.blobs.SmokeScreen;
import com.quasistellar.hollowdungeon.actors.blobs.StenchGas;
import com.quasistellar.hollowdungeon.actors.blobs.StormCloud;
import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.actors.blobs.Web;
import com.quasistellar.hollowdungeon.messages.Messages;

public class BlobImmunity extends FlavourBuff {
	
	{
		type = Buff.buffType.POSITIVE;
	}
	
	public static final float DURATION	= 20f;
	
	@Override
	public int icon() {
		return BuffIndicator.IMMUNITY;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}
	
	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	{
		//all harmful blobs
		immunities.add( Blizzard.class );
		immunities.add( ConfusionGas.class );
		immunities.add( CorrosiveGas.class );
		immunities.add( Electricity.class );
		immunities.add( Fire.class );
		immunities.add( Freezing.class );
		immunities.add( Inferno.class );
		immunities.add( ParalyticGas.class );
		immunities.add( Regrowth.class );
		immunities.add( SmokeScreen.class );
		immunities.add( StenchGas.class );
		immunities.add( StormCloud.class );
		immunities.add( ToxicGas.class );
		immunities.add( Web.class );
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}
}
