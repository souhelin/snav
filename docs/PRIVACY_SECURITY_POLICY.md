# Política de Segurança e Privacidade — Snav Browser

**Branch de implementação:** `feature/privacy-protection-and-policies`  
**Plataforma:** Android nativo, Java e Android WebView.  
**Princípio:** privacidade por padrão, permissões mínimas e transparência.

Este documento distingue recursos implementados de proteções parciais e limitações do Android WebView. Nenhum filtro oferece bloqueio absoluto de rastreamento.

## Estado implementado

| Recurso | Estado real |
|---|---|
| Cookies first-party | Permitidos |
| Cookies de terceiros | Bloqueados por padrão; switch persistente |
| Filtro de rastreadores | Lista local pequena e fixa; bloqueia domínios conhecidos de terceiros em `shouldInterceptRequest` |
| Do Not Track | Opcional, desativado por padrão; envia `DNT: 1` somente nas navegações iniciadas pelo Snav que usam o helper de cabeçalhos |
| HTTPS-First | URLs sem esquema são convertidas para HTTPS; navegação HTTP explícita exige diálogo |
| Erros TLS | Cancelados; não existe opção para ignorar certificado |
| Conteúdo misto | Bloqueado pelo WebView |
| Limpeza por origem | Remove DOM storage via `WebStorage.deleteOrigin` e tenta remover cookies visíveis no caminho raiz |
| Câmera/microfone | Confirmação por solicitação e permissão Android em runtime; recursos desconhecidos são negados |
| Geolocalização | Confirmação por solicitação, permissão Android e callback sem retenção |
| Notificações | Nenhuma concessão explícita; não há fluxo de ativação manual por origem |
| Downloads | URL validada e confirmação antes de iniciar; sem execução automática |
| Backup Android | Desativado |

## Cookies e rastreadores

Cookies próprios permanecem permitidos para preservar sessões. Cookies de terceiros são bloqueados por padrão em Android API 21 ou superior.

O filtro de rastreadores usa uma lista compacta e embutida no código (`doubleclick.net`, domínios Google de anúncios/analytics, Facebook Connect, Hotjar, Clarity, Scorecard Research, Taboola, Outbrain e outros). A comparação tenta preservar recursos do mesmo host ou de hosts relacionados por sufixo.

**Limitações:** a lista não é atualizada remotamente, não cobre todos os rastreadores e pode perder requisições que não passam pelo callback observado, incluindo certos fluxos de Service Worker. A heurística de host não substitui um parser de Public Suffix List. O usuário pode desligar o filtro se um site apresentar incompatibilidade. Não há contagem de bloqueios apresentada como estatística.

## Do Not Track

A preferência é opcional e desativada por padrão. O cabeçalho `DNT: 1` é anexado às navegações iniciadas pelo próprio Snav nos fluxos que usam `loadUrlWithPrivacyHeaders`. O WebView não fornece uma API geral para injetar esse cabeçalho de modo confiável em todos os sub-recursos, redirecionamentos e Service Workers. Sites não são obrigados a respeitar o sinal.

## HTTPS e tráfego cleartext

O manifesto atualmente define `android:usesCleartextTraffic="true"` para permitir que o usuário prossiga em HTTP depois do diálogo. Isso é uma permissão global da aplicação no nível da política de rede Android, não uma autorização por URL. A restrição depende dos interceptadores de navegação e do fluxo de download; qualquer novo cliente HTTP em segundo plano ampliaria o risco.

Erros de certificado TLS são sempre cancelados e conteúdo misto é bloqueado. O envio de POST HTTP iniciado a partir de página HTTPS é bloqueado pelo callback disponível.

**Risco conhecido:** o uso de cleartext global é um compromisso de compatibilidade que aumenta a superfície de ataque. Uma solução mais forte exige remover o cleartext global e abandonar a opção de continuar em HTTP, ou adotar uma arquitetura de rede que permita exceções realmente restritas. Não se deve apresentar a configuração atual como HTTPS-only.

## Permissões de sites

- Câmera e microfone: a página precisa estar em HTTPS e o usuário confirma cada solicitação; a permissão Android é solicitada quando necessária. O app não mantém um painel completo para revisar/revogar permissões por origem.
- Localização: exige HTTPS, confirmação e permissão Android. O callback usa `retain=false`; a permissão Android do sistema continua sujeita às configurações do dispositivo.
- Notificações: o app não concede permissões desconhecidas em `onPermissionRequest`. Não existe configuração de ativação manual por site.
- Downloads: exige confirmação. URLs são limitadas a HTTP/HTTPS, com host válido e sem userinfo; nomes de arquivo são sanitizados. O fluxo legado de armazenamento usa permissão Android até API 28.

## Limpeza de dados por site

A ação da página atual remove os dados de armazenamento web que o WebView expõe por `WebStorage.deleteOrigin` e tenta expirar os nomes de cookies retornados para a URL atual no caminho raiz.

Isso **não equivale a uma limpeza completa garantida**: cookies com caminhos diferentes, escopos de domínio, armazenamento de Service Worker, cache HTTP e outros dados internos podem permanecer. O cache do WebView não é apagado globalmente nessa ação, para evitar remover dados de outros sites. A interface informa que a operação é uma tentativa limitada.

## Interface

As preferências de cookies de terceiros, filtro de rastreadores e DNT usam `MaterialSwitch`, persistência local e feedback Snackbar. A área de informações do site descreve a conexão e oferece a ação de limpeza limitada. A interface ainda não fornece um painel completo de permissões por origem ou ativação de notificações.

## Critérios de validação

Uma funcionalidade só pode ser considerada completa quando houver teste em dispositivos/API relevantes, evidência de comportamento em navegação, sub-recursos e redirecionamentos, revisão de regressões de login e downloads, e verificação de acessibilidade. Build bem-sucedida confirma compilação, não eficácia de filtro ou auditoria de segurança.
