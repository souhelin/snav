# Política de Segurança e Privacidade — Snav Browser

**Status:** especificação normativa e plano de implementação  
**Escopo:** Android nativo, Java, XML e Android WebView  
**Princípio:** privacidade por padrão, permissões mínimas e transparência.

Este documento define o comportamento que o Snav deve adotar. Itens identificados como **planejados** não devem ser apresentados como recursos já disponíveis até que exista implementação, testes e evidência no código.

## 1. Estado verificado na base atual

A inspeção inicial da branch `main` em 19/11/2026 confirmou:

- O manifesto declara `android:usesCleartextTraffic="true"` para permitir HTTP somente após o aviso explícito do Snav; a proteção depende dos interceptadores de navegação e da confirmação de downloads. Essa permissão é global no nível Android e exige revisão obrigatória em qualquer novo cliente de rede.
- O manifesto declara `android:allowBackup="false"`.
- O tratamento atual de erro SSL cancela a navegação; não deve existir opção de ignorar certificados.
- O WebView está habilitado para JavaScript por padrão e existe um controle de interface para desativá-lo.
- A limpeza atualmente visível limpa cache e histórico do WebView.
- Os diagnósticos registram eventos de navegação e host, evitando registrar o URL completo nesses eventos.
- A base inicial já bloqueava cookies de terceiros, exibia confirmação de download, ativava Safe Browsing e bloqueava conteúdo misto. A inspeção inicial não encontrou bloqueio de rastreadores, diálogo HTTPS-First ou gestão de permissões por origem.

Esses pontos são uma fotografia da base inspecionada, não uma certificação de segurança completa.

## 2. Política de privacidade por padrão

1. Coletar e persistir somente dados necessários ao funcionamento.
2. Não enviar histórico, URLs completas, conteúdo de páginas ou identificadores a serviços externos para telemetria.
3. Não registrar query strings, fragmentos, cookies, tokens, credenciais ou conteúdo digitado.
4. Diagnósticos devem ser locais, limitados em tamanho, sem dados sensíveis e com opção clara de limpeza.
5. Não habilitar permissões Android ou capacidades WebView sem uma funcionalidade que as exija.
6. Não contornar erros TLS, avisos de conteúdo misto ou políticas de origem.
7. Não executar JavaScript em páginas por conveniência sem avaliar origem, escopo e risco.
8. Não declarar proteção absoluta contra rastreamento: filtros e controles de WebView possuem limitações.

## 3. Cookies e rastreamento

### Política desejada

- Cookies próprios (first-party): **permitidos** por padrão para preservar sessões e funcionamento dos sites.
- Cookies de terceiros: **bloqueados** por padrão, com controle explícito nas configurações.
- O usuário pode revisar e alterar a política em Preferências; a escolha é persistida localmente.
- A mudança de política deve ser aplicada aos WebViews existentes e novos, sem criar instâncias desnecessárias.

### Implementação exigida

- Usar `CookieManager` e `WebSettings` de forma compatível com a API mínima do projeto.
- Aplicar `setAcceptThirdPartyCookies(webView, false)` por WebView em Android compatível; o switch persistente altera essa preferência. Em versões anteriores à API 21, a API não oferece esse controle.
- Manter cookies first-party habilitados, salvo escolha expressa do usuário.
- Não apagar todos os cookies quando o usuário solicitar a remoção de dados de apenas um site.
- Não tratar bloqueio de cookies de terceiros como bloqueio completo de rastreadores.

### Bloqueio de rastreadores de terceiros — planejado

- Implementar um bloqueador de requisições de terceiros com listas de filtros versionadas, fonte/licença documentadas e atualização segura.
- Começar por uma lista pequena e auditável; evitar bibliotecas ou listas enormes sem medir memória, CPU, tempo de inicialização e tamanho do APK.
- Integrar a decisão no ciclo de requisições do WebView, considerando limitações de `shouldInterceptRequest`, redirecionamentos, Service Workers, esquemas e versões do Android System WebView.
- Permitir exceção por site, sempre visível e reversível.
- Não bloquear indiscriminadamente recursos necessários à autenticação, pagamentos ou funcionamento básico.
- Não baixar nem executar regras remotas sem validação, controle de versão e fallback seguro.
- Exibir contagens apenas se forem calculadas de eventos reais; nunca inventar estatísticas.

## 4. Do Not Track

- Disponibilizar Do Not Track como preferência **opcional**, desativada por padrão até que a interface e a implementação estejam prontas.
- Quando ativada, enviar o sinal `DNT: 1` nos pedidos que o Snav consiga controlar de forma confiável.
- Documentar que o sinal é uma preferência declarativa e não obriga sites a respeitá-la.
- Não prometer cobertura universal: WebView não oferece um mecanismo simples e global para injetar cabeçalhos em todos os tipos de requisição. A implementação deve ser testada para navegação, sub-recursos, redirecionamentos e conteúdo de terceiros.

## 5. HTTPS-First e conexões inseguras

- Preferir HTTPS ao iniciar navegação digitada para um host sem esquema, sem alterar URLs explicitamente fornecidas pelo usuário de forma silenciosa.
- Se uma navegação HTTP permanecer necessária, mostrar um diálogo MD3 antes de continuar, identificando que a conexão não é criptografada.
- A ação principal deve ser voltar/cancelar; continuar em HTTP deve exigir escolha consciente e valer apenas para aquela navegação, salvo preferência futura explícita.
- Nunca ignorar erros de certificado TLS. O comportamento padrão é cancelar.
- Não habilitar cleartext globalmente para contornar sites incompatíveis.

## 6. Permissões de sites

Estado inicial desejado:

