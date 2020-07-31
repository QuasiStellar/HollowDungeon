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
import com.quasistellar.hollowdungeon.items.Recipe;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.scenes.AlchemyScene;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.windows.WndInfoItem;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.ShatteredPixelDungeon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class QuickRecipe extends Component {
	
	private ArrayList<com.quasistellar.hollowdungeon.items.Item> ingredients;
	
	private ArrayList<com.quasistellar.hollowdungeon.ui.ItemSlot> inputs;
	private QuickRecipe.arrow arrow;
	private com.quasistellar.hollowdungeon.ui.ItemSlot output;
	
	public QuickRecipe(com.quasistellar.hollowdungeon.items.Recipe.SimpleRecipe r){
		this(r, r.getIngredients(), r.sampleOutput(null));
	}
	
	public QuickRecipe(com.quasistellar.hollowdungeon.items.Recipe r, ArrayList<com.quasistellar.hollowdungeon.items.Item> inputs, final com.quasistellar.hollowdungeon.items.Item output) {
		
		ingredients = inputs;
		int cost = r.cost(inputs);
		boolean hasInputs = true;
		this.inputs = new ArrayList<>();
		for (final com.quasistellar.hollowdungeon.items.Item in : inputs) {
			com.quasistellar.hollowdungeon.ui.ItemSlot curr;
			curr = new com.quasistellar.hollowdungeon.ui.ItemSlot(in) {
				@Override
				protected void onClick() {
					ShatteredPixelDungeon.scene().addToFront(new com.quasistellar.hollowdungeon.windows.WndInfoItem(in));
				}
			};
			
			ArrayList<com.quasistellar.hollowdungeon.items.Item> similar = Dungeon.hero.belongings.getAllSimilar(in);
			int quantity = 0;
			for (com.quasistellar.hollowdungeon.items.Item sim : similar) {
				//if we are looking for a specific item, it must be IDed
				if (sim.getClass() != in.getClass() || sim.isIdentified()) quantity += sim.quantity();
			}
			
			if (quantity < in.quantity()) {
				curr.sprite.alpha(0.3f);
				hasInputs = false;
			}
			curr.showExtraInfo(false);
			add(curr);
			this.inputs.add(curr);
		}
		
		if (cost > 0) {
			arrow = new arrow(com.quasistellar.hollowdungeon.ui.Icons.get(com.quasistellar.hollowdungeon.ui.Icons.ARROW), cost);
			arrow.hardlightText(0x00CCFF);
		} else {
			arrow = new arrow(com.quasistellar.hollowdungeon.ui.Icons.get(Icons.ARROW));
		}
		if (hasInputs) {
			arrow.icon.tint(1, 1, 0, 1);
			if (!(ShatteredPixelDungeon.scene() instanceof com.quasistellar.hollowdungeon.scenes.AlchemyScene)) {
				arrow.enable(false);
			}
		} else {
			arrow.icon.color(0, 0, 0);
			arrow.enable(false);
		}
		add(arrow);

		this.output = new com.quasistellar.hollowdungeon.ui.ItemSlot(output){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new WndInfoItem(output));
			}
		};
		if (!hasInputs){
			this.output.sprite.alpha(0.3f);
		}
		this.output.showExtraInfo(false);
		add(this.output);
		
		layout();
	}
	
	@Override
	protected void layout() {
		
		height = 16;
		width = 0;
		
		for (ItemSlot item : inputs){
			item.setRect(x + width, y, 16, 16);
			width += 16;
		}
		
		arrow.setRect(x + width, y, 14, 16);
		width += 14;
		
		output.setRect(x + width, y, 16, 16);
		width += 16;
	}
	
	public class arrow extends IconButton {
		
		BitmapText text;
		
		public arrow(){
			super();
		}
		
		public arrow( Image icon ){
			super( icon );
		}
		
		public arrow( Image icon, int count ){
			super( icon );
			text = new BitmapText( Integer.toString(count), PixelScene.pixelFont);
			text.measure();
			add(text);
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			if (text != null){
				text.x = x;
				text.y = y;
				com.quasistellar.hollowdungeon.scenes.PixelScene.align(text);
			}
		}
		
		@Override
		protected void onClick() {
			super.onClick();
			
			//find the window this is inside of and close it
			Group parent = this.parent;
			while (parent != null){
				if (parent instanceof Window){
					((Window) parent).hide();
					break;
				} else {
					parent = parent.parent;
				}
			}
			
			((AlchemyScene)ShatteredPixelDungeon.scene()).populate(ingredients, Dungeon.hero.belongings);
		}
		
		public void hardlightText(int color ){
			if (text != null) text.hardlight(color);
		}
	}
	
	//gets recipes for a particular alchemy guide page
	//a null entry indicates a break in section
	public static ArrayList<QuickRecipe> getRecipes( int pageIdx ){
		ArrayList<QuickRecipe> result = new ArrayList<>();
		switch (pageIdx){
			case 0: default:
				return result;
			case 1:

				return result;
			case 2:

				return result;
			case 3:

				return result;
			case 4:

				return result;
			case 5:

				return result;
			case 6:

				return result;
			case 7:

				return result;
			case 8:

				return result;
		}
	}
	
}
