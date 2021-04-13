package eu.pollux28.imggen.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.pollux28.imggen.ImgGen;
import eu.pollux28.imggen.gen.chunk.ImgGenChunkGenerator;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;


public class Commands {

    public static void init(){
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            LiteralArgumentBuilder<ServerCommandSource> pre = CommandManager.literal("imggen")
                    .requires(executor -> executor.hasPermissionLevel(2));
            pre.then(CommandManager.literal("debug").then(CommandManager.argument("location",Vec3ArgumentType.vec3()).executes(commandContext -> {
                return execute((ServerCommandSource) commandContext.getSource(), Vec3ArgumentType.getPosArgument(commandContext,"location"));
            })));
            dispatcher.register(pre);
        }));

    }

    private static int execute(ServerCommandSource commandSource, PosArgument pos){
        ChunkGenerator chunkGenerator = commandSource.getWorld().getChunkManager().getChunkGenerator();
        if(chunkGenerator instanceof ImgGenChunkGenerator){
            BlockPos blockPos = pos.toAbsoluteBlockPos(commandSource);
            Biome biome = commandSource.getWorld().getBiome(blockPos);
            DynamicRegistryManager dynamicRegistryManager = commandSource.getMinecraftServer().getRegistryManager();
            Identifier id = dynamicRegistryManager.get(Registry.BIOME_KEY).getId(biome);
            int height=-1;
            if(ImgGen.CONFIG.customHeightMap){
                height =((ImgGenChunkGenerator)chunkGenerator).heightMapSource.getHeight(pos.toAbsoluteBlockPos(commandSource).getX(),pos.toAbsoluteBlockPos(commandSource).getZ());
                commandSource.getMinecraftServer().getPlayerManager().broadcastChatMessage(new LiteralText("Biome at : "+(pos.toAbsoluteBlockPos(commandSource).getX())+" "+(pos.toAbsoluteBlockPos(commandSource).getZ())+" is " + id.toString()+ " and height is "+ height),MessageType.SYSTEM,Util.NIL_UUID);
            }
                commandSource.getMinecraftServer().getPlayerManager().broadcastChatMessage(new LiteralText("Biome at : "+(pos.toAbsoluteBlockPos(commandSource).getX())+" "+(pos.toAbsoluteBlockPos(commandSource).getZ())+" is " + id.toString()),MessageType.SYSTEM,Util.NIL_UUID);
        }else {
            commandSource.getMinecraftServer().getPlayerManager().broadcastChatMessage(new LiteralText("Not in a ImageWorldGenerator World !"),MessageType.SYSTEM,Util.NIL_UUID);
        }
        return 1;
    }
}
