package com.serilum.spiritcompanions.data;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class SpiritState {
	public Mob returningToHusk = null;
	public Vec3 lastOwnerPosition = Vec3.ZERO;
	public Vec3 landingPosition = null;
	public Vec3 flightMovement = Vec3.ZERO;
	public int ownerStillTicks = 0;
	public int ownerMovingTicks = 0;
	public int landingDelayTicks = 40;
	public int takeOffDelayTicks = 10;
	public int flightTicksLeft = 0;
	public int orbitStartTicks = 0;
	public long releaseRequestedTick = -1000;
	public boolean isFlyingAround = false;
	public boolean isWalking = false;
}
