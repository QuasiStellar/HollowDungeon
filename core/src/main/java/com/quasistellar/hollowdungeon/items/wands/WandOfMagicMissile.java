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

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.ui.QuickSlotButton;
import com.quasistellar.hollowdungeon.effects.SpellSprite;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class WandOfMagicMissile extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_MAGIC_MISSILE;
	}

	public int min(int lvl){
		return 2+lvl;
	}

	public int max(int lvl){
		return 8+2*lvl;
	}
	
	@Override
	protected void onZap( Ballistica bolt ) {
				
		com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar( bolt.collisionPos );
		if (ch != null) {

			processSoulMark(ch, chargesPerCast());
			ch.damage(damageRoll(), this);
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, Random.Float(0.87f, 1.15f) );

			ch.sprite.burst(0xFFFFFFFF, buffedLvl() / 2 + 2);

			//apply the magic charge buff if we have another wand in inventory of a lower level, or already have the buff
			for (Charger wandCharger : com.quasistellar.hollowdungeon.items.Item.curUser.buffs(Charger.class)){
				if (wandCharger.wand().buffedLvl() < buffedLvl() || com.quasistellar.hollowdungeon.items.Item.curUser.buff(MagicCharge.class) != null){
					com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong(Item.curUser, MagicCharge.class, MagicCharge.DURATION).setLevel(buffedLvl());
					break;
				}
			}

		} else {
			Dungeon.level.pressCell(bolt.collisionPos);
		}
	}

	@Override
	public void onHit(MagesStaff staff, com.quasistellar.hollowdungeon.actors.Char attacker, Char defender, int damage) {
		SpellSprite.show(attacker, com.quasistellar.hollowdungeon.effects.SpellSprite.CHARGE);
		for (Charger c : attacker.buffs(Charger.class)){
			if (c.wand() != this){
				c.gainCharge(0.33f);
			}
		}

	}
	
	protected int initialCharges() {
		return 3;
	}

	public static class MagicCharge extends FlavourBuff {

		{
			type = Buff.buffType.POSITIVE;
			announced = true;
		}

		public static float DURATION = 4f;

		private int level = 0;

		public void setLevel(int level){
			this.level = Math.max(level, this.level);
		}

		@Override
		public void detach() {
			super.detach();
			QuickSlotButton.refresh();
		}

		public int level(){
			return this.level;
		}

		@Override
		public int icon() {
			return BuffIndicator.RECHARGING;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.2f, 0.6f, 1f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		@Override
		public String toString() {
			return Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", level(), dispTurns());
		}
	}

}
