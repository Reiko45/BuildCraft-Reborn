<lore>
Imagina um tubo capaz de colocar blocos, partir blocos e usar blocos. É exatamente esse o tubo que estás a imaginar.
</lore>
<no_lore>
O Tubo de Transporte Listrado serve para partir blocos, colocar blocos e usar os itens que lhe são enviados, e para os transportar para um tubo/inventário ligado.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Operação do tubo"/>
Quando recebe energia, parte o bloco diretamente à sua frente e passa-o para o tubo ou inventário adjacente.
Quando lhe é enviado um bloco, coloca esse bloco diretamente à sua frente.
Quando lhe é enviado um item, tenta «usá-lo» no bloco/entidade à sua frente.

<chapter name="Direção do tubo"/>
Um Tubo de Transporte Listrado tem de estar «apontado» numa direção para afetar o bloco à sua frente.
Com vários tubos/inventários ligados, perde a capacidade de agir sobre o mundo.

<chapter name="Funcionamento do tubo"/>
Se os itens em trânsito não tiverem para onde ir, saem do tubo e caem no chão.
Usar Plugues de Tubos ou pintar os tubos de cores diferentes pode impedir que os tubos se liguem.

<chapter name="Alimentação"/>
Pode receber energia de qualquer fonte de energia MJ, como um motor, um adaptador de energia ou um pulsar de tubo.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Por cada impulso de energia que recebe, usa a energia recebida para tentar partir o bloco à sua frente.
Quanto mais energia tiver um impulso, mais energia é usada para partir o bloco.

<usages stack="buildcraftreborn:pipe_stripes_item"/>