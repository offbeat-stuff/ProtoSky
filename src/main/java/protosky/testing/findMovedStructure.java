package protosky.testing;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class findMovedStructure implements ModInitializer {
    static int i = 0;
    public static boolean searching = false;
    public static Structure structureToFind = null;
    static ArrayList<net.minecraft.util.Pair<BlockBox, BlockBox>>[] structureBoundingBoxLocations = null;
    static List<Structure> structuresList = null;
    static CommandContext<ServerCommandSource> globalContext = null;
    static ServerPlayerEntity globalPlayer = null;
    static Registry<Structure> globalStructureRegistry = null;
    static ServerWorld globalWorld = null;
    static int waitTime = 0;
    public static boolean inWait = false;

    @Override
    public void onInitialize() {
        CommonLifecycleEvents.TAGS_LOADED.register((arg1, arg2) -> {

        });
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            if(waitTime == 1) {
                waitTime--;
                inWait = false;

                structureToFind = structuresList.get(i);

                globalContext.getSource().sendFeedback(Text.literal("Searching for " + structureToFind.toString()), false);
                System.out.println("Searching for " + structureToFind.toString());

                try {
                    BlockPos placeToGo = locateStructure(globalWorld, structureToFind, globalStructureRegistry, globalPlayer);
                    globalPlayer.teleport(placeToGo.getX(), placeToGo.getY(), placeToGo.getZ());
                } catch (NoSuchElementException e) {
                    tickToBeFoundStructure(new ArrayList<>(Collections.singleton(new Pair<>(null, null))));
                }
            } else if(waitTime != 0) {
                waitTime--;
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("detectStructureMoves")
                .then(literal("stop").executes(context -> {
                    searching = false;
                    tickToBeFoundStructure(new ArrayList<>(Collections.singleton(new Pair<>(null, null))));
                    return 1;
                }))
                .then(argument("player", EntityArgumentType.player())
                .then(literal("all").executes(context -> {
                    if(searching) {
                        context.getSource().sendFeedback(Text.literal("Already searching"), false);
                        System.out.println("Already searching");
                        return 1;
                    }
                    i = 0;
                    searching = true;
                    globalContext = context;
                    globalWorld = globalContext.getSource().getWorld();
                    globalStructureRegistry = globalWorld.getRegistryManager().get(Registry.STRUCTURE_KEY);
                    structuresList = globalStructureRegistry.stream().toList();
                    structureBoundingBoxLocations = new ArrayList[structuresList.size()];
                    globalPlayer = EntityArgumentType.getPlayer(context, "player");

                     inWait = true;
                    waitTime = 40;

                    /*structureToFind = structuresList.get(0);
                    context.getSource().sendFeedback(Text.literal("Starting search"), false);
                    System.out.println("Starting search");

                    try {
                        BlockPos placeToGo = locateStructure(globalWorld, structureToFind, globalStructureRegistry, globalPlayer);
                        globalPlayer.teleport(placeToGo.getX(), placeToGo.getY(), placeToGo.getZ());
                    } catch (NoSuchElementException e) {
                        tickToBeFoundStructure(new ArrayList<>(Collections.singleton(new Pair<>(null, null))));
                    }*/

                    return 1;
                }))
                .then(literal("structure").then(
                        argument("structure", RegistryKeyArgumentType.registryKey(Registry.STRUCTURE_KEY)).executes(context -> {
                            if(searching) {
                                context.getSource().sendFeedback(Text.literal("Already searching"), false);
                                System.out.println("Already searching");
                                return 1;
                            }
                            i = 0;
                            searching = true;
                            globalContext = context;
                            globalWorld = globalContext.getSource().getWorld();
                            globalStructureRegistry = globalWorld.getRegistryManager().get(Registry.STRUCTURE_KEY);

                            structuresList = new ArrayList<>(Collections.singleton(globalStructureRegistry.get(RegistryKeyArgumentType.getStructureEntry(context, "structure").getKey().get())));

                            structureBoundingBoxLocations = new ArrayList[structuresList.size()];

                            structureToFind = structuresList.get(0);
                            globalPlayer = EntityArgumentType.getPlayer(context, "player");

                            context.getSource().sendFeedback(Text.literal("Starting search"), false);
                            System.out.println("Starting search");

                            try {
                                BlockPos placeToGo = locateStructure(globalWorld, structureToFind, globalStructureRegistry, globalPlayer);
                                globalPlayer.teleport(placeToGo.getX(), placeToGo.getY(), placeToGo.getZ());
                            } catch (NoSuchElementException e) {
                                tickToBeFoundStructure(new ArrayList<>(Collections.singleton(new Pair<>(null, null))));
                            }
                            return 1;
                        }
                )))
        )));
    }

    private static Vec3i calculateBlockBoxDelta(BlockBox old, BlockBox New) {
        return new Vec3i(old.getMinX() - New.getMinX(), old.getMinY() - New.getMinY(), old.getMinZ() - New.getMinZ());
    }

    private static boolean blockBoxMoved(BlockBox old, BlockBox New) {
        return old.getMinX() != New.getMinX() || old.getMinY() != New.getMinY() || old.getMinZ() != New.getMinZ() || old.getMaxX() != New.getMaxX() || old.getMaxY() != New.getMaxY() || old.getMaxZ() != New.getMaxZ();
    }

    public static synchronized void tickToBeFoundStructure(ArrayList<Pair<BlockBox, BlockBox>> structPoses) {
        structureBoundingBoxLocations[i] = structPoses;

        i++;
        if((i == structuresList.size()) || !searching) {
            int ii = 0;
            StringBuilder stringBuilder = new StringBuilder();

            for(Structure structure : structuresList) {
                List<Pair<BlockBox, BlockBox>> boxLocs = structureBoundingBoxLocations[ii];

                stringBuilder.append(globalStructureRegistry.getId(structure).toString());

                if(boxLocs.size() == 1) {
                    Pair<BlockBox, BlockBox> boxes = boxLocs.get(0);
                    if(boxes.getLeft() == null || boxes.getRight() == null) {
                        stringBuilder.append(": one the bounding boxes was null").append("\n");
                    } else {
                        stringBuilder.append(blockBoxMoved(boxLocs.get(0).getLeft(), boxLocs.get(0).getRight()) ? " moved by " + calculateBlockBoxDelta(boxes.getLeft(), boxes.getRight()) : " didn't move").append("\n");
                    }

                } else {
                    int iii = 0;
                    stringBuilder.append("\n");
                    for(Pair<BlockBox, BlockBox> boxes : boxLocs) {
                        iii++;
                        stringBuilder
                                .append("  Part ").append(iii)
                                .append(blockBoxMoved(boxes.getLeft(), boxes.getRight()) ?
                                        " moved by " + calculateBlockBoxDelta(boxes.getLeft(), boxes.getRight()) :
                                        " didn't move"
                                )
                                .append("\n");
                    }
                }
                ii++;
            }
            globalContext.getSource().sendFeedback(Text.literal(stringBuilder.toString()), true);
            System.out.println(stringBuilder.toString());

            searching = false;
        } else {
            inWait = true;
            waitTime = 40;
        }
    }

    private static BlockPos locateStructure(ServerWorld world, Structure structureToFind, Registry<Structure> structureRegistry, ServerPlayerEntity player) throws NoSuchElementException {
        RegistryEntryList<Structure> structureListToFind =  RegistryEntryList.of(structureRegistry.entryOf(structureRegistry.getKey(structureToFind).get()));
        BlockPos searchStartPos = player.getBlockPos().add(100000, 0 ,0);
        //BlockPos searchStartPos = player.getBlockPos();

        com.mojang.datafixers.util.Pair<BlockPos, RegistryEntry<Structure>> pair = world.getChunkManager()
                .getChunkGenerator()
                .locateStructure(world, structureListToFind, searchStartPos, 100, false);
        if (pair == null) {
            throw new NoSuchElementException();
        } else {
            return pair.getFirst();
        }
    }
}
