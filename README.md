# Microsserviços de Cadastro e Notificação por E-mail

![Java](https://img.shields.io/badge/Java-25%2B-blue?style=for-the-badge&logo=java)
![Spring](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-FF6600?style=for-the-badge&logo=rabbitmq)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-336791?style=for-the-badge&logo=postgresql)

## Sobre o Projeto
Projeto com objetivo de servir como base de estudo.
Ele demonstra uma prática de arquitetura de microsserviços desacoplada e orientada a eventos, utilizando Spring Boot para a lógica de aplicação e RabbitMQ como *Message Broker* para comunicação assíncrona.

O sistema é composto por dois serviços principais:
1.  **`user-service` (Produtor):** Uma API REST para o cadastro de novos usuários.
2.  **`email-service` (Consumidor):** Um serviço que escuta eventos de novos cadastros e envia e-mails de boas-vindas.

##  Tecnologias Utilizadas

-   **Java 25**
-   **Spring Boot**
-   **Spring Data JPA:** (Persistência de dados)
-   **Spring AMQP:** (Integração com RabbitMQ)
-   **Spring Mail:** (Envio de e-mails via SMTP)
-   **PostgreSQL:** (Banco de dados)
-   **RabbitMQ:** (Message Broker)
-   **Lombok:** (Redução de código boilerplate)
-   **Jakarta Validation:** (Validação de DTOs)

##  Arquitetura e Fluxo

O fluxo de dados é projetado para assíncrono, garantindo que o cadastro do usuário seja rápido e que o envio de e-mail (uma operação mais lenta e sujeita a falhas externas) não bloqueie a resposta ao cliente.

1.  Um cliente envia uma requisição `POST /users` para o `user-service`.
2.  O `user-service` valida os dados de entrada (`UserRecordDto`).
3.  O `user-service` salva o novo `UserModel` em seu próprio banco de dados PostgreSQL.
4.  Após a persistência, o `user-service` (através do `UserProducer`) publica uma mensagem contendo os detalhes do e-mail (em formato `EmailDto`) na fila do RabbitMQ.
5.  O `email-service` (através do `EmailConsumer`) está constantemente escutando a fila. Ao receber uma nova mensagem, ele a consome.
6.  O `EmailConsumer` converte a mensagem (`EmailRecordDto`) e chama o `EmailService`.
7.  O `EmailService` tenta enviar o e-mail real usando `JavaMailSender` (SMTP).
8.  Independentemente do sucesso (`SENT`) ou falha (`ERROR`) do envio, o `EmailService` salva um registro (`EmailModel`) em seu *próprio* banco de dados PostgreSQL, servindo como um log de auditoria.

##  Pré-requisitos

-   JDK 17 ou superior
-   Maven ou Gradle
-   Um servidor RabbitMQ (CloudAMQP ou local)
-   Dois bancos de dados PostgreSQL (um para o `user-service` e outro para o `email-service`)
-   Uma conta Gmail com **Senha de App** configurada (para o serviço de e-mail).

##  Configuração

Cada serviço (`ms-user` e `ms-email`) possui seu próprio arquivo `application.properties`.

###  `user-service`

Configure a conexão com o banco de dados de usuários e com o RabbitMQ.

```properties
# server.port=8081 (sugestão)

# Configuração do Banco de Dados (Usuários)
spring.datasource.url=jdbc:postgresql://localhost:5432/ms-user-db
spring.datasource.username= username
spring.datasource.password= senha
spring.jpa.hibernate.ddl-auto=update
````

Recomendação: use porta 8082 para o outro serviço.

# Configuração do RabbitMQ
spring.rabbitmq.addresses=amqps://... (Sua URL do CloudAMQP)
broker.queue.email.name=default.email
