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

package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.effects.MagicMissile;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.ConeAOE;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.watabou.input.GameAction;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class HowlingWraiths extends Skill {

    @Override
    public boolean spell() { return true; }

    @Override
    public void act() {
        GameScene.selectCell(selector);
    }

    private CellSelector.Listener selector = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;

            final int targetCell = cell;
            final Hero hero = Dungeon.hero;

            hero.sprite.idle();
            hero.sprite.zap(targetCell);
            hero.busy();

            final Ballistica shot = new Ballistica( Dungeon.hero.pos, targetCell, Ballistica.WONT_STOP);

            int maxDist = 4;
            int dist = Math.min(shot.dist, maxDist);

            ConeAOE cone;
            cone = new ConeAOE( shot.sourcePos, shot.path.get(dist),
                    maxDist,
                    50,
                    Ballistica.STOP_TARGET);

            //cast to cells at the tip, rather than all cells, better performance.
            for (Ballistica ray : cone.rays){
                ((MagicMissile)Dungeon.hero.sprite.parent.recycle( MagicMissile.class )).reset(
                        MagicMissile.SOUL_CONE,
                        Dungeon.hero.sprite,
                        ray.path.get(ray.dist),
                        null
                );
            }

            //final zap at half distance, for timing of the actual wand effect
            MagicMissile.boltFromChar(Dungeon.hero.sprite.parent,
                    MagicMissile.SOUL_CONE,
                    Dungeon.hero.sprite,
                    shot.path.get(dist / 2),
                    new Callback() {
                        @Override
                        public void call() {
                            ArrayList<Char> affectedChars = new ArrayList<>();
                            for( int tile : cone.cells ){

                                //ignore caster cell
                                if (tile == shot.sourcePos){
                                    continue;
                                }

                                Char ch = Actor.findChar( tile );
                                if (ch != null) {
                                    affectedChars.add(ch);
                                }
                            }

                            for ( Char ch : affectedChars ){
                                ch.damage(10, this);
                            }

                            Dungeon.hero.spendAndNext(1);
                            Dungeon.hero.loseMana(manaCost());
                        }
                    });
        }

        @Override
        public String prompt() {
            return Messages.get(HowlingWraiths.class, "prompt");
        }
    };

    @Override
    public int manaCost() {
        return 33;
    }

    @Override
    public boolean visible() {
        return super.visible() && Dungeon.hero.MP >= 33;
    }

    @Override
    public int icon() {
        return Skill.HOWLING_WRAITHS;
    }

    @Override
    public GameAction action() {
        return HDAction.HOWLING_WRAITHS;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }

}
