# Histórico de alterações — Snav Browser

Este arquivo registra mudanças confirmadas no repositório. Itens de privacidade ainda em planejamento ficam separados do histórico de recursos implementados.

## 2026-11-19 — Base MD3 e estabilidade

### Adicionado
- Redesign das telas principais com layout Material Design 3 contido.
- Papéis semânticos de cores MD3 e suporte ao tema escuro.
- Campo de pesquisa com superfície e contraste ajustados.
- Bottom sheet de informações do site.
- Barra linear de progresso de carregamento, posicionada abaixo da barra de endereço, com atualização suave.
- Recursos oficiais Material Symbols Rounded para ações da interface e navegação inferior.
- Ripple MD3 nas áreas interativas.
- Diagnósticos locais com limite de tamanho e relatório de falhas pendentes.
- Verificação de interface Android por workflow.
- Workflow de release com validação dos segredos, geração e verificação da assinatura do APK.

### Corrigido
- Ação de pesquisa para enviar consultas à web e abrir o teclado ao tocar no campo.
- Navegação de pesquisa e estados da tela.
- Cores e tint dos ícones da navegação inferior.
- Recorte horizontal de ferramentas e espaçamento lateral.
- Progresso de carregamento: hierarquia do layout, acessibilidade e callbacks atrasados durante transições.
- Referências de atributos de tema e recursos de ripple.
- Compatibilidade dos diagnósticos com minSdk 21.
- Configurações padrão de segurança do manifesto e WebView.
- Fluxo legado de permissão de armazenamento para downloads.

### Build e distribuição
- R8/ProGuard e redução de recursos habilitados para release.
- Regras R8 reduzidas e modo otimizado padrão.
- Release CI exige os quatro segredos de assinatura e valida a assinatura com apksigner.
- APK debug pode ser publicado como artefato do workflow de verificação de UI.
- Diretório beta removido.

## 2026-11-19 — Proteções de privacidade no WebView

### Implementado nesta branch
- Permissão cleartext no manifesto habilitada como requisito técnico para a exceção HTTP; o uso é mediado pelos avisos de navegação e confirmação de download e deve ser auditado em futuras integrações de rede.
- Bloqueio de cookies de terceiros por padrão, com preferência persistente para o usuário permitir.
- Diálogo de aviso antes de navegações HTTP; a decisão de continuar vale somente para aquela navegação.
- Erros de certificado TLS continuam sendo cancelados, inclusive nos modos de leitura.
- Solicitações de câmera e microfone exigem origem HTTPS, confirmação do usuário e concessão das permissões Android correspondentes.
- Solicitações de localização exigem origem HTTPS, confirmação e permissão Android em tempo de execução, sem persistir a autorização do site.
- Downloads validam esquema HTTP(S), host e credenciais embutidas; a confirmação vem antes do download e o fluxo legado aguarda a permissão de armazenamento.
- Cabeçalhos Cookie e User-Agent só são anexados ao download quando possuem valor.

### Ainda não implementado
- Bloqueio de rastreadores por listas de filtros.
- Do Not Track.
- Limpeza completa de dados por site e controle de armazenamento por origem.
- Preferências e exceções persistentes por origem para notificações e outras permissões.

## 2026-11-19 — Política de privacidade (especificação)

### Documentado
- Política desejada para cookies first-party e de terceiros.
- Requisitos para bloqueio de rastreadores, Do Not Track e HTTPS-First.
- Padrões de permissões: notificações bloqueadas, downloads com confirmação e permissões sensíveis sob solicitação contextual.
- Requisitos para limpeza de dados por origem e controle de armazenamento local.
- Diretrizes MD3 para listas, texto secundário, badges, dialogs e snackbars.
- Critérios de aceite e limitações técnicas do Android WebView.

**Importante:** esta entrada registra documentação e planejamento. Não significa que os bloqueadores, controles por site ou novos fluxos de permissão já estejam implementados.

## Próximos passos sugeridos

1. Auditar e implementar política de cookies de terceiros.
2. Implementar confirmação de downloads e revisão de permissões de origem.
3. Implementar limpeza por site com indicação explícita dos tipos de dados removidos.
4. Avaliar um bloqueador leve de rastreadores, medindo impacto e compatibilidade.
5. Adicionar HTTPS-First e Do Not Track somente após validar cobertura e limitações do WebView.


## Proteções de privacidade — feature/privacy-protection-and-policies

- Adicionado filtro local e desativável para domínios conhecidos de rastreadores de terceiros.
- Adicionada preferência Do Not Track opcional para navegações iniciadas pelo Snav.
- A ação de limpeza por site agora tenta expirar cookies visíveis no caminho raiz além de remover DOM storage.
- Corrigidas as descrições das permissões no painel de informações do site.
- Documentadas limitações de cobertura do filtro, cabeçalho DNT, limpeza por origem e permissão global de cleartext.


## Revisão das seis pendências de privacidade

- Filtro local aplicado também ao cliente de Service Worker em Android API 24+, quando há Referer disponível.
- Política de rede endurecida: cleartext global desativado; navegação HTTP só tenta upgrade HTTPS; downloads HTTP rejeitados.
- Tela de informações do site ganhou atalho às configurações Android e limpeza da autorização de geolocalização WebView para a origem.
- Documentadas limitações não contornáveis com as APIs públicas do Android WebView: cabeçalho DNT universal, remoção integral de cookies por origem, revogação individual de câmera/microfone e notificações web por origem.
