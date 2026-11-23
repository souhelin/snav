# Política de Segurança e Privacidade — Snav Browser

> **Contexto:** o diagnóstico de implementação abaixo descreve a antiga
> versão baseada em Android WebView. A versão atual usa GeckoView: não tome a
> tabela e os detalhes específicos de WebView como descrição do comportamento
> atual. Consulte [a migração para GeckoView](GECKOVIEW_MIGRATION.md) e o
> [README](../README.md). Os requisitos de segurança continuam aplicáveis.

**Branch de implementação:** `feature/privacy-protection-and-policies`  
**Plataforma:** Android nativo, Java e Android WebView.  
**Princípio:** privacidade por padrão, permissões mínimas e transparência.

Este documento distingue recursos implementados de proteções parciais e limitações do Android WebView. Nenhum filtro oferece bloqueio absoluto de rastreamento.

## Estado implementado

| Recurso | Estado real |
|---|---|
| Cookies first-party | Permitidos |
| Cookies de terceiros | Bloqueados por padrão; switch persistente |
| Filtro de rastreadores | Lista local fixa; bloqueia domínios conhecidos de terceiros em sub-recursos WebView e em requisições de Service Worker suportadas (API 24+) quando há Referer |
| Do Not Track | Opcional, desativado por padrão; envia `DNT: 1` nas navegações iniciadas pelo Snav que usam o helper. Não cobre todos os sub-recursos e Service Workers |
| HTTPS-First | URLs sem esquema são convertidas para HTTPS; HTTP explícito oferece tentativa de upgrade para HTTPS e não permite continuar em HTTP |
| Erros TLS | Cancelados; não existe opção para ignorar certificado |
| Conteúdo misto | Bloqueado pelo WebView |
| Limpeza por origem | Remove DOM Storage via `WebStorage.deleteOrigin` e tenta expirar cookies visíveis; não garante remoção integral de cookies e caches por origem |
| Câmera/microfone | Confirmação por solicitação e permissão Android em runtime; revogação do SO é global para o app, não por domínio |
| Geolocalização | Confirmação por solicitação, permissão Android e callback sem retenção |
| Notificações | Não concedidas; Android WebView não oferece suporte completo e uniforme à API de notificações web com controle nativo por origem |
| Downloads | URL validada e confirmação antes de iniciar; sem execução automática |
| Backup Android | Desativado |

## Cookies e rastreadores

Cookies próprios permanecem permitidos para preservar sessões. Cookies de terceiros são bloqueados por padrão em Android API 21 ou superior.

O filtro de rastreadores usa uma lista compacta e embutida no código (`doubleclick.net`, domínios Google de anúncios/analytics, Facebook Connect, Hotjar, Clarity, Scorecard Research, Taboola, Outbrain e outros). A comparação tenta preservar recursos do mesmo host ou de hosts relacionados por sufixo.

**Limitações:** a lista não é atualizada remotamente, não cobre todos os rastreadores e pode perder requisições que não passam pelo callback observado, incluindo certos fluxos de Service Worker. A heurística de host não substitui um parser de Public Suffix List. O usuário pode desligar o filtro se um site apresentar incompatibilidade. Não há contagem de bloqueios apresentada como estatística.

## Do Not Track

A preferência é opcional e desativada por padrão. O cabeçalho `DNT: 1` é anexado às navegações iniciadas pelo próprio Snav nos fluxos que usam `loadUrlWithPrivacyHeaders`. O WebView não fornece uma API geral para injetar esse cabeçalho de modo confiável em todos os sub-recursos, redirecionamentos e Service Workers. Sites não são obrigados a respeitar o sinal.

## HTTPS e tráfego cleartext

O manifesto define `android:usesCleartextTraffic="false"`. HTTP não é permitido pela política de rede do aplicativo. Quando o usuário tenta abrir um endereço HTTP, o Snav oferece somente tentar a versão HTTPS; não existe opção de continuar em HTTP. Downloads HTTP também são rejeitados antes de exibir a confirmação.

