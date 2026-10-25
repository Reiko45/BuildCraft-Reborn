<lore>
Quer tirar itens e levá-los para outro lugar? É só fornecer energia e vê-los fluir.
</lore>
<no_lore>
O Tubo de Transporte de Madeira serve para extrair itens de um inventário quando recebe energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_wood_item"/>

<chapter name="Mecânica do tubo"/>
Quando alimentado, ele extrai os itens e os transporta para quaisquer outros inventários ou tubos conectados.
A extração acontece pelo lado sólido do tubo, e os itens são transportados para os lados transparentes.
Se ele tiver vários lados transparentes, os itens se distribuem aleatoriamente entre eles.
Se os itens extraídos não tiverem para onde ir, eles saem do tubo e caem no chão.

Algumas máquinas podem enviar itens diretamente para um Tubo de Transporte de Madeira de forma automática, sem que o tubo precise de energia.
Para impedir que os tubos se conectem, você pode usar Tampas de Tubo ou pintá-los de cores diferentes.

<chapter name="Alimentação"/>
Ele pode receber energia de qualquer fonte de MJ, como um motor, um adaptador de energia ou um pulsar de tubo.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
A cada pulso de energia que recebe, ele extrai itens do inventário adjacente.
Quanto mais energia um pulso tiver, mais itens são extraídos por pulso.

<usages stack="buildcraftreborn:pipe_wood_item"/>