| Capacidade | Padrão | Regra |
|---|---|---|
| Localização | Bloqueada | Solicitar somente após ação do usuário; concessão temporária e por origem |
| Câmera/microfone | Bloqueados | Negar por padrão; solicitar no contexto de uso e somente para origem segura |
| Notificações | Bloqueadas | Ativação manual por site; nunca pedir automaticamente ao abrir a página |
| Downloads | Confirmar | Mostrar origem, nome e destino antes de iniciar; usuário decide |
| JavaScript | Permitido para compatibilidade | Controle global já existente; mudança deve persistir e refletir no WebView |
| Cookies first-party | Permitidos | Necessários para sessões; controle de terceiros separado |
| Cookies de terceiros | Bloqueados | Exceção explícita e reversível |

Regras de implementação:

- Usar `WebChromeClient.onPermissionRequest` com negação padrão; conceder somente recursos solicitados, autorizados pelo usuário e compatíveis com HTTPS.
- Implementar `onGeolocationPermissionsShowPrompt` com confirmação clara e persistência mínima.
- Não armazenar concessões permanentes sem uma tela de revisão por origem e ação de revogação.
- Notificações devem permanecer bloqueadas até existir suporte funcional e uma ativação manual por site.
- Downloads devem passar por confirmação explícita. Validar esquema, origem, nome, MIME type e destino; não executar arquivos automaticamente.
- Solicitações de permissão devem usar Dialog MD3; feedback de ações concluídas ou recusadas deve usar Snackbar.

## 7. Limpeza de dados por site e armazenamento local

- Disponibilizar uma ação de limpeza para a origem atual, exibindo o domínio exato antes da confirmação.
- Separar cookies, armazenamento DOM/HTML5, IndexedDB, cache e permissões, pois as APIs Android/WebView disponíveis não oferecem uma operação única e universal para apagar tudo de uma origem.
- Usar `WebStorage.deleteOrigin(origin)` quando aplicável e implementar remoção de cookies por origem com cuidado, sem chamar limpeza global como substituto.
- Cache de recursos do WebView pode exigir limpeza global; se não for possível escopo por origem, informar claramente antes de executar.
- Não afirmar que dados de um site foram totalmente apagados se algum armazenamento não puder ser removido.
- O usuário deve poder controlar o armazenamento local e revogar permissões por site.
- A limpeza global existente deve continuar disponível e ter descrição precisa do que remove.

## 8. Bloqueio de notificações abusivas

- Padrão: bloqueadas.
- Não apresentar prompt automático de notificações ao carregar páginas.
- A ativação é manual, por origem, e revogável.
- Se a versão do WebView não oferecer suporte seguro e previsível, manter a capacidade bloqueada em vez de simular suporte.

## 9. Interface Material Design 3

A área de privacidade e permissões deve manter o minimalismo Snav e usar os componentes existentes sempre que possível:

- Lista de preferências com ícones Material Symbols Rounded oficiais, sem preenchimento quando houver variante outlined disponível.
- Título curto e texto secundário explicando o efeito real da configuração.
- Badge discreto para estados Permitido, Bloqueado ou Perguntar.
- Switch MD3 somente para preferências binárias persistentes.
- Dialog MD3 para solicitações, confirmações de HTTP e limpeza por site.
- Snackbar para feedback breve, sem ocultar erros importantes.
- Não usar badges como substituto de explicação ou de controle.
- Preservar a paleta e os estilos existentes da branch; não inventar cores ou uma nova linguagem visual.

## 10. Arquitetura, desempenho e segurança

- Não transformar `MainActivity` em um segundo subsistema de políticas; antes de implementar, identificar pontos de extensão e extrair componentes somente quando reduzir acoplamento.
- Evitar dependências externas se as APIs Android/WebView existentes forem suficientes.
- Listas de bloqueio devem ser compactas, pré-processadas e consultadas sem bloquear a Main Thread.
- Não realizar parsing de listas, I/O ou atualização de filtros na thread de UI.
- Definir limites de memória e tamanho para regras e logs.
- Tratar falhas de atualização de listas como bloqueio seguro com última versão válida, sem desativar TLS ou filtros silenciosamente.
- Respeitar minSdk 21 e verificar diferenças por versão do Android System WebView.

## 11. Critérios mínimos de aceite

Uma funcionalidade de privacidade só pode ser marcada como concluída quando:

1. Código e recursos estão no repositório.
2. O padrão e o estado persistido foram verificados.
3. A permissão pode ser concedida e revogada conforme a política.
4. O comportamento em HTTP/HTTPS e em origem cruzada foi testado.
5. A interface comunica corretamente o estado.
6. Não há regressão em pesquisa, navegação, login, downloads e carregamento de páginas.
7. Foram executados testes/build disponíveis e seus resultados foram registrados.
8. Limitações conhecidas estão documentadas.

## 12. Matriz de implementação

| Recurso | Estado nesta revisão |
|---|---|
| HTTPS sem cleartext global | Presente no manifesto |
| Cancelamento de erro de certificado SSL | Presente no WebViewClient |
| Diagnóstico sem URL completa nos eventos observados | Presente |
| Bloqueio de cookies de terceiros | Não localizado; planejado |
| Bloqueio de rastreadores | Não localizado; planejado |
| Do Not Track opcional | Não localizado; planejado |
| HTTPS-First com diálogo | Não localizado; planejado |
| Limpeza completa por site | Não localizada; planejada |
| Controle de armazenamento local por origem | Não localizado; planejado |
| Permissões persistentes por origem | Não localizado; planejado |
| Notificações bloqueadas e ativação manual | Não localizado; planejado |
| Downloads com confirmação explícita | Não confirmado nesta revisão; auditar antes de marcar |

Atualizar esta matriz somente com evidência do código e testes. 
