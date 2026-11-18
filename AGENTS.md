# AGENTS.md — SNAV BROWSER
## Regras obrigatórias de desenvolvimento, arquitetura, identidade visual e desempenho

**Projeto:** Snav Browser  
**Repositório oficial:** https://github.com/souhelin/Snav-Browser  
**Plataforma:** Android  
**Idioma principal:** Português (Brasil)

Este documento estabelece as regras obrigatórias para qualquer inteligência artificial, agente de programação ou desenvolvedor que trabalhe no Snav Browser.

As regras devem ser respeitadas em todas as alterações, independentemente da ferramenta utilizada.

**NENHUMA IA ESTÁ AUTORIZADA A IGNORAR ESTAS REGRAS POR PREFERÊNCIA PESSOAL.**

---

# 1. IDENTIDADE DO SNAV

O Snav é um navegador Android com identidade visual própria.

Sua filosofia é:

- Minimalismo.
- Simplicidade.
- Leveza.
- Rapidez.
- Organização.
- Interface limpa.
- Facilidade de utilização.
- Baixo consumo de recursos.

O Snav não deve parecer uma cópia do Chrome, Firefox, Safari ou qualquer outro navegador.

### Regras inegociáveis

1. Preservar a identidade visual original.
2. Não modificar o conceito visual sem autorização.
3. Não adicionar elementos decorativos desnecessários.
4. Não transformar funcionalidades simples em interfaces complexas.
5. Não adicionar componentes apenas para preencher espaço.
6. Não exagerar em animações, sombras, gradientes ou efeitos.
7. Não comprometer a área disponível para navegação.
8. Toda funcionalidade nova deve respeitar o design existente.

**Princípio fundamental: menos elementos, melhor organização e maior eficiência.**

---

# 2. SISTEMA VISUAL E CORES

As cores fazem parte da identidade do Snav e não podem ser modificadas arbitrariamente.

## Paleta de referência

| Elemento | Cor |
|---|---|
| Fundo principal da interface | #34495E |
| Ícones neutros | #757575 |
| Texto principal sobre fundo escuro | #ECF0F1 |
| Texto secundário | #9E9E9E |
| Destaque existente | #E67E22 |
| Superfície de navegação | #FFFFFF |

### Regras obrigatórias

1. Não inventar novas paletas.
2. Não substituir as cores principais por cores da moda.
3. Não utilizar cores vibrantes indiscriminadamente.
4. Não aplicar cores diferentes aos ícones sem justificativa.
5. Não modificar a identidade cromática durante uma implementação funcional.
6. Manter contraste e legibilidade.
7. Evitar fundos desnecessariamente diferentes entre componentes relacionados.

### Regra de precedência

A paleta acima é a referência para novas implementações.

Antes de alterar componentes existentes, verificar os recursos e estilos reais da branch utilizada.

Se houver divergência entre arquivos, identificar a origem da inconsistência antes de realizar uma correção global.

Não realizar mudanças de cor em massa sem autorização.

---

# 3. ÍCONES — PRESERVAÇÃO OBRIGATÓRIA

Os ícones são parte fundamental da identidade visual do Snav.

O projeto já possui recursos próprios, incluindo:

- ic_snav_search
- ic_snav_refresh
- ic_snav_more
- ic_snav_back
- ic_snav_forward
- ic_snav_home
- ic_snav_tabs

Também existem ícones utilizados nas funcionalidades do menu.

### Regras rígidas

1. Preservar os ícones originais.
2. Não substituir os ícones por emojis.
3. Não utilizar caracteres Unicode como substitutos de ícones.
4. Não trocar os desenhos por ícones genéricos.
5. Não utilizar conjuntos diferentes de ícones na mesma interface.
6. Não alterar proporções, espessuras ou formatos arbitrariamente.
7. Não distorcer os recursos existentes.
8. Não remover ícones sem autorização.
9. Manter consistência entre ícones de navegação, endereço e menu.

### Estilo

- Material Design 3 Rounded.
- Aparência minimalista.
- Formas simples.
- Espessuras consistentes.
- Tamanhos proporcionais.
- Alinhamento preciso.

**É proibido redesenhar a identidade dos ícones simplesmente porque outra biblioteca oferece desenhos diferentes.**

Uma substituição somente poderá acontecer quando houver autorização explícita.

---

# 4. INTERFACE — MATERIAL DESIGN 3

O Snav deve seguir os princípios visuais do Material Design 3 (MD3), mantendo sua própria identidade.

MD3 não significa adicionar componentes indiscriminadamente.

### Características obrigatórias

- Layout organizado.
- Hierarquia visual clara.
- Espaçamento consistente.
- Superfícies discretas.
- Cantos arredondados moderados.
- Ícones Rounded.
- Estados de interação claros.
- Boa acessibilidade.
- Tipografia legível.

### Proibições

Não utilizar:

- Glassmorphism exagerado.
- Neumorphism.
- Gradientes decorativos.
- Sombras pesadas.
- Bordas excessivas.
- Cartões desnecessários.
- Botões gigantes.
- Elementos flutuantes sem necessidade.
- Animações constantes.
- Efeitos que prejudiquem o desempenho.

