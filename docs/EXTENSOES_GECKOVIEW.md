# Extensões: integração inicial com GeckoView

O aplicativo usa `org.mozilla.geckoview:geckoview:160.0.20261116130455`.
O `GeckoRuntime` é compartilhado e a Activity gerencia a sessão Gecko ativa.
Esta integração é **preliminar e não foi validada em dispositivos**; não deve
ser apresentada como suporte completo a extensões.

## Como está organizado

`ExtensionManager` usa o `WebExtensionController` do runtime para listar,
instalar, ativar, desativar, remover e atualizar extensões. O Gecko mantém as
instalações, versões, permissões e estado no próprio perfil; o aplicativo não
mantém uma cópia que possa ficar inconsistente. O Snav registra separadamente
apenas a origem informada pelo usuário (arquivo ou host HTTPS). É possível
revogar em conjunto as permissões opcionais concedidas, mas não as obrigatórias.

Para instalar, em **Ferramentas > Extensões**, selecione um XPI ou informe um
link HTTPS direto. O Snav primeiro solicita autorização para ler ou baixar o
arquivo, copia-o para o armazenamento privado (limite de 20 MiB) e verifica
limites e estrutura sem extrair o conteúdo. Em seguida, o GeckoView valida o
manifesto e a assinatura da Mozilla e solicita a decisão sobre permissões e
origens. O arquivo temporário é apagado após o resultado. Redirecionamentos
não são seguidos; o download usa os certificados e verificação de hostname
padrão do Android, sem cookies. Falhas e cancelamentos não autorizam a instalação.
Não há autorização para navegação privada. O consentimento é negado se a tela
ficar indisponível. O prompt de atualização é usado para permissões novas e o
de permissões opcionais, para requisições feitas pela extensão.

A inspeção estrutural **não valida a assinatura nem garante compatibilidade**.
A própria instalação não garante que uma extensão funcione. O usuário deve
conferir nome, permissões, sites e coleta de dados antes de aceitar.

## Limitações importantes

- Não há catálogo integrado nem instalação acionada por páginas. Pode ser
  necessário baixar manualmente um XPI assinado e selecioná-lo no Snav. Links
  que redirecionam, incluindo alguns links de catálogos, não funcionam.
- Os delegates de botões, janelas de extensão, menus de contexto e parte da
  integração com abas ainda não foram implementados. O aplicativo reutiliza uma
  sessão Gecko ao alternar suas abas; não presume isolamento de cada aba como
  uma sessão separada. Muitas extensões dependem dessas capacidades.
- A comunicação nativa descrita pela Mozilla é restrita a extensões
  privilegiadas. O Snav não registra um canal de mensagens nativas genérico para
  extensões de terceiros.
- Nenhuma verificação independente de APIs usadas por um pacote foi concluída.
  O manifesto V2 ou V3 e APIs como `tabs`, `webRequest`, `storage` e
  `declarativeNetRequest` dependem da implementação Gecko e dos delegates do
  aplicativo. Não há promessa de compatibilidade com Firefox desktop ou Chrome.
- A instalação por arquivo temporário significa que o Gecko pode registrar
  como origem um arquivo local. A origem apresentada pelo Snav corresponde ao
  host ou arquivo informado na instalação, não a uma certificação do autor.
- O estado, erros, instalação, atualização, reinicialização, consentimentos e
  limpeza de arquivos devem ser testados com XPIs realmente assinados em
  dispositivos Android antes de qualquer distribuição. Um teste unitário do
  validador, sozinho, não comprova a segurança nem a integração do sistema.

## Referências para desenvolvimento

- [Guia GeckoView de WebExtensions](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html)
- [WebExtensionController da linha Firefox 160](https://github.com/mozilla-firefox/firefox/blob/2887d9de1f69/mobile/android/geckoview/src/main/java/org/mozilla/geckoview/WebExtensionController.java)
- [Documentação WebExtensions da Mozilla](https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions)

A referência do código é a tag `FIREFOX_160_0_RELEASE` (Firefox 160.0), não o
artefato binário específico `160.0.20261116130455`; diferenças devem ser
verificadas na compilação e nos testes com a dependência exata do projeto.
