package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.ui.AttackIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;

public class DreamNail extends Skill {

    @Override
    public void act() {
        Buff delay = Dungeon.hero.buff(Utils.OneTurnDelay.class);
        if (delay == null) {
            GLog.w(Messages.get(this, "delay"));
            return;
        }
        GameScene.selectCell(selector);
    }

    private CellSelector.Listener selector = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;
            final Char enemy = Actor.findChar( cell );
            if (enemy == null
                    || !Dungeon.level.heroFOV[cell]
                    || !Dungeon.level.adjacent(Dungeon.hero.pos, enemy.pos)
                    || Dungeon.hero.isCharmedBy( enemy )){
                GLog.w( Messages.get(DreamNail.class, "bad_target") );
            } else {
                Dungeon.hero.sprite.attack(cell, () -> doAttack(enemy));
            }
        }

        private void doAttack(final Char enemy){

            AttackIndicator.target(enemy);

            Ballistica trajectory = new Ballistica(Dungeon.hero.pos, enemy.pos, Ballistica.STOP_TARGET);
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
            Utils.throwChar(enemy, trajectory, 2);

            enemy.sprite.flash();

            Dungeon.hero.spendAndNext(1f);
        }

        @Override
        public String prompt() {
            return Messages.get(DreamNail.class, "prompt");
        }
    };

    @Override
    public int manaCost() {
        return 0;
    }

    @Override
    public boolean visible() {
        Buff delay = Dungeon.hero.buff(Utils.OneTurnDelay.class);
        return delay != null;
    }

    @Override
    public int icon() {
        return Skill.DREAM_NAIL;
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