### Regra de simplicidade

Se um elemento não melhora a navegação, a compreensão ou a utilização de uma funcionalidade, ele provavelmente não precisa existir.

---

# 5. BARRAS DE ENDEREÇO E NAVEGAÇÃO

As barras de endereço são elementos centrais do Snav.

Qualquer alteração deve preservar sua função, identidade e organização.

### Regras

1. Preservar a integração com o WebView.
2. Não duplicar funcionalidades existentes.
3. Não adicionar controles desnecessários.
4. Manter ícones consistentes.
5. Garantir boa utilização do espaço horizontal.
6. Evitar sobreposição com teclado e conteúdo.
7. Preservar os comportamentos de pesquisa e navegação.
8. Manter a interface responsiva.
9. Não modificar o fluxo de carregamento de páginas sem necessidade.
10. Não provocar fechamento do aplicativo ao enviar pesquisas.

Qualquer alteração na barra de endereço exige testes específicos de digitação, pesquisa, carregamento, teclado e navegação.

---

# 6. ARQUITETURA E CÓDIGO-BASE

O código existente é a referência obrigatória.

A IA deve analisar a implementação real antes de escrever qualquer código.

### Estrutura identificada

- Android nativo.
- Java.
- XML para layouts.
- Activity principal em MainActivity.java.
- WebView como mecanismo de navegação.
- Recursos visuais em res.
- Configurações de tema em values.
- Recursos de interface em drawable.

### Regras obrigatórias

1. Não presumir que o projeto utiliza Kotlin.
2. Não migrar para Jetpack Compose sem autorização.
3. Não substituir a arquitetura existente por preferência pessoal.
4. Não reescrever MainActivity sem necessidade técnica comprovada.
5. Não remover métodos existentes sem analisar suas dependências.
6. Não duplicar listeners.
7. Não duplicar lógica de navegação.
8. Não criar classes desnecessárias.
9. Não introduzir dependências sem justificativa.
10. Preservar compatibilidade com a estrutura atual do projeto.

### Antes de modificar qualquer arquivo

A IA deve:

1. Identificar a branch atual.
2. Ler o arquivo original.
3. Identificar métodos relacionados.
4. Identificar dependências entre componentes.
5. Verificar possíveis efeitos colaterais.
6. Planejar a alteração mínima necessária.

**Nunca substituir um arquivo inteiro quando uma alteração localizada resolve o problema.**

---

# 7. DESEMPENHO E LEVEZA

Desempenho é uma prioridade absoluta.

O Snav precisa funcionar adequadamente em dispositivos com recursos limitados.

### Regras obrigatórias

- Evitar processamento desnecessário na Main Thread.
- Evitar operações repetitivas.
- Evitar listeners duplicados.
- Evitar criação desnecessária de objetos.
- Evitar carregamento desnecessário de imagens.
- Evitar dependências pesadas.
- Evitar operações excessivas de layout.
- Evitar vazamentos de memória.
- Respeitar o ciclo de vida das Activities.
- Evitar tarefas desnecessárias em segundo plano.

### Interface

Não implementar animações complexas quando uma transição simples resolve o problema.

Não utilizar bibliotecas pesadas para reproduzir componentes que podem ser implementados com os recursos já existentes.

### WebView

Preservar:

- Estabilidade.
- Gerenciamento correto de navegação.
- Ciclo de vida.
- Tratamento de erros.
- Configurações necessárias para funcionamento dos sites.
- Comportamento correto do teclado.

Não desativar recursos de segurança para melhorar artificialmente a velocidade.

---

# 8. SEGURANÇA E PRIVACIDADE

Toda implementação deve considerar segurança.

### Regras obrigatórias

1. Não desativar validação HTTPS.
2. Não ignorar erros de certificados.
3. Não habilitar tráfego HTTP indiscriminadamente.
4. Não expor dados privados em logs.
5. Não armazenar informações sensíveis sem necessidade.
6. Validar URLs antes de processá-las.
7. Evitar exposição desnecessária de interfaces JavaScript.
8. Não adicionar permissões sem justificativa.
9. Preservar o isolamento adequado do WebView.
10. Não introduzir código remoto executável sem necessidade e avaliação de segurança.

Qualquer alteração em configurações do WebView exige análise de segurança.

---

# 9. FUNCIONALIDADES EXISTENTES

Uma nova implementação não pode destruir funcionalidades anteriores.

Antes de modificar qualquer componente, verificar se ele participa de:

- Pesquisa.
- Navegação.
- Histórico.
- Downloads.
- Menu.
- Configurações.
- Limpeza de dados.
- Indicadores de segurança.
- Barra de endereço.
- Controles de navegação.
- Diagnóstico e registros de erros.

### Regra

Não remover funcionalidades, mesmo que pareçam desnecessárias, sem autorização.

Se uma funcionalidade apresentar problemas, corrigir sua implementação preservando seu propósito.

---

# 10. COMPATIBILIDADE ANDROID

Todas as implementações precisam respeitar o ambiente Android do projeto.

Verificar:

