# 🚀 Cyna Microservices - Spring Boot

Ce projet est une architecture **microservices** basée sur **Spring Boot**, avec un **Config Server**, **Eureka Server**, **API Gateway**, et plusieurs services métier (**auth-users**, **products**). La base de données utilisée est **MySQL**.

## 🏗️ Architecture des Services

- **Config Server (`config-server`)** : Gère la configuration centralisée.
- **Eureka Server (`eureka-server`)** : Service Discovery pour l'enregistrement des microservices.
- **API Gateway (`api-gateway`)** : Point d'entrée unique pour l'accès aux services.
- **Auth Service (`auth-users`)** : Gestion des utilisateurs et authentification.
- **Product Service (`products`)** : Gestion des produits SaaS .
- **Subscriptions Service (`subscriptions`)** : Subscription aux SaaS.
- **Base de données (`cyna_db`)** : MySQL avec persistance des données.
- **et d'autres qui sont en route...**

## 📦 Prérequis

- **Docker** et **Docker Compose** installés sur votre machine.
- **Java 23+** et **Maven** si vous souhaitez exécuter les services manuellement.
- **Stripe (Avoir une compte client)** et **Stripe cli** 
- **SendMailer** Avoir une compte client

## 🚀 Lancement avec Docker Compose

1. Clonez le projet :
   ```bash
   git clone https://github.com/KFLandry/cyna.git
   cd cyna

2. Cloner le config-repo
   ```bash
   git clone https://github.com/KFLandry/config-repo.git
   
## NB :
Pour un lancement en local hors docker, utiliser le profil : local 
Avec 
Pour lancer le service Subscription en local
  - Entrer vos KEY APIs dans les variables d'environnement de la configuration de lancement ou charger un fichier .env, suivant la nommage des variables present dans la config du service
  - Activer l'ecoute du service Subscriptions des webhooks envoyés par Stripe avec la commande
     ```bash
     stripe listen --forward-to localhost:8083/api/v1/subscriptions/webhook