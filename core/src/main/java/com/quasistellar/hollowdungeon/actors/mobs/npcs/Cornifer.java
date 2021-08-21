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

package com.quasistellar.hollowdungeon.actors.mobs.npcs;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.FCMap;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CorniferSprite;
import com.quasistellar.hollowdungeon.sprites.WandmakerSprite;
import com.quasistellar.hollowdungeon.windows.WndQuest;
import com.quasistellar.hollowdungeon.windows.WndQuestTrade;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class Cornifer extends NPC {

    {
        spriteClass = CorniferSprite.class;

        properties.add(Property.IMMOVABLE);
    }

    private boolean talked = false;
    public boolean bought = false;

    private static final String TALKED    = "talked";
    private static final String BOUGHT    = "bought";

    @Override
    public void storeInBundle( Bundle bundle ) {

        super.storeInBundle( bundle );

        bundle.put( TALKED, talked );
        bundle.put( BOUGHT, bought );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );

        talked = bundle.getBoolean( TALKED );
        bought = bundle.getBoolean( BOUGHT );
    }

    @Override
    public void damage( int dmg, Object src ) {
    }

    @Override
    public void add( Buff buff ) {
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public boolean interact(Char c) {
        sprite.turnTo( pos, Dungeon.hero.pos );

        if (c != Dungeon.hero){
            return true;
        }

        if (!talked && !bought) {
            talked = true;
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndQuest(Cornifer.this, Messages.get(Cornifer.class, "intro_1")){
                        @Override
                        public void hide() {
                            super.hide();
                            GameScene.show(new WndQuestTrade(Cornifer.this, Messages.get(Cornifer.class, "deal"), new FCMap(), 100));
                        }
                    });
                }
            });
        } else if (!bought) {
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndQuestTrade(Cornifer.this, Messages.get(Cornifer.class, "changed"), new FCMap(), 100));
                }
            });
        } else {
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndQuest(Cornifer.this, Messages.get(Cornifer.class, "bought")));
                }
            });
        }

        return true;
    }
}