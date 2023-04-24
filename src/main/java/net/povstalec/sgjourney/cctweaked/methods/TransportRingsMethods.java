package net.povstalec.sgjourney.cctweaked.methods;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.povstalec.sgjourney.block_entities.TransportRingsEntity;
import net.povstalec.sgjourney.data.RingsNetwork;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransportRingsMethods
{

	public static class ActivateClosest implements InterfaceMethod<TransportRingsEntity>
	{
		@Override
		public String getName()
		{
			return "activate";
		}

		@Override
		public MethodResult use(ILuaContext context, TransportRingsEntity rings, IArguments arguments) throws LuaException
		{
			Level level = Objects.requireNonNull(rings.getLevel());

			CompoundTag tag = RingsNetwork.get(level).getClosestRingsFromTag(
					level.dimension().location().toString(),
					rings.getBlockPos(),
					new CompoundTag(),
					32000,
					rings.getID()
			);

			List<String> tagList = tag.getAllKeys().stream().collect(Collectors.toList());

			if(tag.size() > 0) {
				int[] coords = tag.getCompound(tagList.get(0)).getIntArray("Coordinates");

				context.executeMainThreadTask(() ->
				{
					rings.activate(new BlockPos(coords[0], coords[1], coords[2]));
					return null;
				});
			};

			return MethodResult.of();
		}
	}

	public static class ActivateTargetted extends ActivateClosest
	{
		@Override
		public MethodResult use(ILuaContext context, TransportRingsEntity rings, IArguments arguments) throws LuaException
		{
			if (arguments.count() < 1) {
				return super.use(context, rings, arguments);
			} else {
				int desiredPlatform = arguments.getInt(0);

				if (desiredPlatform < 1 || desiredPlatform > 6)
					throw new LuaException("Platform out of bounds <1, 6>");

				Level level = Objects.requireNonNull(rings.getLevel());

				CompoundTag tag = RingsNetwork.get(level).get6ClosestRingsFromTag(
						level.dimension().location().toString(),
						rings.getBlockPos(),
						32000,
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

				if (desiredPlatform > ringsFound)
					throw new LuaException("Platform not found");

				context.executeMainThreadTask(() ->
				{
					rings.activate(ringsPos[desiredPlatform - 1]);
					return null;
				});

				return MethodResult.of();
			}
		}
	}
}
