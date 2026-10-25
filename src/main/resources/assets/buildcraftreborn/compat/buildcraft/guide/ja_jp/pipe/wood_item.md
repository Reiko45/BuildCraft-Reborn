<lore>
アイテムを取り出して、別の場所へ運びたい？動力を与えて、流れていくのを眺めるだけです。
</lore>
<no_lore>
木の輸送パイプは、動力を与えると、インベントリからアイテムを搬出します。
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="パイプの仕組み"/>
動力を与えると、アイテムを搬出し、接続している他のインベントリやパイプへ輸送します。
アイテムはパイプの不透明な面から搬出され、透明な面の方向へ輸送されます。
透明な面が複数ある場合は、アイテムはそのうちのランダムな方向へ進みます。
アイテムに行き場がない場合は、搬出されたアイテムはパイプから地面に落ちます。

一部の機械は、パイプに動力を与えなくても、木の輸送パイプに直接アイテムを自動で出力できます。
パイプ栓を使うか、パイプを別の色に塗ると、パイプ同士の接続を防げます。

<chapter name="動力の供給"/>
エンジン、動力アダプター、パイプパルサーなど、あらゆるMJ動力源から動力を受け取れます。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
動力のパルスを受け取るたびに、隣接するインベントリからアイテムを搬出します。
パルスの動力が大きいほど、1回のパルスで搬出されるアイテムの数が増えます。

<usages stack="buildcraftreborn:pipe_wood_item"/>