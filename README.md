# Reusinage\_SimulateurImpot2024

## 💾 Description

Ce projet consiste à refactorer et tester un simulateur d’impôt sur le revenu basé sur le barème fiscal français applicable aux **revenus de 2023**.
L'objectif est de rendre le code plus lisible, modulaire, maintenable et correctement couvert par des tests unitaires, tout en respectant les normes de qualité logicielle.

---

## 🚀 Technologies utilisées

* **Java 17+**
* **JUnit 5** : tests unitaires
* **Maven** : gestionnaire de dépendances et build
* **Checkstyle** : vérification du style de code
* **JaCoCo** : rapport de couverture de code

---

## 💠 Installation & Exécution

### 1. Cloner le dépôt

```bash
git clone https://github.com/Cassandre9610/Reusinage_SimulateurImpot2024.git
cd Reusinage_SimulateurImpot2024
```

### 2. Exécuter le fichier .bat

```bash
.\automatisation-qualite-code.bat
```
Le rapport HTML sera disponible ici :
`target/site/jacoco/index.html`

### 3. Vérifier la qualité du code avec Checkstyle

```bash
mvn checkstyle:check
```

Les violations de style seront listées dans :
`target/checkstyle-result.xml`

---

## 🦪 Structure des tests

Les tests unitaires couvrent les règles fiscales suivantes :

* Barème progressif de l'impôt 2024
* Calcul du quotient familial
* Application des décotes selon la situation familiale
* Gestion des abattements, enfants à charge, parent isolé, etc.

Les exigences fonctionnelles sont mappées via des identifiants `EXG_IMPOT_XX` visibles dans les noms de méthodes ou commentaires.

---

## ✅ Objectifs de refactoring

* Découpage du code en classes cohérentes (simulateur, foyer, barème, etc.)
* Nom des méthodes explicites
* Tests robustes avec paramètres
* Respect des conventions Java (Checkstyle)
* > 90% de couverture avec JaCoCo

---

## 🔍 Liens utiles

* [Documentation impôts 2024 (BOFiP)](https://bofip.impots.gouv.fr/)
* [Maven](https://maven.apache.org/)
* [JaCoCo](https://www.jacoco.org/)
* [Checkstyle](https://checkstyle.org/)

---

## 🙋‍♀️ Auteure

Ce projet est réalisé dans le cadre d’un exercice de réusinage de code par **Cassandre9610**.
