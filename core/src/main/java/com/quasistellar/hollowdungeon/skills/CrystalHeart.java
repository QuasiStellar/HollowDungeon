package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.input.GameAction;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

public class CrystalHeart extends Skill {

    @Override
    public void act() {
        Buff delay = Dungeon.hero.buff(Utils.OneTurnDelay.class);
        if (delay == null) {
            GLog.w(Messages.get(this, "delay"));
            return;
        }
        GameScene.selectCell( leaper );
    }

    protected CellSelector.Listener leaper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {
            GameScene.unpause = false;
            if (target != null && target != Dungeon.hero.pos) {
                Ballistica route = new Ballistica(Dungeon.hero.pos, target, Ballistica.MAGIC_BOLT);
                int cell = route.collisionPos;

                //can't occupy the same cell as another char, so move back one.
                if (Actor.findChar( cell ) != null && cell != Dungeon.hero.pos)
                    cell = route.path.get(route.dist-1);

                Invisibility.dispel();

                final int dest = cell;
                Dungeon.hero.busy();
                Dungeon.hero.sprite.jump(Dungeon.hero.pos, cell, new Callback() {
                    @Override
                    public void call() {
                        Dungeon.hero.move(dest);
                        Dungeon.level.occupyCell(Dungeon.hero);
                        Dungeon.observe();
                        GameScene.updateFog();

                        Camera.main.shake(2, 0.5f);

                        Dungeon.hero.spendAndNext(1);
                        Buff.detach(Dungeon.hero, Utils.OneTurnDelay.class);
                    }
                }, 0, Dungeon.level.trueDistance( Dungeon.hero.pos, cell ) * 0.05f);
            }
        }

        @Override
        public String prompt() {
            return Messages.get(CrystalHeart.class, "prompt");
        }
    };

    @Override
    public int manaCost() {
        return 0;
    }

    @Override
    public boolean visible() {
        Buff delay = Dungeon.hero.buff(Utils.OneTurnDelay.class);
        return super.visible() && delay != null;
    }

    @Override
    public int icon() {
        return Skill.CRYSTAL_HEART;
    }

    @Override
    public GameAction action() {
        return HDAction.CRYSTAL_HEART;
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
