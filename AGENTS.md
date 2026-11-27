# AGENTS.md — SNAV BROWSER

## Regras obrigatórias para agentes de IA e desenvolvimento

**Projeto:** Snav Browser  
**Plataforma:** Android  
**Linguagem principal:** Java 17  
**Motor de navegação:** Mozilla GeckoView  
**Idioma principal:** Português (Brasil)

Este documento define as regras para qualquer agente de IA ou pessoa que altere o Snav. As instruções devem ser aplicadas à branch em uso, respeitando o código e a documentação atuais.

A implementação real é a fonte de verdade. Não declare recursos como concluídos apenas porque existem APIs no GeckoView, documentação, planos ou código parcial.

---

# 1. IDENTIDADE DO SNAV

O Snav é um navegador Android com identidade visual própria.

Princípios:

- Minimalismo e simplicidade.
- Rapidez e estabilidade.
- Organização e facilidade de uso.
- Privacidade e segurança por padrão.
- Baixo consumo de recursos, medido em dispositivos reais.

O Snav não deve parecer uma cópia de outro navegador.

## Regras visuais

1. Preserve a identidade visual existente.
2. Não altere cores, ícones ou estrutura visual sem necessidade técnica ou solicitação explícita.
3. Não adicione elementos decorativos ou componentes sem função clara.
4. Não reduza desnecessariamente a área de navegação.
5. Mantenha acessibilidade, contraste e adaptação a telas menores.
6. Não introduza animações, sombras ou efeitos que prejudiquem desempenho ou legibilidade.
7. Antes de alterar um componente, inspecione os recursos e estilos da branch atual. Não trate uma paleta histórica deste documento como substituta dos recursos reais.

**Princípio:** cada elemento deve melhorar a navegação, a compreensão ou o uso do navegador.

---

# 2. ÍCONES E IDENTIDADE VISUAL

Os recursos próprios do Snav devem ser preservados. Entre os nomes já utilizados estão:

- `ic_snav_search`
- `ic_snav_refresh`
- `ic_snav_more`
- `ic_snav_back`
- `ic_snav_forward`
- `ic_snav_home`
- `ic_snav_tabs`

Regras:

1. Não substituir ícones originais por emojis, caracteres Unicode ou desenhos genéricos.
2. Não misturar bibliotecas ou estilos de ícones sem justificativa e autorização.
3. Não alterar proporções, espessuras ou formatos arbitrariamente.
4. Verifique os recursos realmente presentes antes de referenciá-los.
5. Mantenha consistência de tamanho, alinhamento, tint e descrição de acessibilidade.

O Snav utiliza princípios do Material Design 3, mas não deve adicionar componentes MD3 sem necessidade. Prefira superfícies discretas, hierarquia clara, espaçamento consistente e estados de interação compreensíveis.

---

# 3. ARQUITETURA ATUAL — GECKOVIEW

**GeckoView é o único motor de navegação do Snav. WebView não é o motor atual e não deve ser reintroduzido.**

A superfície de navegação é `org.mozilla.geckoview.GeckoView`. A navegação, o histórico, o carregamento de páginas, as permissões web, os downloads e o armazenamento do navegador devem utilizar as APIs GeckoView correspondentes.

## Componentes centrais

- `MainActivity`: Activity principal e integração da interface com o navegador.
- `GeckoEngineController`: encapsula o `GeckoRuntime`, a `GeckoSession`, os delegates, navegação, estado da sessão e operações de armazenamento.
- `ExtensionManager`: gerenciamento de WebExtensions por meio do `WebExtensionController`.
- `GeckoPromptController`: interface nativa para prompts web implementados pelo aplicativo.
- `GeckoView`: view que apresenta o conteúdo da sessão.
- `BrowserTab` e estruturas associadas: estado das abas da interface.

Esses nomes descrevem a estrutura observada. Antes de alterar qualquer componente, confirme sua implementação atual, seus chamadores e suas responsabilidades.

## Regras obrigatórias

