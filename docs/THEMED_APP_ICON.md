# Ícone temático do aplicativo

O Snav fornece uma camada monocromática para o ícone adaptativo. O launcher pode
usar essa camada para aplicar as cores do tema e do papel de parede do aparelho.
O recurso depende do Android 13 (API 33) ou posterior, da opção de ícones
temáticos e do suporte do launcher. O aplicativo não controla essa preferência.

A camada usa a silhueta do S da arte original em
`app/src/main/assets/ic_launcher_source.png`. A tarefa `generateLauncherIcons`
gerá a máscara transparente e o ícone adaptativo com `<monochrome>` para API 33.
O recorte, o tamanho e a posição acompanham o primeiro plano colorido existente.
Para Android 8 a 12 e launchers sem suporte, o ícone adaptativo colorido permanece
disponível; o aplicativo não altera os ícones da interface.

O teste instrumentado `ThemedAppIconTest` verifica a camada monocromática e o
fallback colorido. Ele não prova que todos os launchers a exibem: confirme também
no dispositivo, com ícones temáticos ativados e desativados e temas claro e
escuro. A opção e sua localização nas configurações variam entre fabricantes.

Referência: [ícones adaptativos no Android](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive).
