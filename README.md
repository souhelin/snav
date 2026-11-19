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
- Aviso antes de navegar para endereços HTTP.
- Solicitações de câmera, microfone e localização mediadas por confirmação no site e permissões Android em tempo de execução.
- Downloads com confirmação explícita antes de iniciar.
- Diagnósticos locais limitados e tratamento de falhas.
- R8/ProGuard e redução de recursos em release.
- Workflow de release com validação e verificação de assinatura.

Consulte [CHANGELOG.md](CHANGELOG.md) para o histórico detalhado.

## Segurança e privacidade

O manifesto desabilita backup Android e permite HTTP no nível da plataforma para suportar a escolha explícita do usuário após o aviso do Snav. Navegações HTTP e downloads passam por confirmação; erros de certificado SSL são cancelados. Essa exceção aumenta a superfície de risco e exige que novos clientes de rede não façam chamadas HTTP em segundo plano. Esses controles não equivalem a um bloqueador completo de rastreadores.

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

Apache License 2.0. Consulte [LICENSE](LICENSE).
