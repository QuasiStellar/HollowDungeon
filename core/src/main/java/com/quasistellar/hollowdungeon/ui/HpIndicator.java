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
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class HpIndicator extends Component {

	public static final int SIZE = 8;

	private static HpIndicator heroInstance;

	private SmartTexture texture;
	private TextureFilm film;

	private ArrayList<MaskIcon> masks = new ArrayList<>();
	private boolean needsRefresh;
	private Char ch;

	public HpIndicator(Char ch ) {
		super();
		
		this.ch = ch;
		if (ch == Dungeon.hero) {
			heroInstance = this;
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		if (this == heroInstance) {
			heroInstance = null;
		}
	}
	
	@Override
	protected void createChildren() {
		texture = TextureCache.get( Assets.Interfaces.HP );
		film = new TextureFilm( texture, SIZE, SIZE );
	}
	
	@Override
	public synchronized void update() {
		super.update();
		if (needsRefresh){
			needsRefresh = false;
			layout();
		}
	}
	
	@Override
	protected void layout() {

		masks.clear();

		for (int i = 0; i < ch.HP; i++) {
			MaskIcon mask = new MaskIcon( 0 );
			add(mask);
			masks.add(mask);
		}

		for (int i = 0; i < ch.HT - ch.HP; i++) {
			MaskIcon mask = new MaskIcon( 1 );
			add(mask);
			masks.add(mask);
		}
		
		//layout
		int pos = 0;
		for (MaskIcon mask : masks){
			mask.setRect(x + pos * (SIZE + 2), y, 8, 8);
			pos++;
		}
	}

	private class MaskIcon extends Button {

		public Image icon;

		public MaskIcon( int broken ){
			super();

			icon = new Image( texture );
			icon.frame( film.get( broken ) );
			add( icon );
		}

		@Override
		protected void layout() {
			super.layout();
			icon.x = this.x;
			icon.y = this.y;
		}
	}

	public static void refreshHero() {
		if (heroInstance != null) {
			heroInstance.needsRefresh = true;
		}
	}
}
