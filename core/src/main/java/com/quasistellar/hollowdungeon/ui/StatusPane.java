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

import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.windows.WndGame;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;

public class StatusPane extends Component {

	private NinePatch bg;

	private Image soulMeter;

	private com.quasistellar.hollowdungeon.ui.BossHealthBar bossHP;

	private BitmapText level;
	private BitmapText location;

	private HpIndicator hp;
	private DangerIndicator danger;
	private BuffIndicator buffs;

	private ItemSprite geo;
	private BitmapText amt;

	private MenuButton btnMenu;

	private Toolbar.PickedUpItem pickedUp;
	
	private BitmapText version;

	@Override
	protected void createChildren() {

		bg = new NinePatch( Assets.Interfaces.STATUS, 0, 0, 128, 36, 85, 0, 45, 0 );
		add( bg );

		btnMenu = new MenuButton();
		add( btnMenu );

		SmartTexture icons;
		TextureFilm film;

		icons = TextureCache.get( Assets.Interfaces.SOUL_METER );
		film = new TextureFilm( icons, 27, 27 );

		soulMeter = new Image( icons );
		soulMeter.frame( film.get( Math.round(Dungeon.hero.MP / (float)Dungeon.hero.MM * 27) ) );

		add( soulMeter );

		hp = new HpIndicator( Dungeon.hero );
		add( hp );

		geo = new ItemSprite(ItemSpriteSheet.GEO, null);
		add(geo);

		amt = new BitmapText( Integer.toString(Dungeon.geo), PixelScene.pixelFont );
		add(amt);

		bossHP = new BossHealthBar();
		add( bossHP );

		level = new BitmapText( PixelScene.pixelFont);
		level.hardlight( 0xFFEBA4 );
		add( level );

		location = new BitmapText( Dungeon.location, PixelScene.pixelFont);
		location.hardlight( 0xCACFC2 );
		location.measure();
		add(location);

		danger = new DangerIndicator();
		add( danger );

		buffs = new BuffIndicator( Dungeon.hero );
		add( buffs );

		add( pickedUp = new Toolbar.PickedUpItem());
		
		version = new BitmapText( "DEMO", PixelScene.pixelFont); //FIXME
		version.alpha( 0.5f );
		add(version);
	}

	@Override
	protected void layout() {

		height = 32;

		bg.size( width, bg.height );

		soulMeter.x = bg.x + 18 - soulMeter.width / 2f;
		soulMeter.y = bg.y + 21 - soulMeter.height / 2f;
		PixelScene.align(soulMeter);

		hp.setPos(36, 8);

		geo.x = 35;
		geo.y = 24;

		amt.scale.set(PixelScene.align(1.5f));
		amt.x = 48;
		amt.y = 24;
		PixelScene.align(amt);

		bossHP.setPos( 6 + (width - bossHP.width())/2, 20);

		location.x = width - 20 - location.width();
		location.y = 8f - location.baseLine() / 2f;
		PixelScene.align(location);

		danger.setPos( width - danger.width(), 20 );

		buffs.setPos( 31, 40 );

		btnMenu.setPos( width - btnMenu.width(), 1 );
		
		version.scale.set(PixelScene.align(0.5f));
		version.measure();
		version.x = width - version.width();
		version.y = btnMenu.bottom() + (4 - version.baseLine());
		PixelScene.align(version);
	}

	@Override
	public void update() {
		super.update();

		amt.text(Integer.toString(Dungeon.geo));

		SmartTexture icons;
		TextureFilm film;

		icons = TextureCache.get( Assets.Interfaces.SOUL_METER );
		film = new TextureFilm( icons, 27, 27 );
		soulMeter.frame( film.get( Math.round(Dungeon.hero.MP / (float)Dungeon.hero.MM * 27) ) );

	}

	public void pickup(com.quasistellar.hollowdungeon.items.Item item, int cell ) {
		pickedUp.reset( item,
				cell,
				geo.x + 6,
				geo.y + 6);
	}
	
	private static final int[] warningColors = new int[]{0x660000, 0xCC0000, 0x660000};

	private static class MenuButton extends Button {

		private Image image;

		public MenuButton() {
			super();

			width = image.width + 4;
			height = image.height + 4;
		}

		@Override
		protected void createChildren() {
			super.createChildren();

			image = new Image( Assets.Interfaces.MENU, 17, 2, 12, 11 );
			add( image );
		}

		@Override
		protected void layout() {
			super.layout();

			image.x = x + 2;
			image.y = y + 1;
		}

		@Override
		protected void onPointerDown() {
			image.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK );
		}

		@Override
		protected void onPointerUp() {
			image.resetColor();
		}

		@Override
		protected void onClick() {
			com.quasistellar.hollowdungeon.scenes.GameScene.show( new WndGame() );
		}
	}
}
