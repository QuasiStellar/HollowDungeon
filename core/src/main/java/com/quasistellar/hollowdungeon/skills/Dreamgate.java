package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.plants.Swiftthistle;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.scenes.InterlevelScene;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class Dreamgate extends Skill {

    @Override
    public void act() {
        Buff delay = Dungeon.hero.buff(Utils.TwoTurnsDelay.class);
        if (delay == null || delay.cooldown() < 2) {
            GLog.w(Messages.get(this, "delay"));
            return;
        }
        GameScene.show(new WndOptions(Messages.get(this, "name"),
                Messages.get(this, "wnd_body"),
                Messages.get(this, "wnd_return"),
                Messages.get(this, "wnd_set")){
            @Override
            protected void onSelect(int index) {
                if (index == 0){
                    returnBeacon(Dungeon.hero);
                } else if (index == 1){
                    setBeacon(Dungeon.hero);
                }
            }
        });
    }

    private void setBeacon(Hero hero ){
        Dungeon.hero.dreamgateLocation = Dungeon.location;
        Dungeon.hero.dreamgatePos = hero.pos;

        hero.spend( 1f );
        hero.busy();

        GLog.i( Messages.get(this, "set") );

        hero.sprite.operate( hero.pos );
        Sample.INSTANCE.play( Assets.Sounds.BEACON );
    }

    private void returnBeacon( Hero hero ){
//        if (Dungeon.bossLevel()) {
//            GLog.w( Messages.get(this, "preventing") );
//            return;
//        }

//        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
//            Char ch = Actor.findChar(hero.pos + PathFinder.NEIGHBOURS8[i]);
//            if (ch != null && ch.alignment == Char.Alignment.ENEMY) {
//                GLog.w( Messages.get(this, "creatures") );
//                return;
//            }
//        }

        if (Dungeon.hero.dreamgateLocation.equals(Dungeon.location)) {
            Utils.appear( hero, Dungeon.hero.dreamgatePos );
            for(Mob m : Dungeon.level.mobs){
                if (m.pos == hero.pos){
                    //displace mob
                    for(int i : PathFinder.NEIGHBOURS8){
                        if (Actor.findChar(m.pos+i) == null && Dungeon.level.passable[m.pos + i]){
                            m.pos += i;
                            m.sprite.point(m.sprite.worldToCamera(m.pos));
                            break;
                        }
                    }
                }
            }
            Dungeon.level.occupyCell(hero );
            Dungeon.observe();
            GameScene.updateFog();
        } else {

            Buff buff = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
            if (buff != null) buff.detach();

            InterlevelScene.mode = InterlevelScene.Mode.RETURN;
            InterlevelScene.returnLocation = Dungeon.hero.dreamgateLocation;
            InterlevelScene.returnPos = Dungeon.hero.dreamgatePos;
            Game.switchScene( InterlevelScene.class );
        }
    }

    @Override
    public int manaCost() {
        return 0;
    }

    @Override
    public boolean visible() {
        Buff delay = Dungeon.hero.buff(Utils.TwoTurnsDelay.class);
        return delay != null && delay.cooldown() >= 2;
    }

    @Override
    public int icon() {
        return Skill.DREAMGATE;
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
