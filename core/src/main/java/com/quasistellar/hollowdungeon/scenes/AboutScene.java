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

package com.quasistellar.hollowdungeon.scenes;

import com.quasistellar.hollowdungeon.Chrome;
import com.quasistellar.hollowdungeon.effects.Flare;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.ui.ExitButton;
import com.quasistellar.hollowdungeon.ui.Icons;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.ScrollPane;
import com.quasistellar.hollowdungeon.ui.StyledButton;
import com.quasistellar.hollowdungeon.ui.Window;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

public class AboutScene extends PixelScene {

	@Override
	public void create() {
		super.create();

		final float colWidth = 120;
		final float fullWidth = colWidth;

		int w = Camera.main.width;
		int h = Camera.main.height;

		ScrollPane list = new ScrollPane( new Component() );
		add( list );

		Component content = list.content();
		content.clear();

		//*** Hollow Dungeon Credits ***

		CreditsBlock hd = new CreditsBlock(true, Window.SHPX_COLOR,
				"Hollow Dungeon",
				Icons.CUBE_CODE.get(),
				"Developed by: _QuasiStellar_\nBased on Shattered Pixel Dungeon's open source",
				"quasistellar.tech",
				"https://quasistellar.tech");
		hd.setRect((w - fullWidth)/2f, 6, 120, 0);
		content.add(hd);

		CreditsBlock drb = new CreditsBlock(false, Window.SHPX_COLOR,
				"Music:",
				Icons.ALEKS.get(),
				"DrBlacker",
				"youtube",
				"https://www.youtube.com/channel/UCGiKaUDPgtD-TyYN6iFNDYA");
		drb.setSize(colWidth/2f, 0);

		drb.setPos(w/2f - colWidth/2f, hd.bottom()+5);

		content.add(drb);

		CreditsBlock yog = new CreditsBlock(false, Window.SHPX_COLOR,
				"Support:",
				Icons.CHARLIE.get(),
				"Yog Dzewa",
				"vk",
				"https://vk.com/yogdzewa");
		yog.setRect(drb.right(), drb.top(), colWidth/2f, 0);
		content.add(yog);

		StyledButton btnSupport = new StyledButton( Chrome.Type.GREY_BUTTON_TR, "Support the project"){
			@Override
			protected void onClick() {
				DeviceCompat.openURI( "https://liberapay.com/QuasiStellar/" );
			}
		};
		btnSupport.icon(Icons.get(Icons.GOLD));
		btnSupport.setRect(drb.right() - 54, drb.bottom() + 5, 108, 20);
		add(btnSupport);

		//*** Shattered Pixel Dungeon Credits ***

		final int SHPD_COLOR = 0x33BB33;
		CreditsBlock shpd = new CreditsBlock(true, SHPD_COLOR,
				"Shattered Pixel Dungeon",
				Icons.SHPX.get(),
				"Developed by: _00-Evan_\nBased on Pixel Dungeon's open source",
				"ShatteredPixel.com",
				"https://ShatteredPixel.com");

		shpd.setRect(hd.left(), btnSupport.bottom() + 8, colWidth, 0);

		content.add(shpd);

		addLine(shpd.top() - 4, content);

		//*** Pixel Dungeon Credits ***

		final int WATA_COLOR = 0x55AAFF;
		CreditsBlock wata = new CreditsBlock(true, WATA_COLOR,
				"Pixel Dungeon",
				Icons.WATA.get(),
				"Developed by: _Watabou_\nInspired by Brian Walker's Brogue",
				"pixeldungeon.watabou.ru",
				"http://pixeldungeon.watabou.ru");

		wata.setRect(hd.left(), shpd.bottom() + 8, colWidth, 0);

		content.add(wata);

		addLine(wata.top() - 4, content);

		//*** LibGDX Credits ***

		final int GDX_COLOR = 0xE44D3C;
		CreditsBlock gdx = new CreditsBlock(true,
				GDX_COLOR,
				null,
				Icons.LIBGDX.get(),
				"Hollow Dungeon is powered by _LibGDX_!",
				"libgdx.badlogicgames.com",
				"http://libgdx.badlogicgames.com");

		gdx.setRect(wata.left(), wata.bottom() + 8, colWidth, 0);

		content.add(gdx);

		addLine(gdx.top() - 4, content);

		//blocks the rays from the LibGDX icon going above the line
		ColorBlock blocker = new ColorBlock(w, 8, 0xFF000000);
		blocker.y = gdx.top() - 12;
		content.addToBack(blocker);
		content.sendToBack(gdx);

		CreditsBlock arcnor = new CreditsBlock(false, GDX_COLOR,
				"Pixel Dungeon GDX:",
				Icons.ARCNOR.get(),
				"Edu García",
				"twitter.com/arcnor",
				"https://twitter.com/arcnor");
		arcnor.setSize(colWidth/2f, 0);

		arcnor.setPos(drb.left(), gdx.bottom()+5);

		content.add(arcnor);

		CreditsBlock purigro = new CreditsBlock(false, GDX_COLOR,
				"Shattered GDX Help:",
				Icons.PURIGRO.get(),
				"Kevin MacMartin",
				"github.com/prurigro",
				"https://github.com/prurigro/");
		purigro.setRect(arcnor.right()+2, arcnor.top(), colWidth/2f, 0);
		content.add(purigro);

		//*** Transifex Credits ***

		CreditsBlock transifex = new CreditsBlock(true,
				Window.TITLE_COLOR,
				null,
				null,
				"Hollow Dungeon is based on Hollow Knight by Team Cherry, the greatest game I've ever played.",
				"hollowknight.com",
				"https://www.hollowknight.com/");
		transifex.setRect((Camera.main.width - colWidth)/2f, purigro.bottom() + 8, colWidth, 0);
		content.add(transifex);

		addLine(transifex.top() - 4, content);

		addLine(transifex.bottom() + 4, content);

		//*** GH ***

		CreditsBlock gh = new CreditsBlock(true,
				Window.TITLE_COLOR,
				null,
				null,
				"Github repository:",
				"github.com",
				"https://www.youtube.com/watch?v=dQw4w9WgXcQ");
		gh.setRect(transifex.left()-10, transifex.bottom() + 8, colWidth+20, 0);
		content.add(gh);

		CreditsBlock gh2 = new CreditsBlock(true,
				Window.TITLE_COLOR,
				null,
				null,
				"Actual Github repository:",
				"github.com",
				"https://www.youtube.com/watch?v=dQw4w9WgXcQ");
		gh2.setRect(transifex.left()-10, transifex.bottom() + 800, colWidth+20, 0);
		content.add(gh2);

		content.setSize( fullWidth, gh2.bottom()+10 );

		list.setRect( 0, 0, w, h );
		list.scrollTo(0, 0);

		ExitButton btnExit = new ExitButton();
		btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
		add( btnExit );

		fadeIn();
	}
	
