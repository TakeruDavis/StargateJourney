package net.povstalec.sgjourney.block_entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.povstalec.sgjourney.data.RingsNetwork;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.povstalec.sgjourney.capabilities.CCTweakedCapabilities;
import net.povstalec.sgjourney.cctweaked.peripherals.CrystalPeripheralHolder;
import net.povstalec.sgjourney.data.Universe;
import net.povstalec.sgjourney.init.BlockEntityInit;
import net.povstalec.sgjourney.init.ItemInit;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CrystalInterfaceEntity extends BasicInterfaceEntity
{
	private final ItemStackHandler itemHandler = createHandler();
	private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
	CrystalPeripheralHolder peripheralHolder;

	private int inputSignal = 0;
	
	public CrystalInterfaceEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityInit.CRYSTAL_INTERFACE.get(), pos, state);
		
		if(ModList.get().isLoaded("computercraft"))
			peripheralHolder = new CrystalPeripheralHolder(this);
	}
	
	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		itemHandler.deserializeNBT(nbt.getCompound("Inventory"));
	}
	
	@Override
	protected void saveAdditional(@NotNull CompoundTag nbt)
	{
		nbt.put("Inventory", itemHandler.serializeNBT());
		super.saveAdditional(nbt);
	}
	
	//============================================================================================
	//****************************************Capabilities****************************************
	//============================================================================================
	
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
	{
		if(ModList.get().isLoaded("computercraft") && cap == CCTweakedCapabilities.CAPABILITY_PERIPHERAL)
			return peripheralHolder.newPeripheral().cast();
		
		else if(cap == ForgeCapabilities.ITEM_HANDLER)
			return handler.cast();
		
		return super.getCapability(cap, side);
	}
	
	@Override
	public boolean updateInterface()
	{
		if(peripheralHolder != null)
			return peripheralHolder.resetInterface();
		
		return true;
	}

	public void setInputSignal(int inputSignal) {
		this.inputSignal = inputSignal;
	}

	public int getInputSignal() {
		return inputSignal;
	}

	//============================================================================================
	//******************************************Storage*******************************************
	//============================================================================================
	
	private ItemStackHandler createHandler()
	{
		return new ItemStackHandler(8)
			{
				@Override
				protected void onContentsChanged(int slot)
				{
					setChanged();
				}
				
				@Override
				public boolean isItemValid(int slot, @Nonnull ItemStack stack)
				{
					return isValidCrystal(stack);
				}
				
				// Limits the number of items per slot
				public int getSlotLimit(int slot)
				{
					return 1;
				}
				
				@Nonnull
				@Override
				public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
				{
					if(!isItemValid(slot, stack))
					{
						return stack;
					}
					
					return super.insertItem(slot, stack, simulate);
					
				}
			};
	}
	
	private boolean isValidCrystal(ItemStack stack)
	{
		if(stack.getItem() == ItemInit.MEMORY_CRYSTAL.get())
			return true;
		else if(stack.getItem() == ItemInit.MATERIALIZATION_CRYSTAL.get())
			return true;
		else if(stack.getItem() == ItemInit.ENERGY_CRYSTAL.get())
			return true;
		else if(stack.getItem() == ItemInit.COMMUNICATION_CRYSTAL.get())
			return true;
		
		return false;
	}
	
	//============================================================================================
	//*******************************************Energy*******************************************
	//============================================================================================

	@Override
	public long capacity()
	{
		return 50000000;
	}

	@Override
	public long maxReceive()
	{
		return 1000000;
	}

	@Override
	public long maxExtract()
	{
		return 1000000;
	}
	
	//============================================================================================
	//*****************************************CC: Tweaked****************************************
	//============================================================================================
	
	public String getLocalAddress()
	{
		String dimension = this.level.dimension().location().toString();
		String galaxy = Universe.get(this.level).getGalaxiesFromDimension(dimension).getCompound(0).getAllKeys().iterator().next();
		//TODO What if the Dimension is not located inside a Galaxy
		return Universe.get(this.level).getAddressInGalaxyFromDimension(galaxy, dimension);
	}

	public boolean ringUpNthPlatform(int n)
	{
		if (energyBlockEntity instanceof TransportRingsEntity rings) {
			Level level = Objects.requireNonNull(rings.getLevel());

			CompoundTag tag = RingsNetwork.get(level).get6ClosestRingsFromTag(
					level.dimension().location().toString(),
					rings.getBlockPos(),
					TransportRingsEntity.MAX_DISTANCE,
					rings.getID()
			);
			List<String> tagList = tag.getAllKeys().stream().collect(Collectors.toList());

			int ringsFound = tag.size();
			BlockPos[] ringsPos = new BlockPos[6];

			for (int i = 0; i < 6; i++) {
				if (i < ringsFound) {
					int[] coords = tag.getCompound(tagList.get(i)).getIntArray("Coordinates");
					ringsPos[i] = new BlockPos(coords[0], coords[1], coords[2]);
				}
			}

			if (n <= ringsFound) {
				rings.activate(ringsPos[n - 1]);
				return true;
			}
		}

		return false;
	}
	
	//============================================================================================
	//******************************************Ticking*******************************************
	//============================================================================================
	
	public static void tick(Level level, BlockPos pos, BlockState state, CrystalInterfaceEntity advancedInterface)
	{
		BasicInterfaceEntity.tick(level, pos, state, advancedInterface);
	}
}
