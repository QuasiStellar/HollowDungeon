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

package com.quasistellar.hollowdungeon.windows;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.actors.hero.Belongings;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.Geo;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.Recipe;
import com.quasistellar.hollowdungeon.items.bags.Bag;
import com.quasistellar.hollowdungeon.items.bags.VelvetPouch;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.ui.Icons;
import com.quasistellar.hollowdungeon.ui.ItemSlot;
import com.quasistellar.hollowdungeon.ui.QuickSlotButton;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

public class WndBag extends WndTabbed {
	
	//only one wnditem can appear at a time
	private static WndBag INSTANCE;
	
	//FIXME this is getting cumbersome, there should be a better way to manage this
	public static enum Mode {
		ALL,
		UNIDENTIFED,
		UNCURSABLE,
		CURSABLE,
		UPGRADEABLE,
		QUICKSLOT,
		FOR_SALE,
		WEAPON,
		ARMOR,
		ENCHANTABLE,
		WAND,
		SEED,
		FOOD,
		POTION,
		SCROLL,
		UNIDED_POTION_OR_SCROLL,
		EQUIPMENT,
		TRANMSUTABLE,
		ALCHEMY,
		RECYCLABLE,
		NOT_EQUIPPED
	}

	protected static final int COLS_P    = 5;
	protected static final int COLS_L    = 8;
	
	protected static final int SLOT_WIDTH	= 28;
	protected static final int SLOT_HEIGHT	= 28;
	protected static final int SLOT_MARGIN	= 1;
	
	protected static final int TITLE_HEIGHT	= 14;
	
	private Listener listener;
	private WndBag.Mode mode;
	private String title;

	private int nCols;

	protected int count;
	protected int col;
	protected int row;
	
	private static Mode lastMode;
	private static Bag lastBag;
	
	public WndBag( Bag bag, Listener listener, Mode mode, String title ) {
		
		super();
		
		if( INSTANCE != null ){
			INSTANCE.hide();
		}
		INSTANCE = this;
		
		this.listener = listener;
		this.mode = mode;
		this.title = title;
		
		lastMode = mode;
		lastBag = bag;

		nCols = PixelScene.landscape() ? COLS_L : COLS_P;
		int slotsWidth = SLOT_WIDTH * nCols + SLOT_MARGIN * (nCols - 1);

		placeTitle( bag, slotsWidth );
		
		placeItems( bag );

		int slotsHeight = SLOT_HEIGHT * row + SLOT_MARGIN * (row - 1);

		resize( slotsWidth, slotsHeight + TITLE_HEIGHT );

		Belongings stuff = Dungeon.hero.belongings;
		Bag[] bags = {
			stuff.backpack,
			stuff.getItem( VelvetPouch.class )};

		for (Bag b : bags) {
			if (b != null) {
				BagTab tab = new BagTab( b );
				add( tab );
				tab.select( b == bag );
			}
		}

		layoutTabs();
	}
	
	public static WndBag lastBag( Listener listener, Mode mode, String title ) {
		
		if (mode == lastMode && lastBag != null &&
			Dungeon.hero.belongings.backpack.contains( lastBag )) {
			
			return new WndBag( lastBag, listener, mode, title );
			
		} else {
			
			return new WndBag( Dungeon.hero.belongings.backpack, listener, mode, title );
			
		}
	}
	
	protected void placeTitle( Bag bag, int width ){
		
		RenderedTextBlock txtTitle = PixelScene.renderTextBlock(
				title != null ? Messages.titleCase(title) : Messages.titleCase( bag.name() ), 8 );
		txtTitle.hardlight( TITLE_COLOR );
		txtTitle.setPos(
				1,
				(TITLE_HEIGHT - txtTitle.height()) / 2f - 1
		);
		PixelScene.align(txtTitle);
		add( txtTitle );
	}
	
	protected void placeItems( Bag container ) {
		
		// Equipped items
		Belongings stuff = Dungeon.hero.belongings;

		//the container itself if it's not the root backpack
		if (container != Dungeon.hero.belongings.backpack){
			placeItem(container);
			count--; //don't count this one, as it's not actually inside of itself
		}

		// Items in the bag, except other containers (they have tags at the bottom)
		for (Item item : container.items.toArray(new Item[0])) {
			if (!(item instanceof Bag)) {
				placeItem( item );
			} else {
				count++;
			}
		}
		
		// Free Space
		while ((count - 4) < container.capacity()) {
			placeItem( null );
		}
	}
	
	protected void placeItem( final Item item ) {

		count++;
		
		int x = col * (SLOT_WIDTH + SLOT_MARGIN);
		int y = TITLE_HEIGHT + row * (SLOT_HEIGHT + SLOT_MARGIN);
		
		add( new ItemButton( item ).setPos( x, y ) );
		
		if (++col >= nCols) {
			col = 0;
			row++;
		}

	}

