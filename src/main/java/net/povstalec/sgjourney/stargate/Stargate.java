package net.povstalec.sgjourney.stargate;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.povstalec.sgjourney.config.CommonStargateConfig;

public class Stargate
{

	private static long systemWideConnectionCost = CommonStargateConfig.system_wide_connection_energy_cost.get();
	private static long interstellarConnectionCost = CommonStargateConfig.interstellar_connection_energy_cost.get();
	private static long intergalacticConnectionCost = CommonStargateConfig.intergalactic_connection_energy_cost.get();

	private static long systemWideConnectionDraw = CommonStargateConfig.system_wide_connection_energy_draw.get();
	private static long interstellarConnectionDraw = CommonStargateConfig.interstellar_connection_energy_draw.get();
	private static long intergalacticConnectionDraw = CommonStargateConfig.intergalactic_connection_energy_draw.get();
	
	public enum Gen
	{
		NONE(0),
		GEN_1(1),
		GEN_2(2),
		GEN_3(3);
		
		private final int gen;
		
		private Gen(int gen)
		{
			this.gen = gen;
		}
		
		public int getGen()
		{
			return this.gen;
		}
	}
	
	public enum ConnectionType
	{
		SYSTEM_WIDE(systemWideConnectionCost, systemWideConnectionDraw),
		INTERSTELLAR(interstellarConnectionCost, interstellarConnectionDraw),
		INTERGALACTIC(intergalacticConnectionCost, intergalacticConnectionDraw);
		
		private long estabilishingPowerCost;
		private long powerDraw;
		
		ConnectionType(long estabilishingPowerCost, long powerDraw)
		{
			this.estabilishingPowerCost = estabilishingPowerCost;
			this.powerDraw = powerDraw;
		}
		
		public long getEstabilishingPowerCost()
		{
			return this.estabilishingPowerCost;
		}
		
		public long getPowerDraw()
		{
			return this.powerDraw;
		}
	}
	
	public enum FilterType
	{
		NONE,
		WHITELIST,
		BLACKLIST;
	}
	
	public enum FeedbackType
	{
		INFO,
		ERROR,
		MAJOR_ERROR;
		
		FeedbackType()
		{
			
		}
		
		public boolean isError()
		{
			return this == ERROR || this == MAJOR_ERROR;
		}
		
		public boolean shouldPlaySound()
		{
			return this == MAJOR_ERROR;
		}
	}
	
	public enum Feedback
	{
		NONE(FeedbackType.INFO, Component.empty()),
		UNKNOWN_ERROR(FeedbackType.ERROR, createError("unknown", true)),
		
		// Chevron/Symbol
		SYMBOL_ENCODED(FeedbackType.INFO, createInfo("symbol_encoded")),
		SYMBOL_IN_ADDRESS(FeedbackType.ERROR, createError("symbol_in_addres", false)),
		SYMBOL_OUT_OF_BOUNDS(FeedbackType.ERROR, createError("symbol_out_of_bounds", false)),
		
		// Estabilishing Connection
		CONNECTION_ESTABILISHED_SYSTEM_WIDE(FeedbackType.INFO, createInfo("connection_estabilished.system_wide")),
		CONNECTION_ESTABILISHED_INTERSTELLAR(FeedbackType.INFO, createInfo("connection_estabilished.interstellar")),
		CONNECTION_ESTABILISHED_INTERGALACTIC(FeedbackType.INFO, createInfo("connection_estabilished.intergalactic")),
		INCOPLETE_ADDRESS(FeedbackType.ERROR, createError("incomplete_address", false)),
		INVALID_ADDRESS(FeedbackType.ERROR, createError("invalid_address", false)),
		NOT_ENOUGH_POWER(FeedbackType.MAJOR_ERROR, createError("not_enough_power", true)),
		SELF_OBSTRUCTED(FeedbackType.MAJOR_ERROR, createError("self_obstructed", true)),
		TARGET_OBSTRUCTED(FeedbackType.ERROR, createError("target_obstructed", false)),
		SELF_DIAL(FeedbackType.MAJOR_ERROR, createError("self_dial", true)),
		SAME_SYSTEM_DIAL(FeedbackType.MAJOR_ERROR, createError("same_system_dial", true)),
		ALREADY_CONNECTED(FeedbackType.ERROR, createError("already_connected", false)),
		NO_GALAXY(FeedbackType.ERROR, createError("no_galaxy", false)),
		NO_DIMENSIONS(FeedbackType.ERROR, createError("no_dimensions", false)),
		NO_STARGATES(FeedbackType.ERROR, createError("no_stargates", false)),

