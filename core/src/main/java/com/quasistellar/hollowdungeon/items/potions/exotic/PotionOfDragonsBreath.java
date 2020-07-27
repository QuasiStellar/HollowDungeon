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

package com.quasistellar.hollowdungeon.items.potions.exotic;

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.MagicMissile;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.actors.blobs.Blob;
import com.quasistellar.hollowdungeon.actors.blobs.Fire;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Burning;
import com.quasistellar.hollowdungeon.actors.buffs.Cripple;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.ConeAOE;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class PotionOfDragonsBreath extends ExoticPotion {
	
	{
		icon = ItemSpriteSheet.Icons.POTION_DRGBREATH;
	}

	@Override
	//need to override drink so that time isn't spent right away
	protected void drink(final Hero hero) {
		com.quasistellar.hollowdungeon.items.Item.curUser = hero;
		com.quasistellar.hollowdungeon.items.Item.curItem = this;
		
		GameScene.selectCell(targeter);
	}
	
	private com.quasistellar.hollowdungeon.scenes.CellSelector.Listener targeter = new CellSelector.Listener() {
		@Override
		public void onSelect(final Integer cell) {

			if (cell == null && !isKnown()){
				setKnown();
				detach(com.quasistellar.hollowdungeon.items.Item.curUser.belongings.backpack);
			} else if (cell != null) {
				setKnown();
				Sample.INSTANCE.play( Assets.Sounds.DRINK );
				com.quasistellar.hollowdungeon.items.Item.curUser.sprite.operate(com.quasistellar.hollowdungeon.items.Item.curUser.pos, new Callback() {
					@Override
					public void call() {

						com.quasistellar.hollowdungeon.items.Item.curItem.detach(com.quasistellar.hollowdungeon.items.Item.curUser.belongings.backpack);

						com.quasistellar.hollowdungeon.items.Item.curUser.spend(1f);
						com.quasistellar.hollowdungeon.items.Item.curUser.sprite.idle();
						com.quasistellar.hollowdungeon.items.Item.curUser.sprite.zap(cell);
						Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.BURNING );

						final Ballistica bolt = new Ballistica(com.quasistellar.hollowdungeon.items.Item.curUser.pos, cell, Ballistica.STOP_TERRAIN);

						int maxDist = 6;
						int dist = Math.min(bolt.dist, maxDist);

						final ConeAOE cone = new ConeAOE(com.quasistellar.hollowdungeon.items.Item.curUser.pos, bolt.path.get(dist), 6, 60, Ballistica.STOP_TERRAIN | Ballistica.STOP_TARGET );

						//cast to cells at the tip, rather than all cells, better performance.
						for (Ballistica ray : cone.rays){
							((com.quasistellar.hollowdungeon.effects.MagicMissile) com.quasistellar.hollowdungeon.items.Item.curUser.sprite.parent.recycle( com.quasistellar.hollowdungeon.effects.MagicMissile.class )).reset(
									MagicMissile.FIRE_CONE,
									com.quasistellar.hollowdungeon.items.Item.curUser.sprite,
									ray.path.get(ray.dist),
									null
							);
						}
						
						MagicMissile.boltFromChar(com.quasistellar.hollowdungeon.items.Item.curUser.sprite.parent,
								com.quasistellar.hollowdungeon.effects.MagicMissile.FIRE_CONE,
								com.quasistellar.hollowdungeon.items.Item.curUser.sprite,
								bolt.path.get(dist / 2),
								new Callback() {
									@Override
									public void call() {
										for (int cell : cone.cells){
											//ignore caster cell
											if (cell == bolt.sourcePos){
												continue;
											}

											//only ignite cells directly near caster if they are flammable
											if (!Dungeon.level.adjacent(bolt.sourcePos, cell) || com.quasistellar.hollowdungeon.Dungeon.level.flamable[cell]){
												com.quasistellar.hollowdungeon.scenes.GameScene.add( Blob.seed( cell, 5, Fire.class ) );
											}
											
											Char ch = Actor.findChar( cell );
											if (ch != null) {
												
												Buff.affect( ch, Burning.class ).reignite( ch );
												Buff.affect(ch, Cripple.class, 5f);
											}
										}
										Item.curUser.next();
									}
								});
						
					}
				});
			}
		}
		
		@Override
		public String prompt() {
			return Messages.get(PotionOfDragonsBreath.class, "prompt");
		}
	};
}
