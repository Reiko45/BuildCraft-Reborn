<lore>
Queres retirar um certo fluido e transportá-lo para outro lado? Basta dares-lhe energia e vê-lo correr.
</lore>
<no_lore>
O Tubo de fluidos de madeira e diamante serve para extrair fluidos selecionados de um tanque quando recebe energia.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_wood_fluid"/>

<chapter name="Funcionamento do tubo"/>
Quando recebe energia, extrai o fluido selecionado pelo filtro para quaisquer outros tanques/tubos ligados.
A extração de fluidos faz-se a partir do lado opaco do tubo, em direção aos lados transparentes.
Se tiver vários lados transparentes, os fluidos dividem-se de forma igual por cada direção.

Algumas máquinas conseguem enviar fluidos diretamente para este tubo sem precisarem de energia (neste caso, o filtro não é respeitado).
Usar Plugues de Tubos ou pintar os tubos de cores diferentes pode impedir que os tubos se liguem.

<chapter name="Filtragem"/>
Abrir a interface do Tubo de fluidos de madeira e diamante permite-te configurar um filtro.
Colocar nos espaços um balde (ou outro recipiente de fluido) do fluido que queres permite-te filtrar o que é extraído.
Podes usar as opções Lista de permissões e Lista de bloqueios para mudar o que o teu filtro faz:
Lista de permissões: só são extraídos os fluidos selecionados.
Lista de bloqueios: é extraído qualquer fluido exceto o fluido selecionado.

<chapter name="Alimentação"/>
Pode receber energia de qualquer fonte de energia MJ, como um motor, um adaptador de energia ou um pulsar de tubo.
<link to="buildcraftreborn:block/engine_wood"/>
<link to="buildcraftreborn:item/plug_power_adaptor"/>
<link to="buildcraftreborn:item/plug_pulsar"/> 
Por cada impulso de energia que recebe, extrai fluido do tanque adjacente.
Quanto mais energia tiver um impulso, mais fluido é extraído por impulso.

<no_detail>
O Tubo de fluidos de madeira e diamante transporta fluidos para tubos e tanques adjacentes a 1,6 baldes por segundo.
Os tubos de fluidos têm um depósito interno que leva 1 balde por lado e 1 balde no centro.
</no_detail>

<usages stack="buildcraftreborn:pipe_diamond_wood_fluid"/>