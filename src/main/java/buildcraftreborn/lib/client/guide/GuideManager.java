/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.client.guide.data.JsonTypeTags;
import buildcraftreborn.lib.client.guide.entry.IEntryLinkConsumer;
import buildcraftreborn.lib.client.guide.entry.ItemStackValueFilter;
import buildcraftreborn.lib.client.guide.entry.PageEntry;
import buildcraftreborn.lib.client.guide.entry.PageValueType;
import buildcraftreborn.lib.client.guide.loader.MarkdownPageLoader;
import buildcraftreborn.lib.client.guide.parts.GuidePageFactory;
import buildcraftreborn.lib.client.guide.parts.GuidePageStandInRecipes;
import buildcraftreborn.lib.client.guide.parts.contents.ContentsNode;
import buildcraftreborn.lib.client.guide.parts.contents.ContentsNodeGui;
import buildcraftreborn.lib.client.guide.parts.contents.IContentsNode;
import buildcraftreborn.lib.client.guide.parts.contents.PageLink;
import buildcraftreborn.lib.client.guide.parts.contents.PageLinkNormal;
import buildcraftreborn.lib.client.guide.parts.recipe.GuideCraftingRecipes;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.ItemStackKey;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.StringUtilBC;
import buildcraftreborn.lib.misc.search.ISuffixArray;
import buildcraftreborn.lib.misc.search.SimpleSuffixArray;

/** Loads the guide: its books, entries and pages, and the contents and search index of each book.
 * <p>
 * 1.21: the books are reloaded with the resources, but the pages and contents are only loaded the next time a guide
 * is opened, as they need a world (for the creative search tab's item variants). They are loaded again after joining
 * a world or reloading resources. */
public enum GuideManager implements ResourceManagerReloadListener {
    INSTANCE;

    public static final String DEFAULT_LANG = "en_us";
    public static final GuideContentsData BOOK_ALL_DATA = new GuideContentsData(null);
    public static final boolean DEBUG = Boolean.getBoolean("buildcraftreborn.debug.guide");

    /** The keys are the entry names (for example {@code buildcraftreborn:item/list}). */
    private final Map<ResourceLocation, GuidePageFactory> pages = new HashMap<>();
    private final Map<ItemStackKey, GuidePageFactory> generatedPages = new HashMap<>();

    public ISuffixArray<PageLink> quickSearcher = new SimpleSuffixArray<>();
    /** Every {@link PageLink} that has been added to {@link #quickSearcher}. */
    private final Set<PageLink> pageLinksAdded = new HashSet<>();
    private final Map<GuideBook, Map<TypeOrder, ContentsNode>> contents = new HashMap<>();

    /** Every object added to the guide. Generally this means {@code Item}s and {@code IStatement}s. */
    public final Set<Object> objectsAdded = new HashSet<>();

