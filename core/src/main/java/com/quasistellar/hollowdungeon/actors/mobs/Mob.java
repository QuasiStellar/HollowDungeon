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

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.plants.Swiftthistle;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.items.artifacts.DriedRose;
import com.quasistellar.hollowdungeon.items.artifacts.TimekeepersHourglass;
import com.quasistellar.hollowdungeon.items.rings.Ring;
import com.quasistellar.hollowdungeon.items.rings.RingOfWealth;
import com.quasistellar.hollowdungeon.items.stones.StoneOfAggression;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public abstract class Mob extends com.quasistellar.hollowdungeon.actors.Char {

    {
        actPriority = com.quasistellar.hollowdungeon.actors.Actor.MOB_PRIO;

        alignment = Alignment.ENEMY;
    }

    private static final String TXT_DIED = "You hear something died in the distance";

    protected static final String TXT_NOTICE1 = "?!";
    protected static final String TXT_RAGE = "#$%^";
    protected static final String TXT_EXP = "%+dEXP";

    public AiState SLEEPING = new Sleeping();
    public AiState HUNTING = new Hunting();
    public AiState WANDERING = new Wandering();
    public AiState FLEEING = new Fleeing();
    public AiState PASSIVE = new Passive();
    public AiState state = SLEEPING;

    public Class<? extends com.quasistellar.hollowdungeon.sprites.CharSprite> spriteClass;

    protected int target = -1;

    protected com.quasistellar.hollowdungeon.actors.Char enemy;
    protected boolean enemySeen;
    protected boolean alerted = false;

    protected static final float TIME_TO_WAKE_UP = 1f;

    private static final String STATE = "state";
    private static final String SEEN = "seen";
    private static final String TARGET = "target";
    private static final String MAX_LVL = "max_lvl";

    @Override
    public void storeInBundle(Bundle bundle) {

        super.storeInBundle(bundle);

        if (state == SLEEPING) {
            bundle.put(STATE, Sleeping.TAG);
        } else if (state == WANDERING) {
            bundle.put(STATE, Wandering.TAG);
        } else if (state == HUNTING) {
            bundle.put(STATE, Hunting.TAG);
        } else if (state == FLEEING) {
            bundle.put(STATE, Fleeing.TAG);
        } else if (state == PASSIVE) {
            bundle.put(STATE, Passive.TAG);
        }
        bundle.put(SEEN, enemySeen);
        bundle.put(TARGET, target);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {

        super.restoreFromBundle(bundle);

        String state = bundle.getString(STATE);
        if (state.equals(Sleeping.TAG)) {
            this.state = SLEEPING;
        } else if (state.equals(Wandering.TAG)) {
            this.state = WANDERING;
        } else if (state.equals(Hunting.TAG)) {
            this.state = HUNTING;
        } else if (state.equals(Fleeing.TAG)) {
            this.state = FLEEING;
        } else if (state.equals(Passive.TAG)) {
            this.state = PASSIVE;
        }

        enemySeen = bundle.getBoolean(SEEN);

        target = bundle.getInt(TARGET);
    }

    public com.quasistellar.hollowdungeon.sprites.CharSprite sprite() {
        return Reflection.newInstance(spriteClass);
    }

    @Override
    protected boolean act() {

        super.act();

        boolean justAlerted = alerted;
        alerted = false;

        if (justAlerted) {
            sprite.showAlert();
        } else {
            sprite.hideAlert();
            sprite.hideLost();
        }

        if (paralysed > 0) {
            enemySeen = false;
            spend(com.quasistellar.hollowdungeon.actors.Actor.TICK);
            return true;
        }

        enemy = chooseEnemy();

        boolean enemyInFOV = enemy != null && enemy.isAlive() && fieldOfView[enemy.pos] && enemy.invisible <= 0;

        return state.act(enemyInFOV, justAlerted);
    }

    //FIXME this is sort of a band-aid correction for allies needing more intelligent behaviour
    protected boolean intelligentAlly = false;

    protected com.quasistellar.hollowdungeon.actors.Char chooseEnemy() {

        com.quasistellar.hollowdungeon.actors.buffs.Terror terror = buff(com.quasistellar.hollowdungeon.actors.buffs.Terror.class);
        if (terror != null) {
            com.quasistellar.hollowdungeon.actors.Char source = (com.quasistellar.hollowdungeon.actors.Char) Actor.findById(terror.object);
            if (source != null) {
                return source;
            }
        }

        //if we are an enemy, and have no target or current target isn't affected by aggression
        //then auto-prioritize a target that is affected by aggression, even another enemy
        if (alignment == Alignment.ENEMY
                && (enemy == null || enemy.buff(StoneOfAggression.Aggression.class) == null)) {
            for (com.quasistellar.hollowdungeon.actors.Char ch : Actor.chars()) {
                if (ch != this && fieldOfView[ch.pos] &&
                        ch.buff(StoneOfAggression.Aggression.class) != null) {
                    return ch;
                }
            }
        }

        //find a new enemy if..
        boolean newEnemy = false;
        //we have no enemy, or the current one is dead/missing
        if (enemy == null || !enemy.isAlive() || !Actor.chars().contains(enemy) || state == WANDERING) {
            newEnemy = true;
            //We are an ally, and current enemy is another ally.
        } else if (alignment == Alignment.ALLY && enemy.alignment == Alignment.ALLY) {
            newEnemy = true;
            //We are amoked and current enemy is the hero
        } else if (buff(com.quasistellar.hollowdungeon.actors.buffs.Amok.class) != null && enemy == com.quasistellar.hollowdungeon.Dungeon.hero) {
            newEnemy = true;
            //We are charmed and current enemy is what charmed us
        } else if (buff(com.quasistellar.hollowdungeon.actors.buffs.Charm.class) != null && buff(com.quasistellar.hollowdungeon.actors.buffs.Charm.class).object == enemy.id()) {
            newEnemy = true;
            //we aren't amoked and current enemy is invulnerable to us
        } else if (buff(com.quasistellar.hollowdungeon.actors.buffs.Amok.class) == null && enemy.isInvulnerable(getClass())) {
            newEnemy = true;
        }

        if (newEnemy) {

            HashSet<com.quasistellar.hollowdungeon.actors.Char> enemies = new HashSet<>();

            //if the mob is amoked...
            if (buff(com.quasistellar.hollowdungeon.actors.buffs.Amok.class) != null) {
                //try to find an enemy mob to attack first.
                for (Mob mob : com.quasistellar.hollowdungeon.Dungeon.level.mobs)
                    if (mob.alignment == Alignment.ENEMY && mob != this
                            && fieldOfView[mob.pos] && mob.invisible <= 0) {
                        enemies.add(mob);
                    }

                if (enemies.isEmpty()) {
                    //try to find ally mobs to attack second.
                    for (Mob mob : com.quasistellar.hollowdungeon.Dungeon.level.mobs)
                        if (mob.alignment == Alignment.ALLY && mob != this
                                && fieldOfView[mob.pos] && mob.invisible <= 0) {
                            enemies.add(mob);
                        }

                    if (enemies.isEmpty()) {
                        //try to find the hero third
                        if (fieldOfView[com.quasistellar.hollowdungeon.Dungeon.hero.pos] && com.quasistellar.hollowdungeon.Dungeon.hero.invisible <= 0) {
                            enemies.add(com.quasistellar.hollowdungeon.Dungeon.hero);
                        }
                    }
                }

                //if the mob is an ally...
            } else if (alignment == Alignment.ALLY) {
                //look for hostile mobs to attack
                for (Mob mob : com.quasistellar.hollowdungeon.Dungeon.level.mobs)
                    if (mob.alignment == Alignment.ENEMY && fieldOfView[mob.pos]
                            && mob.invisible <= 0 && !mob.isInvulnerable(getClass()))
                        //intelligent allies do not target mobs which are passive, wandering, or asleep
                        if (!intelligentAlly ||
                                (mob.state != mob.SLEEPING && mob.state != mob.PASSIVE && mob.state != mob.WANDERING)) {
                            enemies.add(mob);
                        }

                //if the mob is an enemy...
            } else if (alignment == Alignment.ENEMY) {
                //look for ally mobs to attack
                for (Mob mob : com.quasistellar.hollowdungeon.Dungeon.level.mobs)
                    if (mob.alignment == Alignment.ALLY && fieldOfView[mob.pos] && mob.invisible <= 0 && !mob.isInvulnerable(getClass()))
                        enemies.add(mob);

                //and look for the hero
                if (fieldOfView[com.quasistellar.hollowdungeon.Dungeon.hero.pos] && com.quasistellar.hollowdungeon.Dungeon.hero.invisible <= 0 && !com.quasistellar.hollowdungeon.Dungeon.hero.isInvulnerable(getClass())) {
                    enemies.add(com.quasistellar.hollowdungeon.Dungeon.hero);
                }

            }

            com.quasistellar.hollowdungeon.actors.buffs.Charm charm = buff(com.quasistellar.hollowdungeon.actors.buffs.Charm.class);
            if (charm != null) {
                com.quasistellar.hollowdungeon.actors.Char source = (com.quasistellar.hollowdungeon.actors.Char) Actor.findById(charm.object);
                if (source != null && enemies.contains(source) && enemies.size() > 1) {
                    enemies.remove(source);
                }
            }

            //neutral characters in particular do not choose enemies.
            if (enemies.isEmpty()) {
                return null;
            } else {
                //go after the closest potential enemy, preferring the hero if two are equidistant
                com.quasistellar.hollowdungeon.actors.Char closest = null;
                for (com.quasistellar.hollowdungeon.actors.Char curr : enemies) {
                    if (closest == null
                            || com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, curr.pos) < com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, closest.pos)
                            || com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, curr.pos) == com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, closest.pos) && curr == com.quasistellar.hollowdungeon.Dungeon.hero) {
                        closest = curr;
                    }
                }
                return closest;
            }

        } else
            return enemy;
    }

    @Override
    public void add(com.quasistellar.hollowdungeon.actors.buffs.Buff buff) {
        super.add(buff);
        if (buff instanceof com.quasistellar.hollowdungeon.actors.buffs.Amok || buff instanceof com.quasistellar.hollowdungeon.actors.buffs.Corruption) {
            state = HUNTING;
        } else if (buff instanceof com.quasistellar.hollowdungeon.actors.buffs.Terror) {
            state = FLEEING;
        } else if (buff instanceof com.quasistellar.hollowdungeon.actors.buffs.Sleep) {
            state = SLEEPING;
            postpone(com.quasistellar.hollowdungeon.actors.buffs.Sleep.SWS);
        }
    }

    @Override
    public void remove(com.quasistellar.hollowdungeon.actors.buffs.Buff buff) {
        super.remove(buff);
        if (buff instanceof com.quasistellar.hollowdungeon.actors.buffs.Terror) {
            sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "rage"));
            state = HUNTING;
        }
    }

    protected boolean canAttack(com.quasistellar.hollowdungeon.actors.Char enemy) {
        return com.quasistellar.hollowdungeon.Dungeon.level.adjacent(pos, enemy.pos);
    }

    protected boolean getCloser(int target) {

        if (rooted || target == pos) {
            return false;
        }

        int step = -1;

        if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent(pos, target)) {

            path = null;

            if (Actor.findChar(target) == null
                    && (com.quasistellar.hollowdungeon.Dungeon.level.passable[target] || (flying && com.quasistellar.hollowdungeon.Dungeon.level.avoid[target]))
                    && (!Char.hasProp(this, Char.Property.LARGE) || com.quasistellar.hollowdungeon.Dungeon.level.openSpace[target])) {
                step = target;
            }

        } else {

            boolean newPath = false;
            //scrap the current path if it's empty, no longer connects to the current location
            //or if it's extremely inefficient and checking again may result in a much better path
            if (path == null || path.isEmpty()
                    || !com.quasistellar.hollowdungeon.Dungeon.level.adjacent(pos, path.getFirst())
                    || path.size() > 2 * com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, target))
                newPath = true;
            else if (path.getLast() != target) {
                //if the new target is adjacent to the end of the path, adjust for that
                //rather than scrapping the whole path.
                if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent(target, path.getLast())) {
                    int last = path.removeLast();

                    if (path.isEmpty()) {

                        //shorten for a closer one
                        if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent(target, pos)) {
                            path.add(target);
                            //extend the path for a further target
                        } else {
                            path.add(last);
                            path.add(target);
                        }

                    } else {
                        //if the new target is simply 1 earlier in the path shorten the path
                        if (path.getLast() == target) {

                            //if the new target is closer/same, need to modify end of path
                        } else if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent(target, path.getLast())) {
                            path.add(target);

                            //if the new target is further away, need to extend the path
                        } else {
                            path.add(last);
                            path.add(target);
                        }
                    }

                } else {
                    newPath = true;
                }

            }

            //checks if the next cell along the current path can be stepped into
            if (!newPath) {
                int nextCell = path.removeFirst();
                if (!com.quasistellar.hollowdungeon.Dungeon.level.passable[nextCell]
                        || (!flying && com.quasistellar.hollowdungeon.Dungeon.level.avoid[nextCell])
                        || (Char.hasProp(this, Char.Property.LARGE) && !com.quasistellar.hollowdungeon.Dungeon.level.openSpace[nextCell])
                        || Actor.findChar(nextCell) != null) {

                    newPath = true;
                    //If the next cell on the path can't be moved into, see if there is another cell that could replace it
                    if (!path.isEmpty()) {
                        for (int i : PathFinder.NEIGHBOURS8) {
                            if (com.quasistellar.hollowdungeon.Dungeon.level.adjacent(pos, nextCell + i) && com.quasistellar.hollowdungeon.Dungeon.level.adjacent(nextCell + i, path.getFirst())) {
                                if (com.quasistellar.hollowdungeon.Dungeon.level.passable[nextCell + i]
                                        && (flying || !com.quasistellar.hollowdungeon.Dungeon.level.avoid[nextCell + i])
                                        && (!Char.hasProp(this, Char.Property.LARGE) || com.quasistellar.hollowdungeon.Dungeon.level.openSpace[nextCell + i])
                                        && Actor.findChar(nextCell + i) == null) {
                                    path.addFirst(nextCell + i);
                                    newPath = false;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    path.addFirst(nextCell);
                }
            }

            //generate a new path
            if (newPath) {
                //If we aren't hunting, always take a full path
                PathFinder.Path full = com.quasistellar.hollowdungeon.Dungeon.findPath(this, target, com.quasistellar.hollowdungeon.Dungeon.level.passable, fieldOfView, true);
                if (state != HUNTING) {
                    path = full;
                } else {
                    //otherwise, check if other characters are forcing us to take a very slow route
                    // and don't try to go around them yet in response, basically assume their blockage is temporary
                    PathFinder.Path ignoreChars = com.quasistellar.hollowdungeon.Dungeon.findPath(this, target, com.quasistellar.hollowdungeon.Dungeon.level.passable, fieldOfView, false);
                    if (ignoreChars != null && (full == null || full.size() > 2 * ignoreChars.size())) {
                        //check if first cell of shorter path is valid. If it is, use new shorter path. Otherwise do nothing and wait.
                        path = ignoreChars;
                        if (!com.quasistellar.hollowdungeon.Dungeon.level.passable[ignoreChars.getFirst()]
                                || (!flying && com.quasistellar.hollowdungeon.Dungeon.level.avoid[ignoreChars.getFirst()])
                                || (Char.hasProp(this, Char.Property.LARGE) && !com.quasistellar.hollowdungeon.Dungeon.level.openSpace[ignoreChars.getFirst()])
                                || com.quasistellar.hollowdungeon.actors.Actor.findChar(ignoreChars.getFirst()) != null) {
                            return false;
                        }
                    } else {
                        path = full;
                    }
                }
            }

            if (path != null) {
                step = path.removeFirst();
            } else {
                return false;
            }
        }
        if (step != -1) {
            move(step);
            return true;
        } else {
            return false;
        }
    }

    protected boolean getFurther(int target) {
        if (rooted || target == pos) {
            return false;
        }

        int step = com.quasistellar.hollowdungeon.Dungeon.flee(this, target, com.quasistellar.hollowdungeon.Dungeon.level.passable, fieldOfView, true);
        if (step != -1) {
            move(step);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateSpriteState() {
        super.updateSpriteState();
        if (com.quasistellar.hollowdungeon.Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class) != null
                || com.quasistellar.hollowdungeon.Dungeon.hero.buff(Swiftthistle.TimeBubble.class) != null)
            sprite.add(CharSprite.State.PARALYSED);
    }

    protected float attackDelay() {
        float delay = 1f;
        if (buff(com.quasistellar.hollowdungeon.actors.buffs.Adrenaline.class) != null) delay /= 1.5f;
        return delay;
    }

    protected boolean doAttack(com.quasistellar.hollowdungeon.actors.Char enemy) {

        if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
            sprite.attack(enemy.pos);
            spend(attackDelay());
            return false;

        } else {
            attack(enemy);
            spend(attackDelay());
            return true;
        }
    }

    @Override
    public void onAttackComplete() {
        attack(enemy);
        super.onAttackComplete();
    }

    protected boolean hitWithRanged = false;

    public boolean surprisedBy(com.quasistellar.hollowdungeon.actors.Char enemy) {
        return enemy == com.quasistellar.hollowdungeon.Dungeon.hero && (enemy.invisible > 0 || (!enemySeen && state != PASSIVE));
    }

    public void aggro(com.quasistellar.hollowdungeon.actors.Char ch) {
        enemy = ch;
        if (state != PASSIVE) {
            state = HUNTING;
        }
    }

    public boolean isTargeting(com.quasistellar.hollowdungeon.actors.Char ch) {
        return enemy == ch;
    }

    @Override
    public void damage(int dmg, Object src) {

        if (state == SLEEPING) {
            state = WANDERING;
        }
        if (state != HUNTING) {
            alerted = true;
        }

        super.damage(dmg, src);
    }


    @Override
    public void destroy() {

        super.destroy();

        com.quasistellar.hollowdungeon.Dungeon.level.mobs.remove(this);

        if (com.quasistellar.hollowdungeon.Dungeon.hero.isAlive()) {

            if (alignment == Alignment.ENEMY) {
                com.quasistellar.hollowdungeon.Statistics.enemiesSlain++;
                com.quasistellar.hollowdungeon.Badges.validateMonstersSlain();
                com.quasistellar.hollowdungeon.Statistics.qualifiedForNoKilling = false;
            }
        }
    }

    @Override
    public void die(Object cause) {

        if (hitWithRanged) {
            com.quasistellar.hollowdungeon.Statistics.thrownAssists++;
            com.quasistellar.hollowdungeon.Badges.validateHuntressUnlock();
        }

        if (alignment == Alignment.ENEMY) {
            rollToDropLoot();
        }

        if (com.quasistellar.hollowdungeon.Dungeon.hero.isAlive() && !com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[pos]) {
            GLog.i(Messages.get(this, "died"));
        }

        super.die(cause);
    }

    public void rollToDropLoot() {

        float lootChance = this.lootChance;
        lootChance *= RingOfWealth.dropChanceMultiplier(com.quasistellar.hollowdungeon.Dungeon.hero);

        if (Random.Float() < lootChance) {
            com.quasistellar.hollowdungeon.items.Item loot = createLoot();
            if (loot != null) {
                com.quasistellar.hollowdungeon.Dungeon.level.drop(loot, pos).sprite.drop();
            }
        }

        //ring of wealth logic
        if (Ring.getBuffedBonus(com.quasistellar.hollowdungeon.Dungeon.hero, RingOfWealth.Wealth.class) > 0) {
            int rolls = 1;
            if (properties.contains(Property.BOSS)) rolls = 15;
            else if (properties.contains(Property.MINIBOSS)) rolls = 5;
            ArrayList<com.quasistellar.hollowdungeon.items.Item> bonus = RingOfWealth.tryForBonusDrop(com.quasistellar.hollowdungeon.Dungeon.hero, rolls);
            if (bonus != null && !bonus.isEmpty()) {
                for (com.quasistellar.hollowdungeon.items.Item b : bonus)
                    com.quasistellar.hollowdungeon.Dungeon.level.drop(b, pos).sprite.drop();
                RingOfWealth.showFlareForBonusDrop(sprite);
            }
        }
    }

    protected Object loot = null;
    protected float lootChance = 0;

    @SuppressWarnings("unchecked")
    protected com.quasistellar.hollowdungeon.items.Item createLoot() {
        com.quasistellar.hollowdungeon.items.Item item;
        if (loot instanceof com.quasistellar.hollowdungeon.items.Generator.Category) {

            item = Generator.random((com.quasistellar.hollowdungeon.items.Generator.Category) loot);

        } else if (loot instanceof Class<?>) {

            item = com.quasistellar.hollowdungeon.items.Generator.random((Class<? extends com.quasistellar.hollowdungeon.items.Item>) loot);

        } else {

            item = (Item) loot;

        }
        return item;
    }

    //how many mobs this one should count as when determining spawning totals
    public float spawningWeight() {
        return 1;
    }

    public boolean reset() {
        return false;
    }

    public void beckon(int cell) {

        notice();

        if (state != HUNTING) {
            state = WANDERING;
        }
        target = cell;
    }

    public String description() {
        return Messages.get(this, "desc");
    }

    public void notice() {
        sprite.showAlert();
    }

    public void yell(String str) {
        GLog.newLine();
        com.quasistellar.hollowdungeon.utils.GLog.n("%s: \"%s\" ", Messages.titleCase(name()), str);
    }

    //returns true when a mob sees the hero, and is currently targeting them.
    public boolean focusingHero() {
        return enemySeen && (target == com.quasistellar.hollowdungeon.Dungeon.hero.pos);
    }

    public interface AiState {
        boolean act(boolean enemyInFOV, boolean justAlerted);
    }

    protected class Sleeping implements AiState {

        public static final String TAG = "SLEEPING";

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (enemyInFOV && Random.Float(distance(enemy) + enemy.stealth()) < 1) {

                enemySeen = true;

                notice();
                state = HUNTING;
                target = enemy.pos;

                if (com.quasistellar.hollowdungeon.Dungeon.isChallenged(com.quasistellar.hollowdungeon.Challenges.SWARM_INTELLIGENCE)) {
                    for (Mob mob : com.quasistellar.hollowdungeon.Dungeon.level.mobs) {
                        if (com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, mob.pos) <= 8 && mob.state != mob.HUNTING) {
                            mob.beckon(target);
                        }
                    }
                }

                spend(TIME_TO_WAKE_UP);

            } else {

                enemySeen = false;

                spend(com.quasistellar.hollowdungeon.actors.Actor.TICK);

            }
            return true;
        }
    }

    protected class Wandering implements AiState {

        public static final String TAG = "WANDERING";

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (enemyInFOV && (justAlerted || Random.Float(distance(enemy) / 2f + enemy.stealth()) < 1)) {

                return noticeEnemy();

            } else {

                return continueWandering();

            }
        }

        protected boolean noticeEnemy() {
            enemySeen = true;

            notice();
            alerted = true;
            state = HUNTING;
            target = enemy.pos;

            if (com.quasistellar.hollowdungeon.Dungeon.isChallenged(com.quasistellar.hollowdungeon.Challenges.SWARM_INTELLIGENCE)) {
                for (Mob mob : com.quasistellar.hollowdungeon.Dungeon.level.mobs) {
                    if (com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, mob.pos) <= 8 && mob.state != mob.HUNTING) {
                        mob.beckon(target);
                    }
                }
            }

            return true;
        }

        protected boolean continueWandering() {
            enemySeen = false;

            int oldPos = pos;
            if (target != -1 && getCloser(target)) {
                spend(1 / speed());
                return moveSprite(oldPos, pos);
            } else {
                target = com.quasistellar.hollowdungeon.Dungeon.level.randomDestination(Mob.this);
                spend(com.quasistellar.hollowdungeon.actors.Actor.TICK);
            }

            return true;
        }

    }

    protected class Hunting implements AiState {

        public static final String TAG = "HUNTING";

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;
            if (enemyInFOV && !isCharmedBy(enemy) && canAttack(enemy)) {

                target = enemy.pos;
                return doAttack(enemy);

            } else {

                if (enemyInFOV) {
                    target = enemy.pos;
                } else if (enemy == null) {
                    state = WANDERING;
                    target = com.quasistellar.hollowdungeon.Dungeon.level.randomDestination(Mob.this);
                    return true;
                }

                int oldPos = pos;
                if (target != -1 && getCloser(target)) {

                    spend(1 / speed());
                    return moveSprite(oldPos, pos);

                } else {

                    //if moving towards an enemy isn't possible, try to switch targets to another enemy that is closer
                    com.quasistellar.hollowdungeon.actors.Char newEnemy = chooseEnemy();
                    if (newEnemy != null && enemy != newEnemy) {
                        enemy = newEnemy;
                        return act(enemyInFOV, justAlerted);
                    }

                    spend(com.quasistellar.hollowdungeon.actors.Actor.TICK);
                    if (!enemyInFOV) {
                        sprite.showLost();
                        state = WANDERING;
                        target = com.quasistellar.hollowdungeon.Dungeon.level.randomDestination(Mob.this);
                    }
                    return true;
                }
            }
        }
    }

    //FIXME this works fairly well but is coded poorly. Should refactor
    protected class Fleeing implements AiState {

        public static final String TAG = "FLEEING";

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = enemyInFOV;
            //loses target when 0-dist rolls a 6 or greater.
            if (enemy == null || !enemyInFOV && 1 + Random.Int(com.quasistellar.hollowdungeon.Dungeon.level.distance(pos, target)) >= 6) {
                target = -1;

                //if enemy isn't in FOV, keep running from their previous position.
            } else if (enemyInFOV) {
                target = enemy.pos;
            }

            int oldPos = pos;
            if (target != -1 && getFurther(target)) {

                spend(1 / speed());
                return moveSprite(oldPos, pos);

            } else {

                spend(com.quasistellar.hollowdungeon.actors.Actor.TICK);
                nowhereToRun();

                return true;
            }
        }

        protected void nowhereToRun() {
        }
    }

    protected class Passive implements AiState {

        public static final String TAG = "PASSIVE";

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            enemySeen = false;
            spend(com.quasistellar.hollowdungeon.actors.Actor.TICK);
            return true;
        }
    }


    private static ArrayList<Mob> heldAllies = new ArrayList<>();

    public static void holdAllies(com.quasistellar.hollowdungeon.levels.Level level) {
        heldAllies.clear();
        for (Mob mob : level.mobs.toArray(new Mob[0])) {
            //preserve the ghost no matter where they are
            if (mob instanceof DriedRose.GhostHero) {
                ((DriedRose.GhostHero) mob).clearDefensingPos();
                level.mobs.remove(mob);
                heldAllies.add(mob);

                //preserve intelligent allies if they are near the hero
            } else if (mob.alignment == Alignment.ALLY
                    && mob.intelligentAlly
                    && com.quasistellar.hollowdungeon.Dungeon.level.distance(Dungeon.hero.pos, mob.pos) <= 3) {
                level.mobs.remove(mob);
                heldAllies.add(mob);
            }
        }
    }

    public static void restoreAllies(Level level, int pos) {
        if (!heldAllies.isEmpty()) {

            ArrayList<Integer> candidatePositions = new ArrayList<>();
            for (int i : PathFinder.NEIGHBOURS8) {
                if (!com.quasistellar.hollowdungeon.Dungeon.level.solid[i + pos] && level.findMob(i + pos) == null) {
                    candidatePositions.add(i + pos);
                }
            }
            Collections.shuffle(candidatePositions);

            for (Mob ally : heldAllies) {
                level.mobs.add(ally);
                ally.state = ally.WANDERING;

                if (!candidatePositions.isEmpty()) {
                    ally.pos = candidatePositions.remove(0);
                } else {
                    ally.pos = pos;
                }

            }
        }
        heldAllies.clear();
    }

    public static void clearHeldAllies() {
        heldAllies.clear();
    }
}

