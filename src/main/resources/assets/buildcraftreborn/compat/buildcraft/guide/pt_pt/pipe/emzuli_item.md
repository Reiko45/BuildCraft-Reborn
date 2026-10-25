<lore>
Usar um gate para extrair certos itens é a mais recente revolução técnica na tecnologia de filtragem.
</lore>
<no_lore>
O Tubo de Transporte Emzuli é um tubo controlado por gate que serve para extrair de um inventário os itens selecionados pelo seu filtro quando recebe energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_emzuli_item"/>

<chapter name="Funcionamento do tubo"/>
Quando recebe energia, extrai os itens selecionados no filtro e transporta-os para quaisquer outros inventários/tubos ligados.
A extração de itens faz-se a partir do lado opaco do tubo, e os itens são transportados em direção aos lados transparentes.
Se tiver vários lados transparentes, os itens seguem numa direção aleatória.
Se não tiverem para onde ir, os itens extraídos saem do tubo e caem no chão.

Algumas máquinas conseguem enviar itens diretamente para um Tubo Emzuli, de forma automática, sem que o tubo precise de energia (neste caso, os filtros não são respeitados).
Usar Plugues de Tubos ou pintar os tubos de cores diferentes pode impedir que os tubos se liguem.

<chapter name="Filtragem"/>
A interface do Tubo de Transporte Emzuli tem quatro cores para quatro opções de filtragem separadas.
Colocar itens nos espaços permite-te definir o que cada filtro extrai.
As cores dos pincéis junto aos espaços determinam a cor com que os itens são pintados ao serem extraídos, e podem ser alteradas clicando nelas.
Para selecionar qual dos quatro filtros é extraído, é preciso colocar no tubo um gate com uma ação de Extração Predefinida de uma cor. Por exemplo: Sinal de Redstone -> Extração Predefinida Vermelho.

Podes usar uma Lista num espaço de filtragem para teres opções de filtragem alargadas.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Alimentação"/>
Pode receber energia de qualquer fonte de energia MJ, como um motor, um adaptador de energia ou um pulsar de tubo.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Por cada impulso de energia que recebe, extrai itens do inventário adjacente.
Quanto mais energia tiver um impulso, mais itens são extraídos por impulso.

<usages stack="buildcraftreborn:pipe_emzuli_item"/>