	@Override
	public boolean onSignal(KeyEvent event) {
		if (event.pressed && KeyBindings.getActionForKey( event ) == HDAction.INVENTORY) {
			hide();
			return true;
		} else {
			return super.onSignal(event);
		}
	}
	
	@Override
	public void onBackPressed() {
		if (listener != null) {
			listener.onSelect( null );
		}
		super.onBackPressed();
	}
	
	@Override
	protected void onClick( Tab tab ) {
		hide();
		Game.scene().addToFront(new WndBag(((BagTab) tab).bag, listener, mode, title));
	}
	
	@Override
	public void hide() {
		super.hide();
		if (INSTANCE == this){
			INSTANCE = null;
		}
	}
	
	@Override
	protected int tabHeight() {
		return 20;
	}
	
	private Image icon( Bag bag ) {
		if (bag instanceof VelvetPouch) {
			return Icons.get( Icons.SEED_POUCH );
		} else {
			return Icons.get( Icons.BACKPACK );
		}
	}
	
	private class BagTab extends IconTab {

		private Bag bag;
		
		public BagTab( Bag bag ) {
			super( icon(bag) );
			
			this.bag = bag;
		}
		
	}
	
	public static class Placeholder extends Item {
		{
			name = null;
		}
		
		public Placeholder( int image ) {
			this.image = image;
		}
		
		@Override
		public boolean isIdentified() {
			return true;
		}
		
		@Override
		public boolean isEquipped( Hero hero ) {
			return true;
		}
	}
	
	private class ItemButton extends ItemSlot {
		
		private static final int NORMAL		= 0x9953564D;
		private static final int EQUIPPED	= 0x9991938C;
		
		private Item item;
		private ColorBlock bg;
		
		public ItemButton( Item item ) {
			
			super( item );

			this.item = item;
			if (item instanceof Geo || item instanceof Bag) {
				bg.visible = false;
			}
			
			width = SLOT_WIDTH;
			height = SLOT_HEIGHT;
		}
		
		@Override
		protected void createChildren() {
			bg = new ColorBlock( SLOT_WIDTH, SLOT_HEIGHT, NORMAL );
			add( bg );
			
			super.createChildren();
		}
		
		@Override
		protected void layout() {
			bg.x = x;
			bg.y = y;
			
			super.layout();
		}
		
		@Override
		public void item( Item item ) {
			
			super.item( item );
			if (item != null) {

				bg.texture( TextureCache.createSolid( item.isEquipped( Dungeon.hero ) ? EQUIPPED : NORMAL ) );
				if (item.cursed && item.cursedKnown) {
					bg.ra = +0.3f;
					bg.ga = -0.15f;
				} else if (!item.isIdentified()) {
					bg.ra = 0.3f;
					bg.ba = 0.3f;
				}
				
				if (item.name() == null) {
					enable( false );
				} else {
					enable(
						mode == Mode.FOR_SALE && !item.unique && (item.price() > 0) && (!item.isEquipped( Dungeon.hero ) || !item.cursed) ||
						mode == Mode.UPGRADEABLE && item.isUpgradable() ||
						mode == Mode.UNIDENTIFED && !item.isIdentified() ||
						mode == Mode.QUICKSLOT && (item.defaultAction != null) ||
						mode == Mode.ALCHEMY && Recipe.usableInRecipe(item) ||
						mode == Mode.NOT_EQUIPPED && !item.isEquipped(Dungeon.hero) ||
						mode == Mode.ALL
					);
				}
			} else {
				bg.color( NORMAL );
			}
		}
		
		@Override
		protected void onPointerDown() {
			bg.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.Sounds.CLICK, 0.7f, 0.7f, 1.2f );
		}
		
		protected void onPointerUp() {
			bg.brightness( 1.0f );
		}
		
		@Override
		protected void onClick() {
			if (lastBag != item && !lastBag.contains(item) && !item.isEquipped(Dungeon.hero)){

				hide();

			} else if (listener != null) {
				
				hide();
				listener.onSelect( item );
				
			} else {
				
				Game.scene().addToFront(new WndUseItem( WndBag.this, item ) );
				
			}
		}
		
		@Override
		protected boolean onLongClick() {
			if (listener == null && item.defaultAction != null) {
				hide();
				Dungeon.quickslot.setSlot( 0 , item );
				QuickSlotButton.refresh();
				return true;
			} else {
				return false;
			}
		}
	}
	
	public interface Listener {
		void onSelect( Item item );
	}
}
