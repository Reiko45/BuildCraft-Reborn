<lore>
Usar uma porta lógica para extrair certos itens é a mais nova revolução técnica em tecnologia de filtragem.
</lore>
<no_lore>
O Tubo de Transporte de Emzuli é um tubo controlado por porta lógica que serve para extrair de um inventário os itens determinados por seu filtro quando recebe energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Mecânica do tubo"/>
Quando alimentado, ele extrai os itens selecionados no filtro e os transporta para quaisquer outros inventários ou tubos conectados.
A extração acontece pelo lado sólido do tubo, e os itens são transportados para os lados transparentes.
Se ele tiver vários lados transparentes, os itens seguem em uma direção aleatória.
Se os itens extraídos não tiverem para onde ir, eles saem do tubo e caem no chão.

Algumas máquinas podem enviar itens diretamente para um Tubo de Emzuli de forma automática, sem que o tubo precise de energia (nesse caso, os filtros não são respeitados).
Para impedir que os tubos se conectem, você pode usar Tampas de Tubo ou pintá-los de cores diferentes.

<chapter name="Filtragem"/>
A interface do Tubo de Transporte de Emzuli mostra quatro cores para quatro opções de filtragem separadas.
Colocar itens nos espaços permite definir o que cada filtro extrai.
As cores dos pincéis ao lado dos espaços determinam de que cor os itens serão pintados ao serem extraídos, e podem ser alteradas clicando neles.
Para escolher qual dos quatro filtros é extraído, é preciso colocar nele uma porta lógica com uma ação de Extração Predefinida colorida. Por exemplo: Sinal de Redstone -> Extração Predefinida Vermelha.

Uma Lista pode ser usada em um espaço de filtro para ampliar as opções de filtragem.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Alimentação"/>
Ele pode receber energia de qualquer fonte de MJ, como um motor, um adaptador de energia ou um pulsar de tubo.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
A cada pulso de energia que recebe, ele extrai itens do inventário adjacente.
Quanto mais energia um pulso tiver, mais itens são extraídos por pulso.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>