		// Wormhole
		//TRANSPORT_SUCCESSFUL(FeedbackType.INFO, createInfo("wormhole.transport_successful")),//TODO
		//ENTITY_DESTROYED(FeedbackType.INFO, createInfo("wormhole.entity_destroyed")),
		
		// End Connection
		CONNECTION_ENDED_BY_DISCONNECT(FeedbackType.INFO, createInfo("connection_ended.disconnect")),
		CONNECTION_ENDED_BY_POINT_OF_ORIGIN(FeedbackType.INFO, createInfo("connection_ended.point_of_origin")),
		CONNECTION_ENDED_BY_NETWORK(FeedbackType.INFO, createInfo("connection_ended.stargate_network")),
		CONNECTION_ENDED_BY_AUTOCLOSE(FeedbackType.INFO, createInfo("connection_ended.autoclose")),
		EXCEEDED_CONNECTION_TIME(FeedbackType.ERROR, createError("exceeded_connection_time", false)),
		RAN_OUT_OF_POWER(FeedbackType.ERROR, createError("ran_out_of_power", false)),
		CONNECTION_REROUTED(FeedbackType.ERROR, createError("connection_rerouted", false)),
		WRONG_DISCONNECT_SIDE(FeedbackType.ERROR, createError("wrong_disconnect_side", false)),

		STARGATE_DESTROYED(FeedbackType.ERROR, createError("stargate_destroyed", false)),
		TARGET_STARGATE_DOES_NOT_EXIST(FeedbackType.ERROR, createError("target_stargate_does_not_exist", false)),
		
		// Universe
		
		// Milky Way
		CHEVRON_RAISED(FeedbackType.INFO, createInfo("chevron_raised")),
		CHEVRON_LOWERED(FeedbackType.INFO, createInfo("chevron_lowered")),
		CHEVRON_ALREADY_RAISED(FeedbackType.ERROR, createError("chevron_already_raised", false)),
		CHEVRON_ALREADY_LOWERED(FeedbackType.ERROR, createError("chevron_already_lowered", false));
		
		// Pegasus
		
		private final FeedbackType type;
		private final Component feedbackMessage;
		
		private Feedback(FeedbackType type, Component feedbackMessage)
		{
			this.type = type;
			this.feedbackMessage = feedbackMessage;
		}
		
		public int getCode()
		{
			return this.ordinal();
		}
		
		public Component getFeedbackMessage()
		{
			return this.feedbackMessage;
		}
		
		public boolean playFailSound()
		{
			return this.type.shouldPlaySound();
		}
		
		public boolean isError()
		{
			return this.type.isError();
		}
		
		/*public String getEnglishText()
		{
			if(feedbackMessage.getContents() instanceof TranslatableContents translatable)
				return Language.loadDefault().;
			return "";
		}*/
	}
	
	private static Component createInfo(String feedback)
	{
		return Component.translatable("message.sgjourney.stargate.info." + feedback);
	}
	
	private static Component createError(String feedback, boolean majorError)
	{
		MutableComponent component = Component.translatable("message.sgjourney.stargate.error." + feedback);
		
		return majorError ? component.withStyle(ChatFormatting.DARK_RED) : component.withStyle(ChatFormatting.RED);
	}
}
