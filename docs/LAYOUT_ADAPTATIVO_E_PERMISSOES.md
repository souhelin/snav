# Interface adaptativa e permissões

Abas, biblioteca e downloads são destinos em tela inteira no próprio navegador;
voltar retorna à tela anterior sem substituir a sessão Gecko. As ferramentas
abrem categorias separadas e roláveis. A largura do conteúdo é limitada em janelas
amplas, e listas rolam em alturas pequenas. A orientação é tratada pela activity
existente, preservando a sessão; é necessário testar também redimensionamento,
tamanhos de fonte elevados, acessibilidade e teclado em dispositivos físicos.

A reprodução automática sem som é permitida sem prompt por padrão. As opções em
Privacidade e conteúdo controlam **novas solicitações**: ativar não concede câmera,
microfone, localização ou notificações sem confirmação e autorização do Android.
Capturas já iniciadas não são interrompidas. O GeckoView pode conservar decisões
já armazenadas e não consultar novamente o delegate; as opções não revogam essas
decisões. Consulte Informações do site para os registros que o GeckoView permite
alterar; armazenamento persistente concedido não pode ser revogado individualmente.

Pressionar links ou imagens HTTPS usa `GeckoSession.ContentDelegate.onContextMenu`.
Compartilhar obtém uma cópia temporária (até 20 MiB), concedendo acesso somente
à imagem escolhida. Salvar e compartilhar fazem nova solicitação Gecko sem
redirecionamento e podem falhar em imagens autenticadas ou
endereços não HTTPS. Texto usa a seleção nativa do GeckoView, incluindo copiar e
selecionar tudo, com pesquisa acrescentada ao modo contextual; senhas não são
oferecidas para pesquisa. Conteúdo `blob:`, `data:`, imagens protegidas e páginas
com gestos próprios podem não oferecer essas ações.

Referências: https://developer.android.com/develop/ui/views/layout/screen-compat,
https://developer.android.com/develop/ui/views/components/menus e
https://developer.android.com/develop/ui/compose/layouts/adaptive.
