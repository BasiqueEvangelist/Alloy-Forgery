package wraith.alloyforgery.forges;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.wispforest.owo.moddata.ModDataConsumer;
import io.wispforest.owo.util.TagInjector;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.ForgeControllerItem;
import wraith.alloyforgery.block.ForgeControllerBlock;

import java.util.*;

public class ForgeRegistry {

    public static final Gson GSON = new Gson();
    private static final Identifier MINEABLE_PICKAXE = new Identifier("mineable/pickaxe");

    private static final Map<Identifier, ForgeDefinition> FORGE_DEFINITION_REGISTRY = new HashMap<>();
    private static final Map<Identifier, Block> CONTROLLER_BLOCK_REGISTRY = new HashMap<>();

    private static final Set<Block> CONTROLLER_BLOCKS = new HashSet<>();
    private static final Set<Block> CONTROLLER_BLOCKS_VIEW = Collections.unmodifiableSet(CONTROLLER_BLOCKS);

    public static Optional<ForgeDefinition> getForgeDefinition(Identifier id) {
        return FORGE_DEFINITION_REGISTRY.containsKey(id) ? Optional.of(FORGE_DEFINITION_REGISTRY.get(id)) : Optional.empty();
    }

    public static Optional<Block> getControllerBlock(Identifier id) {
        return FORGE_DEFINITION_REGISTRY.containsKey(id) ? Optional.of(CONTROLLER_BLOCK_REGISTRY.get(id)) : Optional.empty();
    }

    public static Set<Identifier> getForgeIds() {
        return FORGE_DEFINITION_REGISTRY.keySet();
    }

    public static List<Block> getControllerBlocks() {
        return CONTROLLER_BLOCK_REGISTRY.values().stream().toList();
    }

    public static Set<Block> controllerBlocksView() {
        return CONTROLLER_BLOCKS_VIEW;
    }

    public static void store(Identifier id, ForgeDefinition definition, ForgeControllerBlock block) {
        FORGE_DEFINITION_REGISTRY.put(id, definition);
        CONTROLLER_BLOCK_REGISTRY.put(id, block);
        CONTROLLER_BLOCKS.add(block);
    }

    public static void unstore(Identifier id) {
        FORGE_DEFINITION_REGISTRY.remove(id);
        var block = CONTROLLER_BLOCK_REGISTRY.remove(id);
        CONTROLLER_BLOCKS.remove(block);
    }

}
