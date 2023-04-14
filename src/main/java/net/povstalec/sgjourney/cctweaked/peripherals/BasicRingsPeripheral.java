package net.povstalec.sgjourney.cctweaked.peripherals;

import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IDynamicPeripheral;
import net.povstalec.sgjourney.block_entities.BasicInterfaceEntity;
import net.povstalec.sgjourney.block_entities.TransportRingsEntity;
import net.povstalec.sgjourney.cctweaked.methods.InterfaceMethod;
import net.povstalec.sgjourney.cctweaked.methods.TransportRingsMethods.*;

import java.util.HashMap;

public class BasicRingsPeripheral extends BasicInterfacePeripheral implements IDynamicPeripheral
{
	protected TransportRingsEntity rings;
	private final HashMap<String, InterfaceMethod<TransportRingsEntity>> methods = new HashMap<String,InterfaceMethod<TransportRingsEntity>>();

	public BasicRingsPeripheral(BasicInterfaceEntity basicInterface, TransportRingsEntity rings)
	{
		super(basicInterface);
		this.rings = rings;
		
		registerTransportRingsMethods();
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
	public final boolean isRingPlatformActivated()
	{
		return rings.isActivated();
	}

	@LuaFunction
	public final boolean isSender() { return rings.isSender; }

	@LuaFunction
	public final long getRingsEnergy()
	{
		return rings.getEnergyStored();
	}
	
	@LuaFunction
	public final int getChevronsEngaged() { return rings.progress; }

	@SuppressWarnings("unchecked")
	private <Rings extends TransportRingsEntity> void registerTransportRingsMethod(InterfaceMethod<Rings> function)
	{
		methods.put(function.getName(), (InterfaceMethod<TransportRingsEntity>) function);
	}
	
	public void registerTransportRingsMethods()
	{
		registerTransportRingsMethod(new Activate());
	}
}
