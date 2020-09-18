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

import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.tiles.DungeonTerrainTilemap;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.windows.WndJournal;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.input.GameAction;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

public class Toolbar extends Component {

	private Tool btnWait;
	private Tool btnSearch;
	private Tool btnInventory;
	
	private PickedUpItem pickedUp;
	
	private boolean lastEnabled = true;
	public boolean examining = false;

	private static Toolbar instance;

	public enum Mode {
		SPLIT,
		GROUP,
		CENTER
	}
	
	public Toolbar() {
		super();

		instance = this;

		height = btnInventory.height();
	}
	
	@Override
	protected void createChildren() {

		add(btnWait = new Tool(24, 0, 20, 26) {
			@Override
			protected void onClick() {

				examining = false;

				GameScene.layoutSkillTags();

				Buff.prolong(Dungeon.hero, Utils.OneTurnDelay.class, 1);

				Dungeon.hero.rest(false);
			}
			
			@Override
			public GameAction keyAction() {
				return HDAction.WAIT;
			}
			
//			protected boolean onLongClick() {
//				examining = false;
//				Dungeon.hero.rest(true);
//				return true;
//			}
		});

		add(new Button(){
			@Override
			protected void onClick() {
				examining = false;
				Dungeon.hero.rest(true);
			}

			@Override
			public GameAction keyAction() {
				return HDAction.REST;
			}
		});
		
		add(btnSearch = new Tool(44, 0, 20, 26) {
			@Override
			protected void onClick() {
				if (!examining) {
					GameScene.selectCell(informer);
					examining = true;
				} else {
					informer.onSelect(null);
					Dungeon.hero.search(true);
				}
			}
			
			@Override
			public GameAction keyAction() {
				return HDAction.SEARCH;
			}
			
			@Override
			protected boolean onLongClick() {
				Dungeon.hero.search(true);
				return true;
			}
		});
		
		add(btnInventory = new Tool(0, 0, 24, 26) {

			@Override
			protected void onClick() {
				GameScene.show(new com.quasistellar.hollowdungeon.windows.WndBag(Dungeon.hero.belongings.backpack, null, WndBag.Mode.ALL, null));
			}
			
			@Override
			public GameAction keyAction() {
				return HDAction.INVENTORY;
			}
			
//			@Override
//			protected boolean onLongClick() {
//				WndJournal.last_index = 3; //catalog page
//				GameScene.show(new com.quasistellar.hollowdungeon.windows.WndJournal());
//				return true;
//			}
		});

		add(pickedUp = new PickedUpItem());
	}
	
	@Override
	protected void layout() {

		float right = width;
		switch(Mode.valueOf(HDSettings.toolbarMode())){
			case SPLIT:
				btnWait.setPos(x, y);
				btnSearch.setPos(btnWait.right(), y);
				btnInventory.setPos(right - btnInventory.width(), y);
				break;

			//center = group but.. well.. centered, so all we need to do is pre-emptively set the right side further in.
			case CENTER:
				float toolbarWidth = btnWait.width() + btnSearch.width() + btnInventory.width();
				right = (width + toolbarWidth)/2;

			case GROUP:
				btnWait.setPos(right - btnWait.width(), y);
				btnSearch.setPos(btnWait.left() - btnSearch.width(), y);
				btnInventory.setPos(btnSearch.left() - btnInventory.width(), y);
				break;
		}
		right = width;

		if (HDSettings.flipToolbar()) {

			btnWait.setPos( (right - btnWait.right()), y);
			btnSearch.setPos( (right - btnSearch.right()), y);
			btnInventory.setPos( (right - btnInventory.right()), y);

		}

	}

	public static void updateLayout(){
		if (instance != null) instance.layout();
	}
	
	@Override
	public void update() {
		super.update();
		
		if (lastEnabled != (Dungeon.hero.ready && Dungeon.hero.isAlive())) {
			lastEnabled = (Dungeon.hero.ready && Dungeon.hero.isAlive());
			
			for (Gizmo tool : members) {
				if (tool instanceof Tool) {
					((Tool)tool).enable( lastEnabled );
				}
			}
		}
		
		if (!Dungeon.hero.isAlive()) {
			btnInventory.enable(true);
		}
	}
	
	private static com.quasistellar.hollowdungeon.scenes.CellSelector.Listener informer = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			instance.examining = false;
			com.quasistellar.hollowdungeon.scenes.GameScene.examineCell( cell );
		}
		@Override
		public String prompt() {
			return Messages.get(Toolbar.class, "examine_prompt");
		}
	};
	
	private static class Tool extends Button {
		
		private static final int BGCOLOR = 0x7B8073;
		
		private Image base;
		
		public Tool( int x, int y, int width, int height ) {
			super();

			hotArea.blockWhenInactive = true;
			frame(x, y, width, height);
		}

		public void frame( int x, int y, int width, int height) {
			base.frame( x, y, width, height );

			this.width = width;
			this.height = height;
		}
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			base = new Image( Assets.Interfaces.TOOLBAR );
			add( base );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			base.x = x;
			base.y = y;
		}
		
		@Override
		protected void onPointerDown() {
			base.brightness( 1.4f );
		}
		
		@Override
		protected void onPointerUp() {
			if (active) {
				base.resetColor();
			} else {
				base.tint( BGCOLOR, 0.7f );
			}
		}
		
		public void enable( boolean value ) {
			if (value != active) {
				if (value) {
					base.resetColor();
				} else {
					base.tint( BGCOLOR, 0.7f );
				}
				active = value;
			}
		}
	}

	public static class PickedUpItem extends com.quasistellar.hollowdungeon.sprites.ItemSprite {
		
		private static final float DURATION = 0.5f;
		
		private float startScale;
		private float startX, startY;
		private float endX, endY;
		private float left;
		
		public PickedUpItem() {
			super();
			
			originToCenter();
			
			active =
			visible =
				false;
		}
		
		public void reset(Item item, int cell, float endX, float endY ) {
			view( item );
			
			active =
			visible =
				true;
			
			PointF tile = DungeonTerrainTilemap.raisedTileCenterToWorld(cell);
			Point screen = Camera.main.cameraToScreen(tile.x, tile.y);
			PointF start = camera().screenToCamera(screen.x, screen.y);
			
			x = this.startX = start.x - ItemSprite.SIZE / 2;
			y = this.startY = start.y - ItemSprite.SIZE / 2;
			
			this.endX = endX - ItemSprite.SIZE / 2;
			this.endY = endY - com.quasistellar.hollowdungeon.sprites.ItemSprite.SIZE / 2;
			left = DURATION;
			
			scale.set( startScale = Camera.main.zoom / camera().zoom );
			
		}
		
		@Override
		public void update() {
			super.update();
			
			if ((left -= Game.elapsed) <= 0) {
				
				visible =
				active =
					false;
				if (emitter != null) emitter.on = false;
				
			} else {
				float p = left / DURATION;
				scale.set( startScale * (float)Math.sqrt( p ) );
				
				x = startX*p + endX*(1-p);
				y = startY*p + endY*(1-p);
			}
		}
	}
}
