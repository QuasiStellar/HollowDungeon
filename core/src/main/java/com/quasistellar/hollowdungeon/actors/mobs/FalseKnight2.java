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

package com.quasistellar.hollowdungeon.actors.mobs;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.blobs.Blob;
import com.quasistellar.hollowdungeon.actors.blobs.Fire;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.effects.BlobEmitter;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Pushing;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.effects.TargetedCell;
import com.quasistellar.hollowdungeon.effects.particles.FlameParticle;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.FalseKnightSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashSet;

public class FalseKnight2 extends Mob {

    {
        spriteClass = FalseKnightSprite.class;

        HP = HT = 40;

        HUNTING = new Hunting();

        baseSpeed = 1f;
    }

    private int beamCooldown = 1;
    public boolean beamCharged = false;

    private static final String LAST_ENEMY_POS = "last_enemy_pos";
    private static final String LEAP_POS = "leap_pos";
    private static final String LEAP_CD = "leap_cd";
    private static final String HIT_CD = "hit_cd";
    private static final String BEAM_COOLDOWN = "beamCooldown";
    private static final String BEAM_CHARGED = "beamCharged";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LAST_ENEMY_POS, lastEnemyPos);
        bundle.put(LEAP_POS, leapPos);
        bundle.put(LEAP_CD, leapCooldown);
        bundle.put(HIT_CD, hitCooldown);
        bundle.put( BEAM_COOLDOWN, beamCooldown );
        bundle.put( BEAM_CHARGED, beamCharged );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        lastEnemyPos = bundle.getInt(LAST_ENEMY_POS);
        leapPos = bundle.getInt(LEAP_POS);
        leapCooldown = bundle.getFloat(LEAP_CD);
        hitCooldown = bundle.getFloat(HIT_CD);
        beamCooldown = bundle.getInt(BEAM_COOLDOWN);
        beamCharged = bundle.getBoolean(BEAM_CHARGED);
    }

    private int lastEnemyPos = -1;

    @Override
    protected boolean act() {
        AiState lastState = state;
        boolean result = super.act();
        if (paralysed <= 0) leapCooldown --;
        if (beamCooldown > 0)
            beamCooldown--;
        //if state changed from wandering to hunting, we haven't acted yet, don't update.
        if (!(lastState == WANDERING && state == HUNTING)) {
            if (enemy != null) {
                lastEnemyPos = enemy.pos;
            } else {
                lastEnemyPos = Dungeon.hero.pos;
            }
        }

        return result;
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        FalseKnightMaggot2 falseKnightMaggot2;
        falseKnightMaggot2 = new FalseKnightMaggot2();
        falseKnightMaggot2.pos = this.pos;
        GameScene.add( falseKnightMaggot2 );
        Dungeon.level.occupyCell( falseKnightMaggot2 );
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return beamCooldown == 0 && beamCharged;
    }

    private int leapPos = -1;
    private float leapCooldown = 0;

    private float hitCooldown = 0;

    public class Hunting extends Mob.Hunting {

        @Override
        public boolean act( boolean enemyInFOV, boolean justAlerted ) {

            if (beamCharged && beamCooldown == 0) {
                spend( attackDelay() );
                sprite.idle();
                sprite.attack(enemy.pos);
                spend(attackDelay());
                beamCooldown = 1;
                beamCharged = false;
                leapCooldown -= 1;
                return false;
            }

            if (leapPos != -1){

                leapCooldown = 3;
                Ballistica b = new Ballistica(pos, leapPos, Ballistica.STOP_TARGET);

                //check if leap pos is not obstructed by terrain
                if (b.collisionPos != leapPos) {
                    leapPos = -1;
                    return true;
                }

                final Char leapVictim = Actor.findChar(leapPos);
                final int endPos;

                //ensure there is somewhere to land after leaping
                if (leapVictim != null){
                    int bouncepos = -1;
                    for (int i : PathFinder.NEIGHBOURS8){
                        if ((bouncepos == -1 || Dungeon.level.trueDistance(pos, leapPos+i) < Dungeon.level.trueDistance(pos, bouncepos))
                                && Actor.findChar(leapPos+i) == null && Dungeon.level.passable[leapPos+i]){
                            bouncepos = leapPos+i;
                        }
                    }
                    if (bouncepos == -1) {
                        leapPos = -1;
                        return true;
                    } else {
                        endPos = bouncepos;
                    }
                } else {
                    endPos = leapPos;
                }

                //do leap
                sprite.visible = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[leapPos] || Dungeon.level.heroFOV[endPos];
                sprite.jump(pos, leapPos, new Callback() {
                    @Override
                    public void call() {

                        if (leapVictim != null && alignment != leapVictim.alignment){
                            leapVictim.sprite.flash();
                            Sample.INSTANCE.play(Assets.Sounds.HIT);
                        }

                        if (endPos != leapPos){
                            Actor.addDelayed(new Pushing(FalseKnight2.this, leapPos, endPos), -1);
                        }

                        pos = endPos;
                        leapPos = -1;
                        sprite.idle();
                        Dungeon.level.occupyCell(FalseKnight2.this);

                        spend(TICK);

                        throwFire(FalseKnight2.this, Dungeon.hero);

                        next();
                    }
                });
                return false;
            }

            if (leapCooldown <= 0 && enemyInFOV && Dungeon.level.distance(pos, enemy.pos) >= 2) {

                int farthestId = 0;
                for (int i = 1; i < PathFinder.CIRCLE8.length; i++){
                    if (Dungeon.level.trueDistance(pos, enemy.pos+PathFinder.CIRCLE8[i])
                            > Dungeon.level.trueDistance(pos, enemy.pos+PathFinder.CIRCLE8[farthestId])){
                        farthestId = i;
                    }
                }
                int targetPos = enemy.pos + PathFinder.CIRCLE8[farthestId];

                Ballistica b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET);
                //try aiming directly at hero if aiming near them doesn't work
                if (b.collisionPos != targetPos && targetPos != enemy.pos){
                    targetPos = enemy.pos;
                    b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET);
                }
                if (b.collisionPos == targetPos){
                    //get ready to leap
                    leapPos = targetPos;
                    //don't want to overly punish players with slow move or attack speed
                    spend(GameMath.gate(TICK, enemy.cooldown(), 3*TICK));
                    if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[leapPos]){
                        GLog.w(Messages.get(FalseKnight2.this, "leap"));
                        sprite.parent.addToBack(new TargetedCell(leapPos, 0xFF0000));
                        ((FalseKnightSprite)sprite).leapPrep( leapPos );
                        Dungeon.hero.interrupt();
                    }
                    return true;
                }
            }

            if (beamCooldown == 0 && Dungeon.level.distance(pos, enemy.pos) <= 2) {
                ((FalseKnightSprite)sprite).charge( enemy.pos );
                spend( attackDelay() );
                beamCharged = true;
                leapCooldown -= 1;
                return true;
            }

            if (enemyInFOV) {
                target = enemy.pos;
            } else if (enemy == null) {
                state = WANDERING;
                target = Dungeon.level.randomDestination( FalseKnight2.this );
                return true;
            }

            int oldPos = pos;
            if (target != -1 && getCloser( target )) {

                spend( 1 / speed() );
                return moveSprite( oldPos,  pos );

            } else {
                spend( TICK );
                if (!enemyInFOV) {
                    sprite.showLost();
                    state = WANDERING;
                    target = Dungeon.level.randomDestination( FalseKnight2.this );
                }
                return true;
            }
        }
    }

    public static boolean throwFire(final Char thrower, final Char target){

        Ballistica aim = new Ballistica(thrower.pos, target.pos, Ballistica.WONT_STOP);

        for (int i = 0; i < PathFinder.CIRCLE8.length; i++){
            if (aim.sourcePos+PathFinder.CIRCLE8[i] == aim.path.get(1)){
                thrower.sprite.zap(target.pos);
                Buff.append(thrower, FalseKnight2.FireAbility.class).direction = i;

                thrower.sprite.emitter().start(Speck.factory(Speck.ROCK), .03f, 10);
                return true;
            }
        }

        return false;
    }

    public static class FireAbility extends Buff {

        public int direction;
        private int[] curCells;

        HashSet<Integer> toCells = new HashSet<>();

        @Override
        public boolean act() {

            toCells.clear();

            if (curCells == null){
                curCells = new int[1];
                curCells[0] = target.pos;
                spreadFromCell( curCells[0] );

            } else {
                for (Integer c : curCells) {
                    if (FalseKnight2.FireAbility.FireBlob.volumeAt(c, FalseKnight2.FireAbility.FireBlob.class) > 0) spreadFromCell(c);
                }
            }

            for (Integer c : curCells){
                toCells.remove(c);
            }

            if (toCells.isEmpty()){
                detach();
            } else {
                curCells = new int[toCells.size()];
                int i = 0;
                for (Integer c : toCells){
                    GameScene.add(Blob.seed(c, 2, FalseKnight2.FireAbility.FireBlob.class));
                    curCells[i] = c;
                    i++;
                }
            }

            spend(TICK);
            return true;
        }

        private void spreadFromCell( int cell ){
            if (!Dungeon.level.solid[cell + PathFinder.CIRCLE8[left(direction)]]){
                toCells.add(cell + PathFinder.CIRCLE8[left(direction)]);
            }
            if (!Dungeon.level.solid[cell + PathFinder.CIRCLE8[direction]]){
                toCells.add(cell + PathFinder.CIRCLE8[direction]);
            }
            if (!Dungeon.level.solid[cell + PathFinder.CIRCLE8[right(direction)]]){
                toCells.add(cell + PathFinder.CIRCLE8[right(direction)]);
            }
        }

        private int left(int direction){
            return direction == 0 ? 7 : direction-1;
        }

        private int right(int direction){
            return direction == 7 ? 0 : direction+1;
        }

        private static final String DIRECTION = "direction";
        private static final String CUR_CELLS = "cur_cells";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put( DIRECTION, direction );
            bundle.put( CUR_CELLS, curCells );
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            direction = bundle.getInt( DIRECTION );
            curCells = bundle.getIntArray( CUR_CELLS );
        }

        public static class FireBlob extends Blob {

            {
                actPriority = BUFF_PRIO - 1;
                alwaysVisible = true;
            }

            @Override
            protected void evolve() {

                boolean observe = false;
                boolean burned = false;

                int cell;
                for (int i = area.left; i < area.right; i++){
                    for (int j = area.top; j < area.bottom; j++){
                        cell = i + j* Dungeon.level.width();
                        off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;

                        if (off[cell] > 0) {
                            volume += off[cell];
                        }

                        if (cur[cell] > 0 && off[cell] == 0) {
                            Char ch = Actor.findChar( cell );
                            if (ch != null && !ch.isImmune(Fire.class) && !(ch instanceof FalseKnight2)) {
                                ch.damage(1, FalseKnight2.class );
                            }

                            burned = true;
                            CellEmitter.get(cell).start(FlameParticle.FACTORY, 0.03f, 10);
                        }
                    }
                }

                if (burned){
                    Sample.INSTANCE.play(Assets.Sounds.BURNING);
                }
            }

            @Override
            public void use(BlobEmitter emitter) {
                super.use(emitter);

                emitter.pour( Speck.factory( Speck.RATTLE ), 0.1f );
            }

            @Override
            public String tileDesc() {
                return Messages.get(this, "desc");
            }
        }
    }

}