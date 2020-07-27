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

package com.quasistellar.shatteredpixeldungeon.actors.hero;

import com.quasistellar.shatteredpixeldungeon.Assets;
import com.quasistellar.shatteredpixeldungeon.Badges;
import com.quasistellar.shatteredpixeldungeon.Challenges;
import com.quasistellar.shatteredpixeldungeon.Dungeon;
import com.quasistellar.shatteredpixeldungeon.items.BrokenSeal;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.quasistellar.shatteredpixeldungeon.items.weapon.melee.Gloves;
import com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.quasistellar.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife;
import com.quasistellar.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.quasistellar.shatteredpixeldungeon.items.armor.ClothArmor;
import com.quasistellar.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.quasistellar.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.quasistellar.shatteredpixeldungeon.items.bags.ScrollHolder;
import com.quasistellar.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.quasistellar.shatteredpixeldungeon.items.food.Food;
import com.quasistellar.shatteredpixeldungeon.items.food.SmallRation;
import com.quasistellar.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.quasistellar.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.quasistellar.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.quasistellar.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.quasistellar.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.quasistellar.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.quasistellar.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.quasistellar.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;

public enum HeroClass {

	WARRIOR( "warrior", HeroSubClass.BERSERKER, HeroSubClass.GLADIATOR ),
	MAGE( "mage", HeroSubClass.BATTLEMAGE, HeroSubClass.WARLOCK ),
	ROGUE( "rogue", HeroSubClass.ASSASSIN, HeroSubClass.FREERUNNER ),
	HUNTRESS( "huntress", HeroSubClass.SNIPER, HeroSubClass.WARDEN );

	private String title;
	private HeroSubClass[] subClasses;

	HeroClass( String title, HeroSubClass...subClasses ) {
		this.title = title;
		this.subClasses = subClasses;
	}

	public void initHero( com.quasistellar.shatteredpixeldungeon.actors.hero.Hero hero ) {

		hero.heroClass = this;

		initCommon( hero );

		switch (this) {
			case WARRIOR:
				initWarrior( hero );
				break;

			case MAGE:
				initMage( hero );
				break;

			case ROGUE:
				initRogue( hero );
				break;

			case HUNTRESS:
				initHuntress( hero );
				break;
		}

	}

	private static void initCommon( com.quasistellar.shatteredpixeldungeon.actors.hero.Hero hero ) {
		Item i = new ClothArmor().identify();
		if (!com.quasistellar.shatteredpixeldungeon.Challenges.isItemBlocked(i)) hero.belongings.armor = (ClothArmor)i;

		i = new Food();
		if (!com.quasistellar.shatteredpixeldungeon.Challenges.isItemBlocked(i)) i.collect();

		if (com.quasistellar.shatteredpixeldungeon.Dungeon.isChallenged(Challenges.NO_FOOD)){
			new SmallRation().collect();
		}

		new ScrollOfIdentify().identify();

	}

	public com.quasistellar.shatteredpixeldungeon.Badges.Badge masteryBadge() {
		switch (this) {
			case WARRIOR:
				return com.quasistellar.shatteredpixeldungeon.Badges.Badge.MASTERY_WARRIOR;
			case MAGE:
				return com.quasistellar.shatteredpixeldungeon.Badges.Badge.MASTERY_MAGE;
			case ROGUE:
				return com.quasistellar.shatteredpixeldungeon.Badges.Badge.MASTERY_ROGUE;
			case HUNTRESS:
				return com.quasistellar.shatteredpixeldungeon.Badges.Badge.MASTERY_HUNTRESS;
		}
		return null;
	}

	private static void initWarrior( com.quasistellar.shatteredpixeldungeon.actors.hero.Hero hero ) {
		(hero.belongings.weapon = new WornShortsword()).identify();
		com.quasistellar.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone stones = new ThrowingStone();
		stones.quantity(3).collect();
		com.quasistellar.shatteredpixeldungeon.Dungeon.quickslot.setSlot(0, stones);

		if (hero.belongings.armor != null){
			hero.belongings.armor.affixSeal(new BrokenSeal());
		}

		new PotionBandolier().collect();
		com.quasistellar.shatteredpixeldungeon.Dungeon.LimitedDrops.POTION_BANDOLIER.drop();

		new PotionOfHealing().identify();
		new ScrollOfRage().identify();
	}

	private static void initMage( com.quasistellar.shatteredpixeldungeon.actors.hero.Hero hero ) {
		com.quasistellar.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff;

		staff = new MagesStaff(new WandOfMagicMissile());

		(hero.belongings.weapon = staff).identify();
		hero.belongings.weapon.activate(hero);

		com.quasistellar.shatteredpixeldungeon.Dungeon.quickslot.setSlot(0, staff);

		new ScrollHolder().collect();
		com.quasistellar.shatteredpixeldungeon.Dungeon.LimitedDrops.SCROLL_HOLDER.drop();

		new ScrollOfUpgrade().identify();
		new PotionOfLiquidFlame().identify();
	}

