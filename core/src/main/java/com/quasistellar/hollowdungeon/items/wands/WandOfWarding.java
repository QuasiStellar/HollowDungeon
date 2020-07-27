package com.quasistellar.hollowdungeon.items.wands;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Corruption;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.NPC;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.sprites.WardSprite;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.MagicMissile;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfWarding extends com.quasistellar.hollowdungeon.items.wands.Wand {

	{
		image = ItemSpriteSheet.WAND_WARDING;
	}

	@Override
	protected int collisionProperties(int target) {
		if (Dungeon.level.heroFOV[target])  return Ballistica.STOP_TARGET;
		else                                return Ballistica.PROJECTILE;
	}

	private boolean wardAvailable = true;
	
	@Override
	public boolean tryToZap(Hero owner, int target) {
		
		int currentWardEnergy = 0;
		for (com.quasistellar.hollowdungeon.actors.Char ch : Actor.chars()){
			if (ch instanceof Ward){
				currentWardEnergy += ((Ward) ch).tier;
			}
		}
		
		int maxWardEnergy = 0;
		for (Buff buff : com.quasistellar.hollowdungeon.items.Item.curUser.buffs()){
			if (buff instanceof Charger){
				if (((Charger) buff).wand() instanceof WandOfWarding){
					maxWardEnergy += 2 + ((Charger) buff).wand().level();
				}
			}
		}
		
		wardAvailable = (currentWardEnergy < maxWardEnergy);
		
		com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar(target);
		if (ch instanceof Ward){
			if (!wardAvailable && ((Ward) ch).tier <= 3){
				GLog.w( Messages.get(this, "no_more_wards"));
				return false;
			}
		} else {
			if ((currentWardEnergy + 1) > maxWardEnergy){
				GLog.w( Messages.get(this, "no_more_wards"));
				return false;
			}
		}
		
		return super.tryToZap(owner, target);
	}
	
	@Override
	protected void onZap(Ballistica bolt) {

		int target = bolt.collisionPos;
		com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar(target);
		if (ch != null && !(ch instanceof Ward)){
			if (bolt.dist > 1) target = bolt.path.get(bolt.dist-1);

			ch = Actor.findChar(target);
			if (ch != null && !(ch instanceof Ward)){
				GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(bolt.collisionPos);
				return;
			}
		}

		if (!Dungeon.level.passable[target]){
			GLog.w( Messages.get(this, "bad_location"));
			Dungeon.level.pressCell(target);
			
		} else if (ch != null){
			if (ch instanceof Ward){
				if (wardAvailable) {
					((Ward) ch).upgrade( buffedLvl() );
				} else {
					((Ward) ch).wandHeal( buffedLvl() );
				}
				ch.sprite.emitter().burst(MagicMissile.WardParticle.UP, ((Ward) ch).tier);
			} else {
				com.quasistellar.hollowdungeon.utils.GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(target);
			}
			
		} else {
			Ward ward = new Ward();
			ward.pos = target;
			ward.wandLevel = buffedLvl();
			GameScene.add(ward, 1f);
			Dungeon.level.occupyCell(ward);
			ward.sprite.emitter().burst(MagicMissile.WardParticle.UP, ward.tier);
			Dungeon.level.pressCell(target);

		}
	}

	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		com.quasistellar.hollowdungeon.effects.MagicMissile m = MagicMissile.boltFromChar(com.quasistellar.hollowdungeon.items.Item.curUser.sprite.parent,
				MagicMissile.WARD,
				Item.curUser.sprite,
				bolt.collisionPos,
				callback);
		
		if (bolt.dist > 10){
			m.setSpeed(bolt.dist*20);
		}
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void onHit(com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff staff, com.quasistellar.hollowdungeon.actors.Char attacker, com.quasistellar.hollowdungeon.actors.Char defender, int damage) {

		int level = Math.max( 0, staff.buffedLvl() );

		// lvl 0 - 20%
		// lvl 1 - 33%
		// lvl 2 - 43%
		if (Random.Int( level + 5 ) >= 4) {
			for (com.quasistellar.hollowdungeon.actors.Char ch : com.quasistellar.hollowdungeon.actors.Actor.chars()){
				if (ch instanceof Ward){
					((Ward) ch).wandHeal(staff.buffedLvl());
					ch.sprite.emitter().burst(com.quasistellar.hollowdungeon.effects.MagicMissile.WardParticle.UP, ((Ward) ch).tier);
				}
			}
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( 0x8822FF );
		particle.am = 0.3f;
		particle.setLifespan(3f);
		particle.speed.polar(Random.Float(PointF.PI2), 0.3f);
		particle.setSize( 1f, 2f);
		particle.radiateXY(2.5f);
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", level()+2);
		else
			return Messages.get(this, "stats_desc", 2);
	}

	public static class Ward extends NPC {

		public int tier = 1;
		private int wandLevel = 1;

		public int totalZaps = 0;

		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.WardSprite.class;

			alignment = Char.Alignment.ALLY;

			properties.add(Char.Property.IMMOVABLE);
			properties.add(Char.Property.INORGANIC);

			viewDistance = 4;
			state = WANDERING;
		}

		@Override
		protected boolean act() {
			throwItem();
			return super.act();
		}

		@Override
		public String name() {
			return Messages.get(this, "name_" + tier );
		}

		public void upgrade(int wandLevel ){
			if (this.wandLevel < wandLevel){
				this.wandLevel = wandLevel;
			}

			switch (tier){
				case 1: case 2: default:
					break; //do nothing
				case 3:
					HT = 30;
					HP = 10 + (5-totalZaps)*4;
					break;
				case 4:
					HT = 48;
					HP += 18;
					break;
				case 5:
					HT = 70;
					HP += 22;
					break;
				case 6:
					wandHeal(wandLevel);
					break;
			}

			if (tier < 6){
				tier++;
				viewDistance++;
				if (sprite != null){
					((com.quasistellar.hollowdungeon.sprites.WardSprite)sprite).updateTier(tier);
					sprite.place(pos);
				}
				GameScene.updateFog(pos, viewDistance+1);
			}

		}

		private void wandHeal( int wandLevel ){
			if (this.wandLevel < wandLevel){
				this.wandLevel = wandLevel;
			}

			int heal;
			switch(tier){
				default:
					return;
				case 4:
					heal = 8;
					HP = Math.min(HT, HP+9);
					break;
				case 5:
					heal = 10;
					HP = Math.min(HT, HP+10);
					break;
				case 6:
					heal = 15;
					HP = Math.min(HT, HP+15);
					break;
			}

			HP = Math.min(HT, HP+heal);
			if (sprite != null) sprite.showStatus(CharSprite.POSITIVE, Integer.toString(heal));

		}

		@Override
		protected float attackDelay() {
			if (tier > 3){
				return 1f;
			} else {
				return 2f;
			}
		}

		@Override
		protected boolean canAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
			return new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
		}

		@Override
		protected boolean doAttack(com.quasistellar.hollowdungeon.actors.Char enemy) {
			boolean visible = fieldOfView[pos] || fieldOfView[enemy.pos];
			if (visible) {
				sprite.zap( enemy.pos );
			} else {
				zap();
			}

			return !visible;
		}

		private void zap() {
			spend( 1f );

			//always hits
			int dmg = Random.NormalIntRange( 2 + wandLevel, 8 + 4*wandLevel );
			enemy.damage( dmg, WandOfWarding.class );
			if (enemy.isAlive()){
				Wand.processSoulMark(enemy, wandLevel, 1);
			}

			if (!enemy.isAlive() && enemy == Dungeon.hero) {
				Dungeon.fail( getClass() );
			}

			totalZaps++;
			switch(tier){
				case 1: case 2: case 3: default:
					if (totalZaps >= (2*tier-1)){
						die(this);
					}
					break;
				case 4:
					damage(5, this);
					break;
				case 5:
					damage(6, this);
					break;
				case 6:
					damage(7, this);
					break;
			}
		}

		public void onZapComplete() {
			zap();
			next();
		}

		@Override
		protected boolean getCloser(int target) {
			return false;
		}

		@Override
		protected boolean getFurther(int target) {
			return false;
		}

		@Override
		public com.quasistellar.hollowdungeon.sprites.CharSprite sprite() {
			com.quasistellar.hollowdungeon.sprites.WardSprite sprite = (com.quasistellar.hollowdungeon.sprites.WardSprite) super.sprite();
			sprite.linkVisuals(this);
			return sprite;
		}

		@Override
		public void updateSpriteState() {
			super.updateSpriteState();
			((WardSprite)sprite).updateTier(tier);
			sprite.place(pos);
		}
		
		@Override
		public void destroy() {
			super.destroy();
			Dungeon.observe();
			GameScene.updateFog(pos, viewDistance+1);
		}
		
		@Override
		public boolean canInteract(com.quasistellar.hollowdungeon.actors.Char c) {
			return true;
		}

		@Override
		public boolean interact( com.quasistellar.hollowdungeon.actors.Char c ) {
			if (c != com.quasistellar.hollowdungeon.Dungeon.hero){
				return true;
			}
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					com.quasistellar.hollowdungeon.scenes.GameScene.show(new WndOptions( Messages.get(Ward.this, "dismiss_title"),
							Messages.get(Ward.this, "dismiss_body"),
							Messages.get(Ward.this, "dismiss_confirm"),
							Messages.get(Ward.this, "dismiss_cancel") ){
						@Override
						protected void onSelect(int index) {
							if (index == 0){
								die(null);
							}
						}
					});
				}
			});
			return true;
		}

		@Override
		public String description() {
			return Messages.get(this, "desc_" + tier, 2+wandLevel, 8 + 4*wandLevel, tier );
		}
		
		{
			immunities.add( Corruption.class );
		}

		private static final String TIER = "tier";
		private static final String WAND_LEVEL = "wand_level";
		private static final String TOTAL_ZAPS = "total_zaps";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TIER, tier);
			bundle.put(WAND_LEVEL, wandLevel);
			bundle.put(TOTAL_ZAPS, totalZaps);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			tier = bundle.getInt(TIER);
			viewDistance = 3 + tier;
			wandLevel = bundle.getInt(WAND_LEVEL);
			totalZaps = bundle.getInt(TOTAL_ZAPS);
		}
		
		{
			properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.IMMOVABLE);
		}
	}
}
