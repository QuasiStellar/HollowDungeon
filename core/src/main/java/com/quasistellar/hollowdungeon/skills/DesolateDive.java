package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.particles.SmallSoulParticle;
import com.quasistellar.hollowdungeon.effects.particles.SoulParticle;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.input.GameAction;
import com.watabou.utils.PathFinder;

public class DesolateDive extends Skill {

    private static boolean inProcess = false;

    @Override
    public boolean spell() { return true; }

    @Override
    public void act() {
        if (inProcess) {
            return;
        }
        inProcess = true;
        Dungeon.hero.busy();
        Dungeon.hero.sprite.jump(Dungeon.hero.pos, Dungeon.hero.pos, () -> {
            CellEmitter.get(Dungeon.hero.pos).burst(SoulParticle.FACTORY, 15);
            for (int i : PathFinder.NEIGHBOURS8){
                Char enemy = Actor.findChar(Dungeon.hero.pos + i);
                if (enemy != null) {
                    Actor.findChar(Dungeon.hero.pos + i).damage(15, this);
                }
            }
            for (int i : PathFinder.NEIGHBOURS25){
                if (Dungeon.hero.pos + i >= 0 && Dungeon.hero.pos + i < Dungeon.level.length()) {
                    ShockWave shockWave = new ShockWave();
                    shockWave.cell = Dungeon.hero.pos + i;
                    Actor.addDelayed(shockWave, 1);
                }
            }
            Buff.prolong(Dungeon.hero, Hero.Invulnerable.class, 0.9f);
            Dungeon.hero.loseMana(manaCost());
            Dungeon.hero.spendAndNext(1);
            inProcess = false;
        }, 10, 0.2f);
    }

    public static class ShockWave extends Actor {

        public int cell;

        {
            actPriority = HERO_PRIO+1;
        }

        @Override
        protected boolean act() {
            CellEmitter.get(cell).burst(SmallSoulParticle.FACTORY, 1);
            Char mob = Actor.findChar(cell);
            if (mob != null && mob.alignment == Char.Alignment.ENEMY) {
                mob.damage(20, this);
            }
            Actor.remove( this );
            return true;
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
        return Skill.DESOLATE_DIVE;
    }

    @Override
    public GameAction action() {
        return HDAction.DESOLATE_DIVE;
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