1. Não introduza `android.webkit.WebView`, `WebViewClient`, `WebChromeClient`, `CookieManager` ou `WebStorage` no caminho de navegação.
2. Não implemente funcionalidades web por injeção JavaScript ou interceptação manual quando houver API GeckoView apropriada.
3. Não crie um segundo motor de navegação.
4. Não substitua o `GeckoRuntime` ou a gestão de sessões por singletons paralelos.
5. Não acesse diretamente o runtime em telas e componentes sem avaliar se a operação pertence ao `GeckoEngineController`.
6. Não suponha que uma API disponível no GeckoView já esteja integrada à interface do Snav.
7. Não atualize a versão do GeckoView isoladamente sem revisar compatibilidade, notas da versão, dependências, R8, CI e testes.
8. Não remova delegates ou callbacks sem rastrear todos os fluxos que dependem deles.

## GeckoRuntime e GeckoSession

- O `GeckoRuntime` é compartilhado no processo e inicializado pelo controlador.
- O controlador mantém uma `GeckoSession` associada ao `GeckoView` ativo.
- O estado da sessão utiliza `GeckoSession.SessionState` e os mecanismos de salvar/restaurar do Gecko.
- O estado atual das abas da interface não equivale a sessões Gecko independentes: a implementação documentada reutiliza uma sessão ao alternar abas. Não prometa isolamento por aba, execução simultânea independente ou contêineres separados.
- Alterações no modelo de abas exigem projeto explícito de ciclo de vida, persistência, armazenamento, permissões, extensões, memória e restauração. Não basta criar mais objetos de interface.

Antes de mudar o modelo de sessão, leia a documentação GeckoView da versão efetivamente usada e verifique o código de `MainActivity`, `GeckoEngineController` e `BrowserTab`.

---

# 4. NAVEGAÇÃO, DELEGATES E CICLO DE VIDA

A navegação deve passar pelo controlador Gecko e pelos delegates apropriados.

Verifique, conforme o fluxo alterado:

- `NavigationDelegate`: decisões de navegação, novas janelas, esquemas externos e erros.
- `ProgressDelegate`: início/fim de carregamento, progresso e estado da sessão.
- `ContentDelegate`: título, menu de contexto, resposta externa, tela cheia e falha do processo de conteúdo.
- `PermissionDelegate`: permissões web e callbacks associados.
- `ScrollDelegate`: posição e comportamento de rolagem.
- `MediaSession.Delegate`: estado de reprodução e pausa.
- `PromptDelegate`: diálogos, seleção de arquivos e demais prompts suportados.
- `WebExtensionController`: instalação e ciclo de vida das extensões.

Nem todos os delegates ou tipos de prompt estão necessariamente implementados. Consulte a implementação atual antes de adicionar uma funcionalidade.

## Ciclo de vida

1. Ative e foque a sessão apenas quando a tela de navegação estiver em primeiro plano e visível.
2. Ao ocultar a navegação ou pausar a Activity, atualize o estado ativo da sessão e das extensões conforme o fluxo existente.
3. Não feche a sessão apenas para ocultar uma tela ou abrir um painel interno.
4. Ao encerrar a Activity, finalize delegates, prompts pendentes, downloads pertencentes à Activity, executores e controladores na ordem correta.
5. Não mantenha callbacks que referenciem uma Activity destruída.
6. Não reutilize callbacks de permissões ou prompts para solicitações de outra origem.
7. Ao salvar estado, use as APIs de sessão Gecko e valide o comportamento em recriação da Activity e morte do processo.
8. Uma notificação de crash do processo de conteúdo não prova que a recuperação automática está implementada. Não prometa recuperação sem testar o fluxo completo.

A pausa da sessão e de mídia não garante suspensão total de JavaScript, rede, WebRTC ou captura. Descreva apenas o comportamento efetivamente implementado.

---

# 5. ABAS E ISOLAMENTO

A implementação atual alterna abas da interface reutilizando uma sessão Gecko. Isso é uma limitação arquitetural conhecida.

Regras:

- Não afirme que cada aba possui cookies, armazenamento, histórico ou permissões isolados.
- Não apresente abas como sessões simultâneas independentes.
- Não tente simular isolamento apenas limpando histórico ou dados entre trocas.
- Qualquer evolução para sessões independentes deve especificar como runtime, sessão, view, estado, downloads, permissões, extensões e memória serão gerenciados.
- Teste alternância rápida, restauração, fechamento, abertura, rotação e retorno do segundo plano.
- Preserve o conteúdo e o estado da aba ativa quando uma operação de interface não exigir navegação.

---

# 6. WEBEXTENSIONS

O Snav integra WebExtensions por meio do `WebExtensionController` do GeckoView.

O `ExtensionManager` atual oferece fluxos de catálogo Mozilla, instalação por arquivo XPI ou HTTPS, listagem, ativação/desativação, atualização, remoção e gerenciamento de permissões, conforme os métodos existentes.

## Segurança da instalação

Ao alterar instalação ou atualização:

1. Exija ação e confirmação explícitas do usuário.
2. Aceite somente HTTPS em downloads remotos, valide host e redirecionamentos e rejeite credenciais na URL.
3. Preserve limites de tamanho compactado e descompactado.
4. Preserve a validação estrutural e a verificação SHA-256 quando fornecida pelo catálogo.
5. Não substitua a validação de assinatura e compatibilidade feita pelo Gecko.
6. Não confunda a inspeção local do manifesto com uma auditoria completa das APIs usadas pela extensão.
7. Remova arquivos temporários após sucesso, falha ou cancelamento.
8. Não conceda permissões opcionais sem consentimento.
9. Não conceda navegação privada automaticamente.
10. Se a tela de consentimento não estiver ativa, negue a solicitação conforme a política implementada.

## Limitações que devem ser comunicadas

A instalação aceita não garante funcionamento completo. A implementação documentada possui limitações na integração de abas, menus de contexto e canal genérico de mensagens nativas. A compatibilidade depende da versão do GeckoView, das APIs usadas pela extensão e dos delegates que o Snav realmente implementa.

Não prometa compatibilidade universal com extensões do Firefox Desktop ou Chrome. Teste individualmente instalação, reinicialização, atualização, permissões, desinstalação e comportamento funcional.

Leia `docs/EXTENSOES_GECKOVIEW.md` antes de alterar esse subsistema.

---

# 7. PROMPTS, FORMULÁRIOS E ARQUIVOS

A existência de um `PromptDelegate` não significa que todos os prompts tenham interface pronta.

A implementação atual possui `GeckoPromptController` para os tipos explicitamente tratados no código. Consulte a implementação antes de presumir suporte a:

- Diálogos JavaScript.
- Confirmação de saída com alterações não salvas.
- Reenvio de formulários POST.
- Seleção de arquivos simples ou múltiplos.
- Seleção HTML de opções.
- Data, hora e cor.
- Autenticação HTTP.
- Identidade/FedCM e outros fluxos avançados.

Regras:

1. Conclua cada callback exatamente uma vez.
2. Trate cancelamento, Activity destruída, origem alterada e prompt substituído.
3. Não conceda permissões por ausência de UI.
4. Para arquivos, prefira seletor do sistema e URIs `content://` com concessões mínimas.
5. Não solicite acesso amplo ao armazenamento para resolver seleção de arquivos.
6. Não declare suporte a captura direta por câmera, seleção de pastas ou provedores específicos sem implementação e testes.

---

# 8. SEGURANÇA E PRIVACIDADE

Segurança não pode ser reduzida para facilitar uma implementação.

## Regras obrigatórias

