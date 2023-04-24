package net.povstalec.sgjourney.cctweaked.peripherals;

import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraftforge.common.util.LazyOptional;
import net.povstalec.sgjourney.block_entities.BasicInterfaceEntity;
import net.povstalec.sgjourney.block_entities.EnergyBlockEntity;
import net.povstalec.sgjourney.block_entities.TransportRingsEntity;
import net.povstalec.sgjourney.block_entities.stargate.AbstractStargateEntity;

public class BasicPeripheralHolder
{
	BasicInterfaceEntity basicInterface;
	private BasicInterfacePeripheral basicInterfacePeripheral;
	private LazyOptional<IPeripheral> peripheral;
	
	public BasicPeripheralHolder(BasicInterfaceEntity basicInterface)
	{
		this.basicInterface = basicInterface;
	}
	
	public static BasicInterfacePeripheral createPeripheral(BasicInterfaceEntity basicInterface, EnergyBlockEntity energyBlockEntity)
	{
		if(energyBlockEntity instanceof AbstractStargateEntity stargate)
			return new BasicStargatePeripheral(basicInterface, stargate);
		else if(energyBlockEntity instanceof TransportRingsEntity transportRings)
			return new BasicRingsPeripheral(basicInterface, transportRings);

		return new BasicInterfacePeripheral(basicInterface);
	}
	
	public boolean resetInterface()
	{
		BasicInterfacePeripheral newPeripheral = createPeripheral(basicInterface, basicInterface.findEnergyBlockEntity());
		if (basicInterfacePeripheral != null && basicInterfacePeripheral.equals(newPeripheral))
		{
			// Peripheral is same as before, no changes needed.
			return false;
		}

		// Peripheral has changed, invalidate the capability and trigger a block update.
		basicInterfacePeripheral = newPeripheral;
		if (peripheral != null)
		{
			peripheral.invalidate();
			peripheral = LazyOptional.of(() -> newPeripheral);
		}
		return true;
	}
	
	public LazyOptional<IPeripheral> newPeripheral()
	{
		basicInterfacePeripheral = createPeripheral(basicInterface, basicInterface.energyBlockEntity);
		peripheral = LazyOptional.of(() -> basicInterfacePeripheral);
		
		if (peripheral == null)
		{
			basicInterfacePeripheral = createPeripheral(basicInterface, basicInterface.findEnergyBlockEntity());
			peripheral = LazyOptional.of(() -> basicInterfacePeripheral);
		}
		return peripheral;
	}
}