- Android SDK.
- Versões mínimas suportadas.
- Compatibilidade das APIs.
- Permissões.
- Ciclo de vida.
- Teclado.
- Orientação.
- Gerenciamento de arquivos.
- Comportamento do WebView.

Não utilizar APIs incompatíveis com a configuração real do projeto.

---

# 11. GRADLE, COMPILAÇÃO E APK

Uma implementação não está concluída apenas porque o código foi escrito.

### Regras obrigatórias

1. Verificar as configurações Gradle existentes.
2. Preservar as configurações de assinatura.
3. Não remover configurações de keystore.
4. Não substituir secrets existentes.
5. Não expor senhas ou chaves privadas.
6. Não modificar configurações de build sem justificativa.
7. Executar os testes disponíveis.
8. Executar a compilação correspondente.
9. Verificar o resultado da compilação.
10. Confirmar a assinatura do APK quando se tratar de uma build de release.

### Comandos de referência

```bash
./gradlew test
./gradlew assembleDebug
./gradlew assembleRelease
```

Os comandos devem ser adaptados às tarefas realmente existentes no projeto.

Nunca afirmar que o APK foi gerado ou assinado sem verificar os resultados.

---

# 12. CONTROLE DE ALTERAÇÕES

Toda alteração deve ser específica e justificada.

### Proibido

- Refatoração global não solicitada.
- Mudanças visuais não autorizadas.
- Migração de arquitetura não solicitada.
- Remoção de funcionalidades.
- Substituição de bibliotecas sem justificativa.
- Alterações em arquivos não relacionados.
- Modificação de recursos visuais por preferência pessoal.

### Obrigatório

- Preservar código funcional.
- Evitar duplicações.
- Manter compatibilidade.
- Identificar riscos.
- Documentar mudanças importantes.
- Informar arquivos modificados.
- Informar testes executados.
- Informar problemas ainda existentes.

Se uma solicitação exigir uma alteração que possa comprometer a estabilidade, explicar o risco antes de prosseguir.

---

# 13. REGRAS DE EXPERIÊNCIA VISUAL

Toda nova tela ou componente deve responder positivamente às seguintes perguntas:

1. Mantém a identidade do Snav?
2. Segue o minimalismo?
3. Respeita as cores existentes?
4. Utiliza os ícones corretos?
5. Segue os princípios MD3?
6. Evita poluição visual?
7. Utiliza o espaço adequadamente?
8. Funciona em telas menores?
9. É acessível?
10. Não prejudica o desempenho?

Se alguma resposta for negativa, revisar a implementação.

---

# 14. PROCESSO OBRIGATÓRIO DE DESENVOLVIMENTO

Toda IA deve seguir esta sequência:

**ETAPA 1 — INSPEÇÃO**

Analisar branch, arquivos, arquitetura e dependências.

**ETAPA 2 — DIAGNÓSTICO**

Identificar o problema real e os componentes envolvidos.

**ETAPA 3 — PLANEJAMENTO**

Definir a menor alteração capaz de resolver o problema.

**ETAPA 4 — IMPLEMENTAÇÃO**

Modificar somente os arquivos necessários.

**ETAPA 5 — VERIFICAÇÃO**

Verificar erros, compatibilidade, segurança e funcionalidades anteriores.

**ETAPA 6 — COMPILAÇÃO**

Executar os testes e a build disponíveis.

**ETAPA 7 — RELATÓRIO**

Apresentar o que foi alterado, os resultados e o que ainda falta.

Não pular etapas para acelerar artificialmente o desenvolvimento.

---

# 15. RELATÓRIO FINAL OBRIGATÓRIO

Ao concluir qualquer tarefa, apresentar:

**Alterações realizadas**
- Arquivos modificados.
- Funcionalidades implementadas.
- Correções realizadas.

**Interface**
- Confirmar preservação das cores.
- Confirmar preservação dos ícones.
- Confirmar consistência MD3.
- Confirmar ausência de elementos desnecessários.

**Desempenho e segurança**
- Informar impactos identificados.

**Testes**
- Testes executados.
- Resultado da compilação.
- Resultado da assinatura, quando aplicável.

**Pendências**
- Problemas não resolvidos.
- Testes que não puderam ser realizados.

Nunca inventar resultados.

---

# 16. REGRA SUPREMA — PRESERVAÇÃO DO SNAV

O Snav não deve perder sua identidade a cada atualização.

Nenhuma inteligência artificial está autorizada a:

- Transformar seu visual em outro produto.
- Substituir seus ícones originais arbitrariamente.
- Modificar sua paleta sem autorização.
- Adicionar componentes exagerados.
- Reescrever sua arquitetura sem necessidade.
- Sacrificar desempenho por estética.
- Sacrificar segurança por conveniência.
- Remover funcionalidades existentes.

A prioridade é construir um navegador Android:

- Minimalista.
- Moderno.
- Leve.
- Rápido.
- Seguro.
- Estável.
- Consistente.
- Fácil de manter.

**O Snav deve evoluir sem perder sua identidade.**

Estas regras são obrigatórias para todos os agentes de inteligência artificial que trabalham neste repositório.