- Nunca ignorar erros de certificado TLS.
- Nunca permitir navegação HTTP como fallback silencioso.
- Preservar o modo HTTPS-only e a política de cleartext do aplicativo.
- Validar esquemas, hosts, credenciais e destinos antes de navegar, baixar ou abrir intents.
- Não expor interfaces JavaScript privilegiadas a páginas ou extensões de terceiros.
- Não habilitar depuração remota em builds de produção.
- Não registrar conteúdo de páginas, tokens, cookies, credenciais, cabeçalhos sensíveis ou dados pessoais.
- Não conceder permissões sem gesto/contexto válido e consentimento.
- Negar classes de permissões não suportadas explicitamente.
- Revalidar a origem antes de concluir callbacks assíncronos de permissão.
- Não armazenar dados sensíveis sem necessidade.
- Não enfraquecer proteções Gecko para corrigir incompatibilidade de um site.
- Não afirmar proteção total contra rastreamento.

## Permissões de sites

Localização, câmera e microfone devem passar pelo fluxo de consentimento do Snav e pelas permissões Android correspondentes quando exigidas.

- Solicitações simultâneas não podem substituir callbacks pendentes.
- A origem deve ser conferida antes de concluir a solicitação.
- Negação, cancelamento e encerramento devem concluir o callback de forma segura.
- Permissões não suportadas devem continuar negadas.
- Não confunda permissão Android global do aplicativo com permissão web por origem.

## Cookies, rastreamento e Do Not Track

A política atual usa configurações de proteção contra rastreamento e isolamento de cookies do GeckoView, incluindo os ajustes presentes em `GeckoEngineController`. Não reintroduza controles específicos de WebView, como `CookieManager.setAcceptThirdPartyCookies`.

O Do Not Track é um sinal opcional e limitado aos fluxos em que o Snav anexa o cabeçalho. Sites podem ignorá-lo. Não o descreva como bloqueio de rastreamento.

## Limpeza de dados

Use `StorageController` e as flags suportadas pelo GeckoView. Operações de limpeza podem exigir desanexar/fechar e reabrir a sessão para evitar recriação imediata de dados.

- Diferencie limpeza por host de limpeza global.
- A limpeza por host não garante remover dados de todas as origens de terceiros incorporadas.
- Não apresente limpeza por site como apagamento integral quando a API não oferece essa garantia.
- Preserve o estado de erro e reanexe a sessão mesmo quando a operação falhar, conforme o controlador.
- Não execute operações de armazenamento concorrentes se o controlador as serializar ou bloquear.

---

# 9. DOWNLOADS E CONTEÚDO EXTERNO

Downloads devem usar os dados de resposta fornecidos pelo GeckoView e o fluxo nativo do Android já existente.

1. Exija confirmação explícita antes de iniciar.
2. Valide esquema HTTPS, host, redirecionamentos e nome do arquivo.
3. Não execute arquivos baixados automaticamente.
4. Feche o corpo de respostas quando o fluxo for cancelado ou não puder ser processado.
5. Não bloqueie a Main Thread com operações de rede ou disco.
6. Trate cancelamento, falha, falta de espaço e encerramento da Activity.
7. Não prometa continuidade após morte do processo: o executor atual pertence à Activity e precisa ser avaliado antes de qualquer afirmação de resiliência.
8. Não registre URLs completas quando puderem conter tokens ou dados privados.

Para esquemas externos, abra somente destinos permitidos e após ação do usuário. Nunca encaminhe URLs arbitrárias a intents sem validação.

---

# 10. DESEMPENHO E RECURSOS

GeckoView é um motor de navegador completo e tem custos de memória e inicialização próprios. Não estime desempenho apenas pelo tamanho do AAR ou por uma build bem-sucedida.

Regras:

- Evite trabalho de rede, disco, parsing pesado ou processamento de arquivos na Main Thread.
- Use executores e threads com ciclo de vida definido.
- Evite múltiplas cópias de arquivos e buffers sem necessidade.
- Feche streams, respostas, sessões e recursos nativos.
- Evite delegates duplicados e listeners que retenham Activities.
- Limite caches e tarefas em segundo plano.
- Não mantenha sessões ativas sem necessidade.
- Meça memória, tempo de inicialização, consumo em repouso e comportamento com muitas abas em dispositivos representativos.
- Não sacrifique segurança para obter ganhos artificiais.

---

# 11. ANDROID E DEPENDÊNCIAS

