package com.shugabrush.raintegration.unification.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import com.almostreliable.unified.utils.JsonCompare;
import com.google.gson.JsonObject;
import com.shugabrush.raintegration.RAIntegration;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class FluidRecipeLink
{

    /**
     * This cache is an optimization to avoid creating many ResourceLocations for just a few different types.
     * Having fewer ResourceLocation instances can greatly speed up equality checking when these are used as map keys.
     */
    private static final Map< String, ResourceLocation> PARSED_TYPE_CACHE = new HashMap<>();
    private static final ResourceLocation SHAPED_RECIPE_TYPE = PARSED_TYPE_CACHE.computeIfAbsent(
            "minecraft:crafting_shaped",
            ResourceLocation::new);
    private static final ResourceLocation SHAPELESS_RECIPE_TYPE = PARSED_TYPE_CACHE.computeIfAbsent(
            "minecraft:crafting_shapeless",
            ResourceLocation::new);

    static
    {
        PARSED_TYPE_CACHE.putIfAbsent("crafting_shaped", SHAPED_RECIPE_TYPE);
        PARSED_TYPE_CACHE.putIfAbsent("crafting_shapeless", SHAPELESS_RECIPE_TYPE);
    }

    private final ResourceLocation id;
    private final ResourceLocation type;
    private final JsonObject originalRecipe;
    private final boolean isCraftingRecipe;

    @Nullable
    private DuplicateLink duplicateLink;
    @Nullable
    private JsonObject unifiedRecipe;
    @Nullable
    private Fluid craftingRecipeOutput;

    private FluidRecipeLink(ResourceLocation id, JsonObject originalRecipe, ResourceLocation type)
    {
        this.id = id;
        this.originalRecipe = originalRecipe;
        this.type = type;
        this.isCraftingRecipe = type == SHAPED_RECIPE_TYPE || type == SHAPELESS_RECIPE_TYPE;
    }

    @Nullable
    public static FluidRecipeLink of(ResourceLocation id, JsonObject originalRecipe)
    {
        try
        {
            String typeString = originalRecipe.get("type").getAsString();
            ResourceLocation type = PARSED_TYPE_CACHE.computeIfAbsent(typeString, ResourceLocation::new);
            return new FluidRecipeLink(id, originalRecipe, type);
        }
        catch (Exception e)
        {
            RAIntegration.LOGGER.warn("Could not detect recipe type for recipe '{}', skipping.", id);
            return null;
        }
    }

    /**
     * Compare two recipes for equality with given rules. Keys from rules will automatically count as ignored field for
     * the base comparison.
     * If base comparison succeed then the recipes will be compared for equality with rules from
     * {@link JsonCompare.Rule}.
     * Rules are sorted, first rule with the highest priority will be used.
     *
     * @param first          first recipe to compare
     * @param second         second recipe to compare
     * @param compareContext Settings and context to use for comparison.
     * @return the recipe where rules are applied and the recipes are compared for equality, or null if the recipes are
     *         not equal
     */
    @Nullable
    public static FluidRecipeLink compare(FluidRecipeLink first, FluidRecipeLink second,
                                          JsonCompare.CompareContext compareContext)
    {
        if (first.isCraftingRecipe && first.getCraftingRecipeOutput() != second.getCraftingRecipeOutput())
        {
            return null;
        }

        JsonObject selfActual = first.getActual();
        JsonObject toCompareActual = second.getActual();

        JsonObject compare = null;
        if (first.isCraftingRecipe)
        {
            compare = JsonCompare.compareShaped(selfActual, toCompareActual, compareContext);
        }
        else if (JsonCompare.matches(selfActual, toCompareActual, compareContext))
        {
            compare = JsonCompare.compare(compareContext.settings().getRules(), selfActual, toCompareActual);
        }

        if (compare == null)
            return null;
        if (compare == selfActual)
            return first;
        if (compare == toCompareActual)
            return second;
        return null;
    }

    public ResourceLocation getId()
    {
        return id;
    }

    public ResourceLocation getType()
    {
        return type;
    }

    public JsonObject getOriginal()
    {
        return originalRecipe;
    }

    public boolean hasDuplicateLink()
    {
        return duplicateLink != null;
    }

    @Nullable
    public DuplicateLink getDuplicateLink()
    {
        return duplicateLink;
    }

    private void updateDuplicateLink(@Nullable DuplicateLink duplicateLink)
    {
        Objects.requireNonNull(duplicateLink);
        if (hasDuplicateLink() && getDuplicateLink() != duplicateLink)
        {
            throw new IllegalStateException("Recipe is already linked to " + getDuplicateLink());
        }

        this.duplicateLink = duplicateLink;
        this.duplicateLink.addDuplicate(this);
    }

    @Nullable
    public JsonObject getUnified()
    {
        return unifiedRecipe;
    }

    public boolean isUnified()
    {
        return unifiedRecipe != null;
    }

    void setUnified(JsonObject json)
    {
        Objects.requireNonNull(json);
        if (isUnified())
        {
            throw new IllegalStateException("Recipe already unified");
        }

        this.unifiedRecipe = json;
    }

    @Nullable
    private Fluid getCraftingRecipeOutput()
    {
        if (craftingRecipeOutput == null)
        {
            JsonObject recipe = unifiedRecipe == null ? originalRecipe : unifiedRecipe;
            try
            {
                String outputString = recipe
                        .getAsJsonObject("result")
                        .getAsJsonPrimitive("item")
                        .getAsString();
                craftingRecipeOutput = BuiltInRegistries.FLUID.get(new ResourceLocation(outputString));
            }
            catch (Exception e)
            {
                RAIntegration.LOGGER.warn("Could not detect crafting recipe output for recipe '{}'.", id);
                craftingRecipeOutput = Fluids.EMPTY;
            }
        }

        if (craftingRecipeOutput == Fluids.EMPTY)
        {
            return null;
        }

        return craftingRecipeOutput;
    }

    @Override
    public String toString()
    {
        String duplicate = duplicateLink != null ? " (duplicate)" : "";
        String unified = unifiedRecipe != null ? " (unified)" : "";
        return String.format("['%s'] %s%s%s", type, id, duplicate, unified);
    }

    /**
     * Checks for duplicate against given recipe data. If recipe data already has a duplicate link,
     * the master from the link will be used. Otherwise, we will create a new link if needed.
     *
     * @param otherRecipe    Recipe data to check for duplicate against.
     * @param compareContext Settings and context to use for comparison.
     * @return True if recipe is a duplicate, false otherwise.
     */
    public boolean handleDuplicate(FluidRecipeLink otherRecipe, JsonCompare.CompareContext compareContext)
    {
        DuplicateLink selfDuplicate = getDuplicateLink();
        DuplicateLink otherDuplicate = otherRecipe.getDuplicateLink();

        if (selfDuplicate != null && otherDuplicate != null)
        {
            return selfDuplicate == otherDuplicate;
        }

        if (selfDuplicate == null && otherDuplicate == null)
        {
            FluidRecipeLink compare = compare(this, otherRecipe, compareContext);
            if (compare == null)
            {
                return false;
            }

            DuplicateLink newLink = new DuplicateLink(compare);
            updateDuplicateLink(newLink);
            otherRecipe.updateDuplicateLink(newLink);
            return true;
        }

        if (otherDuplicate != null)
        {
            FluidRecipeLink compare = compare(this, otherDuplicate.getMaster(), compareContext);
            if (compare == null)
            {
                return false;
            }
            otherDuplicate.updateMaster(compare);
            updateDuplicateLink(otherDuplicate);
            return true;
        }

        // selfDuplicate != null
        FluidRecipeLink compare = compare(selfDuplicate.getMaster(), otherRecipe, compareContext);
        if (compare == null)
        {
            return false;
        }
        selfDuplicate.updateMaster(compare);
        otherRecipe.updateDuplicateLink(selfDuplicate);
        return true;
    }

    public JsonObject getActual()
    {
        return getUnified() != null ? getUnified() : getOriginal();
    }

    public static final class DuplicateLink
    {

        private final Set< FluidRecipeLink> recipes = new HashSet<>();
        private FluidRecipeLink currentMaster;

        private DuplicateLink(FluidRecipeLink master)
        {
            updateMaster(master);
        }

        private void updateMaster(FluidRecipeLink master)
        {
            Objects.requireNonNull(master);
            addDuplicate(master);
            this.currentMaster = master;
        }

        private void addDuplicate(FluidRecipeLink recipe)
        {
            recipes.add(recipe);
        }

        public FluidRecipeLink getMaster()
        {
            return currentMaster;
        }

        public Set< FluidRecipeLink> getRecipes()
        {
            return Collections.unmodifiableSet(recipes);
        }

        public Set< FluidRecipeLink> getRecipesWithoutMaster()
        {
            return recipes.stream().filter(recipe -> recipe != currentMaster).collect(Collectors.toSet());
        }

        @Override
        public String toString()
        {
            return "Link{currentMaster=" + currentMaster + ", recipes=" + recipes.size() + "}";
        }
    }
}