    private boolean needsLoading = true;
    private boolean isInReload = false;

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        GuideBookRegistry.INSTANCE.reload(resourceManager);
        invalidate();
    }

    /** Loads everything again the next time a guide is opened (after joining a world, or when recipes change). */
    public void invalidate() {
        needsLoading = true;
        GuideCraftingRecipes.INSTANCE.invalidate();
        generatedPages.clear();
    }

    /** Loads the pages and contents, if they aren't already. */
    public void ensureLoaded() {
        if (needsLoading) {
            reload();
        }
    }

    public void reload() {
        if (isInReload) {
            throw new IllegalStateException("Cannot reload while we are reloading!");
        }
        try {
            isInReload = true;
            reload0(Minecraft.getInstance().getResourceManager());
            needsLoading = false;
        } finally {
            isInReload = false;
        }
    }

    private void reload0(ResourceManager resourceManager) {
        Stopwatch watch = Stopwatch.createStarted();
        // (The books themselves are only loaded on a resource reload: an open guide keeps its book, so making new
        // ones here would leave it showing a book with no contents data.)
        GuidePageRegistry.INSTANCE.reload(resourceManager);
        GuideCraftingRecipes.INSTANCE.invalidate();
        generatedPages.clear();

        Map<GuideBook, Set<String>> domains = new HashMap<>();
        Set<String> allDomains = new HashSet<>();
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            domains.put(book, new HashSet<>());
        }
        for (PageEntry<?> entry : GuidePageRegistry.INSTANCE.getAllEntries()) {
            allDomains.add(entry.typeTags.domain);
            GuideBook book = GuideBookRegistry.INSTANCE.getBook(entry.book);
            if (book != null) {
                domains.get(book).add(entry.typeTags.domain);
            }
        }
        BOOK_ALL_DATA.generate(allDomains);
        for (Entry<GuideBook, Set<String>> entry : domains.entrySet()) {
            entry.getKey().data.generate(entry.getValue());
        }

        pages.clear();
        String langCode = Minecraft.getInstance().getLanguageManager().getSelected();
        // load the default ones, then replace any with the current language's
        loadLangInternal(resourceManager, DEFAULT_LANG);
        if (!DEFAULT_LANG.equals(langCode)) {
            loadLangInternal(resourceManager, langCode);
        }

        generateContentsPage();

        int possible = GuidePageRegistry.INSTANCE.getReloadableEntryMap().size();
        if (DEBUG) {
            BuildCraftReborn.LOGGER.info("[lib.guide] {} search terms", pageLinksAdded.size());
        }
        BuildCraftReborn.LOGGER.info("[lib.guide] Loaded {} possible and {} actual guide pages in {}ms.", possible,
            pages.size(), watch.elapsed(TimeUnit.MILLISECONDS));
    }

    private void loadLangInternal(ResourceManager resourceManager, String lang) {
        for (Entry<ResourceLocation, PageEntry<?>> mapEntry : GuidePageRegistry.INSTANCE.getReloadableEntryMap()
            .entrySet()) {
            ResourceLocation entryKey = mapEntry.getKey();
            ResourceLocation file = ResourceLocation.fromNamespaceAndPath(entryKey.getNamespace(),
                "compat/buildcraft/guide/" + lang + "/" + entryKey.getPath() + ".md");
            Optional<Resource> resource = resourceManager.getResource(file);
            if (resource.isPresent()) {
                try (BufferedReader reader = resource.get().openAsReader()) {
                    // put the original page in so that the different lang variants override it
                    pages.put(entryKey, MarkdownPageLoader.INSTANCE.loadPage(reader, entryKey, mapEntry.getValue()));
                    if (DEBUG) {
                        BuildCraftReborn.LOGGER.info("[lib.guide.loader] Loaded page '{}'.", entryKey);
                    }
                } catch (IOException | RuntimeException e) {
                    BuildCraftReborn.LOGGER.warn("[lib.guide.loader] Couldn't load guide page '{}'", file, e);
                }
            } else if (!pages.containsKey(entryKey) && lang.equals(DEFAULT_LANG)) {
                BuildCraftReborn.LOGGER.warn("[lib.guide.loader] Unable to load guide page '{}' because it isn't in any "
                    + "resource pack!", file);
            }
        }
    }

    private void generateContentsPage() {
        objectsAdded.clear();
        contents.clear();
        genTypeMap(null);
        for (GuideBook book : GuideBookRegistry.INSTANCE.getAllEntries()) {
            genTypeMap(book);
        }
        quickSearcher = new SimpleSuffixArray<>();
        pageLinksAdded.clear();

        for (Entry<ResourceLocation, PageEntry<?>> mapEntry : GuidePageRegistry.INSTANCE.getReloadableEntryMap()
            .entrySet()) {
            GuidePageFactory entryFactory = getFactoryFor(mapEntry.getKey());
            if (entryFactory == null) {
                continue;
            }
            PageEntry<?> entry = mapEntry.getValue();
            ISimpleDrawable icon = entry.createDrawable();
            PageLine line = new PageLine(icon, icon, 2, StringUtilBC.formatStringForWhite(entry.title), true);
            objectsAdded.add(entry.getBasicValue());
            addChild(entry.book, entry.typeTags, new PageLinkNormal(line, true, entry.getTooltip(), entryFactory));
        }

        ContentsNode othersRoot = new ContentsNode(LocaleUtil.localize("buildcraft.guide.contents.all_group"), 0);
        for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : contents.entrySet()) {
            GuideBook book = bookEntry.getKey();
            if (book != null && !book.appendAllEntries) {
                continue;
            }
            for (ContentsNode root : bookEntry.getValue().values()) {
                root.addChild(othersRoot);
            }
        }
        final IEntryLinkConsumer adder = (tags, page) -> {
            if (pageLinksAdded.add(page)) {
                quickSearcher.add(page, page.getSearchName());
            }
            String title = LocaleUtil.localize(tags.type);
            IContentsNode subNode = othersRoot.getChild(title);
            if (subNode instanceof ContentsNode node) {
                node.addChild(page);
            } else if (subNode == null) {
                ContentsNode subContents = new ContentsNode(title, 1);
                othersRoot.addChild(subContents);
                subContents.addChild(page);
            } else {
                throw new IllegalStateException("Unknown node type " + subNode.getClass());
            }
        };
        for (PageValueType<?> type : GuidePageRegistry.INSTANCE.types.values()) {
            type.iterateAllDefault(adder);
        }

        quickSearcher.generate();
        for (Map<TypeOrder, ContentsNode> map : contents.values()) {
            for (ContentsNode node : map.values()) {
                node.sort();
            }
        }
    }

    private void genTypeMap(@Nullable GuideBook book) {
        Map<TypeOrder, ContentsNode> map = new HashMap<>();
        contents.put(book, map);
        for (TypeOrder order : GuiGuide.SORTING_TYPES) {
            map.put(order, new ContentsNode("root", -1));
        }
    }

    private void addChild(ResourceLocation bookType, JsonTypeTags tags, PageLink page) {
        if (pageLinksAdded.add(page)) {
            quickSearcher.add(page, page.getSearchName());
        }

        for (Entry<GuideBook, Map<TypeOrder, ContentsNode>> bookEntry : contents.entrySet()) {
            GuideBook book = bookEntry.getKey();
            if (book != null && !book.name.equals(bookType)) {
                continue;
            }
            for (Entry<TypeOrder, ContentsNode> entry : bookEntry.getValue().entrySet()) {
                String[] ordered = tags.getOrdered(entry.getKey());
                ContentsNode node = entry.getValue();
                for (int i = 0; i < ordered.length; i++) {
                    String title = LocaleUtil.localize(ordered[i]);
                    IContentsNode subNode = node.getChild(title);
                    if (subNode instanceof ContentsNode contentsNode) {
                        node = contentsNode;
                    } else if (subNode == null) {
                        ContentsNode subContents = new ContentsNode(title, i);
                        node.addChild(subContents);
                        node = subContents;
                    } else {
                        throw new IllegalStateException("Unknown node type " + subNode.getClass());
                    }
                }
                node.addChild(page);
            }
        }
    }

    @Nullable
    public GuidePageFactory getFactoryFor(ResourceLocation partialLocation) {
        return pages.get(partialLocation);
    }

    @Nullable
    public GuidePageFactory getFactoryFor(Object value) {
        if (value instanceof ItemStackValueFilter filter) {
            value = filter.stack.baseStack;
        } else if (value instanceof ItemStackKey key) {
            value = key.baseStack;
        }
        if (value instanceof ItemStack stack) {
            return getPageFor(stack);
        }
        ResourceLocation entry = getEntryFor(value);
        return entry == null ? null : getFactoryFor(entry);
    }

    @Nullable
    public static ResourceLocation getEntryFor(Object obj) {
        for (Entry<ResourceLocation, PageEntry<?>> entry : GuidePageRegistry.INSTANCE.getReloadableEntryMap()
            .entrySet()) {
            if (entry.getValue().matches(obj)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public GuidePageFactory getPageFor(ItemStack stack) {
        ResourceLocation entry = getEntryFor(stack);
        if (entry != null) {
            GuidePageFactory factory = getFactoryFor(entry);
            if (factory != null) {
                return factory;
            }
        }
        // Create a dummy page for the stack, with its recipes
        return generatedPages.computeIfAbsent(new ItemStackKey(stack.copyWithCount(1)),
            key -> GuidePageStandInRecipes.createFactory(key.baseStack));
    }

    public ContentsNodeGui getGuiContents(GuiGuide gui, TypeOrder sortingOrder) {
        Map<TypeOrder, ContentsNode> map = contents.get(gui.book);
        if (map == null) {
            throw new IllegalStateException("Unknown book " + gui.book);
        }
        ContentsNode node = map.get(sortingOrder);
        if (node == null) {
            throw new IllegalStateException("Unknown sorting order " + sortingOrder);
        }
        node.resetVisibility();
        return new ContentsNodeGui(gui, node);
    }

    /** @return The names of every page that loaded (for tests). */
    public List<ResourceLocation> getLoadedPages() {
        return new ArrayList<>(pages.keySet());
    }
}
