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
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.HollowDungeon;
import com.quasistellar.hollowdungeon.messages.Languages;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.scenes.TitleScene;
import com.quasistellar.hollowdungeon.services.updates.Updates;
import com.quasistellar.hollowdungeon.ui.CheckBox;
import com.quasistellar.hollowdungeon.ui.GameLog;
import com.quasistellar.hollowdungeon.ui.Icons;
import com.quasistellar.hollowdungeon.ui.OptionSlider;
import com.quasistellar.hollowdungeon.ui.RedButton;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Toolbar;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static com.quasistellar.hollowdungeon.levels.traps.Trap.WHITE;
import static com.quasistellar.hollowdungeon.windows.WndTitledMessage.GAP;

//TODO seeing as a fair bit of this is platform-dependant, might be better to have a per-platform wndsettings
public class WndSettings extends WndTabbed {

	private static final int WIDTH_P	    = 122;
	private static final int WIDTH_L	    = 223;

	private static final int SLIDER_HEIGHT	= 24;
	private static final int BTN_HEIGHT	    = 18;
	private static final float GAP          = 2;

	private final DisplayTab  display;
	private final UITab       ui;
	private final DataTab     data;
	private final AudioTab    audio;
	private final LangsTab    langs;

	public static int last_index = 0;

	public WndSettings() {
		super();

		float height;

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		display = new DisplayTab();
		display.setSize(width, 0);
		height = display.height();
		add( display );

		add( new IconTab(Icons.get(Icons.DISPLAY)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				display.visible = display.active = value;
				if (value) last_index = 0;
			}
		});

		ui = new UITab();
		ui.setSize(width, 0);
		height = Math.max(height, ui.height());
		add( ui );

