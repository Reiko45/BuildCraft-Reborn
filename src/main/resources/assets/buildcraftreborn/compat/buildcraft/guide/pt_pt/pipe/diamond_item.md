<lore>
Escolher que itens podem ir para onde pode ser útil para o teu sistema.
</lore>
<no_lore>
O Tubo de Transporte de Diamante serve para transportar itens de um tubo, numa direção definida pelo seu filtro, para outro tubo ou inventário adjacente.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_item"/>

<chapter name="Definir o filtro"/>
Cada lado do Tubo de Transporte de Diamante distingue-se por uma cor diferente.
Abrir a interface do Tubo de Transporte de Diamante permite-te configurar um filtro.
Colocar os itens que queres no espaço de uma certa cor permite-te filtrar que itens podem ir em que direção.
Ter vários dos mesmos itens numa cor permite-te dar «peso» ao sítio para onde os teus itens vão.

Podes usar uma Lista num espaço de filtragem para teres opções de filtragem alargadas.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Funcionamento do tubo"/>
Se houver várias direções possíveis para os itens, estes são distribuídos entre elas (consoante o peso que lhes deres).
Qualquer direção válida sem filtro definido deixa passar qualquer item, mas as direções filtradas têm prioridade.
Os itens não voltam para trás na direção de onde vieram; seguem apenas noutras direções.
Se não houver nenhum tubo ligado, ou se o inventário adjacente estiver cheio e o item não tiver mais para onde ir, o item sai do tubo e cai no chão.

Algumas máquinas conseguem enviar itens diretamente para um Tubo de Transporte de Diamante, de forma automática, sem precisarem de passar por um tubo de extração.
Usar Plugues de Tubos ou pintar os tubos de cores diferentes pode impedir que os tubos se liguem.

<usages stack="buildcraftreborn:pipe_diamond_item"/>