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

package com.quasistellar.hollowdungeon.ui;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.mobs.FalseKnight1;
import com.quasistellar.hollowdungeon.actors.mobs.FalseKnight2;
import com.quasistellar.hollowdungeon.actors.mobs.FalseKnight3;
import com.quasistellar.hollowdungeon.actors.mobs.FalseKnightMaggot1;
import com.quasistellar.hollowdungeon.actors.mobs.FalseKnightMaggot2;
import com.quasistellar.hollowdungeon.actors.mobs.FalseKnightMaggot3;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.watabou.input.GameAction;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class AttackIndicator extends Tag {
	
	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;

	private static float delay;
	
	private static AttackIndicator instance;
	
	private CharSprite sprite = null;
	
	private Mob lastTarget;
	private ArrayList<Mob> candidates = new ArrayList<>();
	
	public AttackIndicator() {
		super( DangerIndicator.COLOR );
		
		instance = this;
		lastTarget = null;
		
		setSize( 24, 24 );
		visible( false );
		enable( false );
	}
	
	@Override
	public GameAction keyAction() {
		return HDAction.TAG_ATTACK;
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
	}
	
	@Override
	protected synchronized void layout() {
		super.layout();
		
		if (sprite != null) {
			sprite.x = x + (width - sprite.width()) / 2 + 1;
			sprite.y = y + (height - sprite.height()) / 2;
			// FIXIT: bicycle
			if (lastTarget instanceof FalseKnight1 ||
					lastTarget instanceof FalseKnight2 ||
					lastTarget instanceof FalseKnight3 ||
					lastTarget instanceof FalseKnightMaggot1 ||
					lastTarget instanceof FalseKnightMaggot2 ||
					lastTarget instanceof FalseKnightMaggot3) {
				sprite.x += 300;
			}
			PixelScene.align(sprite);
		}
	}
	
	@Override
	public synchronized void update() {
		super.update();

		if (!bg.visible){
			enable(false);
			if (delay > 0f) delay -= Game.elapsed;
			if (delay <= 0f) active = false;
		} else {
			delay = 0.75f;
			active = true;
		
			if (Dungeon.hero.isAlive()) {

				enable(Dungeon.hero.ready);

			} else {
				visible( false );
				enable( false );
			}
		}
	}
	
	private synchronized void checkEnemies() {

		candidates.clear();
		int v = Dungeon.hero.visibleEnemies();
		for (int i=0; i < v; i++) {
			Mob mob = Dungeon.hero.visibleEnemy( i );
			if ( Dungeon.hero.canAttack( mob) ) {
				candidates.add( mob );
			}
		}
		
		if (!candidates.contains( lastTarget )) {
			if (candidates.isEmpty()) {
				lastTarget = null;
			} else {
				active = true;
				lastTarget = Random.element( candidates );
				updateImage();
				flash();
			}
		} else {
			if (!bg.visible) {
				active = true;
				flash();
			}
		}
		
		visible( lastTarget != null );
		enable( bg.visible );
	}
	
	private synchronized void updateImage() {
		
		if (sprite != null) {
			sprite.killAndErase();
			sprite = null;
		}
		
		sprite = Reflection.newInstance(lastTarget.spriteClass);
		active = true;
		sprite.linkVisuals(lastTarget);
		sprite.idle();
		sprite.paused = true;
		add( sprite );

		layout();
	}
	
	private boolean enabled = true;
	private synchronized void enable( boolean value ) {
		enabled = value;
		if (sprite != null) {
			sprite.alpha( value ? ENABLED : DISABLED );
		}
	}
	
	private synchronized void visible( boolean value ) {
		bg.visible = value;
		if (sprite != null) {
			sprite.visible = value;
		}
	}
	
	@Override
	protected void onClick() {
		if (enabled) {
			if (Dungeon.hero.handle( lastTarget.pos )) {
				Dungeon.hero.next();
			}
		}
	}
	
	public static void target( Char target ) {
		synchronized (instance) {
			instance.lastTarget = (Mob) target;
			instance.updateImage();

			TargetHealthIndicator.instance.target(target);
		}
	}
	
	public static void updateState() {
		instance.checkEnemies();
	}
}
