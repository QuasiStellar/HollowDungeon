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
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.sprites.MissileSprite;
import com.watabou.input.GameAction;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class VengefulSpirit extends Skill {

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

            final Ballistica shot = new Ballistica( Dungeon.hero.pos, targetCell, Ballistica.STOP_TERRAIN);

            ((MissileSprite) HollowDungeon.scene().recycle(MissileSprite.class)).
                    reset(Dungeon.hero.pos, shot.collisionPos, new VengefulSpiritShot(), new Callback() {
                        @Override
                        public void call() {

                            ArrayList<Char> chars = new ArrayList<>();

                            for (int c : shot.subPath(1, shot.path.size()-1)) {

                                Char ch;
                                if ((ch = Actor.findChar( c )) != null) {
                                    chars.add( ch );
                                }

                            }

                            for (Char ch : chars) {
                                ch.damage( 15, this );
                                ch.sprite.flash();
                            }

                            hero.spendAndNext(1f);

                        }
                    });

            Dungeon.hero.loseMana(manaCost());

        }

        @Override
        public String prompt() {
            return Messages.get(VengefulSpirit.class, "prompt");
        }
    };

    public static class VengefulSpiritShot extends Item {
        {
            image = ItemSpriteSheet.VENGEFUL_SPIRIT;
        }
    }

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
        return Skill.VENGEFUL_SPIRIT;
    }

    @Override
    public GameAction action() {
        return HDAction.VENGEFUL_SPIRIT;
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
