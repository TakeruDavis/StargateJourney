package net.povstalec.sgjourney.common.block_entities.stargate;

import com.google.common.collect.Maps;
import net.povstalec.sgjourney.common.config.CommonStargateConfig;
import net.povstalec.sgjourney.common.stargate.StargatePart;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.povstalec.sgjourney.StargateJourney;
import net.povstalec.sgjourney.common.config.StargateJourneyConfig;
import net.povstalec.sgjourney.common.init.BlockEntityInit;
import net.povstalec.sgjourney.common.init.PacketHandlerInit;
import net.povstalec.sgjourney.common.init.SoundInit;
import net.povstalec.sgjourney.common.misc.ArrayHelper;
import net.povstalec.sgjourney.common.packets.ClientboundUniverseStargateUpdatePacket;
import net.povstalec.sgjourney.common.stargate.Addressing;
import net.povstalec.sgjourney.common.stargate.Stargate;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class UniverseStargateEntity extends AbstractStargateEntity
{
	private static final double angle = (double) 360 / 54;

	public static final int WAIT_TICKS = 20;
	public int animationTicks = 0;
	
	protected static final String UNIVERSAL = StargateJourney.MODID + ":universal";
	private static final String POINT_OF_ORIGIN = UNIVERSAL;
	private static final String SYMBOLS = UNIVERSAL;

	public int oldRotation = 0;
	public int rotation = 0;
	public Integer currentSymbol = null;
	public int[] addressBuffer = new int[0];
	public int symbolBuffer = 0;

	public UniverseStargateEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityInit.UNIVERSE_STARGATE.get(), pos, state, Stargate.Gen.GEN_1);
	}
	
	@Override
	public void onLoad()
	{
		if(level.isClientSide())
			return;
		
		setPointOfOrigin(POINT_OF_ORIGIN);
        setSymbols(SYMBOLS);
        
        super.onLoad();
	}
	
	@Override
	public void load(CompoundTag nbt)
	{
        super.load(nbt);
        
        rotation = nbt.getInt("Rotation");
        oldRotation = rotation;
        addressBuffer = nbt.getIntArray("AddressBuffer");
        symbolBuffer = nbt.getInt("SymbolBuffer");
    }
	
	@Override
	protected void saveAdditional(@NotNull CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		
		nbt.putInt("Rotation", rotation);
		nbt.putIntArray("AddressBuffer", addressBuffer);
		nbt.putInt("SymbolBuffer", symbolBuffer);
	}
	
	public SoundEvent chevronEngageSound()
	{
		return SoundInit.UNIVERSE_CHEVRON_ENGAGE.get();
	}
	
	public SoundEvent failSound()
	{
		return SoundInit.UNIVERSE_DIAL_FAIL.get();
	}

	public int getRotation()
	{
		return rotation;
	}
	
	public void setRotation(int rotation)
	{
		this.rotation = rotation;
	}
	
	@Override
	public Stargate.Feedback engageSymbol(int symbol)
	{
		if(level.isClientSide())
			return Stargate.Feedback.NONE;
		
		if(Addressing.addressContainsSymbol(getAddress(), symbol))
			return Stargate.Feedback.SYMBOL_ENCODED;
		
		if(symbol > 35)
			return Stargate.Feedback.SYMBOL_OUT_OF_BOUNDS;
		
		if(symbol == 0)
		{
			if(isConnected())
				return disconnectStargate(Stargate.Feedback.CONNECTION_ENDED_BY_DISCONNECT);
			else if(!isConnected() && addressBuffer.length == 0)
				return Stargate.Feedback.INCOPLETE_ADDRESS;
		}
		
		addressBuffer = ArrayHelper.growIntArray(addressBuffer, symbol);
		synchronizeWithClient(level);
		return Stargate.Feedback.SYMBOL_ENCODED;
	}
	
	@Override
	protected Stargate.Feedback encodeChevron(int symbol)
	{
		symbolBuffer++;
		animationTicks++;
		return super.encodeChevron(symbol);
	}

	@Nullable
	public Integer getCurrentSymbol()
	{
		int position = (int) Math.floor(this.rotation / angle);

		int positionInGroup = position % 6;
		if (positionInGroup == 0 || positionInGroup == 5)
			return null;

		positionInGroup--;
		int group = (int) Math.floor(position / 6.0);

		int symbol = group * 4 + positionInGroup;

		if (isCurrentSymbol(symbol, angle / 2)) { //test bounds
			return symbol;
		}

		return null;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, UniverseStargateEntity stargate)
	{
		stargate.rotate();

		AbstractStargateEntity.tick(level, pos, state, stargate);
	}

	private void rotate()
	{
		if(!isConnected() && addressBuffer.length > symbolBuffer)
		{
			if(animationTicks <= 0)
				rotateToSymbol(addressBuffer[symbolBuffer]);
			else if(animationTicks >= WAIT_TICKS)
				animationTicks = 0;
			else if(animationTicks > 0)
				animationTicks++;
		}
		else if(!isConnected() && addressBuffer.length == 0)
		{
			rotateToDefault();
		}
		else if(!level.isClientSide())
			PacketHandlerInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)),
					new ClientboundUniverseStargateUpdatePacket(worldPosition, symbolBuffer, addressBuffer, animationTicks, rotation, oldRotation));

		setChanged();
	}
	
	public void rotate(boolean clockwise)
	{
		oldRotation = rotation;
		if(clockwise)
			rotation -= 2;
		else
			rotation += 2;

		if(rotation >= 360)
		{
			rotation -= 360;
			oldRotation -= 360;
		}
		else if(rotation < 0)
		{
			rotation += 360;
			oldRotation += 360;
		}
		syncRotation();
		setChanged();
	}

	public boolean isCurrentSymbol(int desiredSymbol) {
		return isCurrentSymbol(desiredSymbol, 1.0);
	}

	public boolean isCurrentSymbol(int desiredSymbol, double tolerance)
	{
		int whole = desiredSymbol / 4;
		int leftover = desiredSymbol % 4;
		
		double desiredPosition = 3 * (angle / 2) + whole * 40 + (angle * leftover);
		
		double position = rotation;
		double lowerBound = desiredPosition - tolerance;
		double upperBound = desiredPosition + tolerance;
		
		if(position > lowerBound && position < upperBound)
			return true;
		return false;
	}
	
	public float getRotation(float partialTick)
	{
		return StargateJourneyConfig.disable_smooth_animations.get() ?
				(float) getRotation() : Mth.lerp(partialTick, this.oldRotation, this.rotation);
	}
	
	private void rotateToSymbol(int desiredSymbol)
	{
		if(isCurrentSymbol(desiredSymbol))
		{
			if(!level.isClientSide())
				synchronizeWithClient(level);
			
			if(isCurrentSymbol(0))
				this.lockPrimaryChevron();
			else
				this.encodeChevron(desiredSymbol);
			
			if(!level.isClientSide())
				synchronizeWithClient(level);
		}
		else
			rotate(getBestRotationDirection(desiredSymbol));
	}
	
	private void rotateToDefault()
	{
		if(rotation == 0)
		{
			if(!level.isClientSide())
				synchronizeWithClient(level);
		}
		else {
			rotate(getBestRotationDirection(0.0D, rotation));
		}
	}
	
	private boolean getBestRotationDirection(int desiredSymbol)
	{
		int whole = desiredSymbol / 4;
		int leftover = desiredSymbol % 4;
		
		double desiredPosition = 3 * (angle / 2) + whole * 40 + angle * leftover;


		return getBestRotationDirection(desiredPosition, rotation);
	}

	private static boolean getBestRotationDirection(double desiredRotation, double rotation)
	{
		
		double difference = desiredRotation - rotation;
		
		if(difference >= 180.0D)
			rotation =+ 360.0D;
		else if(difference <= -180.0D)
			rotation =- 360.0D;
		
		double lowerBound = (double) (desiredRotation - 1);
		
		if(rotation > lowerBound)
			return true;
		else
			return false;
	}
	
	@Override
	public Stargate.Feedback resetStargate(Stargate.Feedback feedback)
	{
		symbolBuffer = 0;
		addressBuffer = new int[0];
		return super.resetStargate(feedback);
	}
	private void synchronizeWithClient(Level level)
	{
		if(level.isClientSide())
			return;
		PacketHandlerInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(this.worldPosition)), new ClientboundUniverseStargateUpdatePacket(this.worldPosition, symbolBuffer, addressBuffer, animationTicks, rotation, oldRotation));
	}

	private void syncRotation()
	{
		this.oldRotation = this.rotation;
		if(!this.level.isClientSide())
			synchronizeWithClient(this.level);
	}
}
