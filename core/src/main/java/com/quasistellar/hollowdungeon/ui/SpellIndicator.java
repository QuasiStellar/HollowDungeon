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

package com.quasistellar.hollowdungeon.ui;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.ShatteredPixelDungeon;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.skills.Skill;
import com.quasistellar.hollowdungeon.windows.WndInfoSkill;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

public class SpellIndicator extends VerticalTag {

	private Image skillIcon;
	private Skill skill;

	public SpellIndicator(Skill skill) {
		super( 0xCDD5C0 );
		this.skill = skill;

		setSize( 24, 24 );

		visible = false;

	}
	
	@Override
	protected synchronized void layout() {
		super.layout();
		
		if (skillIcon != null) {
			skillIcon.x = x + (width - skillIcon.width()) / 2;
			skillIcon.y = y + (height - skillIcon.height()) / 2;
			PixelScene.align(skillIcon);
		}
	}

	private synchronized void updateImage() {

		if (skillIcon != null) {
			skillIcon.killAndErase();
			skillIcon = null;
		}

		try {
			SmartTexture icons;
			TextureFilm film;

			icons = TextureCache.get( Assets.Interfaces.SKILLS );
			film = new TextureFilm( icons, 18, 18 );

			skillIcon = new Image( icons );
			skillIcon.frame( film.get(skill.icon()) );

			add( skillIcon );

			skillIcon.x = x + (width - skillIcon.width()) / 2;
			skillIcon.y = y + (height - skillIcon.height()) / 2;
			PixelScene.align(skillIcon);

		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
		}
	}
	
	@Override
	public synchronized void update() {
		if (!Dungeon.hero.isAlive())
			visible = false;
		else if (Dungeon.hero.MP >= skill.manaCost() && skill.visible()) {
			if (!visible) {
				visible = true;
				updateImage();
				flash();
			}
		} else {
			if (visible) {
				visible = false;
				updateImage();
			}
		}

		super.update();
	}

	@Override
	protected void onClick() {
		skill.act();
	}

	@Override
	protected boolean onLongClick() {
		GameScene.show(new WndInfoSkill(skill));
		return true;
	}
}