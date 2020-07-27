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

import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.rings.Ring;
import com.quasistellar.hollowdungeon.items.weapon.Weapon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;

public class ItemSlot extends Button {

	public static final int DEGRADED	= 0xFF4444;
	public static final int UPGRADED	= 0x44FF44;
	public static final int FADED       = 0x999999;
	public static final int WARNING		= 0xFF8800;
	public static final int ENHANCED	= 0x3399FF;
	
	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;
	
	protected com.quasistellar.hollowdungeon.sprites.ItemSprite sprite;
	protected com.quasistellar.hollowdungeon.items.Item item;
	protected BitmapText status;
	protected BitmapText extra;
	protected Image      itemIcon;
	protected BitmapText level;
	
	private static final String TXT_STRENGTH	= ":%d";
	private static final String TXT_TYPICAL_STR	= "%d?";

	private static final String TXT_LEVEL	= "%+d";

	// Special "virtual items"
	public static final com.quasistellar.hollowdungeon.items.Item CHEST = new com.quasistellar.hollowdungeon.items.Item() {
		public int image() { return ItemSpriteSheet.CHEST; }
	};
	public static final com.quasistellar.hollowdungeon.items.Item LOCKED_CHEST = new com.quasistellar.hollowdungeon.items.Item() {
		public int image() { return ItemSpriteSheet.LOCKED_CHEST; }
	};
	public static final com.quasistellar.hollowdungeon.items.Item CRYSTAL_CHEST = new com.quasistellar.hollowdungeon.items.Item() {
		public int image() { return ItemSpriteSheet.CRYSTAL_CHEST; }
	};
	public static final com.quasistellar.hollowdungeon.items.Item TOMB = new com.quasistellar.hollowdungeon.items.Item() {
		public int image() { return ItemSpriteSheet.TOMB; }
	};
	public static final com.quasistellar.hollowdungeon.items.Item SKELETON = new com.quasistellar.hollowdungeon.items.Item() {
		public int image() { return ItemSpriteSheet.BONES; }
	};
	public static final com.quasistellar.hollowdungeon.items.Item REMAINS = new com.quasistellar.hollowdungeon.items.Item() {
		public int image() { return ItemSpriteSheet.REMAINS; }
	};
	
	public ItemSlot() {
		super();
		sprite.visible(false);
		enable(false);
	}
	
	public ItemSlot( com.quasistellar.hollowdungeon.items.Item item ) {
		this();
		item( item );
	}
		
	@Override
	protected void createChildren() {
		
		super.createChildren();
		
		sprite = new ItemSprite();
		add(sprite);
		
		status = new BitmapText( PixelScene.pixelFont);
		add(status);
		
		extra = new BitmapText( PixelScene.pixelFont);
		add(extra);
		
		level = new BitmapText( PixelScene.pixelFont);
		add(level);
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		sprite.x = x + (width - sprite.width) / 2f;
		sprite.y = y + (height - sprite.height) / 2f;
		PixelScene.align(sprite);
		
		if (status != null) {
			status.measure();
			if (status.width > width){
				status.scale.set(PixelScene.align(0.8f));
			} else {
				status.scale.set(1f);
			}
			status.x = x;
			status.y = y;
			PixelScene.align(status);
		}
		
		if (extra != null) {
			extra.x = x + (width - extra.width());
			extra.y = y;
			PixelScene.align(extra);
		}

		if (itemIcon != null){
			itemIcon.x = x + width - (ItemSpriteSheet.Icons.SIZE + itemIcon.width())/2f;
			itemIcon.y = y + (ItemSpriteSheet.Icons.SIZE - itemIcon.height)/2f;
			PixelScene.align(itemIcon);
		}
		
		if (level != null) {
			level.x = x + (width - level.width());
			level.y = y + (height - level.baseLine() - 1);
			com.quasistellar.hollowdungeon.scenes.PixelScene.align(level);
		}

	}
	
	public void item( Item item ) {
		if (this.item == item) {
			if (item != null) {
				sprite.frame(item.image());
				sprite.glow(item.glowing());
			}
			updateText();
			return;
		}

		this.item = item;

		if (item == null) {

			enable(false);
			sprite.visible(false);

			updateText();
			
		} else {
			
			enable(true);
			sprite.visible(true);

			sprite.view( item );
			updateText();
		}
	}

	private void updateText(){

		if (itemIcon != null){
			remove(itemIcon);
			itemIcon = null;
		}

		if (item == null){
			status.visible = extra.visible = level.visible = false;
			return;
		} else {
			status.visible = extra.visible = level.visible = true;
		}

		status.text( item.status() );

		if (item.icon != -1 && (item.isIdentified() || (item instanceof Ring && ((Ring) item).isKnown()))){
			extra.text( null );

			itemIcon = new Image(Assets.Sprites.ITEM_ICONS);
			itemIcon.frame(com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet.Icons.film.get(item.icon));
			add(itemIcon);

		} else if (item instanceof Weapon) {

			if (item.levelKnown){
				int str = ((Weapon)item).STRReq();
				extra.text( Messages.format( TXT_STRENGTH, str ) );
				extra.resetColor();
			} else {
				int str = ((Weapon)item).STRReq(0);
				extra.text( Messages.format( TXT_TYPICAL_STR, str ) );
				extra.hardlight( WARNING );
			}
			extra.measure();

		} else {

			extra.text( null );

		}

		int trueLvl = item.visiblyUpgraded();
		int buffedLvl = item.buffedVisiblyUpgraded();

		if (trueLvl != 0 || buffedLvl != 0) {
			level.text( Messages.format( TXT_LEVEL, buffedLvl ) );
			level.measure();
			if (trueLvl == buffedLvl || buffedLvl <= 0) {
				level.hardlight(buffedLvl > 0 ? UPGRADED : DEGRADED);
			} else {
				level.hardlight(buffedLvl > trueLvl ? ENHANCED : WARNING);
			}
		} else {
			level.text( null );
		}

		layout();
	}
	
	public void enable( boolean value ) {
		
		active = value;
		
		float alpha = value ? ENABLED : DISABLED;
		sprite.alpha( alpha );
		status.alpha( alpha );
		extra.alpha( alpha );
		level.alpha( alpha );
		if (itemIcon != null) itemIcon.alpha( alpha );
	}

	public void showExtraInfo( boolean show ){

		if (show){
			add(extra);
		} else {
			remove(extra);
		}

	}
}
