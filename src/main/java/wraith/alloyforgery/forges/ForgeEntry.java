package wraith.alloyforgery.forges;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.entry.EntryRegisterContext;
import me.basiqueevangelist.dynreg.entry.EntryScanContext;
import me.basiqueevangelist.dynreg.entry.RegistrationEntries;
import me.basiqueevangelist.dynreg.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.entry.json.EntryDescriptionReaders;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.ForgeControllerItem;
import wraith.alloyforgery.block.ForgeControllerBlock;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ForgeEntry implements RegistrationEntry {
    private static final Identifier MINEABLE_PICKAXE = new Identifier("mineable/pickaxe");
    private final Identifier id;
    private final int forgeTier;
    private final float speedMultiplier;
    private final int fuelCapacity;
    private final Identifier mainMaterialId;
    private final List<Identifier> additionalMaterialIds;
    private final Identifier controllerBlockRegistryId;

    public ForgeEntry(Identifier id, JsonObject json) {
        this.id = id;
        this.forgeTier = JsonHelper.getInt(json, "tier");
        this.speedMultiplier = JsonHelper.getFloat(json, "speed_multiplier", 1);
        this.fuelCapacity = JsonHelper.getInt(json, "fuel_capacity", 48000);
        this.mainMaterialId = Identifier.tryParse(JsonHelper.getString(json, "material"));
        this.additionalMaterialIds = new ArrayList<>();
        JsonHelper.getArray(json, "additional_materials", new JsonArray()).forEach(jsonElement -> additionalMaterialIds.add(Identifier.tryParse(jsonElement.getAsString())));
        this.controllerBlockRegistryId = AlloyForgery.id(mainMaterialId.getPath() + "_forge_controller");
    }

    public ForgeEntry(Identifier id, PacketByteBuf buf) {
        this.id = id;
        this.forgeTier = buf.readVarInt();
        this.speedMultiplier = buf.readFloat();
        this.fuelCapacity = buf.readVarInt();
        this.mainMaterialId = buf.readIdentifier();
        this.additionalMaterialIds = buf.readList(PacketByteBuf::readIdentifier);
        this.controllerBlockRegistryId = AlloyForgery.id(mainMaterialId.getPath() + "_forge_controller");
    }

    public static void init() {
        RegistrationEntries.registerEntryType(AlloyForgery.id("forge"), ForgeEntry::new);

        EntryDescriptionReaders.register(AlloyForgery.id("forge"), ForgeEntry::new);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public void scan(EntryScanContext ctx) {
        ctx.announce(Registry.BLOCK, controllerBlockRegistryId)
            .dependency(Registry.BLOCK, mainMaterialId);

        ctx.announce(Registry.ITEM, controllerBlockRegistryId)
            .dependency(Registry.BLOCK, controllerBlockRegistryId);
    }

    @SuppressWarnings({"UnstableApiUsage"})
    @Override
    public void register(EntryRegisterContext ctx) {
        var def = new ForgeDefinition(forgeTier, speedMultiplier, fuelCapacity, Registry.BLOCK.get(mainMaterialId), additionalMaterialIds.stream().map(Registry.BLOCK::get).toList());

        var block = ctx.register(Registry.BLOCK, controllerBlockRegistryId, new ForgeControllerBlock(def));
        ForgeRegistry.store(id, def, block);

        FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> (ForgeControllerBlockEntity) blockEntity, block);

        // TODO: put blocks in tags.

        ctx.register(Registry.ITEM, controllerBlockRegistryId, new ForgeControllerItem(block, new Item.Settings().group(AlloyForgery.ALLOY_FORGERY_GROUP)));
    }

    @Override
    public void onRemoved() {
        ForgeRegistry.unstore(id);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(forgeTier);
        buf.writeFloat(speedMultiplier);
        buf.writeVarInt(fuelCapacity);

        buf.writeIdentifier(mainMaterialId);
        buf.writeCollection(additionalMaterialIds, PacketByteBuf::writeIdentifier);
    }
}
