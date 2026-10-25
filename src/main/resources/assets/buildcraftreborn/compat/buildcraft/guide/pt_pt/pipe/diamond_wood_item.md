<lore>
Queres retirar certos itens e transportá-los para outro lado? Basta dares-lhe energia e vê-los seguir caminho.
</lore>
<no_lore>
O Tubo de transporte de madeira e diamante serve para extrair de um inventário os itens selecionados pelo seu filtro quando recebe energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_item"/>

<chapter name="Funcionamento do tubo"/>
Quando recebe energia, extrai os itens selecionados no filtro e transporta-os para quaisquer outros inventários/tubos ligados.
A extração de itens faz-se a partir do lado opaco do tubo, e os itens são transportados em direção aos lados transparentes.
Se tiver vários lados transparentes, os itens seguem numa direção aleatória.
Se não tiverem para onde ir, os itens extraídos saem do tubo e caem no chão.

Algumas máquinas conseguem enviar itens diretamente para um Tubo de transporte de madeira e diamante, de forma automática, sem que o tubo precise de energia (neste caso, o filtro não é respeitado).
Usar Plugues de Tubos ou pintar os tubos de cores diferentes pode impedir que os tubos se liguem.

<chapter name="Filtragem"/>
Abrir a interface do Tubo de transporte de madeira e diamante permite-te configurar um filtro.
Colocar nos espaços os itens que queres permite-te filtrar o que é extraído.
Podes usar as opções Lista de permissões, Lista de bloqueios ou Rotação para mudar o que o teu filtro faz:
Lista de permissões: só são extraídos os itens selecionados.
Lista de bloqueios: são extraídos todos os itens exceto os selecionados.
Rotação: extrai os itens exatamente pela ordem selecionada nos espaços de filtro acima (da esquerda para a direita)

Podes usar uma Lista num espaço de filtragem para teres opções de filtragem alargadas.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Alimentação"/>
Pode receber energia de qualquer fonte de energia MJ, como um motor, um adaptador de energia ou um pulsar de tubo.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Por cada impulso de energia que recebe, extrai itens do inventário adjacente.
Quanto mais energia tiver um impulso, mais itens são extraídos por impulso.

<usages stack="buildcraftreborn:pipe_diamond_wood_item"/>