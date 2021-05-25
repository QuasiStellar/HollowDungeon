/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
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
import com.quasistellar.hollowdungeon.effects.BlobEmitter;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.effects.particles.FlameParticle;
import com.quasistellar.hollowdungeon.effects.particles.InfectionParticle;
import com.quasistellar.hollowdungeon.effects.particles.PurpleParticle;
import com.quasistellar.hollowdungeon.items.Geo;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.AspidSprite;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.SwarmSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Aspid extends Mob {

    {
        spriteClass = AspidSprite.class;

        HP = HT = 12;
        viewDistance = 5;

        flying = true;
        loot = new Geo(6);

        HUNTING = new Hunting();
    }

    private Ballistica beam;
    private int beamTarget = -1;
    private int beamCooldown;
    public boolean beamCharged;

    @Override
    protected boolean canAttack( Char enemy ) {

        if (beamCooldown == 0) {
            Ballistica aim = new Ballistica(pos, enemy.pos, Ballistica.STOP_TERRAIN);

            if (enemy.invisible == 0 && !isCharmedBy(enemy) && fieldOfView[enemy.pos] && aim.subPath(1, aim.dist).contains(enemy.pos)){
                beam = aim;
                beamTarget = aim.collisionPos;
                return true;
            } else
                //if the beam is charged, it has to attack, will aim at previous location of target.
                return beamCharged;
        } else
            return super.canAttack(enemy);
    }

    @Override
    protected boolean act() {
        if (beamCharged && state != HUNTING){
            beamCharged = false;
            sprite.idle();
        }
        if (beam == null && beamTarget != -1) {
            beam = new Ballistica(pos, beamTarget, Ballistica.STOP_TERRAIN);
            sprite.turnTo(pos, beamTarget);
        }
        if (beamCooldown > 0)
            beamCooldown--;
        return super.act();
    }

    @Override
    protected boolean getCloser( int target ) {
        if (state == HUNTING) {
            return enemySeen && getFurther( target );
        } else {
            return super.getCloser( target );
        }
    }

    @Override
    protected boolean doAttack( Char enemy ) {

        if (beamCooldown > 0) {
            spend( attackDelay() );
            return true;
        } else if (!beamCharged){
            ((AspidSprite)sprite).charge( enemy.pos );
            spend( attackDelay() );
            beamCharged = true;
            return true;
        } else {

            spend( attackDelay() * 2 );

            beam = new Ballistica(pos, beamTarget, Ballistica.STOP_TERRAIN);
//            if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[beam.collisionPos] ) {
//                sprite.zap( beam.collisionPos );
//                return false;
//            } else {
                sprite.idle();
                deathGaze();
                return true;
//            }
        }

    }

    //used so resistances can differentiate between melee and magical attacks
    public static class DeathGaze{}

    public void deathGaze(){
        if (!beamCharged || beamCooldown > 0 || beam == null)
            return;

        beamCharged = false;
        beamCooldown = 3;

        for (int pos : beam.subPath(1, beam.dist)) {

            GameScene.add(Blob.seed(pos, 2, FireBlob.class));
        }

        beam = null;
        beamTarget = -1;
    }

    public static class FireBlob extends Blob {

        {
            actPriority = BUFF_PRIO - 1;
            alwaysVisible = true;
        }

        @Override
        protected void evolve() {

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
                        if (ch != null && !ch.isImmune(Fire.class) && !(ch instanceof Aspid)) {
                            ch.damage(1, Aspid.class );
                        }

                        CellEmitter.get(cell).start(InfectionParticle.FACTORY, 0.03f, 10);
                    }
                }
            }
        }

        @Override
        public void use(BlobEmitter emitter) {
            super.use(emitter);

            emitter.pour( Speck.factory( Speck.INFECTION ), 0.1f );
        }

        @Override
        public String tileDesc() {
            return Messages.get(this, "desc");
        }
    }

    private static final String BEAM_TARGET     = "beamTarget";
    private static final String BEAM_COOLDOWN   = "beamCooldown";
    private static final String BEAM_CHARGED    = "beamCharged";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( BEAM_TARGET, beamTarget);
        bundle.put( BEAM_COOLDOWN, beamCooldown );
        bundle.put( BEAM_CHARGED, beamCharged );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(BEAM_TARGET))
            beamTarget = bundle.getInt(BEAM_TARGET);
        beamCooldown = bundle.getInt(BEAM_COOLDOWN);
        beamCharged = bundle.getBoolean(BEAM_CHARGED);
    }

    private class Hunting extends Mob.Hunting{
        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            //even if enemy isn't seen, attack them if the beam is charged
            if (beamCharged && enemy != null && canAttack(enemy)) {
                enemySeen = enemyInFOV;
                return doAttack(enemy);
            }
            return super.act(enemyInFOV, justAlerted);
        }
    }
}