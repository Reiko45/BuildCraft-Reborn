<lore>
液体を取り出して、別の場所へ運びたい？動力を与えて、流れていくのを眺めるだけです。
</lore>
<no_lore>
木の防水パイプは、動力を与えると、タンクから液体を搬出します。
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_fluid"/>

<chapter name="パイプの仕組み"/>
動力を与えると、液体を搬出し、接続している他のタンクやパイプへ輸送します。
一部の機械は、動力がなくても木の防水パイプに直接液体を出力できます。
液体はパイプの不透明な面から搬出され、透明な面の方向へ送られます。
透明な面が複数ある場合は、液体はそれぞれの方向へ均等に分かれます。

一部の機械は、動力がなくても木の防水パイプに直接液体を出力できます。
パイプ栓を使うか、パイプを別の色に塗ると、パイプ同士の接続を防げます。

<chapter name="動力の供給"/>
エンジン、動力アダプター、パイプパルサーなど、あらゆるMJ動力源から動力を受け取れます。
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
動力のパルスを受け取るたびに、隣接するタンクから液体を搬出します。
パルスの動力が大きいほど、1回のパルスで搬出される液体の量が増えます。

<no_detail>
木の防水パイプは、隣接するパイプやタンクへ毎秒バケツ0.2杯分の液体を輸送します。
防水パイプには内部タンクがあり、各面にバケツ1杯分、中央にバケツ1杯分の液体を保持できます。
</no_detail>

<usages stack="buildcraftreborn:pipe_wood_fluid"/>