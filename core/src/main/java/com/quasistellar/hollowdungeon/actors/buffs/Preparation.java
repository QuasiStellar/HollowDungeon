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

package com.quasistellar.hollowdungeon.actors.buffs;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.NPC;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.Effects;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.ui.ActionIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.actors.hero.HeroAction;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Preparation extends Buff implements com.quasistellar.hollowdungeon.ui.ActionIndicator.Action {
	
	{
		//always acts after other buffs, so invisibility effects can process first
		actPriority = Actor.BUFF_PRIO - 1;
	}
	
	public enum AttackLevel{
		LVL_1( 1,  0.15f, 0.05f, 1, 1),
		LVL_2( 3,  0.30f, 0.15f, 1, 3),
		LVL_3( 6,  0.45f, 0.30f, 2, 5),
		LVL_4( 11, 0.60f, 0.50f, 3, 7);
		
		final int turnsReq;
		final float baseDmgBonus, KOThreshold;
		final int damageRolls, blinkDistance;
		
		AttackLevel( int turns, float base, float threshold, int rolls, int dist){
			turnsReq = turns;
			baseDmgBonus = base; KOThreshold = threshold;
			damageRolls = rolls; blinkDistance = dist;
		}
		
		public boolean canKO(com.quasistellar.hollowdungeon.actors.Char defender){
			if (defender.properties().contains(Char.Property.MINIBOSS)){
				return (defender.HP/(float)defender.HT) < (KOThreshold/5f);
			} else {
				return (defender.HP/(float)defender.HT) < KOThreshold;
			}
		}
		
		public int damageRoll( com.quasistellar.hollowdungeon.actors.Char attacker ){
			int dmg = attacker.damageRoll();
			for( int i = 1; i < damageRolls; i++){
				int newDmg = attacker.damageRoll();
				if (newDmg > dmg) dmg = newDmg;
			}
			return Math.round(dmg * (1f + baseDmgBonus));
		}
		
		public static AttackLevel getLvl(int turnsInvis){
			List<AttackLevel> values = Arrays.asList(values());
			Collections.reverse(values);
			for ( AttackLevel lvl : values ){
				if (turnsInvis >= lvl.turnsReq){
					return lvl;
				}
			}
			return LVL_1;
		}
	}
	
	private int turnsInvis = 0;
	
	@Override
	public boolean act() {
		if (target.invisible > 0){
			turnsInvis++;
			if (AttackLevel.getLvl(turnsInvis).blinkDistance > 0 && target == Dungeon.hero){
				ActionIndicator.setAction(this);
			}
			spend(Actor.TICK);
		} else {
			detach();
		}
		return true;
	}
	
	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}
	
	public int damageRoll( com.quasistellar.hollowdungeon.actors.Char attacker ){
		return AttackLevel.getLvl(turnsInvis).damageRoll(attacker);
	}

	public boolean canKO( com.quasistellar.hollowdungeon.actors.Char defender ){
		return AttackLevel.getLvl(turnsInvis).canKO(defender);
	}
	
	@Override
	public int icon() {
		return BuffIndicator.PREPARATION;
	}
	
	@Override
	public void tintIcon(Image icon) {
		switch (AttackLevel.getLvl(turnsInvis)){
			case LVL_1:
				icon.hardlight(0f, 1f, 0f);
				break;
			case LVL_2:
				icon.hardlight(1f, 1f, 0f);
				break;
			case LVL_3:
				icon.hardlight(1f, 0.6f, 0f);
				break;
			case LVL_4:
				icon.hardlight(1f, 0f, 0f);
				break;
		}
	}

	@Override
	public float iconFadePercent() {
		AttackLevel level = AttackLevel.getLvl(turnsInvis);
		if (level == AttackLevel.LVL_4){
			return 0;
		} else {
			float turnsForCur = level.turnsReq;
			float turnsForNext = AttackLevel.values()[level.ordinal()+1].turnsReq;
			turnsForNext -= turnsForCur;
			float turnsToNext = turnsInvis - turnsForCur;
			return Math.min(1, (turnsForNext - turnsToNext)/(turnsForNext));
		}
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}
	
	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		
		AttackLevel lvl = AttackLevel.getLvl(turnsInvis);

		desc += "\n\n" + Messages.get(this, "desc_dmg",
				(int)(lvl.baseDmgBonus*100),
				(int)(lvl.KOThreshold*100),
				(int)(lvl.KOThreshold*20));
		
		if (lvl.damageRolls > 1){
			desc += " " + Messages.get(this, "desc_dmg_likely");
		}
		
		if (lvl.blinkDistance > 0){
			desc += "\n\n" + Messages.get(this, "desc_blink", lvl.blinkDistance);
		}
		
		desc += "\n\n" + Messages.get(this, "desc_invis_time", turnsInvis);
		
		if (lvl.ordinal() != AttackLevel.values().length-1){
			AttackLevel next = AttackLevel.values()[lvl.ordinal()+1];
			desc += "\n" + Messages.get(this, "desc_invis_next", next.turnsReq);
		}
		
		return desc;
	}
	
	private static final String TURNS = "turnsInvis";
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		turnsInvis = bundle.getInt(TURNS);
		if (AttackLevel.getLvl(turnsInvis).blinkDistance > 0){
			com.quasistellar.hollowdungeon.ui.ActionIndicator.setAction(this);
		}
	}
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TURNS, turnsInvis);
	}
	
	@Override
	public Image getIcon() {
		Image actionIco = Effects.get(com.quasistellar.hollowdungeon.effects.Effects.Type.WOUND);
		tintIcon(actionIco);
		return actionIco;
	}
	
	@Override
	public void doAction() {
		GameScene.selectCell(attack);
	}
	
	private com.quasistellar.hollowdungeon.scenes.CellSelector.Listener attack = new CellSelector.Listener() {
		
		@Override
		public void onSelect(Integer cell) {
			if (cell == null) return;
			final com.quasistellar.hollowdungeon.actors.Char enemy = Actor.findChar( cell );
			if (enemy == null || Dungeon.hero.isCharmedBy(enemy) || enemy instanceof NPC){
				GLog.w(Messages.get(Preparation.class, "no_target"));
			} else {
				
				//just attack them then!
				if (Dungeon.hero.canAttack(enemy)){
					Dungeon.hero.curAction = new HeroAction.Attack( enemy );
					Dungeon.hero.next();
					return;
				}
				
				AttackLevel lvl = AttackLevel.getLvl(turnsInvis);
				
				boolean[] passable = Dungeon.level.passable.clone();
				//need to consider enemy cell as passable in case they are on a trap or chasm
				passable[cell] = true;
				PathFinder.buildDistanceMap(Dungeon.hero.pos, passable, lvl.blinkDistance+1);
				if (PathFinder.distance[cell] == Integer.MAX_VALUE){
					GLog.w(Messages.get(Preparation.class, "out_of_reach"));
					return;
				}
				
				//we can move through enemies when determining blink distance,
				// but not when actually jumping to a location
				for (com.quasistellar.hollowdungeon.actors.Char ch : com.quasistellar.hollowdungeon.actors.Actor.chars()){
					if (ch != Dungeon.hero)  passable[ch.pos] = false;
				}
				
				PathFinder.Path path = PathFinder.find(Dungeon.hero.pos, cell, passable);
				int attackPos = path == null ? -1 : path.get(path.size()-2);
				
				if (attackPos == -1 ||
						Dungeon.level.distance(attackPos, Dungeon.hero.pos) > lvl.blinkDistance){
					com.quasistellar.hollowdungeon.utils.GLog.w(Messages.get(Preparation.class, "out_of_reach"));
					return;
				}
				
				Dungeon.hero.pos = attackPos;
				Dungeon.level.occupyCell(Dungeon.hero);
				//prevents the hero from being interrupted by seeing new enemies
				Dungeon.observe();
				Dungeon.hero.checkVisibleMobs();
				
				Dungeon.hero.sprite.place( Dungeon.hero.pos );
				Dungeon.hero.sprite.turnTo( Dungeon.hero.pos, cell);
				CellEmitter.get( Dungeon.hero.pos ).burst( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.WOOL ), 6 );
				Sample.INSTANCE.play( Assets.Sounds.PUFF );

				Dungeon.hero.curAction = new HeroAction.Attack( enemy );
				com.quasistellar.hollowdungeon.Dungeon.hero.next();
			}
		}
		
		@Override
		public String prompt() {
			return Messages.get(Preparation.class, "prompt", AttackLevel.getLvl(turnsInvis).blinkDistance);
		}
	};
}
