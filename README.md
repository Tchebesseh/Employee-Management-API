# Employee Management API

---

## Description

Ce projet est une **API REST** robuste et complète, développée en **Java avec Spring Boot**. Elle est conçue pour simplifier la gestion des ressources humaines au sein d'une entreprise, en offrant des fonctionnalités complètes pour les employés, les départements, le suivi des présences, et la génération de rapports statistiques et analytiques.

### Fonctionnalités principales

* **Gestion des entités**: Création, consultation, mise à jour et suppression (CRUD) des **employés** et des **départements**.
* **Suivi des présences**: Enregistrement précis des **pointages d'arrivée et de départ** des employés.
* **Rapports et analyses**: Génération de rapports détaillés sur les **présences** et analyses **budgétaires**.
* **Documentation API**: Entièrement documentée avec **Swagger (OpenAPI)** pour une exploration facile des endpoints.
* **Fonctionnalités avancées**: Prise en charge de la **pagination**, du **tri** et de la **recherche** pour optimiser l'accès aux données.

---

## Technologies utilisées

* **Java 17**
* **Spring Boot**: Framework principal pour le développement de l'API.
* **Spring Data JPA**: Pour une gestion simplifiée de la persistance des données.
* **PostgreSQL**: Base de données relationnelle robuste.
* **Maven**: Outil de gestion de projet et de build.
* **Docker & Docker Compose**: Pour la conteneurisation et l'orchestration des services.
* **Swagger (springdoc-openapi)**: Pour la génération automatique de la documentation de l'API.
* **Postman**: Utilisé pour tester et interagir avec l'API.

---

## Prérequis

Assure-toi d'avoir les outils suivants installés et fonctionnels sur ta machine :

* **[Docker](https://docs.docker.com/get-docker/)**: Indispensable pour la conteneurisation.
* **[Docker Compose](https://docs.docker.com/compose/install/)**: Généralement inclus avec Docker Desktop.
* **[Maven](https://maven.apache.org/install.html)**: Recommandé pour la construction du projet, même si Docker peut gérer une partie du build.
* **Java 17**: Nécessaire si tu comptes exécuter l'application localement sans Docker.

---

## Démarrage de l'application

Suis ces étapes pour lancer l'API rapidement avec Docker Compose :

1.  **Clone le dépôt** :
    ```bash
       git clone https://github.com/AbdelRaoufkone/employee_management_api.git
    cd employee_management_api
    ```
    *Assure-toi d'être dans le répertoire racine du projet.*

2.  **Construis et démarre les services** avec Docker Compose :
    ```bash
    docker-compose up --build
    ```
    Cette commande va construire l'image Docker de l'application (si ce n'est pas déjà fait) et démarrer à la fois l'API Spring Boot et la base de données PostgreSQL.

L'API sera ensuite accessible à l'adresse suivante :
`http://localhost:8080/api`

La base de données PostgreSQL est exposée sur le port **5432** localement, ce qui te permet d'utiliser des outils externes comme PgAdmin pour te connecter.

---

## Exécution des tests

Pour exécuter les tests unitaires et d'intégration du projet localement avec Maven :

```bash
mvn clean test
```

## Documentation Swagger

L'API est documentée avec Swagger. Pour y accéder, ouvre dans ton navigateur : http://localhost:8080/swagger-ui.html
Tu y trouveras toutes les routes, les modèles, les paramètres et les exemples.

## Utilisation de la collection Postman

Dans le dossier `postman/`, tu trouveras le fichier `employee-management-api.postman_collection.json` contenant les requêtes préconfigurées pour tester toutes les fonctionnalités de l’API.

Pour l’utiliser :

1. Ouvre Postman.
2. Clique sur **Import**.
3. Sélectionne le fichier JSON dans `postman/employee-management-api.postman_collection.json`.
4. La collection sera ajoutée à ton espace de travail.
5. Modifie l’URL de base si besoin (variables d’environnement) pour pointer vers `http://localhost:8080`.
6. Lance les requêtes pour tester l’API.

Pour toute question ou problème, n’hésite pas à ouvrir un ticket (issue) dans le dépôt.
