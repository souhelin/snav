# Atualização por arrasto no GeckoView

A superfície de navegação usa `PullToRefreshBox` do Material 3 Compose; a
Activity e as demais telas continuam em Views. `RefreshAwareGeckoView` encaminha
por nested scroll somente o arrasto vertical elegível e entrega todos os eventos
originais ao Gecko, sem enviá-los duas vezes. A consulta inicial a
`onTouchEventForDetailResult` segue a abordagem do
[NestedGeckoView da Mozilla](https://github.com/mozilla-firefox/firefox/blob/2887d9de1f69/mobile/android/android-components/components/browser/engine-gecko/src/main/java/mozilla/components/browser/engine/gecko/NestedGeckoView.kt).
Ela rejeita gestos tratados pelo conteúdo ou não tratados pelo controlador de
rolagem do Gecko, rolagem disponível para cima, overscroll vertical proibido e
resposta tardia após movimento significativo. O Gecko só documenta os campos
de direção como válidos para eventos tratados pelo controlador de rolagem.

O classificador exige um dedo, arrasto predominantemente vertical, limite de
110 dp e soltura. O Material 3 usado aqui tem limite posicional padrão de 80 dp
e multiplicador de arrasto de 0,5; a ponte escala os deltas nested scroll por
160/110 para que o limite corresponda a aproximadamente 110 dp do dedo. O
indicador e as animações são do próprio Compose Material 3. Ao cancelar, a ponte retrai os
deltas já enviados e não confirma a atualização. O carregamento ativo bloqueia
nova atualização e a URL é conferida novamente após a soltura.

Como o Gecko responde de forma assíncrona e algumas páginas usam rolagens ou
gestos próprios, é indispensável validar em dispositivo a interação completa:
páginas curtas e longas, topo e meio, elementos com rolagem própria,
`overscroll-behavior`, gestos horizontais/diagonais, pinça e seleção. Uma
resposta tardia pode rejeitar um gesto legítimo para evitar uma atualização
acidental. Em testes no emulador Android 35, o gesto não atualizou uma página
curta sem rolagem após essa verificação conservadora. O teste correspondente
continua falhando: essa limitação ainda precisa de uma solução que preserve os
gestos da página e a seleção de texto. O botão de recarregar do menu continua
disponível.

`RefreshGeckoIntegrationTest` executa gestos com uma sessão Gecko real e
páginas de teste no emulador; `PullGestureTrackerTest` cobre o classificador de
forma isolada. A compilação ou o teste unitário não substitui uma execução em
emulador ou dispositivo físico. Verifique o resultado do job `emulator` no PR.
