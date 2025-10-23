# Guide d'Installation - Cuboria Moderation Plugin

## Prérequis

- Serveur Minecraft Paper 1.21.4
- Java 21
- Base de données MariaDB ou MySQL
- Webhooks Discord (optionnel)

## Étape 1 : Compilation

### Option A : Avec Maven installé
```bash
./build.sh
```

### Option B : Sans Maven
Si Maven n'est pas disponible, compilez sur votre machine locale :
```bash
mvn clean package
```

Le fichier JAR sera généré dans `target/CuboriaModerationPlugin-1.0.0.jar`

## Étape 2 : Installation du plugin

1. Copiez le fichier JAR dans le dossier `plugins` de votre serveur :
   ```bash
   cp target/CuboriaModerationPlugin-1.0.0.jar /chemin/vers/serveur/plugins/
   ```

2. Démarrez le serveur une première fois pour générer les fichiers de configuration

3. Arrêtez le serveur

## Étape 3 : Configuration de la base de données

### Créer la base de données
```sql
CREATE DATABASE cuboria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cuboria_user'@'localhost' IDENTIFIED BY 'mot_de_passe_securise';
GRANT ALL PRIVILEGES ON cuboria.* TO 'cuboria_user'@'localhost';
FLUSH PRIVILEGES;
```

### Configurer le plugin
Éditez `plugins/CuboriaModerationPlugin/config.yml` :

```yaml
database:
  enabled: true
  host: "localhost"
  port: 3306
  database: "cuboria"
  username: "cuboria_user"
  password: "mot_de_passe_securise"
  pool-size: 10
```

## Étape 4 : Configuration Discord (Optionnel)

### Créer les webhooks

1. Sur votre serveur Discord, allez dans les paramètres d'un salon
2. Intégrations → Webhooks → Nouveau Webhook
3. Copiez l'URL du webhook
4. Répétez pour le salon de changelog

### Configurer les webhooks
Dans `config.yml` :

```yaml
discord:
  webhook-logs: "https://discord.com/api/webhooks/VOTRE_ID/VOTRE_TOKEN"
  webhook-changelog: "https://discord.com/api/webhooks/VOTRE_ID/VOTRE_TOKEN"
```

## Étape 5 : Personnalisation

### Templates de sanctions
Éditez `plugins/CuboriaModerationPlugin/sanctions.yml` pour personnaliser les raisons de sanction.

Exemple d'ajout d'un nouveau template de ban :
```yaml
sanctions:
  ban:
    templates:
      # ... templates existants ...
      - reason: "Ma nouvelle raison"
        duration: 604800  # 7 jours en secondes
        duration-text: "7 jours"
        message: "&cVous avez été banni pendant 7 jours pour ma raison."
```

**Durées courantes :**
- 1 heure = 3600
- 1 jour = 86400
- 7 jours = 604800
- 30 jours = 2592000
- Permanent = -1

### Messages personnalisés
Dans `config.yml`, section `messages`, vous pouvez personnaliser tous les messages du plugin.

### Items du staff
Dans `config.yml`, section `staff-items`, personnalisez les items, leurs noms, lores et positions.

## Étape 6 : Permissions

Ajoutez les permissions dans votre plugin de permissions (LuckPerms, etc.) :

```yaml
# Groupe Staff
permissions:
  - cuboria.staff
  - cuboria.staff.sanction
  - cuboria.staff.freeze
  - cuboria.staff.vanish

# Groupe Joueur
permissions:
  - cuboria.report
```

## Étape 7 : Démarrage

1. Démarrez le serveur
2. Vérifiez les logs pour confirmer que le plugin s'est chargé :
   ```
   [CuboriaModerationPlugin] ✓ Configuration Manager initialisé
   [CuboriaModerationPlugin] ✓ Database Manager initialisé
   [CuboriaModerationPlugin] ✓ Discord Webhook Manager initialisé
   [CuboriaModerationPlugin] ✓ Staff Mode Manager initialisé
   [CuboriaModerationPlugin] ✓ Freeze Manager initialisé
   [CuboriaModerationPlugin] ✓ Sanction Manager initialisé
   [CuboriaModerationPlugin] ✓ Report Manager initialisé
   [CuboriaModerationPlugin] ✓ GUI Manager initialisé
   [CuboriaModerationPlugin] Plugin de modération chargé avec succès!
   ```

## Test du plugin

1. Connectez-vous au serveur avec un compte ayant les permissions staff
2. Testez `/staffco` pour activer le mode staff
3. Testez `/sanction <joueur>` pour ouvrir le menu de sanction
4. Testez `/report <joueur> <raison>` pour créer un signalement
5. Vérifiez que les webhooks Discord fonctionnent

## Dépannage

### Le plugin ne se charge pas
- Vérifiez que vous utilisez Paper 1.21.4
- Vérifiez que Java 21 est installé
- Consultez les logs dans `logs/latest.log`

### Erreur de base de données
- Vérifiez que MariaDB/MySQL est démarré
- Vérifiez les identifiants dans config.yml
- Vérifiez que l'utilisateur a les bonnes permissions

### Les webhooks Discord ne fonctionnent pas
- Vérifiez que les URLs sont correctes
- Les webhooks sont optionnels, le plugin fonctionnera sans
- Vérifiez les permissions du webhook sur Discord

### Les commandes ne fonctionnent pas
- Vérifiez les permissions
- Tapez `/plugins` pour confirmer que le plugin est chargé
- Vérifiez `plugin.yml` pour les alias de commandes

## Support

Pour toute assistance, contactez EmyXtrm sur le Discord de Cuboria.
