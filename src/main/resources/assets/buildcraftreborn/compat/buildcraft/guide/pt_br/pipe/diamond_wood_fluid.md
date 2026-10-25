<lore>
Quer tirar um fluido específico e levá-lo para outro lugar? É só fornecer energia e vê-lo fluir.
</lore>
<no_lore>
O Tubo de Fluido de Madeira e Diamante serve para extrair de um tanque os fluidos selecionados quando recebe energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Mecânica do tubo"/>
Quando alimentado, ele extrai o fluido selecionado pelo filtro e o envia para quaisquer outros tanques ou tubos conectados.
A extração acontece pelo lado sólido do tubo, e os fluidos seguem para os lados transparentes.
Se ele tiver vários lados transparentes, os fluidos se dividem igualmente em cada direção.

Algumas máquinas podem enviar fluidos diretamente para ele sem que ele precise de energia (nesse caso, o filtro não é respeitado).
Para impedir que os tubos se conectem, você pode usar Tampas de Tubo ou pintá-los de cores diferentes.

<chapter name="Filtragem"/>
Ao abrir a interface do Tubo de Fluido de Madeira e Diamante, você pode configurar um filtro.
Colocar nos espaços um balde (ou outro recipiente de fluido) com o fluido desejado permite filtrar o que ele extrai.
Você pode usar as opções Lista branca e Lista negra para mudar o funcionamento do filtro:
Lista branca: apenas os fluidos selecionados são extraídos.
Lista negra: qualquer fluido, exceto o selecionado, é extraído.

<chapter name="Alimentação"/>
Ele pode receber energia de qualquer fonte de MJ, como um motor, um adaptador de energia ou um pulsar de tubo.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
A cada pulso de energia que recebe, ele extrai fluido do tanque adjacente.
Quanto mais energia um pulso tiver, mais fluido é extraído por pulso.

<no_detail>
O Tubo de Fluido de Madeira e Diamante transporta fluidos para os tubos e tanques adjacentes a 1,6 balde por segundo.
Os tubos de fluido têm um tanque interno que comporta 1 balde por lado e 1 balde no centro.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>