<lore>
아이템을 뽑아 다른 곳으로 옮기고 싶나요? 동력을 주고 흘러가는 모습을 지켜보세요.
</lore>
<no_lore>
나무 아이템 파이프는 동력을 받으면 인벤토리에서 아이템을 추출하는 데 사용합니다.
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="파이프 작동 방식"/>
동력을 받으면 아이템을 추출해 연결된 다른 인벤토리/파이프로 운송합니다.
아이템은 파이프의 막힌 면에서 추출되어 투명한 면 쪽으로 운송됩니다.
투명한 면이 여러 개라면 아이템은 그 사이에서 무작위로 이동합니다.
아이템이 갈 곳이 없으면 추출된 아이템은 파이프에서 바닥으로 떨어집니다.

일부 기계는 파이프에 동력이 없어도 나무 아이템 파이프로 아이템을 자동으로 직접 내보낼 수 있습니다.
파이프 마개를 쓰거나 파이프를 다른 색으로 칠하면 파이프끼리 연결되지 않게 할 수 있습니다.

<chapter name="동력 공급"/>
엔진, 전력 어댑터, 파이프 펄서 같은 모든 MJ 동력원에서 동력을 받을 수 있습니다.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
동력 펄스를 받을 때마다 인접한 인벤토리에서 아이템을 추출합니다.
펄스의 동력이 클수록 펄스당 추출되는 아이템이 많아집니다.

<usages stack="buildcraftreborn:pipe_wood_item"/>