	@Override
	protected void onBackPressed() {
		HollowDungeon.switchScene(TitleScene.class);
	}

	private void addLine( float y, Group content ){
		ColorBlock line = new ColorBlock(Camera.main.width, 1, 0xFF333333);
		line.y = y;
		content.add(line);
	}

	private static class CreditsBlock extends Component {

		boolean large;
		RenderedTextBlock title;
		Image avatar;
		com.quasistellar.hollowdungeon.effects.Flare flare;
		RenderedTextBlock body;

		RenderedTextBlock link;
		ColorBlock linkUnderline;
		PointerArea linkButton;

		//many elements can be null, but body is assumed to have content.
		private CreditsBlock(boolean large, int highlight, String title, Image avatar, String body, String linkText, String linkUrl){
			super();

			this.large = large;

			if (title != null) {
				this.title = PixelScene.renderTextBlock(title, large ? 8 : 6);
				if (highlight != -1) this.title.hardlight(highlight);
				add(this.title);
			}

			if (avatar != null){
				this.avatar = avatar;
				add(this.avatar);
			}

			if (large && highlight != -1 && this.avatar != null){
				this.flare = new Flare( 7, 24 ).color( highlight, true ).show(this.avatar, 0);
				this.flare.angularSpeed = 20;
			}

			this.body = PixelScene.renderTextBlock(body, 6);
			if (highlight != -1) this.body.setHightlighting(true, highlight);
			if (large) this.body.align(RenderedTextBlock.CENTER_ALIGN);
			add(this.body);

			if (linkText != null && linkUrl != null){

				int color = 0xFFFFFFFF;
				if (highlight != -1) color = 0xFF000000 | highlight;
				this.linkUnderline = new ColorBlock(1, 1, color);
				add(this.linkUnderline);

				this.link = PixelScene.renderTextBlock(linkText, 6);
				if (highlight != -1) this.link.hardlight(highlight);
				add(this.link);

				linkButton = new PointerArea(0, 0, 0, 0){
					@Override
					protected void onClick( PointerEvent event ) {
						DeviceCompat.openURI( linkUrl );
					}
				};
				add(linkButton);
			}

		}

		@Override
		protected void layout() {
			super.layout();

			float topY = top();

			if (title != null){
				title.maxWidth((int)width());
				title.setPos( x + (width() - title.width())/2f, topY);
				topY += title.height() + (large ? 2 : 1);
			}

			if (large){

				if (avatar != null){
					avatar.x = x + (width()-avatar.width())/2f;
					avatar.y = topY;
					PixelScene.align(avatar);
					if (flare != null){
						flare.point(avatar.center());
					}
					topY = avatar.y + avatar.height() + 2;
				}

				body.maxWidth((int)width());
				body.setPos( x + (width() - body.width())/2f, topY);
				topY += body.height() + 2;

			} else {

				if (avatar != null){
					avatar.x = x;
					body.maxWidth((int)(width() - avatar.width - 1));

					if (avatar.height() > body.height()){
						avatar.y = topY;
						body.setPos( avatar.x + avatar.width() + 1, topY + (avatar.height() - body.height())/2f);
						topY += avatar.height() + 1;
					} else {
						avatar.y = topY + (body.height() - avatar.height())/2f;
						PixelScene.align(avatar);
						body.setPos( avatar.x + avatar.width() + 1, topY);
						topY += body.height() + 2;
					}

				} else {
					topY += 1;
					body.maxWidth((int)width());
					body.setPos( x, topY);
					topY += body.height()+2;
				}

			}

			if (link != null){
				if (large) topY += 1;
				link.maxWidth((int)width());
				link.setPos( x + (width() - link.width())/2f, topY);
				topY += link.height() + 2;

				linkButton.x = link.left()-1;
				linkButton.y = link.top()-1;
				linkButton.width = link.width()+2;
				linkButton.height = link.height()+2;

				linkUnderline.size(link.width(), PixelScene.align(0.49f));
				linkUnderline.x = link.left();
				linkUnderline.y = link.bottom()+1;

			}

			topY -= 2;

			height = Math.max(height, topY - top());
		}
	}
}
