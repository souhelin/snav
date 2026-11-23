# Atualização por arrasto no GeckoView

A implementação atual é uma medida provisória baseada em Views, não em
`PullToRefreshBox` do Compose. Ela observa os eventos sem consumi-los e exige
110 dp, um único dedo, predominância vertical, início no topo e soltura para
recarregar. Os testes locais do classificador cobrem limites e cancelamentos.

A `RefreshAwareGeckoView` consulta `onTouchEventForDetailResult` no início do
contato, como o adaptador `NestedGeckoView` da Mozilla. O reconhecimento é
bloqueado enquanto a resposta não chega, quando o site trata o contato, quando
há rolagem possível para cima, quando a página não permite overscroll vertical
ou quando o resultado é ambíguo. Isso reduz ativações acidentais, mas também
pode impedir a atualização em páginas curtas e quando a resposta é lenta.

O GeckoView usado pelo aplicativo não implementa `NestedScrollingChild`: sua
rolagem não entrega os deltas não consumidos esperados pelo nested scroll do
Compose. Apenas envolver `GeckoView` em `PullToRefreshBox` não faria o gesto
funcionar. Uma ponte precisa distinguir a rolagem efetivamente consumida pela
página, seus elementos internos, seleção e zoom, e encaminhar os deltas sem
furtar eventos. A posição recebida pelo `ScrollDelegate` também não identifica
rolagens em elementos aninhados da página. As informações iniciais do Gecko
ajudam, mas a observação atual **não garante** que gestos manipulados pelo conteúdo da página nunca provoquem uma
atualização; não substitui a integração Compose solicitada.

A integração definitiva deve ser implementada e testada com o GeckoView e em
emuladores/dispositivos antes de ser considerada concluída. Verificar páginas
longas e curtas, rolagens aninhadas e diagonais, textos selecionáveis, gestos
próprios da página, pinça, aba vazia, mudanças de página e atualização repetida.

Referência de integração Gecko: [NestedGeckoView da Mozilla](https://github.com/mozilla-firefox/firefox/blob/2887d9de1f69/mobile/android/android-components/components/browser/engine-gecko/src/main/java/mozilla/components/browser/engine/gecko/NestedGeckoView.kt).
