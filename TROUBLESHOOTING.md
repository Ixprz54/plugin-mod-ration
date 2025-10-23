# Guide de Résolution - Problèmes de Compilation

## Erreur: ConcurrentModificationException lors de la compilation

### Cause

Cette erreur est causée par l'utilisation de **Java 24** (version preview/early-access) avec le plugin qui est conçu pour **Java 21 LTS**.

### Solution

#### Option 1: Installer Java 21 (RECOMMANDÉ)

1. **Télécharger Java 21:**
   - Oracle JDK 21: https://www.oracle.com/java/technologies/downloads/#java21
   - OpenJDK 21: https://adoptium.net/temurin/releases/?version=21

2. **Configurer IntelliJ IDEA pour utiliser Java 21:**

   a. Ouvrir IntelliJ IDEA

   b. `File` → `Project Structure` (ou `Ctrl+Alt+Shift+S`)

   c. Dans `Project`:
      - Cliquer sur `SDK` → `Add SDK` → `Download JDK`
      - Sélectionner `Version: 21` et un vendor (Eclipse Temurin recommandé)
      - Cliquer `Download`

   d. Dans `Project`:
      - `SDK`: Sélectionner le Java 21 que vous venez d'installer
      - `Language level`: Sélectionner `21 - Pattern matching for switch`

   e. Dans `Modules`:
      - Sélectionner votre module
      - `Language level`: Choisir `21`

   f. Cliquer `OK`

3. **Configurer Maven pour Java 21:**

   a. `File` → `Settings` (ou `Ctrl+Alt+S`)

   b. `Build, Execution, Deployment` → `Build Tools` → `Maven` → `Runner`

   c. `JRE`: Sélectionner Java 21

   d. Cliquer `OK`

4. **Recompiler:**
   ```bash
   mvn clean package
   ```

#### Option 2: Modifier temporairement pour Java 24 (NON RECOMMANDÉ)

Si vous devez absolument utiliser Java 24, modifiez le `pom.xml`:

```xml
<properties>
    <maven.compiler.source>24</maven.compiler.source>
    <maven.compiler.target>24</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

⚠️ **Attention**: Java 24 n'est pas une version LTS et peut causer des problèmes de compatibilité avec Paper 1.21.4.

### Vérifier la version de Java utilisée

Dans IntelliJ IDEA, ouvrez le terminal et tapez:
```bash
java -version
```

Vous devriez voir:
```
openjdk version "21.0.x" ...
```

### Problème résolu dans le code

Le fix inclus dans ce commit résout également un problème potentiel avec `.toList()` en utilisant `.collect(Collectors.toList())` pour une meilleure compatibilité.

## Autres problèmes courants

### Erreur: "package io.papermc.paper does not exist"

**Cause:** Les dépendances Maven n'ont pas été téléchargées.

**Solution:**
```bash
mvn clean install
```

### Erreur: "Cannot resolve symbol 'Bukkit'"

**Cause:** IntelliJ n'a pas importé le projet Maven correctement.

**Solution:**
1. Clic droit sur `pom.xml`
2. `Maven` → `Reload Project`

### Erreur lors du build: "Failed to execute goal"

**Cause:** Cache Maven corrompu.

**Solution:**
```bash
mvn clean
mvn dependency:purge-local-repository
mvn package
```

## Configuration recommandée

Pour un développement optimal de plugins Paper:

- **Java:** 21 LTS (OpenJDK ou Oracle)
- **Maven:** 3.8.x ou supérieur
- **IntelliJ IDEA:** 2023.x ou supérieur
- **Paper:** 1.21.4

## Besoin d'aide ?

Si le problème persiste après avoir suivi ce guide:

1. Vérifiez que Java 21 est bien configuré dans IntelliJ
2. Supprimez le dossier `target/` et recompilez
3. Vérifiez les logs Maven pour des erreurs spécifiques
4. Assurez-vous d'avoir une connexion internet pour télécharger les dépendances

## Compilation réussie

Quand la compilation fonctionne, vous devriez voir:
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: X.XXX s
[INFO] Finished at: 2025-XX-XXTXX:XX:XX+XX:XX
[INFO] ------------------------------------------------------------------------
```

Le fichier JAR sera dans: `target/CuboriaModerationPlugin-1.0.0.jar`
