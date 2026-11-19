# Snav Browser

Snav é um navegador Android construído para navegação simples, interface minimalista, baixo consumo de recursos e evolução sustentável.

![Logo do Snav](image/logo.png)

## Princípios do projeto

- Preservar a identidade visual original do Snav.
- Usar Material Design 3 sem excesso de componentes.
- Priorizar estabilidade, segurança, privacidade e desempenho.
- Evitar dependências desnecessárias.
- Manter compatibilidade com a arquitetura Android nativa existente.

## Funcionalidades presentes na base atual

- Navegação web por Android WebView.
- Campo de pesquisa com envio de consultas.
- Interface MD3 com tema claro/escuro e cores semânticas.
- Material Symbols Rounded e feedback ripple.
- Bottom sheet com informações do site.
- Indicador linear de carregamento.
- Preferências para JavaScript e escurecimento de páginas.
- Limpeza de cache e histórico.
- Cookies first-party permitidos e cookies de terceiros bloqueados por padrão, com controle persistente.
- Filtro local, limitado e desativável para domínios conhecidos de rastreadores de terceiros.
- Preferência Do Not Track opcional para navegações iniciadas pelo Snav.
- Política HTTPS-only: HTTP oferece somente tentativa de upgrade para HTTPS; tráfego cleartext e downloads HTTP são bloqueados.
- Limpeza por origem de DOM storage e tentativa limitada de expiração de cookies visíveis.
- Filtro local também aplicado a requisições de Service Worker suportadas no Android API 24+.
- Atalho às configurações Android para revogação global de permissões; localização WebView pode ser limpa por origem.
- Solicitações de câmera, microfone e localização mediadas por confirmação no site e permissões Android em tempo de execução.
- Downloads com confirmação explícita antes de iniciar.
- Diagnósticos locais limitados e tratamento de falhas.
- R8/ProGuard e redução de recursos em release.
- Workflow de release com validação e verificação de assinatura.

Consulte [CHANGELOG.md](CHANGELOG.md) para o histórico detalhado.

## Segurança e privacidade

O manifesto desabilita backup Android e permite HTTP no nível da plataforma para suportar a escolha explícita do usuário após o aviso do Snav. Cleartext global está desativado: navegações HTTP oferecem somente upgrade para HTTPS e downloads HTTP são bloqueados. Erros de certificado SSL são cancelados. O filtro de rastreadores usa uma lista local pequena e não bloqueia todo rastreamento. DNT só é enviado nas navegações iniciadas pelo Snav que usam o helper de cabeçalhos. A limpeza por site é limitada e não garante remoção de todos os cookies, caches ou armazenamentos internos. Novos clientes de rede não devem fazer chamadas HTTP em segundo plano.

A política e o plano de evolução estão em [docs/PRIVACY_SECURITY_POLICY.md](docs/PRIVACY_SECURITY_POLICY.md). A documentação distingue recursos existentes de itens planejados para evitar alegações incorretas.

## Desenvolvimento

- Plataforma: Android.
- Linguagem principal: Java.
- Interface: XML.
- Navegação: Android WebView.
- SDK de compilação: 35.
- SDK mínimo: 21.
- Material Components: dependência já utilizada pelo projeto.

Leia [AGENTS.md](AGENTS.md) antes de alterar o código. Ele contém as regras obrigatórias de arquitetura, identidade visual, segurança, desempenho e validação.

Comandos de verificação disponíveis:

```bash
./gradlew test
./gradlew assembleDebug
./gradlew assembleRelease
```

A release exige configuração de assinatura válida no ambiente. Não considere uma compilação concluída sem verificar o resultado do Gradle e a assinatura quando aplicável.

## Licença

MIT License. Consulte [LICENSE](LICENSE).
