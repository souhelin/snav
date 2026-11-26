# Revisão do GeckoView e da interface — 26/11/2026

## Identificador

`namespace`, `applicationId`, pacotes Java/Kotlin, testes, nome XML da view
customizada e filtro de testes do CI usam `io.github.souhelin.snav`.
O nome de exibição continua **Snav Browser**. A autoridade do FileProvider
continua derivada de `${applicationId}.fileprovider`.

**Atenção:** o Android considera o novo identificador outro aplicativo.
Não há atualização da instalação anterior nem migração automática de dados,
permissões, favoritos ou extensões. Exporte os favoritos antes da troca.
As configurações de assinatura não foram alteradas.

## Correções implementadas nesta revisão

- Tela 3 sem divisórias entre seções e sem contorno nos botões de destinos.
- Ícones de voltar preservados, com tint explícito `colorOnSurface` nas
  ferramentas e nos painéis internos; isso inclui abas, biblioteca e downloads.
- Informações do site removidas da barra e acessíveis pelo menu de três pontos.
- Ações de página não são criadas no menu da tela inicial/ferramentas. Com uma
  página utilizável, aparecem informações, favoritos, fixação, cópia,
  tradução, leitura e abertura externa. Recarregar/interromper permanece
  contextual à tela de navegação.
- Barra de endereço recolhe ao avançar na rolagem e reaparece ao voltar na
  rolagem ou chegar ao topo. Limiar de 24 dp evita oscilações; transições de
  180 ms usam translação. A margem do conteúdo muda uma vez por transição,
  não a cada quadro. Sem espaço permanente reservado à barra oculta.
- A sessão é inativada/desfocada fora da tela 2, quando coberta por um painel
  interno e no `onPause`. É reativada ao retornar com a Activity em primeiro
  plano, sem fechar a sessão, perder seu histórico ou recarregar a página.
  A ponte `MediaSession` pausa a reprodução ao ocultar a navegação; retornar
  não força reprodução automática.

### Permissões

Foram encontrados caminhos que concluíam a autorização do conteúdo sem
obter a permissão Android correspondente, além da possibilidade de pedir
localização precisa sem aproximada e de substituir callbacks pendentes.

- Localização autorizada pelo usuário também passa pelas permissões Android.
- Ao solicitar localização precisa, solicita-se a aproximada junto, conforme
  exigido no Android 12+. A aproximada concedida é aceita.
- Câmera/microfone possuem fallback de solicitação Android após o consentimento
  do site, caso o Gecko ainda não tenha solicitado as permissões do aplicativo.
- Solicitações Android concorrentes não substituem callbacks existentes;
  uma segunda solicitação de mídia também não apaga a primeira pendente.
- Negação permanente do Android oferece acesso às configurações do aplicativo.
- Novas solicitações de sites são recusadas na tela inicial e em painéis
  internos. A origem é conferida novamente antes de concluir uma autorização.
- Tipos de permissão não suportados continuam negados. HTTP/TLS não recebem
  exceções. Permissões não são concedidas automaticamente por esta correção.

Decisões anteriormente bloqueadas pelo Gecko devem ser revistas em
**Menu > Informações do site > Permissões**. Permissões do Android também
podem precisar ser reativadas nas configurações do sistema.

### Prompts: lacuna real encontrada

`session.setPromptDelegate(null)` **não** fornece UI pronta de diálogos e
arquivos. A documentação anterior da migração dizia o contrário.

O novo `GeckoPromptController` fornece diálogos Material para `alert`,
`confirm`, entrada de texto JavaScript, saída com alterações não salvas e
reenvio de formulário POST. O seletor Android permite envio de um ou vários
arquivos, conforme o pedido da página, usando concessões de leitura de URIs
`content://`, sem permissão ampla de armazenamento.

Cancelamento, retorno sem arquivo, substituição de prompt, encerramento da
Activity e dispensa pelo Gecko concluem ou descartam a solicitação sem
reutilizar um callback para outro site. O seletor não atende seleção de pasta
nem captura direta pela câmera: o atributo `capture` ainda abre o seletor de
documentos, não uma câmera integrada.

## O que ainda falta ou tem cobertura limitada

A presença de uma API no GeckoView não significa que o aplicativo ofereça
sua interface ou implementação. Esta lista resulta da inspeção do código,
não de certificação de compatibilidade com todos os sites.

