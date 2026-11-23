# Atualização por arrasto no GeckoView

A implementação atual é uma medida provisória baseada em Views, não em
`PullToRefreshBox` do Compose. Ela observa os eventos sem consumi-los e exige
110 dp, um único dedo, predominância vertical, início no topo e soltura para
recarregar. Os testes locais do classificador cobrem limites e cancelamentos.

O GeckoView usado pelo aplicativo não implementa `NestedScrollingChild`: sua
rolagem não entrega os deltas não consumidos esperados pelo nested scroll do
Compose. Apenas envolver `GeckoView` em `PullToRefreshBox` não faria o gesto
funcionar. Uma ponte precisa distinguir a rolagem efetivamente consumida pela
página, seus elementos internos, seleção e zoom, e encaminhar os deltas sem
furtar eventos. A posição recebida pelo `ScrollDelegate` também não identifica
rolagens em elementos aninhados da página. A observação atual, portanto, **não
garante** que gestos manipulados pelo conteúdo da página nunca provoquem uma
atualização; não substitui a integração Compose solicitada.

A integração definitiva deve ser implementada e testada com o GeckoView e em
emuladores/dispositivos antes de ser considerada concluída. Verificar páginas
longas e curtas, rolagens aninhadas e diagonais, textos selecionáveis, gestos
próprios da página, pinça, aba vazia, mudanças de página e atualização repetida.
