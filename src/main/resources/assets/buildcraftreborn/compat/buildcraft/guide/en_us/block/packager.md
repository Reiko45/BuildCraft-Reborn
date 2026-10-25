<lore>
A recipe, folded up and posted.
</lore>
<no_lore>
The Recipe Packager packs the ingredients of a crafting recipe into a package, ready for a Stamping Table to craft.
</no_lore>
<chapter name="The pattern"/>
The 3x3 grid is the crafting grid of the recipe. Click a slot with an item to make that slot ask for the item. Click an empty slot with an empty hand to make it ask for anything (it lights up), and click again to turn it off. Clicking a slot that asks for an item, with an empty hand, clears it.
All the slots asking for "anything" get the same item, one that no other slot asks for by name.
<recipes_usages stack="buildcraftreborn:packager"/>
<chapter name="Packing"/>
Put paper in the slot at the top right. Every quarter of a second the packager takes one item for each lit slot, from its nine storage slots first and then from the inventories on its four sides, and turns a piece of paper into a package with each item in its slot. The package comes out of the slot below the paper, and the packager waits until it has been taken.
A package can go back in instead of paper, to fill its empty slots: the packager won't put anything where the package already holds something.
<chapter name="Automation"/>
Pipes put ingredients into the storage slots from the sides (only items that top up a stack already there, or that the pattern can use), paper or packages in from the top or bottom, and take packages out of any side. Delivery robots can carry the packages to a Stamping Table.