Consulte sempre `app/build.gradle` e os arquivos Gradle reais da branch antes de alterar versões.

Configuração observada nesta linha de desenvolvimento:

| Configuração | Valor |
|---|---|
| Android mínimo | API 26 |
| Compile SDK | API 37 |
| Target SDK | API 35 |
| Java | 17 |
| GeckoView | 160.0.20261116130455 |
| Interface | Android Views/XML e Compose Material 3 em integração específica |

Esses valores podem mudar. O arquivo Gradle é a fonte de verdade.

Regras:

1. Não presumir Kotlin como linguagem principal da aplicação.
2. Não migrar a interface inteira para Compose sem solicitação e análise.
3. Não alterar minSdk, targetSdk, AGP, Gradle ou GeckoView sem avaliar impacto e CI.
4. Não adicionar dependências redundantes.
5. Não adicionar permissões ao manifesto sem necessidade comprovada.
6. Não alterar assinatura, keystore ou secrets.
7. Não introduzir APIs incompatíveis com minSdk sem proteção adequada.
8. Preserve os ABIs e as propriedades de build existentes, salvo quando a tarefa exigir mudança.

---

# 12. DOCUMENTAÇÃO E FONTES DE VERDADE

Antes de alterar um subsistema, leia a documentação relacionada e compare com o código atual.

Referências do projeto:

- `README.md`: arquitetura, requisitos e comandos atuais.
- `docs/REVISAO_GECKOVIEW_E_INTERFACE.md`: limitações e roteiro de validação da integração atual.
- `docs/GECKOVIEW_MIGRATION.md`: histórico da migração; resultados históricos não validam commits posteriores.
- `docs/EXTENSOES_GECKOVIEW.md`: implementação e limites de WebExtensions.
- `docs/PRIVACY_SECURITY_POLICY.md`: requisitos de segurança, mas contém trechos históricos escritos para WebView.

**Atenção à documentação legada:** qualquer trecho que descreva Android WebView, `WebViewClient`, `CookieManager`, `WebStorage` ou limitações específicas de WebView não descreve automaticamente o comportamento atual. Não copie essas instruções para código GeckoView. Corrija a documentação obsoleta quando a tarefa incluir documentação; não esconda divergências.

Use a documentação oficial da versão GeckoView efetivamente utilizada para confirmar assinatura, disponibilidade e semântica das APIs. Não baseie decisões apenas em exemplos de outra versão.

---

# 13. PROCESSO OBRIGATÓRIO DE DESENVOLVIMENTO

Siga esta sequência em toda alteração:

## 1 — Inspeção

- Confirme repositório, branch e estado atual.
- Leia o arquivo que será alterado.
- Leia os componentes relacionados e suas dependências.
- Consulte as instruções aplicáveis em `AGENTS.md` e documentação do subsistema.
- Verifique se há mudanças pré-existentes que não podem ser sobrescritas.

## 2 — Diagnóstico

- Identifique o problema real.
- Diferencie bug, limitação da API, ausência de integração e falta de teste.
- Não presuma que uma funcionalidade está ausente ou completa sem evidência.

## 3 — Planejamento

- Defina a menor alteração segura.
- Identifique riscos de regressão, ciclo de vida, sessão, segurança e compatibilidade.
- Não amplie o escopo com refatorações não solicitadas.

## 4 — Implementação

- Modifique somente os arquivos necessários.
- Preserve APIs e comportamentos utilizados por outros componentes.
- Não substitua arquivos inteiros quando uma mudança localizada for suficiente.
- Não adicione dependências ou permissões sem justificativa.

## 5 — Verificação

- Revise o diff.
- Execute testes relevantes.
- Verifique regressões de navegação, abas, permissões, prompts, downloads e extensões conforme o escopo.
- Faça build debug e, quando pertinente, release/R8.
- Não confunda compilação com validação funcional.

## 6 — Relatório

Informe arquivos alterados, decisões, testes executados, resultados reais, limitações e pendências. Nunca invente testes ou resultados.

---

# 14. BUILD E TESTES

