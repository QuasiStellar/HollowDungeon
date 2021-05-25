package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.input.GameAction;

public class MonarchWings extends Skill {

    @Override
    public void act() {
        if (Dungeon.hero.buff(MonarchWingsDelay.class) != null) {
            GLog.w(Messages.get(this, "delay"));
            return;
        }
        GameScene.unpause = false;
        GameScene.selectCell( leaper );
    }

    protected CellSelector.Listener leaper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {
            if (target != null && target != Dungeon.hero.pos) {

                Ballistica route = new Ballistica(Dungeon.hero.pos, target, Ballistica.MAGIC_BOLT);
                int cell = route.collisionPos;

                if (route.dist > 2) {
                    cell = route.path.get(2);
                }

                //can't occupy the same cell as another char, so move back one.
                if (Actor.findChar( cell ) != null && cell != Dungeon.hero.pos)
                    cell = route.path.get(route.dist > 2 ? 1 : route.dist-1);

                Invisibility.dispel();

                final int dest = cell;
                Dungeon.hero.busy();
                Dungeon.hero.sprite.jump(Dungeon.hero.pos, cell, () -> {
                    Dungeon.hero.move(dest);
                    Dungeon.level.occupyCell(Dungeon.hero);
                    Dungeon.observe();
                    GameScene.updateFog();

                    //Camera.main.shake(2, 0.5f);

                    Dungeon.hero.spendAndNext(1);
                    Buff.prolong(Dungeon.hero, MonarchWingsDelay.class, 1.9f);
                }, 10, Dungeon.level.trueDistance( Dungeon.hero.pos, cell ) * 0.1f);
            }
        }

        @Override
        public String prompt() {
            return Messages.get(MonarchWings.class, "prompt");
        }
    };

    public static class MonarchWingsDelay extends FlavourBuff {
        //does nothing
    }

    @Override
    public int manaCost() {
        return 0;
    }

    @Override
    public boolean visible() {
        return super.visible() && Dungeon.hero.buff(MonarchWingsDelay.class) == null;
    }

    @Override
    public int icon() {
        return Skill.MONARCH_WINGS;
    }

    @Override
    public GameAction action() {
        return HDAction.MONARCH_WINGS;
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