| Área | Situação / próximo trabalho |
|---|---|
| Abas reais | As abas do app alternam estados de uma única `GeckoSession`; não são sessões independentes com execução simultânea. |
| Formulários e autenticação | Ainda faltam UI de seleção HTML (`ChoicePrompt`), data/hora, cor e autenticação HTTP (`AuthPrompt`). Os handlers padrão não equivalem a UI pronta. |
| Arquivos e captura | Validar provedores locais/nuvem; implementar captura direta e pastas separadamente, sem ampliar permissões indiscriminadamente. |
| Preenchimento/autenticação avançados | Sem gerenciador próprio de senhas/autofill ou UI completa para prompts de identidade/FedCM. WebAuthn/passkeys precisam de validação específica. |
| Mídia e PiP | Pausa básica implementada, mas não há controlador PiP completo, notificação de mídia ou integração de áudio do Android. O manifesto sozinho não implementa PiP. |
| Pausa | `setActive(false)` reduz atividade/consumo e `MediaSession.pause()` pausa reprodução; não garante suspensão total de JavaScript, rede, WebRTC ou captura em andamento. Downloads confirmados continuam independentes. |
| Permissões especiais | XR, rede/dispositivo local e outras classes não contempladas são negadas explicitamente. Mídia seleciona câmera/microfone; não há compartilhamento de tela/dispositivo seletivo. |
| Extensões | Integrações de abas, contexto e mensagens nativas continuam limitadas; consulte a documentação de extensões. |
| Navegador padrão | O manifesto não registra ainda os intent-filters web/BROWSABLE necessários para abrir links externos como navegador padrão. |
| Downloads longos | O executor pertence à Activity; não há serviço de download resiliente à morte do processo. |
| Dados por site | A limpeza do Gecko por host não cobre automaticamente dados de origens de terceiros. |

## Verificação local inicial desta revisão

- `git diff --check`: sem erros.
- Análise sintática de todos os Java com Tree-sitter e parse de todos os XML:
  sem erros. Isso **não substitui compilação ou verificação de tipos Android**.
- Adicionados testes unitários para direção/limiar de rolagem e testes
  instrumentados para inativação/retomada da sessão, espaço da barra,
  ausência de divisórias, identidade e instalação do delegate de prompts.
- Tentativa de `gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug`
  impedida por ausência de Gradle, JDK de desenvolvimento e SDK Android neste
  ambiente. Nenhum APK foi compilado ou assinado aqui.
- Testes instrumentados e validação visual não executados: sem emulador ou
  dispositivo conectado. As mudanças precisam passar pelo workflow Android.

### Diagnóstico e correção do CI do PR #13

As execuções [45808002707](https://github.com/souhelin/snav/actions/runs/45808002707)
e [45807961156](https://github.com/souhelin/snav/actions/runs/45807961156),
no commit `a91e6a0`, aprovaram `verify` (build debug e testes unitários) e
`material-legacy` (Android 10/API 29). As anotações do job `emulator` registraram
uma falha de teste, `shortPageCanRefreshAtTop`, e uma falha independente no
upload de relatórios por cota de armazenamento de artefatos esgotada.

O teste de página curta exigia atualização por um gesto `UNHANDLED`, contrário
à política conservadora já descrita na branch de origem. Foi substituído por
um teste ativo que exige ausência de atualização/navegação acidental e
recarregamento explícito bem-sucedido. A implementação de gestos não foi
relaxada: pull-to-refresh em página curta continua sendo uma limitação, não
uma funcionalidade corrigida. Veja [os detalhes](PULL_TO_REFRESH.md).

O CI agora publica contagens de testes no resumo do job e falhas detalhadas
nas anotações, sem depender de artefatos. Somente o upload de relatórios é
best-effort, restrito a falhas e com retenção de três dias. A execução dos
testes, ausência de relatórios válidos e falhas de Gradle/emulador continuam
bloqueantes. Seis testes Python do publicador de resumos foram executados
localmente e passaram. A cota da conta GitHub não foi alterada nem foram
apagados artefatos existentes.

Essa correção não muda cores, ícones, componentes MD3, permissões ou políticas
de segurança do aplicativo. A nova execução Android do commit de correção
precisa ser conferida no PR; os resultados acima pertencem ao commit anterior.

### Roteiro obrigatório em dispositivo/CI

1. Instalar o novo pacote; pesquisar, digitar, enviar pelo teclado e pelo botão.
2. Abrir páginas longas/curtas; rolar nos dois sentidos, topo, fling,
   atualização por arrasto, teclado, rotação e vídeo em tela cheia.
3. Confirmar que o menu inicial não contém ações de página; abrir informações
   pelo menu depois de navegar. Conferir contraste claro/escuro/dinâmico.
4. Em Android 12+, testar localização aproximada e precisa; em todos os
   Android suportados, permitir/negar câmera, microfone, ambos e localização.
   Repetir após negação permanente e após trocar de origem durante o pedido.
5. Testar `alert`, `confirm`, `prompt`, beforeunload e POST; uploads único e
   múltiplo, cancelamento/voltar, provedores de nuvem e navegação durante prompt.
6. Voltar à tela 1 durante mídia, retornar ao navegador, abrir ferramentas,
   trocar de Activity e recriar o processo; verificar sessão, pausa e retomada.
7. Executar testes unitários, instrumentados, build release/R8 e assinatura
   com o keystore existente no CI, sem expor seus segredos.
