<lore>
Imagine um tubo capaz de colocar blocos, quebrar blocos e usar blocos. Este é o tubo que você está imaginando.
</lore>
<no_lore>
O Tubo de Transporte Listrado serve para quebrar blocos, colocar blocos, usar os itens que passam por ele e transportá-los para um tubo ou inventário conectado.
</no_lore>

<recipes stack="buildcraftreborn:pipe_stripes_item"/>

<chapter name="Funcionamento do tubo"/>
Quando alimentado, ele quebra o bloco logo à sua frente e o passa para o tubo ou inventário adjacente.
Quando um bloco passa por ele, ele coloca o bloco logo à sua frente.
Quando um item passa por ele, ele tenta 'usá-lo' no bloco ou na entidade à sua frente.

<chapter name="Direção do tubo"/>
Um Tubo de Transporte Listrado precisa estar 'apontando' para uma direção para agir sobre o bloco à sua frente.
Com vários tubos ou inventários conectados, ele perde a capacidade de agir sobre o mundo.

<chapter name="Mecânica do tubo"/>
Se os itens em trânsito não tiverem para onde ir, eles saem do tubo e caem no chão.
Para impedir que os tubos se conectem, você pode usar Tampas de Tubo ou pintá-los de cores diferentes.

<chapter name="Alimentação"/>
Ele pode receber energia de qualquer fonte de MJ, como um motor, um adaptador de energia ou um pulsar de tubo.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
A cada pulso de energia que recebe, ele usa essa energia para tentar quebrar o bloco à sua frente.
Quanto mais energia um pulso tiver, mais energia é usada para quebrar o bloco.

<usages stack="buildcraftreborn:pipe_stripes_item"/>