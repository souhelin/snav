# Downloads, permissões, extensões e wallpapers

## Downloads

O Snav salva a resposta recebida pelo GeckoView, preservando a resposta original
em vez de repetir a solicitação sem a sessão do site. É necessário confirmar o
download e a resposta final deve ser HTTPS e bem-sucedida. A lista de downloads
na Biblioteca abre os arquivos disponíveis; downloads em andamento mostram
progresso quando o servidor informa o tamanho, ou os bytes recebidos quando não
informa. Há um canal de notificações separado; no Android 13+ é necessária a
permissão de notificações, mas recusá-la não bloqueia o download.

A transferência usa uma thread do processo do aplicativo, **não** o serviço
DownloadManager do Android. Se o processo for encerrado, ela pode ser
interrompida e não é retomada automaticamente. Downloads já concluídos ficam
em Downloads. Em Android 8–9 o acesso à pasta pública depende da autorização
de armazenamento. Notificações e progresso precisam de validação em aparelho,
incluindo transferências grandes, perda de rede e interrupção do processo.

## Permissões de sites

Em Informações do site, o gerenciador consulta os registros de permissão do
GeckoView para a origem atual. É possível solicitar bloqueio ou retorno ao
estado de pergunta, quando a API permite. Câmera e microfone exigem confirmação
do site e autorização Android; captura de tela e áudio do dispositivo não são
tratadas como câmera ou microfone. Localização, notificações, reprodução
automática, mídia protegida e armazenamento são solicitados ao usuário quando
GeckoView fornece a solicitação. Notificações de sites usam um canal separado e
a autorização Android. Ações adicionais de notificações, Web Push, XR e acesso
à rede local não estão integrados; pedidos não reconhecidos são recusados.
Conceder armazenamento persistente não pode ser revertido individualmente pela
API GeckoView. Os registros exibidos são os fornecidos pelo GeckoView e não
representam todas as permissões Android ou decisões temporárias de mídia.

## Extensões

Quando a extensão ativada expõe `optionsPageUrl`, suas próprias configurações
são abertas em uma sessão GeckoView dedicada, restrita à origem interna da
extensão. Links HTTPS acionados pelo usuário abrem no navegador. Extensões sem
página de opções não exibem a ação. A integração completa de abas da extensão
não está implementada; a compatibilidade das páginas depende do GeckoView e das
APIs usadas por cada extensão.

## Unsplash

A seleção é uma lista curada de fotos de temas distintos, com autor e página
original associados a cada imagem; a escolha evita repetir a última imagem
quando há outra disponível. Ao ficar offline, são usadas imagens com crédito no
cache, quando disponíveis. A seleção não consulta a API de busca do Unsplash:
ela requer uma chave de aplicativo e cumprimento das respectivas diretrizes.
A lista é finita e não garante ausência de repetição após percorrê-la. A opção
nas ferramentas remove também o crédito exibido na tela inicial.
