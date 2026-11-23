# Atalhos e navegação

A tela inicial mostra até quatro páginas fixadas e as quatro páginas mais recentes
registradas no histórico local. Use **Fixar na tela inicial** no menu da página
atual ou mantenha um recente pressionado para fixá-lo. Mantenha um fixado
pressionado para removê-lo. Os fixados permanecem ao limpar o histórico.

Para mostrar ícones, o aplicativo tenta buscar `/favicon.ico` diretamente de
cada origem HTTPS exibida, inclusive ao reabrir a tela inicial. Essa requisição
adicional não usa cookies, não segue redirecionamentos, respeita a preferência
Do Not Track e não consulta serviços de terceiros. Se o ícone não estiver
disponível ou não for decodificável, é usada a inicial do nome da página.

## Verificação em dispositivo

- Navegar entre páginas HTTPS, voltar e avançar, abrir links em nova aba e trocar
  de abas; confirmar que o conteúdo continua visível e que a barra de progresso
  desaparece. Testar restauração da aba e rotação.
- Desconectar a rede e abrir uma página; confirmar a mensagem de erro e que
  **Tentar novamente** funciona após restaurar a conexão. Cancelar uma tentativa
  de HTTP e confirmar que a tela anterior continua acessível.
- Abrir Ferramentas a partir da tela inicial e da navegação. Confirmar que a
  barra superior está oculta e que o botão voltar retorna à tela anterior.
- Fixar quatro páginas, tentar fixar uma quinta, desafixar e fixar outra;
  reiniciar o aplicativo e confirmar a persistência. Abrir atalhos e verificar
  os quatro recentes, títulos, ícones e alternativa quando não houver ícone.
- Limpar o histórico e os dados de navegação e confirmar que os recentes somem,
  mas os fixados permanecem. Verificar acessibilidade, tema escuro e tela inicial
  com teclado aberto em telas pequenas.
