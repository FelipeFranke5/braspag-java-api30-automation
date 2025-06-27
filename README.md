# API E-commerce 3.0 - Automação Java

## Objetivo

Este projeto foi criado como uma biblioteca 
secundária para substituir o arquivo Python 
responsável por executar a automação disponível no projeto 
[my-automator-v2](https://github.com/FelipeFranke5/my-automator-v2).

Antes de existir essa biblioteca, a automação responsável por obter
as informações de um cadastro na [API E-commerce Cielo](https://docs.cielo.com.br/) 
(utilizando a Braspag) era executado em um arquivo Python e depois chamado no projeto 
Java mencionado acima.

## O que é a Braspag

A Braspag é uma ferramenta administrativa que permite para os lojistas consultarem 
diversas informações de uma loja afiliada à Cielo, por exemplo um 
[relatório de vendas](https://docs.cielo.com.br/ecommerce-cielo/docs/transacoes)
ou [extrato de cobrança](https://docs.cielo.com.br/ecommerce-cielo/docs/extrato-da-cobranca), além de poder customizar algumas configurações relacionadas ao
comportamento da API E-commerce, como por exemplo [configurar as regras de retentativa
de uma recorrência](https://docs.cielo.com.br/ecommerce-cielo/docs/configurando-a-recorrencia).

Para acesso interno de colaboradores, é possível realizar diversas tarefas, sendo uma delas a
pesquisa de informações sobre uma loja afiliada que possui cadastro na Braspag. Criei essa
automação para facilitar a consulta (e / ou processamento) em lote de uma ou várias lojas.
Ou seja, o script disponível neste projeto foi criado pensando na execução assíncrona de
forma independente.

A automação utiliza a biblioteca [Selenium](https://github.com/SeleniumHQ/seleniumhq.github.io/tree/trunk)
para realizar login na ferramenta e executar o processo de busca. Se a loja for encontrada, será
possível obter as informações mais relevantes.

## Quem pode executar esse script

Para que seja possível utilizar essa biblioteca, é
necessário ter uma conta na Braspag com acesso de colaborador. Ou seja, o acesso de lojista não pode
ser utilizado, já que não possui escopo para pesquisar outras lojas.

Assumindo que alguém tenha credenciais com escopo de colaborador, a biblioteca pode ser chamada
por outro código escrito em Java. Basta criar um novo objeto da classe **AutomationRunnerImpl** e
chamar a sua única função pública: **run**. O resultado (caso a automação seja realizada com sucesso)
é um objeto do tipo **CieloMerchant**, no qual contém as informações de:

- **establishmentCode** - Número do Estabelecimento Cielo (EC)
- **merchantId** - Identificador da Loja (MerchantId)
- **documentType** - Tipo de documento (CPF ou CNPJ)
- **documentNumber** - Número do documento
- **name** - Nome Fantasia ou Nome da Pessoa
- **blocked** - Booleano que indica se o cadastro está bloqueado atualmente
- **pixEnabled** - Booleano que indica se o [PIX](https://docs.cielo.com.br/ecommerce-cielo/docs/pix) está habilitado
- **antifraudEnabled** - Booleano que indica se o [antifraude](https://docs.cielo.com.br/risco/docs/antifraude) está habilitado
- **tokenizationEnabled** - Booleano que indica se o [Cartão Protegido](https://docs.cielo.com.br/ecommerce-cielo/docs/tokenizacao-de-bandeira) está habilitado
- **velocityEnabled** - Booleano que indica se o [Velocity](https://docs.cielo.com.br/ecommerce-cielo/docs/velocity) está habilitado
- **smartRecurrencyEnabled** - Booleano que indica se a [Recorrência Programada](https://docs.cielo.com.br/ecommerce-cielo/docs/recorr%C3%AAncia-programada-cielo) está habilitada
- **zeroAuthEnabled** - Booleano que indica se o [Zero Auth](https://docs.cielo.com.br/ecommerce-cielo/docs/zero-auth) está habilitado
- **binQueryEnabled** - Booleano que indica se o [Consulta BIN](https://docs.cielo.com.br/ecommerce-cielo/docs/consulta-bin) está habilitado
- **selectiveAuthEnabled** - Booleano que indica se a Autenticação Seletiva está habilitada (Opção interna)
- **automaticCancelationEnabled** - Booleano que indica se a [Garantia de Cancelamento](https://docs.cielo.com.br/ecommerce-cielo/docs/garantia-de-cancelamento#com-garantia-de-cancelamento) está habilitada
- **forceBraspagAuthEnabled** - Booleano que indica se a opção 'Forçar Autenticação Braspag' está habilitado (Opção interna)
- **mtlsEnabled** - Booleano que indica se o MTLS está habilitado (Opção interna)
- **webhookEnabled** - Booleano que indica se o cadastro possui alguma [URL de Notificação](https://docs.cielo.com.br/ecommerce-cielo/docs/webhook)
- **whiteListIpCount** - Byte que representa a quantidade de [IP's cadastrados](https://docs.cielo.com.br/ecommerce-cielo/docs/configura%C3%A7%C3%A3o-de-ips)

## Parâmetros obrigatórios para instanciar a classe AutomationRunnerImpl

- 1º parâmetro: Nome de Usuário
- 2º parâmetro: Senha
- 3º parâmetro: Número do Estabelecimento Cielo (EC)

## Sobre a versão do Selenium

Este projeto foi criado levando em consideração que será executado em um ambiente virtual Docker contendo
uma instalação do Chromium, por isso no arquivo pom.xml existe a dependência **selenium-devtools-v136**, para "dar match"
com a versão que é instalada no projeto original.
