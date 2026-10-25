<lore>
월드의 아이템을 주워 이리저리 옮기는 것, 이것이 미래입니다!
</lore>
<no_lore>
흑요석 아이템 파이프는 월드에 있는 아이템을 주워 연결된 파이프/인벤토리로 운송하는 데 사용합니다.
</no_lore>

<recipes stack="buildcraftreborn:pipe_obsidian_item"/>

<chapter name="파이프 작동"/>
동력을 받으면 위쪽 3x4x3 범위의 아이템을 주울 수 있습니다.
동력이 없어도 파이프에 닿는 아이템은 계속 줍습니다.

<chapter name="파이프 방향"/>
흑요석 아이템 파이프가 작용 범위를 가지려면 한 방향을 '향하고' 있어야 합니다.
파이프에 연결이 여러 개 있으면 월드에서 아이템을 줍는 능력을 잃습니다.

<chapter name="파이프 작동 방식"/>
받은 아이템이 갈 곳이 없으면 파이프에서 바닥으로 떨어집니다.
파이프 마개를 쓰거나 파이프를 다른 색으로 칠하면 파이프끼리 연결되지 않게 할 수 있습니다.

<chapter name="동력 공급"/>
엔진, 전력 어댑터, 파이프 펄서 같은 모든 MJ 동력원에서 동력을 받을 수 있습니다.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
동력 펄스를 받을 때마다 바닥에서 아이템을 줍습니다.
펄스의 동력이 클수록 펄스당 줍는 아이템이 많아집니다.

<usages stack="buildcraftreborn:pipe_obsidian_item"/>