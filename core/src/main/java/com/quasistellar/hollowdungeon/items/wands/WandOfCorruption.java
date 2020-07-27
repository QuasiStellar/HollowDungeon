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

package com.quasistellar.hollowdungeon.items.wands;

import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.Statistics;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.MagicMissile;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.HashMap;

public class WandOfCorruption extends Wand {

	{
		image = ItemSpriteSheet.WAND_CORRUPTION;
	}
	
	//Note that some debuffs here have a 0% chance to be applied.
	// This is because the wand of corruption considers them to be a certain level of harmful
	// for the purposes of reducing resistance, but does not actually apply them itself
	
	private static final float MINOR_DEBUFF_WEAKEN = 1/4f;
	private static final HashMap<Class<? extends com.quasistellar.hollowdungeon.actors.buffs.Buff>, Float> MINOR_DEBUFFS = new HashMap<>();
	static{
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Weakness.class,       2f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Vulnerable.class,     2f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Cripple.class,        1f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Blindness.class,      1f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Terror.class,         1f);
		
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Chill.class,          0f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Ooze.class,           0f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Roots.class,          0f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Vertigo.class,        0f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Drowsy.class,         0f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Bleeding.class,       0f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Burning.class,        0f);
		MINOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Poison.class,         0f);
	}
	
	private static final float MAJOR_DEBUFF_WEAKEN = 1/2f;
	private static final HashMap<Class<? extends com.quasistellar.hollowdungeon.actors.buffs.Buff>, Float> MAJOR_DEBUFFS = new HashMap<>();
	static{
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Amok.class,           3f);
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Slow.class,           2f);
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Hex.class,            2f);
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Paralysis.class,      1f);
		
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Charm.class,          0f);
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.MagicalSleep.class,   0f);
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.SoulMark.class,       0f);
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Corrosion.class,      0f);
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Frost.class,          0f);
		MAJOR_DEBUFFS.put(com.quasistellar.hollowdungeon.actors.buffs.Doom.class,           0f);
	}
	
	@Override
	protected void onZap(Ballistica bolt) {
		com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar(bolt.collisionPos);

		if (ch != null){
			
			if (!(ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Mob)){
				return;
			}

			com.quasistellar.hollowdungeon.actors.mobs.Mob enemy = (com.quasistellar.hollowdungeon.actors.mobs.Mob) ch;

			float corruptingPower = 3 + buffedLvl()/2f;
			
			//base enemy resistance is usually based on their exp, but in special cases it is based on other criteria
			float enemyResist = 1 + enemy.HP;
			if (ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Mimic || ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Statue){
				enemyResist = 1 + Dungeon.depth;
			} else if (ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Piranha || ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Bee) {
				enemyResist = 1 + Dungeon.depth/2f;
			} else if (ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Wraith) {
				//divide by 5 as wraiths are always at full HP and are therefore ~5x harder to corrupt
				enemyResist = (1f + Dungeon.depth/3f) / 5f;
			} else if (ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Yog.BurningFist || ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Yog.RottingFist) {
				enemyResist = 1 + 30;
			} else if (ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Yog.Larva || ch instanceof com.quasistellar.hollowdungeon.actors.mobs.King.Undead){
				enemyResist = 1 + 5;
			} else if (ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Swarm){
				//child swarms don't give exp, so we force this here.
				enemyResist = 1 + 3;
			}
			
			//100% health: 5x resist   75%: 3.25x resist   50%: 2x resist   25%: 1.25x resist
			enemyResist *= 1 + 4*Math.pow(enemy.HP/(float)enemy.HT, 2);
			
			//debuffs placed on the enemy reduce their resistance
			for (com.quasistellar.hollowdungeon.actors.buffs.Buff buff : enemy.buffs()){
				if (MAJOR_DEBUFFS.containsKey(buff.getClass()))         enemyResist *= (1f-MAJOR_DEBUFF_WEAKEN);
				else if (MINOR_DEBUFFS.containsKey(buff.getClass()))    enemyResist *= (1f-MINOR_DEBUFF_WEAKEN);
				else if (buff.type == com.quasistellar.hollowdungeon.actors.buffs.Buff.buffType.NEGATIVE)           enemyResist *= (1f-MINOR_DEBUFF_WEAKEN);
			}
			
			//cannot re-corrupt or doom an enemy, so give them a major debuff instead
			if(enemy.buff(com.quasistellar.hollowdungeon.actors.buffs.Corruption.class) != null || enemy.buff(com.quasistellar.hollowdungeon.actors.buffs.Doom.class) != null){
				corruptingPower = enemyResist - 0.001f;
			}
			
			if (corruptingPower > enemyResist){
				corruptEnemy( enemy );
			} else {
				float debuffChance = corruptingPower / enemyResist;
				if (Random.Float() < debuffChance){
					debuffEnemy( enemy, MAJOR_DEBUFFS);
				} else {
					debuffEnemy( enemy, MINOR_DEBUFFS);
				}
			}

			processSoulMark(ch, chargesPerCast());
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 0.8f * Random.Float(0.87f, 1.15f) );
			
		} else {
			com.quasistellar.hollowdungeon.Dungeon.level.pressCell(bolt.collisionPos);
		}
	}
	
	private void debuffEnemy(com.quasistellar.hollowdungeon.actors.mobs.Mob enemy, HashMap<Class<? extends com.quasistellar.hollowdungeon.actors.buffs.Buff>, Float> category ){
		
		//do not consider buffs which are already assigned, or that the enemy is immune to.
		HashMap<Class<? extends com.quasistellar.hollowdungeon.actors.buffs.Buff>, Float> debuffs = new HashMap<>(category);
		for (com.quasistellar.hollowdungeon.actors.buffs.Buff existing : enemy.buffs()){
			if (debuffs.containsKey(existing.getClass())) {
				debuffs.put(existing.getClass(), 0f);
			}
		}
		for (Class<?extends com.quasistellar.hollowdungeon.actors.buffs.Buff> toAssign : debuffs.keySet()){
			 if (debuffs.get(toAssign) > 0 && enemy.isImmune(toAssign)){
			 	debuffs.put(toAssign, 0f);
			 }
		}
		
		//all buffs with a > 0 chance are flavor buffs
		Class<?extends com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff> debuffCls = (Class<? extends com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff>) Random.chances(debuffs);
		
		if (debuffCls != null){
			com.quasistellar.hollowdungeon.actors.buffs.Buff.append(enemy, debuffCls, 6 + buffedLvl()*3);
		} else {
			//if no debuff can be applied (all are present), then go up one tier
			if (category == MINOR_DEBUFFS)          debuffEnemy( enemy, MAJOR_DEBUFFS);
			else if (category == MAJOR_DEBUFFS)     corruptEnemy( enemy );
		}
	}
	
	private void corruptEnemy( Mob enemy ){
		//cannot re-corrupt or doom an enemy, so give them a major debuff instead
		if(enemy.buff(com.quasistellar.hollowdungeon.actors.buffs.Corruption.class) != null || enemy.buff(com.quasistellar.hollowdungeon.actors.buffs.Doom.class) != null){
			GLog.w( Messages.get(this, "already_corrupted") );
			return;
		}
		
		if (!enemy.isImmune(com.quasistellar.hollowdungeon.actors.buffs.Corruption.class)){
			enemy.HP = enemy.HT;
			for (com.quasistellar.hollowdungeon.actors.buffs.Buff buff : enemy.buffs()) {
				if (buff.type == com.quasistellar.hollowdungeon.actors.buffs.Buff.buffType.NEGATIVE
						&& !(buff instanceof com.quasistellar.hollowdungeon.actors.buffs.SoulMark)) {
					buff.detach();
				} else if (buff instanceof com.quasistellar.hollowdungeon.actors.buffs.PinCushion){
					buff.detach();
				}
			}
			if (enemy.alignment == Char.Alignment.ENEMY){
				enemy.rollToDropLoot();
			}
			
			com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(enemy, com.quasistellar.hollowdungeon.actors.buffs.Corruption.class);
			
			Statistics.enemiesSlain++;
			Badges.validateMonstersSlain();
			com.quasistellar.hollowdungeon.Statistics.qualifiedForNoKilling = false;
		} else {
			Buff.affect(enemy, com.quasistellar.hollowdungeon.actors.buffs.Doom.class);
		}
	}

	@Override
	public void onHit(com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff staff, com.quasistellar.hollowdungeon.actors.Char attacker, com.quasistellar.hollowdungeon.actors.Char defender, int damage) {
		// lvl 0 - 25%
		// lvl 1 - 40%
		// lvl 2 - 50%
		if (Random.Int( buffedLvl() + 4 ) >= 3){
			com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong( defender, com.quasistellar.hollowdungeon.actors.buffs.Amok.class, 4+ buffedLvl()*2);
		}
	}

	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar( com.quasistellar.hollowdungeon.items.Item.curUser.sprite.parent,
				com.quasistellar.hollowdungeon.effects.MagicMissile.SHADOW,
				Item.curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.ZAP );
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( 0 );
		particle.am = 0.6f;
		particle.setLifespan(2f);
		particle.speed.set(0, 5);
		particle.setSize( 0.5f, 2f);
		particle.shuffleXY(1f);
	}

}
