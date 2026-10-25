<lore>
Uma receita, dobrada e enviada pelo correio.
</lore>
<no_lore>
O Empacotador de Receitas empacota os ingredientes de uma receita de fabricação em um pacote, pronto para ser fabricado por uma Mesa de Estampagem.
</no_lore>
<chapter name="O padrão"/>
A grade 3x3 é a grade de fabricação da receita. Clique em um espaço segurando um item para que esse espaço peça esse item. Clique em um espaço vazio com a mão vazia para que ele peça qualquer coisa (ele acende) e clique de novo para desativá-lo. Clicar com a mão vazia em um espaço que pede um item o limpa.
Todos os espaços que pedem "qualquer coisa" recebem o mesmo item, um que nenhum outro espaço peça pelo nome.
<recipes_usages stack="buildcraftreborn:packager"/>
<chapter name="Empacotamento"/>
Coloque papel no espaço do canto superior direito. A cada quarto de segundo, o empacotador pega um item para cada espaço aceso, primeiro dos seus nove espaços de armazenamento e depois dos inventários dos seus quatro lados, e transforma uma folha de papel em um pacote com cada item em seu espaço. O pacote sai pelo espaço abaixo do papel, e o empacotador espera até que ele seja retirado.
Também é possível colocar um pacote de volta no lugar do papel para preencher seus espaços vazios: o empacotador não colocará nada onde o pacote já tiver algo.
<chapter name="Automação"/>
Os tubos inserem ingredientes nos espaços de armazenamento pelas laterais (somente itens que completem uma pilha já existente ou que o padrão possa usar), papel ou pacotes por cima ou por baixo, e retiram pacotes por qualquer lado. Robôs entregadores podem levar os pacotes até uma Mesa de Estampagem.