Erros de certificado TLS são sempre cancelados e conteúdo misto é bloqueado. O envio de POST HTTP iniciado a partir de página HTTPS também é bloqueado pelo callback disponível.

Essa decisão remove a exceção global de cleartext. Sites que não oferecem HTTPS deixam de funcionar no navegador, intencionalmente, para preservar a política HTTPS-only.

## Permissões de sites

- Câmera e microfone: exigem HTTPS, confirmação por solicitação e permissão Android. A tela de informações oferece atalho às configurações Android do aplicativo; revogar câmera/microfone nas configurações Android revoga a permissão para o app inteiro, não para um domínio isolado. O WebView não expõe API pública para revogação por origem dessas capturas.
- Localização: exige HTTPS, confirmação e permissão Android. O callback usa `retain=false`; a ação de gerenciamento chama `GeolocationPermissions.clear(origin)` para limpar a autorização WebView daquela origem e abre as configurações do app para permissões Android globais.
- Notificações: não são concedidas. WebView Android não expõe o mesmo fluxo de notificações do Chrome completo, então o Snav não apresenta um switch que prometa ativação por origem sem suporte real.
- Downloads: exige confirmação. URLs são limitadas a HTTPS, com host válido e sem userinfo; nomes de arquivo são sanitizados. O fluxo legado de armazenamento usa permissão Android até API 28.

## Limpeza de dados por site

A ação da página atual remove o DOM Storage da origem por `WebStorage.deleteOrigin` e tenta expirar os nomes de cookies visíveis para a URL atual no caminho raiz.

Isso **não equivale a uma limpeza completa garantida**: a API pública `CookieManager.getCookie` não expõe atributos Path/Domain de cada cookie, portanto cookies com escopos diferentes podem permanecer. Cache HTTP, dados internos de Service Worker e outros stores não podem ser removidos com garantia por origem pelas APIs públicas do WebView. A interface descreve a operação como limitada. Não é usado `removeAllCookies`, pois apagaria dados de outros sites.

## Interface

As preferências de cookies de terceiros, filtro de rastreadores e DNT usam `MaterialSwitch`, persistência local e feedback Snackbar. A área de informações do site descreve a conexão e oferece a ação de limpeza limitada. A interface ainda não fornece um painel completo de permissões por origem ou ativação de notificações.

## Critérios de validação

Uma funcionalidade só pode ser considerada completa quando houver teste em dispositivos/API relevantes, evidência de comportamento em navegação, sub-recursos e redirecionamentos, revisão de regressões de login e downloads, e verificação de acessibilidade. Build bem-sucedida confirma compilação, não eficácia de filtro ou auditoria de segurança.


## Limites das seis áreas solicitadas

1. **Filtro de rastreadores:** melhorado para sub-recursos e Service Workers (API 24+) com uma lista local fixa. Não é EasyList/EasyPrivacy completa; não há atualização automática nem cobertura garantida quando o Referer não é enviado.
2. **DNT:** permanece limitado às navegações iniciadas pelo Snav. O WebView não oferece API pública para adicionar um cabeçalho arbitrário a todos os sub-recursos e requisições de Service Worker. Interceptar e refazer todas as requisições manualmente quebraria cookies, POST, cache, redirecionamentos, CORS e segurança TLS; essa abordagem não foi adotada.
3. **Limpeza por site:** limitada pelas APIs públicas de cookies e armazenamento do WebView. Não usar limpeza global para fingir escopo por origem.
4. **Revogação de permissões:** localização WebView pode ser limpa por origem. Câmera/microfone são permissões Android do aplicativo e a revogação pelo sistema é global; WebView não fornece revogação pública por domínio.
5. **Notificações:** permanecem bloqueadas/não concedidas. WebView não oferece suporte uniforme para gerenciar notificações web por origem como um navegador Chromium completo.
6. **HTTP:** resolvido de forma estrita: cleartext global desativado, tentativa de upgrade HTTPS e downloads apenas HTTPS. Sites sem HTTPS são bloqueados.

Não classificar as áreas 1–5 como cobertura completa do navegador Chromium. O Snav usa Android WebView e deve declarar suas limitações.
