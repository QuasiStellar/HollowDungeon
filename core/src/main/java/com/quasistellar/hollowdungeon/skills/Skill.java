package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.HDSettings;
import com.watabou.input.GameAction;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class Skill implements Bundlable {

    public static final int MOTHWING_CLOAK = 0;
    public static final int CRYSTAL_HEART = 1;
    public static final int MONARCH_WINGS = 2;
    public static final int DREAM_NAIL = 3;
    public static final int DREAMGATE = 4;
    public static final int FOCUS = 5;
    public static final int VENGEFUL_SPIRIT = 6;
    public static final int DESOLATE_DIVE = 7;
    public static final int HOWLING_WRAITHS = 8;

    public static final int NONE = 10;

    public void act() {
        Dungeon.hero.spend(1);
        Dungeon.hero.busy();
        Dungeon.hero.sprite.operate( Dungeon.hero.pos );
    }

    public boolean spell() { return false; }

    public int manaCost() {
        return 1;
    }

    public boolean visible() {
        return HDSettings.skills();
    }

    public int icon() {
        return NONE;
    }

    public GameAction action() {
        return HDAction.NONE;
    }

    public String desc(){
        return "";
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
    }

}
