# Extensões no GeckoView

O Snav usa `org.mozilla.geckoview:geckoview:160.0.20261116130455` e um
`GeckoRuntime` compartilhado. O gerenciador utiliza `WebExtensionController`
para instalar, listar, ativar, desativar, atualizar e desinstalar. O Gecko é a
fonte de verdade para instalações persistentes, versões e permissões. O Snav
armazena somente a origem informada na instalação, sem os parâmetros do link.

## Instalação

Em **Ferramentas > Extensões**, pesquise o catálogo recomendado da Mozilla,
indique um link HTTPS ou selecione um arquivo XPI. Um clique de usuário em um
link HTTPS com caminho terminado em `.xpi` também oferece a instalação. Para
links que não expõem esse sufixo, use o gerenciador ou escolha o arquivo.
Catálogo e downloads enviam solicitações aos servidores envolvidos; os
redirecionamentos são limitados a cinco e cada destino deve usar HTTPS, sem
credenciais. O catálogo é filtrado por Android e versão 160; resultados do
catálogo não garantem compatibilidade com todas as capacidades do Snav.

O arquivo é copiado para o cache privado, com limites de tamanho compactado e
descompactado e verificação de estrutura. Downloads do catálogo são conferidos
contra o SHA-256 informado pela API da Mozilla. O verificador do Snav lê o
manifesto e avisa sobre recursos de integração conhecidos que ainda são
parciais; não é uma análise completa das APIs usadas. O Gecko valida manifesto,
compatibilidade e assinatura Mozilla. O usuário confirma os avisos e, depois,
as permissões apresentadas pelo `PromptDelegate`; navegação privada não é
concedida. Quando a tela de consentimento não está ativa, a solicitação é
negada. O arquivo temporário é apagado após resultado ou cancelamento.

## Gerenciamento e limites

A tela de detalhes apresenta estado, versão, descrição, origem e permissões.
É possível revogar as permissões opcionais concedidas, não as obrigatórias.
A atualização usa `WebExtensionController.update`; novas permissões requerem
consentimento. O Snav integra ações de navegador e página e abre seus popups em
uma sessão Gecko separada, com navegação externa HTTPS por ação do usuário.

Ainda existem limitações: o navegador reutiliza uma sessão Gecko ao alternar
abas, não implementa todos os delegates de criação/gerenciamento de abas nem
os menus de contexto de extensões, e não disponibiliza um canal de mensagens
nativas genérico a extensões de terceiros. Manifestos V2 e V3, event pages,
content scripts, storage, webRequest e declarativeNetRequest dependem do
suporte real do Gecko e da extensão. O verificador não garante que uma API
utilizada indiretamente esteja disponível. Não há garantia de compatibilidade
com extensões para Firefox desktop ou Chrome.

As verificações locais não substituem testes de instalação, reinicialização,
atualização, permissões e falhas com XPIs assinados, nem a avaliação em várias
versões do Android. Consulte os jobs do PR antes da distribuição.

## Desenvolver uma extensão

Use as APIs WebExtensions compatíveis com Firefox para Android, forneça
`manifest.json`, versão e um ID Gecko, solicite apenas as permissões necessárias
e faça a assinatura pela Mozilla antes de distribuir. Teste as funcionalidades no
Snav; uma instalação aceita não garante que todos os delegates estejam
implementados. Para extensões embutidas privilegiadas, a comunicação nativa tem
regras diferentes e não deve ser exposta indiscriminadamente a terceiros.

Referências oficiais: [WebExtensions no GeckoView](https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html),
[WebExtensionController da linha Firefox 160](https://github.com/mozilla-firefox/firefox/blob/2887d9de1f69/mobile/android/geckoview/src/main/java/org/mozilla/geckoview/WebExtensionController.java),
[API do catálogo Mozilla](https://mozilla.github.io/addons-server/topics/api/addons.html) e
[documentação WebExtensions](https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions).
A referência de código é a tag `FIREFOX_160_0_RELEASE`, não o artefato binário
exato `160.0.20261116130455`.
