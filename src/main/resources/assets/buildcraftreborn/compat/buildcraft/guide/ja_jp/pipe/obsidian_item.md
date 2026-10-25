<lore>
ワールドに落ちたアイテムを拾って運ぶ、これぞ未来！
</lore>
<no_lore>
黒曜石の輸送パイプは、ワールドに落ちているアイテムを拾い、接続先のパイプやインベントリへ輸送するのに使います。
</no_lore>

<recipes stack="buildcraftreborn:pipe_obsidian_item"/>

<chapter name="パイプの動作"/>
動力を与えると、上にある3x4x3の範囲のアイテムを拾えます。
動力がなくても、パイプに触れたアイテムは拾います。

<chapter name="パイプの向き"/>
黒曜石の輸送パイプが効果範囲を持つには、ある方向を「向いて」いる必要があります。
パイプが複数の接続を持つと、ワールドからアイテムを拾う能力を失います。

<chapter name="パイプの仕組み"/>
受け取ったアイテムに行き場がない場合は、パイプから地面に落ちます。
パイプ栓を使うか、パイプを別の色に塗ると、パイプ同士の接続を防げます。

<chapter name="動力の供給"/>
エンジン、動力アダプター、パイプパルサーなど、あらゆるMJ動力源から動力を受け取れます。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
動力のパルスを受け取るたびに、地面からアイテムを拾います。
パルスの動力が大きいほど、1回のパルスで拾うアイテムの数が増えます。

<usages stack="buildcraftreborn:pipe_obsidian_item"/>