	private static void initRogue( com.quasistellar.shatteredpixeldungeon.actors.hero.Hero hero ) {
		(hero.belongings.weapon = new Dagger()).identify();

		CloakOfShadows cloak = new CloakOfShadows();
		(hero.belongings.misc1 = cloak).identify();
		hero.belongings.misc1.activate( hero );

		com.quasistellar.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife knives = new ThrowingKnife();
		knives.quantity(3).collect();

		com.quasistellar.shatteredpixeldungeon.Dungeon.quickslot.setSlot(0, cloak);
		com.quasistellar.shatteredpixeldungeon.Dungeon.quickslot.setSlot(1, knives);

		new VelvetPouch().collect();
		com.quasistellar.shatteredpixeldungeon.Dungeon.LimitedDrops.VELVET_POUCH.drop();

		new ScrollOfMagicMapping().identify();
		new PotionOfInvisibility().identify();
	}

	private static void initHuntress( Hero hero ) {

		(hero.belongings.weapon = new Gloves()).identify();
		SpiritBow bow = new SpiritBow();
		bow.identify().collect();

		com.quasistellar.shatteredpixeldungeon.Dungeon.quickslot.setSlot(0, bow);

		new VelvetPouch().collect();
		Dungeon.LimitedDrops.VELVET_POUCH.drop();

		new PotionOfMindVision().identify();
		new ScrollOfLullaby().identify();
	}

	public String title() {
		return Messages.get(HeroClass.class, title);
	}

	public HeroSubClass[] subClasses() {
		return subClasses;
	}

	public String spritesheet() {
		switch (this) {
			case WARRIOR: default:
				return com.quasistellar.shatteredpixeldungeon.Assets.Sprites.WARRIOR;
			case MAGE:
				return com.quasistellar.shatteredpixeldungeon.Assets.Sprites.MAGE;
			case ROGUE:
				return com.quasistellar.shatteredpixeldungeon.Assets.Sprites.ROGUE;
			case HUNTRESS:
				return com.quasistellar.shatteredpixeldungeon.Assets.Sprites.HUNTRESS;
		}
	}

	public String splashArt(){
		switch (this) {
			case WARRIOR: default:
				return com.quasistellar.shatteredpixeldungeon.Assets.Splashes.WARRIOR;
			case MAGE:
				return com.quasistellar.shatteredpixeldungeon.Assets.Splashes.MAGE;
			case ROGUE:
				return com.quasistellar.shatteredpixeldungeon.Assets.Splashes.ROGUE;
			case HUNTRESS:
				return Assets.Splashes.HUNTRESS;
		}
	}
	
	public String[] perks() {
		switch (this) {
			case WARRIOR: default:
				return new String[]{
						Messages.get(HeroClass.class, "warrior_perk1"),
						Messages.get(HeroClass.class, "warrior_perk2"),
						Messages.get(HeroClass.class, "warrior_perk3"),
						Messages.get(HeroClass.class, "warrior_perk4"),
						Messages.get(HeroClass.class, "warrior_perk5"),
				};
			case MAGE:
				return new String[]{
						Messages.get(HeroClass.class, "mage_perk1"),
						Messages.get(HeroClass.class, "mage_perk2"),
						Messages.get(HeroClass.class, "mage_perk3"),
						Messages.get(HeroClass.class, "mage_perk4"),
						Messages.get(HeroClass.class, "mage_perk5"),
				};
			case ROGUE:
				return new String[]{
						Messages.get(HeroClass.class, "rogue_perk1"),
						Messages.get(HeroClass.class, "rogue_perk2"),
						Messages.get(HeroClass.class, "rogue_perk3"),
						Messages.get(HeroClass.class, "rogue_perk4"),
						Messages.get(HeroClass.class, "rogue_perk5"),
				};
			case HUNTRESS:
				return new String[]{
						Messages.get(HeroClass.class, "huntress_perk1"),
						Messages.get(HeroClass.class, "huntress_perk2"),
						Messages.get(HeroClass.class, "huntress_perk3"),
						Messages.get(HeroClass.class, "huntress_perk4"),
						Messages.get(HeroClass.class, "huntress_perk5"),
				};
		}
	}
	
	public boolean isUnlocked(){
		//always unlock on debug builds
		if (DeviceCompat.isDebug()) return true;
		
		switch (this){
			case WARRIOR: default:
				return true;
			case MAGE:
				return com.quasistellar.shatteredpixeldungeon.Badges.isUnlocked(com.quasistellar.shatteredpixeldungeon.Badges.Badge.UNLOCK_MAGE);
			case ROGUE:
				return com.quasistellar.shatteredpixeldungeon.Badges.isUnlocked(com.quasistellar.shatteredpixeldungeon.Badges.Badge.UNLOCK_ROGUE);
			case HUNTRESS:
				return com.quasistellar.shatteredpixeldungeon.Badges.isUnlocked(Badges.Badge.UNLOCK_HUNTRESS);
		}
	}
	
	public String unlockMsg() {
		switch (this){
			case WARRIOR: default:
				return "";
			case MAGE:
				return Messages.get(HeroClass.class, "mage_unlock");
			case ROGUE:
				return Messages.get(HeroClass.class, "rogue_unlock");
			case HUNTRESS:
				return Messages.get(HeroClass.class, "huntress_unlock");
		}
	}

	private static final String CLASS	= "class";
	
	public void storeInBundle( Bundle bundle ) {
		bundle.put( CLASS, toString() );
	}
	
	public static HeroClass restoreInBundle( Bundle bundle ) {
		String value = bundle.getString( CLASS );
		return value.length() > 0 ? valueOf( value ) : ROGUE;
	}
}