O repositório pode não conter Gradle Wrapper. Confira a disponibilidade de Gradle, JDK e Android SDK no ambiente antes de executar comandos.

Comandos documentados no README atual:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

Para testes instrumentados em emulador x86_64:

```bash
gradle -PsnavEmulatorTest=true :app:connectedDebugAndroidTest
```

Para release:

```bash
gradle :app:assembleRelease
```

Para AAB:

```bash
gradle -PsnavBuildBundle=true :app:bundleRelease
```

Adapte os comandos apenas após verificar as tarefas reais. Não afirme que uma tarefa foi executada quando o ambiente não possui as ferramentas ou o dispositivo necessários.

## Cobertura mínima conforme a alteração

- Pesquisa, endereço, teclado e navegação HTTPS.
- Voltar, avançar, recarregar, parar e erros de carregamento.
- Alternância e restauração de abas.
- Rotação, recriação da Activity e retorno do segundo plano.
- Permissões por origem, negação e callbacks concorrentes.
- Prompts JavaScript, beforeunload e formulários.
- Upload de arquivos e cancelamento.
- Downloads, redirecionamentos e falhas.
- Instalação, atualização, remoção e permissões de extensões.
- Limpeza de dados por host e global.
- Crash do processo de conteúdo e comportamento de recuperação.
- Build release com R8 e assinatura quando aplicável.

Testes instrumentados devem rodar em instalação isolada quando instalarem ou removerem extensões de teste.

Uma build bem-sucedida comprova compilação e empacotamento, não compatibilidade com sites, segurança completa, desempenho ou estabilidade em dispositivos físicos.

---

# 15. CONTROLE DE ALTERAÇÕES

Não faça:

- Migração de arquitetura não solicitada.
- Reintrodução de WebView como motor.
- Refatoração global sem necessidade.
- Mudanças visuais arbitrárias.
- Remoção de funcionalidades sem análise.
- Redução de proteções de segurança para contornar bugs.
- Atualizações de dependências sem validação.
- Alterações em arquivos não relacionados.

Faça:

- Preserve o comportamento funcional.
- Identifique e corrija a causa raiz.
- Mantenha consistência com a arquitetura GeckoView.
- Documente limitações reais.
- Mantenha o diff focado.
- Informe os arquivos modificados e os testes realizados.

Se uma solicitação exigir uma decisão arquitetural que altere isolamento, armazenamento ou segurança, explique o impacto e apresente a alternativa tecnicamente segura antes de aplicar uma mudança irreversível.

---

# 16. RELATÓRIO FINAL OBRIGATÓRIO

Ao concluir uma tarefa, informe:

**Alterações**
- Arquivos modificados.
- Comportamentos implementados ou corrigidos.
- Decisões técnicas relevantes.

**Arquitetura**
- Como a alteração se integra ao GeckoView.
- Impactos em runtime, sessão, abas, delegates e ciclo de vida.

**Segurança e desempenho**
- Proteções preservadas.
- Riscos ou impactos identificados.
- Limitações conhecidas.

**Verificação**
- Testes executados e seus resultados.
- Build executada e resultado.
- Dispositivo/emulador utilizado, quando aplicável.
- Assinatura confirmada somente quando realmente verificada.

**Pendências**
- Testes não executados.
- Limitações ainda existentes.
- Riscos que exigem acompanhamento.

Nunca invente resultados, cobertura ou compatibilidade.

---

# 17. REGRA FINAL — EVOLUIR SEM REGREDIR

O Snav deve evoluir como navegador Android baseado em GeckoView, mantendo sua identidade visual e seus princípios de simplicidade.

Prioridades:

1. Segurança e privacidade.
2. Estabilidade e integridade dos dados.
3. Correção funcional.
4. Desempenho medido.
5. Acessibilidade e clareza.
6. Manutenibilidade.
7. Consistência visual.

**GeckoView é a arquitetura atual. WebView é legado e não deve orientar novas implementações.**

Estas regras são obrigatórias para agentes de IA e contribuidores deste repositório.
