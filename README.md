# 🚀 Cyna Microservices - Spring Boot

Ce projet est une architecture **microservices** basée sur **Spring Boot**, avec un **Config Server**, **Eureka Server**, **API Gateway**, et plusieurs services métier (**auth-users**, **products**). La base de données utilisée est **MySQL**.

## 🏗️ Architecture des Services

- **Config Server (`config-server`)** : Gère la configuration centralisée.
- **Eureka Server (`eureka-server`)** : Service Discovery pour l'enregistrement des microservices.
- **API Gateway (`api-gateway`)** : Point d'entrée unique pour l'accès aux services.
- **Auth Service (`auth-users`)** : Gestion des utilisateurs et authentification.
- **Product Service (`products`)** : Gestion des produits.
- **Base de données (`cyna_db`)** : MySQL avec persistance des données.
- **et d'autres qui sont en route...**

## 📦 Prérequis

- **Docker** et **Docker Compose** installés sur votre machine.
- **Java 23+** et **Maven** si vous souhaitez exécuter les services manuellement.

## 🚀 Lancement avec Docker Compose

1. Clonez le projet :
   ```bash
   git clone https://github.com/KFLandry/cyna.git
   cd cyna

2. Cloner le config-repo
   ```bash
   git clone https://github.com/KFLandry/config-repo.git
   
NB : Pour un lancement en local hors docker, utiliser le porfil : local 