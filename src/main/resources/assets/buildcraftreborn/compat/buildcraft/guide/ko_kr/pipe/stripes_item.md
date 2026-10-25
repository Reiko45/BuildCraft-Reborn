<lore>
블록을 설치하고, 부수고, 사용할 수 있는 파이프를 상상해 보세요. 바로 그 파이프입니다.
</lore>
<no_lore>
줄무늬 아이템 파이프는 블록을 부수고, 블록을 설치하고, 들어온 아이템을 사용하며, 이를 연결된 파이프/인벤토리로 운송하는 데 사용합니다.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="파이프 작동"/>
동력을 받으면 바로 앞의 블록을 부수고 인접한 파이프나 인벤토리로 넘깁니다.
블록이 들어오면 바로 앞에 그 블록을 설치합니다.
아이템이 들어오면 앞에 있는 블록/엔티티에 그 아이템을 '사용'하려고 시도합니다.

<chapter name="파이프 방향"/>
줄무늬 아이템 파이프가 앞의 블록에 작용하려면 한 방향을 '향하고' 있어야 합니다.
여러 파이프/인벤토리가 연결되어 있으면 월드에 작용하는 능력을 잃습니다.

<chapter name="파이프 작동 방식"/>
이동 중인 아이템이 갈 곳이 없으면 파이프에서 바닥으로 떨어집니다.
파이프 마개를 쓰거나 파이프를 다른 색으로 칠하면 파이프끼리 연결되지 않게 할 수 있습니다.

<chapter name="동력 공급"/>
엔진, 전력 어댑터, 파이프 펄서 같은 모든 MJ 동력원에서 동력을 받을 수 있습니다.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
동력 펄스를 받을 때마다 받은 동력을 사용해 앞의 블록을 부수려고 시도합니다.
펄스의 동력이 클수록 블록을 부수는 데 더 많은 동력이 쓰입니다.

<usages stack="buildcraftreborn:pipe_stripes_item"/>