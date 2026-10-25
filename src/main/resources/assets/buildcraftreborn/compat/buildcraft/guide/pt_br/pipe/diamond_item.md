<lore>
Escolher quais itens podem ir para onde pode ser útil para o seu sistema.
</lore>
<no_lore>
O Tubo de Transporte de Diamante serve para transportar itens de um tubo, na direção definida por seu filtro, para outro tubo ou inventário adjacente.
</no_lore>

<recipes stack="buildcraftreborn:pipe_diamond_item"/>

<chapter name="Configurando o filtro"/>
Cada lado do Tubo de Transporte de Diamante é identificado por uma cor diferente.
Ao abrir a interface do Tubo de Transporte de Diamante, você pode configurar um filtro.
Colocar os itens desejados nos espaços de uma cor permite filtrar quais itens podem ir em cada direção.
Colocar o mesmo item várias vezes em uma cor permite dar mais 'peso' à direção para onde seus itens vão.

Uma Lista pode ser usada em um espaço de filtro para ampliar as opções de filtragem.
<link to="buildcraftreborn:item/list"/> 

<chapter name="Mecânica do tubo"/>
Se houver várias direções possíveis para os itens, eles serão distribuídos entre elas (de acordo com o peso que você der).
Qualquer direção válida sem filtro deixa passar qualquer item, mas as direções filtradas têm prioridade.
Os itens não voltam na direção de onde vieram; seguem sempre em outra direção.
Se não houver nenhum tubo conectado, ou se o inventário adjacente estiver cheio e o item não tiver mais para onde ir, ele sai do tubo e cai no chão.

Algumas máquinas podem enviar itens diretamente para um Tubo de Transporte de Diamante de forma automática, sem precisar de um tubo de extração.
Para impedir que os tubos se conectem, você pode usar Tampas de Tubo ou pintá-los de cores diferentes.

<usages stack="buildcraftreborn:pipe_diamond_item"/>