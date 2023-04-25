package net.povstalec.sgjourney.cctweaked.peripherals;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import net.povstalec.sgjourney.block_entities.BasicInterfaceEntity;
import net.povstalec.sgjourney.block_entities.TransportRingsEntity;
import net.povstalec.sgjourney.cctweaked.methods.InterfaceMethod;

import java.util.HashMap;

public class BasicRingsPeripheral extends BasicInterfacePeripheral implements IDynamicPeripheral
{
	protected TransportRingsEntity rings;
	private final HashMap<String, InterfaceMethod<TransportRingsEntity>> methods = new HashMap<String,InterfaceMethod<TransportRingsEntity>>();

	public BasicRingsPeripheral(BasicInterfaceEntity basicInterface, TransportRingsEntity rings)
	{
		super(basicInterface);
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
	public final float getProgress() {
		return rings.progress;
	}

	@LuaFunction
	public void activate(ILuaContext context) throws LuaException
	{
		context.executeMainThreadTask(() ->
		{
			boolean result = rings.activate();

			if (!result)
				throw new LuaException("No platforms found");

			return null;
		});
	}

}
