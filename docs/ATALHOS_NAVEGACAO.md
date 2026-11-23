# Atalhos e navegação

A tela inicial mostra até quatro páginas fixadas e as quatro páginas mais recentes
registradas no histórico local. Use **Fixar na tela inicial** no menu da página
atual ou mantenha um recente pressionado para fixá-lo. Mantenha um fixado
pressionado para removê-lo. Os fixados permanecem ao limpar o histórico.

Para mostrar ícones, o aplicativo tenta buscar `/favicon.ico` diretamente de
cada origem HTTPS exibida, inclusive ao reabrir a tela inicial. Essa requisição
adicional não usa cookies, não segue redirecionamentos, respeita a preferência
Do Not Track e não consulta serviços de terceiros. Se o ícone não estiver
disponível ou não for decodificável, é usado o símbolo oficial Material Symbols Rounded de página da Web.

## Verificação em dispositivo

- Navegar entre páginas HTTPS, voltar e avançar, abrir links em nova aba e trocar
  de abas; confirmar que o conteúdo continua visível e que a barra de progresso
  desaparece. Testar restauração da aba e rotação. Abrir links em nova aba
  repetidamente e trocar entre uma aba vazia e uma página durante o carregamento;
  a tela de aba vazia não deve aparecer sobre uma página. Testar também a troca
  de abas com o modo de leitura ativo.
- Desconectar a rede e abrir uma página; confirmar a mensagem de erro e que
  **Tentar novamente** funciona após restaurar a conexão. Interromper um
  carregamento não deve apresentar erro de uma navegação anterior. Cancelar uma tentativa
  de HTTP e confirmar que a tela anterior continua acessível.
- Abrir Ferramentas a partir da tela inicial e da navegação. Confirmar que a
  barra superior está oculta e que o botão voltar retorna à tela anterior.
- Fixar quatro páginas, tentar fixar uma quinta, desafixar e fixar outra;
  reiniciar o aplicativo e confirmar a persistência. Abrir atalhos e verificar
  os quatro recentes, títulos, ícones e alternativa quando não houver ícone.
- Limpar o histórico e os dados de navegação e confirmar que os recentes somem,
  mas os fixados permanecem. Verificar acessibilidade, tema escuro e tela inicial
  com teclado aberto em telas pequenas.
- Visitar pelo menos 5 URLs HTTPS diferentes e confirmar que os 4 recentes mais
  novos aparecem na ordem correta. Voltar a uma página, reiniciar o aplicativo e
  verificar a ordem, os títulos e a persistência. Os fixados devem continuar
  independentes da lista de recentes.
- Na tela inicial, confirmar que apenas o menu aparece no topo. As ações de
  página ficam desativadas até haver uma página aberta; gerenciar abas,
  biblioteca e preferências continuam disponíveis. Repetir após abrir uma aba
  vazia, interromper o carregamento, limpar os dados e diante de um erro.
- Abrir uma página HTTPS e confirmar que o endereço e o ícone de informações
  aparecem na tela 2. Salvar nos favoritos pelo menu e verificar na biblioteca;
  a tela de informações não deve exibir as ações de favoritos ou histórico.

## Menu dos atalhos

Mantenha pressionada uma página fixada ou recente para abrir suas opções. É
possível adicionar um site HTTPS, abrir em outra aba, baixar o link, editar ou
remover um fixado, e controlar as seções da tela inicial. A opção de aba privada
está desativada porque o Snav ainda não oferece navegação privada. Para recuperar
atalhos ocultos ou configurar seções vazias, abra o menu de três pontos e toque
em **Configurar atalhos**. O download de um link não segue redirecionamentos e
pode não funcionar em sites que os exigem.
