package net.povstalec.sgjourney.common.block_entities.stargate;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.povstalec.sgjourney.common.init.BlockEntityInit;
import net.povstalec.sgjourney.common.init.SoundInit;
import net.povstalec.sgjourney.common.stargate.Stargate;

public class TollanStargateEntity extends AbstractStargateEntity
{
	public TollanStargateEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityInit.TOLLAN_STARGATE.get(), pos, state, Stargate.Gen.GEN_2, 2);
	}
	
	@Override
    public void onLoad()
	{
        if(level.isClientSide())
        	return;
		pointOfOrigin = "sgjourney:tauri";
		symbols = "sgjourney:milky_way";
        
        super.onLoad();
    }

	@Override
	public SoundEvent getChevronEngageSound()
	{
		return SoundInit.TOLLAN_CHEVRON_ENGAGE.get();
	}

	@Override
	public SoundEvent getWormholeOpenSound()
	{
		return SoundInit.TOLLAN_WORMHOLE_OPEN.get();
	}

	@Override
	public SoundEvent getWormholeCloseSound()
	{
		return SoundInit.TOLLAN_WORMHOLE_CLOSE.get();
	}

	@Override
	public SoundEvent getFailSound()
	{
		return SoundInit.TOLLAN_DIAL_FAIL.get();
	}

	@Override
	public void playRotationSound(){}

	@Override
	public void stopRotationSound(){}
}
