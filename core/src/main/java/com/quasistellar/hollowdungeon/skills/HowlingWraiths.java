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

            GameScene.unpause = false;

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
