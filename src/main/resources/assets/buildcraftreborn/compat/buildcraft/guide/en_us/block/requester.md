<lore>
Tell the robots what you need, and wait.
</lore>
<no_lore>
The Requester asks delivery robots for items.
</no_lore>
<chapter name="Requests"/>
The left side of the requester holds 20 requests: click a request slot with a stack to ask for that many of that item, and with an empty hand to clear it. What arrives goes in the matching slot on the right, which only takes the item asked for.
<recipes_usages stack="buildcraftreborn:requester"/>
<chapter name="Delivery"/>
Put a Docking Station on a pipe next to the requester, with a gate giving it the "Request Needed Items" action. Delivery robots then bring what the requester still needs from stations that provide items.
<chapter name="Automation"/>
Pipes can take the delivered items out (and put matching items in). A comparator reads how full the requested slots are.
