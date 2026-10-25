<lore>
ブロックを置いたり、壊したり、使ったりできるパイプを想像してみてください。それがまさにこのパイプです。
</lore>
<no_lore>
ストライプ輸送パイプは、ブロックの破壊や設置、送り込まれたアイテムの使用を行い、それらを接続先のパイプやインベントリへ輸送するのに使います。
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="パイプの動作"/>
動力を与えると、すぐ正面にあるブロックを壊し、隣接するパイプやインベントリへ渡します。
ブロックを送り込むと、そのブロックをすぐ正面に設置します。
アイテムを送り込むと、正面のブロックやエンティティに対してそのアイテムを「使用」しようとします。

<chapter name="パイプの向き"/>
ストライプ輸送パイプが正面のブロックに作用するには、ある方向を「向いて」いる必要があります。
複数のパイプやインベントリが接続されていると、ワールドに作用する能力を失います。

<chapter name="パイプの仕組み"/>
移動中のアイテムに行き場がない場合は、パイプから地面に落ちます。
パイプ栓を使うか、パイプを別の色に塗ると、パイプ同士の接続を防げます。

<chapter name="動力の供給"/>
エンジン、動力アダプター、パイプパルサーなど、あらゆるMJ動力源から動力を受け取れます。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
動力のパルスを受け取るたびに、その動力を使って正面のブロックを壊そうとします。
パルスの動力が大きいほど、ブロックの破壊に使われる動力も多くなります。

<usages stack="buildcraftreborn:pipe_stripes_item"/>