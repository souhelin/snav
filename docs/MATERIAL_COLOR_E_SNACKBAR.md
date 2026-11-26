# Cores dinâmicas e mensagens

O Snav usa as cores dinâmicas do Material Components para Android quando o
sistema e o fabricante as oferecem (Android 12 ou posterior). A preferência
**Cores dinâmicas** nas configurações permite voltar à paleta do Snav; ela é
salva e a Activity é recriada para aplicar a escolha. Em aparelhos não
compatíveis, o controle fica indisponível e a paleta do Snav é usada. O modo
claro ou escuro segue a configuração do dispositivo. As cores são aplicadas à
interface do navegador, não ao conteúdo dos sites nem ao ícone temático do
launcher, cuja opção é independente.

O Snackbar do Material é exibido na parte inferior para feedback breve e pode
ser dispensado por arrasto. Ao desafixar um atalho, **Desfazer** tenta fixá-lo
novamente, respeitando o limite de quatro atalhos; caso não seja possível, uma
mensagem informa o resultado. O tempo de exibição é gerenciado pelo componente,
incluindo ajustes de acessibilidade do Android. Operações que exigem decisão ou
alertas mais longos continuam usando seus fluxos existentes.

`MaterialFeedbackTest` verifica a ação do Snackbar e o contraste das cores de
superfície nos modos de cor disponíveis no emulador. Testes em dispositivos,
com papel de parede, modo escuro, leitor de tela, teclado e diferentes versões,
ainda são recomendados.
