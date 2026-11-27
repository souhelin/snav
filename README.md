# Snav Browser

Um navegador para Android com foco em navegação simples, privacidade e uma interface própria. O Snav usa **GeckoView**, componente da Mozilla para exibir conteúdo da web.

![Logo do Snav](image/logo.png)

> A documentação descreve o código desta branch. Isso não significa que todos os recursos estejam disponíveis em uma versão distribuída do aplicativo.

## Visão geral

O Snav mantém uma interface enxuta e controles acessíveis no próprio aplicativo. A organização visual segue os princípios do **Material Design 3 tradicional**, adaptados à identidade do navegador:

- Hierarquia clara de títulos e informações.
- Cores organizadas por funções semânticas, com suporte aos temas claro e escuro.
- Tipografia legível e níveis de ênfase consistentes.
- Superfícies e cantos moderados, sem decoração desnecessária.
- Estados de interação perceptíveis e feedback breve.
- Componentes usados quando ajudam na navegação, sem ocupar espaço à toa.

A aparência acompanha o tema do dispositivo. Em aparelhos compatíveis, **Cores dinâmicas** pode usar a paleta do sistema; a opção também pode ser desativada para manter as cores do Snav. Quando o recurso não está disponível, o aplicativo usa sua própria paleta. As mensagens breves usam Snackbar, incluindo a opção de desfazer ao desafixar uma página.

Veja [cores e mensagens](docs/MATERIAL_COLOR_E_SNACKBAR.md) para os detalhes da implementação visual.

## Navegação

- Pesquise ou abra endereços pela tela de pesquisa.
- Acesse abas, favoritos, histórico e downloads pelo menu. As abas compartilham uma sessão Gecko, o que limita o isolamento entre elas e a integração com extensões.
- A tela inicial exibe até quatro atalhos fixados e quatro páginas recentes. Os atalhos podem ser editados e gerenciados.
- Puxe a página para baixo a partir do topo para tentar atualizá-la. O gesto depende da resposta do Gecko e pode ser rejeitado para evitar atualizações acidentais. Em páginas curtas, sem rolagem, o gesto não funcionou no emulador testado; nesse caso, use a opção de recarregar no menu. Consulte [atualização por arrasto](docs/PULL_TO_REFRESH.md).
- Consulte informações do site e use o modo de leitura ou a tradução quando estiverem disponíveis. O resultado depende da página e dos serviços envolvidos.

As correções de interface, permissões e lacunas conhecidas da integração estão registradas na [revisão do GeckoView](docs/REVISAO_GECKOVIEW_E_INTERFACE.md).

## Extensões

Em **Ferramentas > Extensões**, o gerenciador permite pesquisar no catálogo da Mozilla e instalar arquivos XPI por HTTPS ou pelo seletor de arquivos. O download e a instalação pedem confirmação. O GeckoView verifica o pacote e sua assinatura.

A tela também oferece controles para ativar, desativar, atualizar e remover extensões, além de revogar permissões opcionais.

A instalação de uma extensão não garante compatibilidade. A integração com abas e menus de contexto é limitada, e o Snav não oferece um canal genérico de mensagens nativas para extensões de terceiros. O suporte a cada extensão e às suas APIs depende do GeckoView e do que o aplicativo implementa.

Consulte [extensões no GeckoView](docs/EXTENSOES_GECKOVIEW.md) para conhecer os detalhes, limites e orientações de desenvolvimento.

## Privacidade e segurança

O Snav usa as configurações de proteção contra rastreamento do GeckoView e o isolamento dinâmico de cookies de terceiros. A navegação por HTTP é bloqueada; quando possível, o aplicativo tenta acessar a versão HTTPS do endereço. Erros de certificado não são ignorados.

Outros controles:

- Downloads exigem confirmação antes de iniciar.
- Solicitações de localização, câmera e microfone passam pela confirmação no aplicativo e pelas permissões do Android, quando necessárias.
- A limpeza de dados por site usa a API do GeckoView. Ela não garante a remoção de dados pertencentes a origens de terceiros.
- Do Not Track é enviado nas navegações iniciadas pelo Snav que usam esse cabeçalho. Os sites podem ignorar o sinal.

Essas medidas não bloqueiam todo rastreamento nem eliminam todos os riscos. A [política de privacidade e segurança](docs/PRIVACY_SECURITY_POLICY.md) descreve os requisitos e as limitações. Trechos escritos para o antigo WebView devem ser lidos junto à [documentação da migração para GeckoView](docs/GECKOVIEW_MIGRATION.md).

## Ícone do aplicativo

No Android 13 ou posterior, o ícone pode acompanhar as cores do tema do dispositivo quando os ícones temáticos estão ativados e o launcher oferece suporte. Nos demais casos, o Snav usa o ícone colorido.

Veja [detalhes do ícone temático](docs/THEMED_APP_ICON.md).

## Desenvolvimento

| Item | Configuração |
|---|---|
| Plataforma | Android |
| Android mínimo | API 26 (Android 8.0) |
| SDK de compilação | API 37 |
| SDK de destino | API 35 |
| Linguagem principal | Java 17 |
| Interface | Android Views/XML e Compose Material 3 na atualização por arrasto |
| Android Gradle Plugin | 9.1.1 |
| Gradle | 9.3.1 |
| GeckoView | 160.0.20261116130455 |
| Identificador do aplicativo | `io.github.souhelin.snav` |
| ABIs de distribuição | ARM64 e ARMv7 |

A mudança do identificador faz o Android tratar esta versão como um aplicativo separado da versão anterior. Ela não atualiza a instalação antiga nem transfere automaticamente dados, extensões ou permissões. A publicação também exige um cadastro correspondente na loja e uma configuração de assinatura adequada.

### Ambiente

O repositório não inclui Gradle Wrapper. Antes de compilar, instale:

- JDK 17.
- Gradle 9.3.1.
- Android SDK 37.
- Android Build Tools 36.0.0.

### Build e testes

Para executar os testes unitários e gerar uma build de depuração:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

Para executar os testes instrumentados em um emulador x86_64 conectado:

```bash
gradle -PsnavEmulatorTest=true :app:connectedDebugAndroidTest
```

A propriedade seleciona x86_64 para o emulador. As compilações normais continuam usando as ABIs de distribuição. Execute os testes instrumentados em uma instalação isolada: os testes de extensões instalam e removem pacotes de teste.

O workflow [Android PR verification](.github/workflows/android-pr-verify.yml) executa build, testes unitários e testes em emulador. Os testes no emulador não substituem a validação em dispositivos físicos.

### Release

Para gerar um Android App Bundle (AAB) com as ABIs de distribuição:

```bash
gradle -PsnavBuildBundle=true :app:bundleRelease
```

Essa propriedade mantém os arquivos intermediários do AAB separados dos APKs por arquitetura.

Para gerar uma release assinada, configure as variáveis de ambiente `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD`, e execute:

```bash
gradle :app:assembleRelease
```

Sem todas as variáveis, o projeto pode gerar uma release não assinada para validar o build. Não compartilhe chaves ou senhas. O [workflow de release](.github/workflows/android-release.yml) descreve a configuração de CI, quando disponível.

## Documentação do projeto

Antes de contribuir, leia [AGENTS.md](AGENTS.md) e [WRITING_STYLE.md](WRITING_STYLE.md). Consulte também o [histórico de alterações](CHANGELOG.md). Resultados registrados no changelog se referem às versões e aos testes indicados em cada entrada.

O código do Snav está sob a [licença MIT](LICENSE). A documentação dos recursos e as dependências podem ter licenças próprias.
