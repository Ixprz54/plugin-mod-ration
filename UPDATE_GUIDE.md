# Guide de Mise à Jour - Résolution de l'erreur "zip file closed"

## Problème Résolu

L'erreur `java.lang.IllegalStateException: zip file closed` qui empêchait le plugin de se charger a été corrigée.

### Cause du problème

Le **maven-shade-plugin** n'était pas correctement configuré, ce qui causait :
- Des problèmes de classloader lors du chargement des classes
- Des conflits avec les fichiers META-INF
- Un JAR mal construit

### Solution Appliquée

Le `pom.xml` a été mis à jour avec :
1. ✅ `createDependencyReducedPom` désactivé
2. ✅ Filtres pour exclure les fichiers de signature problématiques
3. ✅ Transformers pour gérer correctement le MANIFEST
4. ✅ Relocation complète de toutes les dépendances (HikariCP, MariaDB, Gson)

## Instructions de Recompilation

### Étape 1 : Récupérer les dernières modifications

Si tu as cloné le repository, fais un pull :
```bash
git pull origin claude/moderation-plugin-development-011CUQ2w44xdwH9AUwmMCZqr
```

Ou télécharge le nouveau `pom.xml` depuis le repository.

### Étape 2 : Nettoyer les anciens fichiers

Dans IntelliJ IDEA, ouvre le terminal Maven et exécute :
```bash
mvn clean
```

### Étape 3 : Recompiler le plugin

```bash
mvn package
```

**OU** utilise le bouton Maven dans IntelliJ :
1. Ouvre l'onglet **Maven** (généralement à droite)
2. Déplie `CuboriaModerationPlugin` → `Lifecycle`
3. Double-clic sur **clean**
4. Puis double-clic sur **package**

### Étape 4 : Vérifier la compilation

Tu devrais voir :
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: XX.XXX s
```

Le nouveau JAR sera dans : `target/CuboriaModerationPlugin-1.0.0.jar`

### Étape 5 : Installer sur le serveur

1. **Arrête le serveur**
2. **Supprime l'ancien JAR** du dossier `plugins/`
3. **Copie le nouveau JAR** dans `plugins/`
4. **Démarre le serveur**

### Étape 6 : Vérifier le chargement

Dans les logs, tu devrais maintenant voir :
```
[INFO]: [CuboriaModerationPlugin] ═══════════════════════════════════════
[INFO]: [CuboriaModerationPlugin]   Cuboria Moderation Plugin
[INFO]: [CuboriaModerationPlugin]   Version: 1.0.0
[INFO]: [CuboriaModerationPlugin]   Développeur: EmyXtrm
[INFO]: [CuboriaModerationPlugin] ═══════════════════════════════════════
[INFO]: [CuboriaModerationPlugin] ✓ Configuration Manager initialisé
[INFO]: [CuboriaModerationPlugin] ✓ Database Manager initialisé
[INFO]: [CuboriaModerationPlugin] ✓ Discord Webhook Manager initialisé
[INFO]: [CuboriaModerationPlugin] ✓ Staff Mode Manager initialisé
[INFO]: [CuboriaModerationPlugin] ✓ Freeze Manager initialisé
[INFO]: [CuboriaModerationPlugin] ✓ Sanction Manager initialisé
[INFO]: [CuboriaModerationPlugin] ✓ Report Manager initialisé
[INFO]: [CuboriaModerationPlugin] ✓ GUI Manager initialisé
[INFO]: [CuboriaModerationPlugin] Plugin de modération chargé avec succès!
```

### Étape 7 : Tester les commandes

Connecte-toi au serveur et teste :
```
/staffco
```

Tu devrais voir :
```
[Cuboria] Mode staff activé! Inventaire sauvegardé.
```

## Configuration de la Base de Données

⚠️ **IMPORTANT** : Avant d'utiliser le plugin, configure la base de données dans `plugins/CuboriaModerationPlugin/config.yml` :

```yaml
database:
  enabled: true
  host: "localhost"
  port: 3306
  database: "cuboria"
  username: "ton_utilisateur"
  password: "ton_mot_de_passe"
  pool-size: 10
```

Si tu n'as pas encore créé la base de données :

```sql
CREATE DATABASE cuboria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cuboria_user'@'localhost' IDENTIFIED BY 'mot_de_passe_securise';
GRANT ALL PRIVILEGES ON cuboria.* TO 'cuboria_user'@'localhost';
FLUSH PRIVILEGES;
```

Les tables seront créées automatiquement au premier démarrage.

## Si le problème persiste

1. **Vérifie Java 21** : Le serveur doit utiliser Java 21 (pas Java 24)
2. **Vérifie les logs** : Cherche d'autres erreurs dans `logs/latest.log`
3. **Teste sans base de données** : Met temporairement `database.enabled: false` dans le config
4. **Vérifie les permissions** : Le fichier JAR doit avoir les bonnes permissions de lecture

## Prochaines Étapes

Une fois le plugin chargé :
1. Configure les webhooks Discord (optionnel)
2. Personnalise les templates de sanction dans `sanctions.yml`
3. Personnalise les messages dans `config.yml`
4. Configure les permissions pour ton groupe staff
5. Teste toutes les fonctionnalités

## Besoin d'aide ?

Si le plugin ne se charge toujours pas :
- Envoie les **logs complets** du démarrage
- Vérifie que tu utilises **Paper 1.21.4**
- Vérifie que tu utilises **Java 21** (pas 24)
- Assure-toi que le nouveau `pom.xml` a bien été utilisé pour la compilation
