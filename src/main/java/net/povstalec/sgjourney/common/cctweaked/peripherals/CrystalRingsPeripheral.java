package net.povstalec.sgjourney.cctweaked.peripherals;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import net.povstalec.sgjourney.block_entities.CrystalInterfaceEntity;
import net.povstalec.sgjourney.block_entities.TransportRingsEntity;
import net.povstalec.sgjourney.cctweaked.methods.InterfaceMethod;

import java.util.HashMap;
import java.util.Optional;

public class CrystalRingsPeripheral extends CrystalInterfacePeripheral implements IDynamicPeripheral
{
	protected TransportRingsEntity rings;
	private HashMap<String, InterfaceMethod<TransportRingsEntity>> methods = new HashMap<String,InterfaceMethod<TransportRingsEntity>>();

	public CrystalRingsPeripheral(CrystalInterfaceEntity crystalInterface, TransportRingsEntity rings)
	{
		super(crystalInterface);
		this.crystalInterface = crystalInterface;
		this.rings = rings;
	}

	@Override
	public String[] getMethodNames()
	{
		return methods.keySet().toArray(new String[0]);
	}

	@Override
	public MethodResult callMethod(IComputerAccess computer, ILuaContext context, int method, IArguments arguments)
			throws LuaException
	{
		String methodName = getMethodNames()[method];
		
		return methods.get(methodName).use(context, this.rings, arguments);
	}
	
	//============================================================================================
	//*****************************************CC: Tweaked****************************************
	//============================================================================================

	@LuaFunction
	public final boolean isActivated()
	{
		return rings.isActivated();
	}

	@LuaFunction
	public final boolean isSender() {
		return rings.isSender;
	}

	@LuaFunction
	public final long getRingsEnergy()
	{
		return rings.getEnergyStored();
	}

	@LuaFunction
	public final float getProgress()
	{
		return rings.progress;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@LuaFunction
	public void activate(ILuaContext context, Optional<Integer> platform) throws LuaException
	{
		context.executeMainThreadTask(() ->
		{
			if (platform.isPresent())
			{
				int platformNumber = platform.get();
				if (platformNumber < 1 || platformNumber > 6)
					throw new LuaException("Platform out of bounds <1, 6>");

				boolean result = crystalInterface.ringUpNthPlatform(platformNumber);

				if (!result)
					throw new LuaException("Platform not found");
			}
			else
			{
				boolean result = rings.activate();

				if (!result)
					throw new LuaException("No platforms found");
			}

			return null;
		});
	}
}
