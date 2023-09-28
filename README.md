# Flashcard Service

## Description

The Flashcard Service is a component of our learning platform, managing flashcards. 
These flashcards consist of multiple sides, each containing text, images, and customizable labels. Functionalities are follows:

1. **Querying and Creating Flashcards:** Users can create and interact with flashcards, customizing the content on each side.

2. **Question and Answer Designation:** Users can designate sides as 'Questions,' determining which side they need to guess first.

3. **Spaced Repetition:** The service utilizes spaced repetition to determine when a user should study a flashcard again, based on their success.

4. **Learning Progress Tracking:** The service tracks user progress with flashcards, success rates, and suggesting future study intervals.
## Environment variables
### Relevant for deployment

| Name                       | Description                        | Value in Dev Environment                           | Value in Prod Environment                                                |
|----------------------------|------------------------------------|----------------------------------------------------|--------------------------------------------------------------------------|
| spring.datasource.url      | PostgreSQL database URL            | jdbc:postgresql://localhost:6032/flashcard_service | jdbc:postgresql://flashcard-service-db-postgresql:5432/flashcard-service |
| spring.datasource.username | Database username                  | root                                               | gits                                                                     |
| spring.datasource.password | Database password                  | root                                               | *secret*                                                                 |
| DAPR_HTTP_PORT             | Dapr HTTP Port                     | 6000                                               | 3500                                                                     |
| server.port                | Port on which the application runs | 6001                                               | 6001                                                                     |

### Other properties

| Name                                    | Description                               | Value in Dev Environment                | Value in Prod Environment                                                  |
|-----------------------------------------|-------------------------------------------|-----------------------------------------|----------------------------------------------------------------------------|
| spring.graphql.graphiql.enabled         | Enable GraphiQL web interface for GraphQL | true                                    | true                                                                       |
| spring.graphql.graphiql.path            | Path for GraphiQL when enabled            | /graphiql                               | /graphiql                                                                  |
| spring.profiles.active                  | Active Spring profile                     | dev                                     | prod                                                                       |
| spring.jpa.properties.hibernate.dialect | Hibernate dialect for PostgreSQL          | org.hibernate.dialect.PostgreSQLDialect | org.hibernate.dialect.PostgreSQLDialect                                    |
| spring.sql.init.mode                    | SQL initialization mode                   | always                                  | always                                                                     |
| spring.jpa.show-sql                     | Show SQL queries in logs                  | true                                    | true                                                                       |
| spring.sql.init.continue-on-error       | Continue on SQL init error                | true                                    | true                                                                       |
| spring.jpa.hibernate.ddl-auto           | Hibernate DDL auto strategy               | create                                  | update                                                                     |
| DAPR_GRPC_PORT                          | Dapr gRPC Port                            | -                                       | 50001                                                                      |
| logging.level.root                      | Logging level for root logger             | INFO                                    | -                                                                          |

## API description

The GraphQL API is described in the [api.md file](api.md).

The endpoint for the GraphQL API is `/graphql`. The GraphQL Playground is available at `/graphiql`.

## Get started

A guide how to start development can be
found [wiki](https://gits-enpro.readthedocs.io/en/latest/dev-manuals/backend/get-started.html).


