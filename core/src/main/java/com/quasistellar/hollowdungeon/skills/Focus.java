package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.ui.HpIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.utils.Callback;

public class Focus extends Skill {

    @Override
    public boolean spell() { return true; }

    @Override
    public void act() {
        Buff delay = Dungeon.hero.buff(Utils.OneTurnDelay.class);
        if (delay == null) {
            GLog.w(Messages.get(this, "delay"));
            return;
        }
        int effect = Math.min( Dungeon.hero.HT - Dungeon.hero.HP, 1 );
        if (effect > 0) {
            Dungeon.hero.HP += effect;
            Dungeon.hero.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 1 );
            Dungeon.hero.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "value", effect) );
            HpIndicator.refreshHero();
            Dungeon.hero.spendAndNext(1);
            Dungeon.hero.loseMana(manaCost());
        } else {
            GLog.i( Messages.get(this, "already_full") );
        }
    }

    @Override
    public int manaCost() {
        return 33;
    }

    @Override
    public boolean visible() {
        return Dungeon.hero.MP >= 33 && Dungeon.hero.buff(Utils.OneTurnDelay.class) != null;
    }

    @Override
    public int icon() {
        return Skill.FOCUS;
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
