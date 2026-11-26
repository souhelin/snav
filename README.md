# Snav Browser

O Snav Browser é um navegador para Android. O projeto prioriza uma interface simples e controles de navegação, privacidade e dados acessíveis no aplicativo.

![Logo do Snav](image/logo.png)

O navegador usa o GeckoView, componente da Mozilla para exibir conteúdo da web. Esta documentação descreve o código do repositório; ela não garante que todos os recursos estejam disponíveis em uma versão distribuída do aplicativo.

## Ícone do aplicativo

No Android 13 ou posterior, o ícone pode acompanhar as cores do tema do dispositivo quando a opção de ícones temáticos está ativada e o launcher oferece suporte. Caso contrário, o Snav usa o ícone colorido. Consulte [os detalhes do ícone temático](docs/THEMED_APP_ICON.md).

## Aparência e feedback

O Snav acompanha o modo claro ou escuro do dispositivo. Em dispositivos
compatíveis, **Cores dinâmicas** usa a paleta do sistema; a preferência pode
ser desativada para usar as cores do Snav. Em versões sem suporte, a paleta do
aplicativo é usada automaticamente. Mensagens breves usam Snackbar, incluindo
uma opção de desfazer ao desafixar uma página. Veja
[cores e mensagens](docs/MATERIAL_COLOR_E_SNACKBAR.md).

## Navegação

As correções de interface e permissões e as lacunas atuais da integração estão
na [revisão do GeckoView](docs/REVISAO_GECKOVIEW_E_INTERFACE.md).

- Pesquise ou abra endereços pela tela de pesquisa.
- Use as abas, os favoritos, o histórico e os downloads pelo menu. As abas compartilham uma sessão Gecko; isso limita o isolamento e a integração com extensões.
- A tela inicial mostra até quatro atalhos fixados e quatro páginas recentes. Os atalhos têm opções de edição e gerenciamento.
- Arraste a página para baixo a partir do topo para atualizá-la. O gesto depende da resposta do Gecko e pode ser rejeitado para evitar atualizações acidentais. No emulador, a atualização por arrasto não funcionou em uma página curta sem rolagem; use a opção de recarregar do menu nesse caso. Consulte [a documentação de atualização por arrasto](docs/PULL_TO_REFRESH.md).
- Acesse informações do site, modo de leitura e tradução quando disponíveis. O funcionamento depende da página e dos serviços envolvidos.

## Extensões

O gerenciador em **Ferramentas > Extensões** oferece pesquisa no catálogo da Mozilla e instalação de arquivos XPI por HTTPS ou pelo seletor de arquivos. O download e a instalação exigem confirmação, e o GeckoView verifica o pacote e sua assinatura. A tela também oferece controles de ativação, desativação, atualização, remoção e revogação de permissões opcionais.

A instalação não garante compatibilidade. A integração de abas e menus de contexto de extensões é limitada, e não há canal genérico de mensagens nativas para extensões de terceiros. A compatibilidade de cada extensão e de suas APIs depende do GeckoView e das capacidades implementadas pelo Snav. Consulte [a documentação de extensões](docs/EXTENSOES_GECKOVIEW.md) para detalhes, limites e orientações de desenvolvimento.

## Privacidade e segurança

O Snav usa as configurações de proteção contra rastreamento do GeckoView e isolamento dinâmico de cookies de terceiros. A navegação por HTTP é bloqueada e o aplicativo oferece uma tentativa de acesso por HTTPS. O aplicativo não ignora erros de certificados.

Downloads exigem confirmação. As solicitações de localização, câmera e microfone passam por confirmação no aplicativo e pelas permissões do Android quando necessário. A limpeza de dados por site usa a API do GeckoView e não garante a remoção de dados de origens de terceiros. A opção Do Not Track se aplica às navegações iniciadas pelo Snav que usam esse cabeçalho; sites podem ignorá-lo.

Essas medidas não impedem todo rastreamento nem eliminam todos os riscos. A [política de privacidade e segurança](docs/PRIVACY_SECURITY_POLICY.md) contém requisitos e limitações; partes escritas para o antigo WebView devem ser interpretadas junto à [documentação da migração para GeckoView](docs/GECKOVIEW_MIGRATION.md).

## Desenvolvimento

| Configuração | Valor |
|---|---|
| Android mínimo | API 26 (Android 8.0) |
| SDK de compilação | API 37 |
| SDK de destino | API 35 |
| Java | 17 |
| Android Gradle Plugin / Gradle | 9.1.1 / 9.3.1 |
| GeckoView | 160.0.20261116130455 |
| Identificador do aplicativo | `io.github.souhelin.snav` |
| Interface | Android Views/XML e Compose Material 3 para atualização por arrasto |
| ABIs de distribuição | ARM64 e ARMv7 |

A mudança do identificador faz o Android tratar esta versão como um aplicativo separado da versão anterior. Ela não atualiza a instalação antiga e não transfere automaticamente dados, extensões ou permissões. A publicação também exige um cadastro correspondente na loja e a configuração adequada de assinatura.

O repositório não inclui Gradle Wrapper. Instale o Gradle 9.3.1, o JDK 17, o SDK Android 37 e o Build Tools 36.0.0 antes de executar:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

Para executar os testes instrumentados em um emulador x86_64 conectado, use:

```bash
gradle -PsnavEmulatorTest=true :app:connectedDebugAndroidTest
```

A propriedade de teste seleciona x86_64 para o emulador; as compilações normais mantêm as ABIs de distribuição. Execute os testes instrumentados em uma instalação isolada: os testes de extensões instalam e removem pacotes de teste. O workflow [Android PR verification](.github/workflows/android-pr-verify.yml) executa build, testes unitários e testes em emulador. Testes em emulador não substituem a validação em dispositivos físicos.

Para gerar um Android App Bundle (AAB) com as ABIs de distribuição, use `gradle -PsnavBuildBundle=true :app:bundleRelease`. A propriedade separa os arquivos intermediários do AAB dos APKs por arquitetura.

Para gerar uma release assinada, configure `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD` no ambiente e execute `gradle :app:assembleRelease`. Sem todas as variáveis, o projeto pode gerar uma release não assinada para validar o build. Não compartilhe chaves ou senhas. O [workflow de release](.github/workflows/android-release.yml) descreve a configuração de CI, quando disponível.

Antes de contribuir, leia [as regras de desenvolvimento](AGENTS.md) e [o padrão de escrita](WRITING_STYLE.md). Consulte também o [histórico de alterações](CHANGELOG.md), observando que resultados históricos se referem às versões e aos testes indicados em cada registro.

## Licença

O código do Snav está sob a [licença MIT](LICENSE). Consulte a documentação dos recursos e dependências para suas respectivas licenças.