		add( new IconTab(Icons.get(Icons.PREFS)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				ui.visible = ui.active = value;
				if (value) last_index = 1;
			}
		});

		data = new DataTab();
		data.setSize(width, 0);
		height = Math.max(height, data.height());
		add( data );

		add( new IconTab(Icons.get(Icons.DISCORD)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				data.visible = data.active = value;
				if (value) last_index = 2;
			}
		});

		audio = new AudioTab();
		audio.setSize(width, 0);
		height = Math.max(height, audio.height());
		add( audio );

		add( new IconTab(Icons.get(Icons.AUDIO)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				audio.visible = audio.active = value;
				if (value) last_index = 3;
			}
		});

		langs = new LangsTab();
		langs.setSize(width, 0);
		height = Math.max(height, langs.height());
		add(langs);


		IconTab langsTab = new IconTab(Icons.get(Icons.LANGS)){
			@Override
			protected void select(boolean value) {
				super.select(value);
				langs.visible = langs.active = value;
				if (value) last_index = 4;
			}

			@Override
			protected void createChildren() {
				super.createChildren();
			}

		};
		add(langsTab);

		resize(width, (int) Math.ceil(height));

		layoutTabs();

		select(last_index);

	}

	@Override
	public void hide() {
		super.hide();
		//resets generators because there's no need to retain chars for languages not selected
		if (HollowDungeon.scene().getClass() != TitleScene.class) {
			HollowDungeon.seamlessResetScene(new Game.SceneChangeCallback() {
				@Override
				public void beforeCreate() {
					Game.platform.resetGenerators();
				}
				@Override
				public void afterCreate() {}
			});
		}
	}

	private static class DisplayTab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		OptionSlider optScale;
		CheckBox chkSaver;
		RedButton btnOrientation;
		ColorBlock sep2;
		OptionSlider optBrightness;
		OptionSlider optVisGrid;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			if ((int) Math.ceil(2 * Game.density) < PixelScene.maxDefaultZoom) {
				optScale = new OptionSlider(Messages.get(this, "scale"),
						(int)Math.ceil(2* Game.density)+ "X",
						PixelScene.maxDefaultZoom + "X",
						(int)Math.ceil(2* Game.density),
						PixelScene.maxDefaultZoom ) {
					@Override
					protected void onChange() {
						if (getSelectedValue() != HDSettings.scale()) {
							HDSettings.scale(getSelectedValue());
							HollowDungeon.seamlessResetScene();
						}
					}
				};
				optScale.setSelectedValue(PixelScene.defaultZoom);
				add(optScale);
			}

			if (!DeviceCompat.isDesktop() && PixelScene.maxScreenZoom >= 2) {
				chkSaver = new CheckBox(Messages.get(this, "saver")) {
					@Override
					protected void onClick() {
						super.onClick();
						if (checked()) {
							checked(!checked());
							HollowDungeon.scene().add(new WndOptions(Icons.get(Icons.DISPLAY),
									Messages.get(DisplayTab.class, "saver"),
									Messages.get(DisplayTab.class, "saver_desc"),
									Messages.get(DisplayTab.class, "okay"),
									Messages.get(DisplayTab.class, "cancel")) {
								@Override
								protected void onSelect(int index) {
									if (index == 0) {
										checked(!checked());
										HDSettings.powerSaver(checked());
									}
								}
							});
						} else {
							HDSettings.powerSaver(checked());
						}
					}
				};
				chkSaver.checked( HDSettings.powerSaver() );
				add( chkSaver );
			}

			if (!DeviceCompat.isDesktop()) {
				btnOrientation = new RedButton(PixelScene.landscape() ?
						Messages.get(this, "portrait")
						: Messages.get(this, "landscape")) {
					@Override
					protected void onClick() {
						HDSettings.landscape(!PixelScene.landscape());
					}
				};
				add(btnOrientation);
			}

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);

			optBrightness = new OptionSlider(Messages.get(this, "brightness"),
					Messages.get(this, "dark"), Messages.get(this, "bright"), -1, 1) {
				@Override
				protected void onChange() {
					HDSettings.brightness(getSelectedValue());
				}
			};
			optBrightness.setSelectedValue(HDSettings.brightness());
			add(optBrightness);

			optVisGrid = new OptionSlider(Messages.get(this, "visual_grid"),
					Messages.get(this, "off"), Messages.get(this, "high"), -1, 2) {
				@Override
				protected void onChange() {
					HDSettings.visualGrid(getSelectedValue());
				}
			};
			optVisGrid.setSelectedValue(HDSettings.visualGrid());
			add(optVisGrid);

		}

		@Override
		protected void layout() {

			float bottom = y;

			title.setPos((width - title.width())/2, bottom + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2*GAP;

			bottom = sep1.y + 1;

			if (optScale != null){
				optScale.setRect(0, bottom + GAP, width, SLIDER_HEIGHT);
				bottom = optScale.bottom();
			}

			if (width > 200 && chkSaver != null && btnOrientation != null) {
				chkSaver.setRect(0, bottom + GAP, width/2-1, BTN_HEIGHT);
				btnOrientation.setRect(chkSaver.right()+ GAP, bottom + GAP, width/2-1, BTN_HEIGHT);
				bottom = btnOrientation.bottom();
			} else {
				if (chkSaver != null) {
					chkSaver.setRect(0, bottom + GAP, width, BTN_HEIGHT);
					bottom = chkSaver.bottom();
				}

				if (btnOrientation != null) {
					btnOrientation.setRect(0, bottom + GAP, width, BTN_HEIGHT);
					bottom = btnOrientation.bottom();
				}
			}

			sep2.size(width, 1);
			sep2.y = bottom + GAP;
			bottom = sep2.y + 1;

			if (width > 200){
				optBrightness.setRect(0, bottom + GAP, width/2-GAP/2, SLIDER_HEIGHT);
				optVisGrid.setRect(optBrightness.right() + GAP, optBrightness.top(), width/2-GAP/2, SLIDER_HEIGHT);
			} else {
				optBrightness.setRect(0, bottom + GAP, width, SLIDER_HEIGHT);
				optVisGrid.setRect(0, optBrightness.bottom() + GAP, width, SLIDER_HEIGHT);
			}

			height = optVisGrid.bottom();
		}

	}

	private static class UITab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		RenderedTextBlock barDesc;
		RedButton btnSplit; RedButton btnGrouped; RedButton btnCentered;
		CheckBox chkFlipToolbar;
		CheckBox chkFlipTags;
		ColorBlock sep2;
		CheckBox chkFullscreen;
		CheckBox chkFont;
		ColorBlock sep3;
		RedButton btnKeyBindings;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			barDesc = PixelScene.renderTextBlock(Messages.get(this, "mode"), 9);
			add(barDesc);

			btnSplit = new RedButton(Messages.get(this, "split")){
				@Override
				protected void onClick() {
					textColor(TITLE_COLOR);
					btnGrouped.textColor(WHITE);
					btnCentered.textColor(WHITE);
					HDSettings.toolbarMode(Toolbar.Mode.SPLIT.name());
					Toolbar.updateLayout();
				}
			};
			if (HDSettings.toolbarMode().equals(Toolbar.Mode.SPLIT.name())) btnSplit.textColor(TITLE_COLOR);
			add(btnSplit);

			btnGrouped = new RedButton(Messages.get(this, "group")){
				@Override
				protected void onClick() {
					btnSplit.textColor(WHITE);
					textColor(TITLE_COLOR);
					btnCentered.textColor(WHITE);
					HDSettings.toolbarMode(Toolbar.Mode.GROUP.name());
					Toolbar.updateLayout();
				}
			};
			if (HDSettings.toolbarMode().equals(Toolbar.Mode.GROUP.name())) btnGrouped.textColor(TITLE_COLOR);
			add(btnGrouped);

			btnCentered = new RedButton(Messages.get(this, "center")){
				@Override
				protected void onClick() {
					btnSplit.textColor(WHITE);
					btnGrouped.textColor(WHITE);
					textColor(TITLE_COLOR);
					HDSettings.toolbarMode(Toolbar.Mode.CENTER.name());
					Toolbar.updateLayout();
				}
			};
			if (HDSettings.toolbarMode().equals(Toolbar.Mode.CENTER.name())) btnCentered.textColor(TITLE_COLOR);
			add(btnCentered);

			chkFlipToolbar = new CheckBox(Messages.get(this, "flip_toolbar")){
				@Override
				protected void onClick() {
					super.onClick();
					HDSettings.flipToolbar(checked());
					Toolbar.updateLayout();
				}
			};
			chkFlipToolbar.checked(HDSettings.flipToolbar());
			add(chkFlipToolbar);

			chkFlipTags = new CheckBox(Messages.get(this, "flip_indicators")){
				@Override
				protected void onClick() {
					super.onClick();
					HDSettings.flipTags(checked());
					GameScene.layoutTags();
				}
			};
			chkFlipTags.checked(HDSettings.flipTags());
			add(chkFlipTags);

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);

			chkFullscreen = new CheckBox( Messages.get(this, "fullscreen") ) {
				@Override
				protected void onClick() {
					super.onClick();
					HDSettings.fullscreen(checked());
				}
			};
			chkFullscreen.checked(HDSettings.fullscreen());
			chkFullscreen.enable(DeviceCompat.supportsFullScreen());
			add(chkFullscreen);

			chkFont = new CheckBox(Messages.get(this, "system_font")){
				@Override
				protected void onClick() {
					super.onClick();
					HollowDungeon.seamlessResetScene(new Game.SceneChangeCallback() {
						@Override
						public void beforeCreate() {
							HDSettings.systemFont(checked());
						}

						@Override
						public void afterCreate() {
							//do nothing
						}
					});
				}
			};
			chkFont.checked(HDSettings.systemFont());
			add(chkFont);

			if (DeviceCompat.hasHardKeyboard()){

				sep3 = new ColorBlock(1, 1, 0xFF000000);
				add(sep3);

				btnKeyBindings = new RedButton(Messages.get(this, "key_bindings")){
					@Override
					protected void onClick() {
						super.onClick();
						HollowDungeon.scene().addToFront(new WndKeyBindings());
					}
				};

				add(btnKeyBindings);
			}
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2*GAP;

			barDesc.setPos((width-barDesc.width())/2f, sep1.y + 1 + GAP);
			PixelScene.align(barDesc);

			int btnWidth = (int)(width - 2* GAP)/3;
			btnSplit.setRect(0, barDesc.bottom() + GAP, btnWidth, 16);
			btnGrouped.setRect(btnSplit.right()+ GAP, btnSplit.top(), btnWidth, 16);
			btnCentered.setRect(btnGrouped.right()+ GAP, btnSplit.top(), btnWidth, 16);

			if (width > 200) {
				chkFlipToolbar.setRect(0, btnGrouped.bottom() + GAP, width / 2 - 1, BTN_HEIGHT);
				chkFlipTags.setRect(chkFlipToolbar.right() + GAP, chkFlipToolbar.top(), width/2 -1, BTN_HEIGHT);
				sep2.size(width, 1);
				sep2.y = chkFlipTags.bottom() + 2;
				chkFullscreen.setRect(0, sep2.y + 1 + GAP, width / 2 - 1, BTN_HEIGHT);
				chkFont.setRect(chkFullscreen.right() + GAP, chkFullscreen.top(), width/2 - 1, BTN_HEIGHT);
			}
			else {
				chkFlipToolbar.setRect(0, btnGrouped.bottom() + GAP, width, BTN_HEIGHT);
				chkFlipTags.setRect(0, chkFlipToolbar.bottom() + GAP, width, BTN_HEIGHT);
				sep2.size(width, 1);
				sep2.y = chkFlipTags.bottom() + 2;
				chkFullscreen.setRect(0, sep2.y + 1 + GAP, width, BTN_HEIGHT);
				chkFont.setRect(0, chkFullscreen.bottom() + GAP, width, BTN_HEIGHT);
			}

			if (btnKeyBindings != null){
				sep3.size(width, 1);
				sep3.y = chkFont.bottom() + 2;
				btnKeyBindings.setRect(0, sep3.y + 1 + GAP, width, BTN_HEIGHT);
				height = btnKeyBindings.bottom();
			} else {
				height = chkFont.bottom();
			}
		}

	}

	private static class DataTab extends Component{

		RenderedTextBlock title;
		ColorBlock sep1;
		CheckBox chkUpdates;
		CheckBox chkWifi;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			chkUpdates = new CheckBox(Messages.get(this, "updates")){
				@Override
				protected void onClick() {
					super.onClick();
					HDSettings.updates(checked());
					Updates.clearUpdate();
				}
			};
			chkUpdates.checked(HDSettings.updates());
			add(chkUpdates);

			if (!DeviceCompat.isDesktop()){
				chkWifi = new CheckBox(Messages.get(this, "wifi")){
					@Override
					protected void onClick() {
						super.onClick();
						HDSettings.WiFi(checked());
					}
				};
				chkWifi.checked(HDSettings.WiFi());
				add(chkWifi);
			}
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2*GAP;

			chkUpdates.setRect(0, sep1.y + 1 + GAP, width, BTN_HEIGHT);

			float pos = chkUpdates.bottom();
			if (chkWifi != null){
				chkWifi.setRect(0, pos + GAP, width, BTN_HEIGHT);
				pos = chkWifi.bottom();
			}

			height = pos;

		}
	}

	private static class AudioTab extends Component {

		RenderedTextBlock title;
		ColorBlock sep1;
		OptionSlider optMusic;
		CheckBox chkMusicMute;
		ColorBlock sep2;
		OptionSlider optSFX;
		CheckBox chkMuteSFX;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			optMusic = new OptionSlider(Messages.get(this, "music_vol"), "0", "10", 0, 10) {
				@Override
				protected void onChange() {
					HDSettings.musicVol(getSelectedValue());
				}
			};
			optMusic.setSelectedValue(HDSettings.musicVol());
			add(optMusic);

			chkMusicMute = new CheckBox(Messages.get(this, "music_mute")){
				@Override
				protected void onClick() {
					super.onClick();
					HDSettings.music(!checked());
				}
			};
			chkMusicMute.checked(!HDSettings.music());
			add(chkMusicMute);

			sep2 = new ColorBlock(1, 1, 0xFF000000);
			add(sep2);

			optSFX = new OptionSlider(Messages.get(this, "sfx_vol"), "0", "10", 0, 10) {
				@Override
				protected void onChange() {
					HDSettings.SFXVol(getSelectedValue());
					if (Random.Int(100) == 0){
						Sample.INSTANCE.play(Assets.Sounds.MIMIC);
					} else {
						Sample.INSTANCE.play(Random.oneOf(Assets.Sounds.GOLD,
								Assets.Sounds.HIT,
								Assets.Sounds.ITEM,
								Assets.Sounds.SHATTER,
								Assets.Sounds.EVOKE,
								Assets.Sounds.SECRET));
					}
				}
			};
			optSFX.setSelectedValue(HDSettings.SFXVol());
			add(optSFX);

			chkMuteSFX = new CheckBox( Messages.get(this, "sfx_mute") ) {
				@Override
				protected void onClick() {
					super.onClick();
					HDSettings.soundFx(!checked());
					Sample.INSTANCE.play( Assets.Sounds.CLICK );
				}
			};
			chkMuteSFX.checked(!HDSettings.soundFx());
			add( chkMuteSFX );
		}

		@Override
		protected void layout() {
			title.setPos((width - title.width())/2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2 * GAP;

			optMusic.setRect(0, sep1.y + 1 + GAP, width, SLIDER_HEIGHT);
			chkMusicMute.setRect(0, optMusic.bottom() + GAP, width, BTN_HEIGHT);

			sep2.size(width, 1);
			sep2.y = chkMusicMute.bottom() + GAP;

			optSFX.setRect(0, sep2.y + 1 + GAP, width, SLIDER_HEIGHT);
			chkMuteSFX.setRect(0, optSFX.bottom() + GAP, width, BTN_HEIGHT);

			height = chkMuteSFX.bottom();
		}

	}

	private static class LangsTab extends Component {

		final static int COLS_P = 1;
		final static int COLS_L = 2;

		RenderedTextBlock title;
		ColorBlock sep1;
		RedButton[] lanBtns;

		@Override
		protected void createChildren() {
			title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.hardlight(TITLE_COLOR);
			add(title);

			sep1 = new ColorBlock(1, 1, 0xFF000000);
			add(sep1);

			final ArrayList<Languages> langs = new ArrayList<>(Arrays.asList(Languages.values()));

			Languages nativeLang = Languages.matchLocale(Locale.getDefault());
			langs.remove(nativeLang);
			//move the native language to the top.
			langs.add(0, nativeLang);

			final Languages currLang = Messages.lang();

			lanBtns = new RedButton[langs.size()];
			for (int i = 0; i < langs.size(); i++){
				final int langIndex = i;
				RedButton btn = new RedButton(Messages.titleCase(langs.get(i).nativeName())) {
					@Override
					protected void onClick() {
						super.onClick();
						Messages.setup(langs.get(langIndex));
						HollowDungeon.seamlessResetScene(new Game.SceneChangeCallback() {
							@Override
							public void beforeCreate() {
								HDSettings.language(langs.get(langIndex));
								GameLog.wipe();
								Game.platform.resetGenerators();
							}
							@Override
							public void afterCreate() {}
						});
					}
				};
				if (currLang == langs.get(i)){
					btn.textColor(TITLE_COLOR);
				}
				lanBtns[i] = btn;
				add(btn);
			}

		}

		@Override
		protected void layout() {
			title.setPos((width - title.width()) / 2, y + GAP);
			sep1.size(width, 1);
			sep1.y = title.bottom() + 2 * GAP;
			y = sep1.y + GAP + 1;

			int cols = PixelScene.landscape() ? COLS_L : COLS_P;
			float btnWidth = width / cols - 1;
			for (RedButton btn : lanBtns) {
				btn.setRect(x, y, btnWidth, BTN_HEIGHT);
				btn.setPos(x, y);
				x += btnWidth + GAP;
				if (x + btnWidth > width) {
					x = 0;
					y += BTN_HEIGHT + GAP;
				}
			}
			if (x > 0) {
				y += BTN_HEIGHT + GAP;
			}

		}
	}
}
