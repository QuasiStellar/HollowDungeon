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

package com.quasistellar.hollowdungeon.windows;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDSettings;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.InterlevelScene;
import com.quasistellar.hollowdungeon.scenes.IntroScene;
import com.quasistellar.hollowdungeon.scenes.PixelScene;
import com.quasistellar.hollowdungeon.sprites.FetidRatSprite;
import com.quasistellar.hollowdungeon.sprites.GnollTricksterSprite;
import com.quasistellar.hollowdungeon.sprites.GreatCrabSprite;
import com.quasistellar.hollowdungeon.ui.ActionIndicator;
import com.quasistellar.hollowdungeon.ui.CheckBox;
import com.quasistellar.hollowdungeon.ui.OptionSlider;
import com.quasistellar.hollowdungeon.ui.RedButton;
import com.quasistellar.hollowdungeon.ui.RenderedTextBlock;
import com.quasistellar.hollowdungeon.ui.Window;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.noosa.Game;

public class WndRealtime extends Window {

    private static final int WIDTH		= 120;
    private static final int BTN_HEIGHT	= 20;
    private static final float GAP		= 2;
    private static final int HEIGHT         = 138;
    private static final int SLIDER_HEIGHT	= 24;
    private static final int GAP_TINY 		= 2;
    private static final int GAP_SML 		= 6;
    private static final int GAP_LRG 		= 18;

    public WndRealtime() {

        super();

        RenderedTextBlock skillDesc = PixelScene.renderTextBlock(Messages.get(this, "skill_desc"), 7);
        skillDesc.setPos(width/2f, GAP);
        add(skillDesc);

        CheckBox skillCheck = new CheckBox(Messages.get(this, "skill")){
            @Override
            protected void onClick() {
                super.onClick();
                HDSettings.skills(checked());
            }
        };

        skillCheck.setRect(0, skillDesc.bottom() + GAP, WIDTH, BTN_HEIGHT);
        skillCheck.checked(HDSettings.skills());
        add(skillCheck);

        RenderedTextBlock realtimeDesc = PixelScene.renderTextBlock(Messages.get(this, "realtime_desc"), 7);
        realtimeDesc.setPos(width/2f, skillCheck.bottom() + GAP);
        add(realtimeDesc);

        CheckBox realtimeCheck = new CheckBox(Messages.get(this, "realtime")){
            @Override
            protected void onClick() {
                super.onClick();
                HDSettings.realtime(checked());
            }
        };

        realtimeCheck.setRect(0, realtimeDesc.bottom() + GAP, WIDTH, BTN_HEIGHT);
        realtimeCheck.checked(HDSettings.realtime());
        add(realtimeCheck);

        OptionSlider delaySlider = new OptionSlider(Messages.get(this, "delay"), "0.5", "5", 1, 10) {
            @Override
            protected void onChange() {
                HDSettings.delay(getSelectedValue());
            }
        };
        delaySlider.setSelectedValue(HDSettings.delay());
        delaySlider.setRect(0, realtimeCheck.bottom() + GAP_TINY, WIDTH, SLIDER_HEIGHT);
        add(delaySlider);

        RenderedTextBlock skipDesc = PixelScene.renderTextBlock(Messages.get(this, "skip"), 7);
        skipDesc.setPos(width/2f, delaySlider.top() + delaySlider.height() + GAP);
        add(skipDesc);

        RedButton btnStart = new RedButton( Messages.get(this, "start") ) {
            @Override
            protected void onClick() {
                Dungeon.hero = null;
                ActionIndicator.action = null;
                InterlevelScene.mode = InterlevelScene.Mode.DESCEND;

//                if (HDSettings.intro()) {
//                    HDSettings.intro( false );
//                    Game.switchScene( IntroScene.class );
//                } else {
                    Game.switchScene( InterlevelScene.class );
//                }
            }
        };
        btnStart.setRect( 0, skipDesc.bottom() + GAP, WIDTH, BTN_HEIGHT );
        add( btnStart );

        resize(WIDTH, (int)btnStart.bottom());
    }
}