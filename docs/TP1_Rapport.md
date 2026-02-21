# TP1 — Base de l'application et fonctionnalités de base

## 1) Objectif
Ce livrable met en place la base de l'application **Rep-It** avec Kotlin + Jetpack Compose,
en se concentrant sur la gestion de routines simples et leur affichage dans une page d'accueil.

## 2) Prototype Figma
Prototype interactif (écrans accueil + cartes routines) :
- Lien : `https://www.figma.com/file/REPIT-TP1-PROTOTYPE` *(à remplacer par le lien partagé de l'équipe)*

## 3) Configuration initiale
- Projet Android Studio : module `app` configuré.
- Stack UI : Jetpack Compose + Material 3.
- Langage : Kotlin.

## 4) Fonctionnalités implémentées
### 4.1 Interface utilisateur
- Écran d'accueil `HomeScreen` avec barre de titre.
- Liste scrollable de routines avec cartes (titre, description, fréquence, durée).

### 4.2 Gestion des routines
- Modèle `Routine`.
- Données chargées depuis un fichier utilitaire `RoutineUtil`.
- Exposition des routines via `RoutineRepository`.

## 5) Tests réalisés
- Compilation de l'application via Gradle.
- Tests unitaires existants exécutés.

## 6) Résultat attendu
Au lancement de l'application, l'utilisateur voit directement la page d'accueil contenant les routines définies dans le fichier utilitaire.
