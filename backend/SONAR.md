![](https://vectorseek.com/wp-content/uploads/2023/08/Sonarqube-Logo-Vector.svg-.png)

# Une démarche qualité basée sur des analyses régulières Sonar

Ce document présente la démarche pour vous connecter au serveur **SonarQube**, qui vous  
permettra de suivre la qualité de vos développements.

Pour l’instant, nous nous focalisons sur la partie back-end, mais toute proposition visant à  
connecter ou améliorer la qualité du front-end sera la bienvenue.

## Configuration du serveur

Le serveur est accessible à l’adresse :  
[https://im2ag-sonar.univ-grenoble-alpes.fr/](https://im2ag-sonar.univ-grenoble-alpes.fr/)

Vous pouvez vous y connecter avec vos identifiants `Agalan`.

Sur le serveur, il faut créer un nouveau projet et obtenir une clé d’accès, qui servira pour l’accès distant.

Choisissez un nom de projet cohérent avec les conventions Maven :  
`<fr.uga.miage.m1>:<pc_classroomGroupName>`.

Définissez l’`artifactId` en suivant la convention :  
`pc_classroomGroupName`.

![Pasted image 20250916005909.png](docs/sonar/createProject.png)

## Création d’un token

- Pour pouvoir pousser votre code, vous devez disposer d’un token, comme avec `GitHub`.
- Pour cela, rendez-vous dans `My Account > Security`.

![Pasted image 20250916132153.png](docs/sonar/createToken.png)

⚠️ Gardez-le précieusement, il sera utile plus tard.
## Configuration du projet Maven

### Lien du serveur

- Afin que `Maven` puisse trouver le serveur Sonar, ajoutez-le dans les `properties`.

  ```xml
  <sonar.host.url>https://im2ag-sonar.univ-grenoble-alpes.fr</sonar.host.url>
  ```

### L’identité de votre projet

- L’identité de votre projet `Maven` doit suivre la configuration que vous avez ajoutée dans Sonar. Assurez-vous donc que, dans le fichier `pom.xml` :

    - `groupId`
      ```xml
      <groupId>fr.uga.miage.m1</groupId>  
      ```
    * `atrifactId`
       ```xml
      <groupId>pc-classroomGroupName</groupId>  
      ```
    * `name`
  ```xml
      <name>pc-classroomGroupName</name>  
      ```


## Les plugins utiles

### Maven Surefire Plugin

- Ce plugin sert à lancer les tests au sein d’un projet `Maven`.

```xml
<plugin>  
    <groupId>org.apache.maven.plugins</groupId>  
    <artifactId>maven-surefire-plugin</artifactId>  
    <version>3.5.4</version>  
</plugin>
```

### JaCoCo

Le plugin **JaCoCo** sert à mesurer la **couverture du code** par les tests automatisés (unitaires, d’intégration, etc.).

Ajoutez donc :
```xml
<plugin>  
    <groupId>org.jacoco</groupId>  
    <artifactId>jacoco-maven-plugin</artifactId>  
    <version>0.8.13</version>  
    <executions>        
	    <execution>  
            <goals>  
                <goal>prepare-agent</goal>  
            </goals>  
        </execution>  
        <execution>            
	        <id>generate-code-coverage-report</id>  
            <phase>test</phase>  
            <goals>                
	            <goal>report</goal>  
            </goals>  
        </execution>  
    </executions>  
</plugin>
```


## Vérifier que vous avez bien configuré 👍 ✅

Pour vérifier que votre projet fonctionne, une seule commande permet de compiler et tester votre programme :

```shell
mvn install
```

Pour nettoyer votre projet, utilisez la commande :

```shell
mvn clean
```

## Test qualité avec Maven

Le lancement d’un test qualité se fait désormais à partir d’un goal `Maven` :
- Pensez à générer votre token (cf. [Création d’un token](#création-dun-token))
```shell
mvn clean verify sonar:sonar -Dsonar.login=myAuthenticationToken
```

⚠️ Attention : la commande `mvn install` doit réussir avant de tester celle-ci !

<details> <summary>💡 À savoir</summary>

* Ces tests peuvent être réalisés de manière continue en les intégrant à un serveur Jenkins,  
  par exemple, ou en les lançant manuellement. Le serveur Sonar conserve les deltas.


</details>


##  Configuration Sonarlint

**SonarLint** est un plugin qui sert à analyser ton code **directement dans l’IDE** afin de :
- **Détecter en temps réel** les problèmes de qualité (bugs potentiels, vulnérabilités, mauvaises pratiques, duplications, etc.).
- **Appliquer les règles de codage** définies par Sonar (ou personnalisées).
- **Aider à corriger immédiatement** les erreurs avec des explications et parfois des suggestions de correction.
- **Éviter l’accumulation de dettes techniques** en signalant les problèmes au moment où tu écris le code, avant même le commit ou l’analyse SonarQube/SonarCloud.

## Ajout du plugin

Allez dans `Settings > Plugins > Marketplace` et installez **SonarLint** :

![Pasted image 20250916150654.png](docs/sonar/pluginSonar.png)


Voici la version corrigée :

## Liaison avec le serveur Sonar (pas obligatoire)

- Vous pouvez connecter **SonarLint** directement au serveur Sonar pour voir immédiatement les résultats depuis le serveur.  
  Vous pourrez ainsi surveiller, entre autres :
    - votre couverture de tests,
    - les _code smells_ dont Manuel vous parlera,
    - les failles de sécurité.

### Configuration

- Allez dans les paramètres du plugin et ajoutez une connexion :

![Pasted image 20250916155414.png](docs/sonar/settingsSonar.png)

* Configurez une connexion de type **SonarQube**.

![Pasted image 20250916154654.png](docs/sonar/choixServeur.png)

* Saisissez votre token. (cf. [Création d’un token](#création-dun-token))

![Pasted image 20250916154712.png](docs/sonar/tapToken.png)

* Ensuite, allez dans la configuration du projet et, dans la liste des projets, sélectionnez le vôtre :

![Pasted image 20250916160012.png](docs/sonar/choixProjet.png)


Enjoy 😁