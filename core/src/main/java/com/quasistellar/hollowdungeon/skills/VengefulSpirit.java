package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.ShatteredPixelDungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.items.Dewdrop;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.sprites.MissileSprite;
import com.quasistellar.hollowdungeon.ui.HpIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;
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

            ((MissileSprite) ShatteredPixelDungeon.scene().recycle(MissileSprite.class)).
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
            return Messages.get(this, "prompt");
        }
    };

    public static class VengefulSpiritShot extends Item {
        {
            image = ItemSpriteSheet.VENGEFUL_SPIRIT;
        }
    }

    @Override
    public int manaCost() {
        return 0;
    }

    @Override
    public int icon() {
        return Skill.VENGEFUL_SPIRIT;
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
