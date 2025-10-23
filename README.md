# Cuboria Moderation Plugin

Plugin de modération complet pour serveur Paper 1.21.4 développé pour Cuboria.

**Développeur:** EmyXtrm

## Fonctionnalités

### Mode Staff (`/staffco`)
- Activation du mode staff avec sauvegarde automatique de l'inventaire
- Items spéciaux pour la modération
- Mode vanish automatique
- God mode activé
- Restauration de l'inventaire avec `/staffdeco`

### Outils Staff
- **Vanish** : Toggle mode invisible
- **Freeze** : Immobilise un joueur (invulnérable + impossible d'attaquer)
- **Téléportation** : Se téléporter à un joueur
- **Inspection** : Voir l'inventaire et les stats d'un joueur
- **Rapports** : Consulter les signalements en attente

### Système de Sanctions (`/sanction <joueur>`)
- Interface GUI intuitive
- 4 types de sanctions :
  - **Warn** : Avertissement
  - **Kick** : Expulsion
  - **Mute** : Réduire au silence
  - **Ban** : Bannissement (temporaire ou permanent)
- Templates de raisons personnalisables
- Historique des sanctions en base de données
- Notifications Discord via webhooks

### Convocation Discord
- Bouton spécial dans le menu de sanction
- Freeze le joueur avec message de convocation
- Ban automatique permanent si déconnexion pendant la convocation
- Toggle freeze/unfreeze

### Système de Rapports (`/report <joueur> <raison>`)
- Permet aux joueurs de signaler d'autres joueurs
- Cooldown de 60 secondes entre chaque report
- Notifications au staff
- Interface pour consulter et traiter les rapports
- Logs Discord

### Intégration Discord
- Webhook pour les logs d'actions staff
- Webhook pour le changelog
- Embeds colorés selon le type d'action
- Informations détaillées sur chaque sanction

## Installation

1. Compilez le plugin avec Maven :
   ```bash
   mvn clean package
   ```

2. Le fichier JAR sera généré dans `target/CuboriaModerationPlugin-1.0.0.jar`

3. Placez le JAR dans le dossier `plugins` de votre serveur Paper 1.21.4

4. Configurez la base de données dans `config.yml`

5. Configurez les webhooks Discord dans `config.yml`

6. Redémarrez le serveur

## Configuration

### Database (config.yml)
```yaml
database:
  enabled: true
  host: "localhost"
  port: 3306
  database: "cuboria"
  username: "root"
  password: "password"
  pool-size: 10
```

### Discord Webhooks (config.yml)
```yaml
discord:
  webhook-logs: "https://discord.com/api/webhooks/..."
  webhook-changelog: "https://discord.com/api/webhooks/..."
```

### Templates de Sanctions (sanctions.yml)
Les templates sont entièrement personnalisables. Vous pouvez ajouter, modifier ou supprimer des raisons de sanction.

Exemple pour un warn :
```yaml
sanctions:
  warn:
    templates:
      - reason: "Votre raison"
        duration: 0
        message: "&cMessage affiché au joueur"
```

## Permissions

- `cuboria.staff` - Accès au mode staff
- `cuboria.staff.sanction` - Accès aux sanctions
- `cuboria.staff.freeze` - Permet de freeze un joueur
- `cuboria.staff.vanish` - Permet de se rendre invisible
- `cuboria.report` - Permet de signaler un joueur (accordée par défaut)

## Commandes

| Commande | Description | Permission |
|----------|-------------|------------|
| `/staffco` ou `/staff` | Active le mode staff | `cuboria.staff` |
| `/staffdeco` | Désactive le mode staff | `cuboria.staff` |
| `/sanction <joueur>` | Ouvre le menu de sanction | `cuboria.staff.sanction` |
| `/report <joueur> <raison>` | Signale un joueur | `cuboria.report` |

## Architecture Technique

### Base de données
Le plugin utilise MariaDB avec HikariCP pour le pooling de connexions.

**Tables créées automatiquement :**
- `cuboria_sanctions` - Historique des sanctions
- `cuboria_reports` - Signalements des joueurs
- `cuboria_staff_inventories` - Inventaires sauvegardés du staff

### Structure du code
```
fr.cuboria.moderation/
├── commands/           # Commandes du plugin
├── database/          # Gestion de la base de données
├── gui/               # Interfaces utilisateur
├── listeners/         # Événements Bukkit
├── managers/          # Gestionnaires de fonctionnalités
├── models/            # Modèles de données
└── utils/             # Utilitaires
```

## Fonctionnalités avancées

### Freeze System
- Le joueur freeze ne peut ni bouger ni interagir
- Le joueur freeze est invulnérable
- Le joueur freeze ne peut pas attaquer
- Seules les commandes de message sont autorisées

### Summon System
- Envoie un message de convocation au joueur
- Freeze automatique
- Détection de déconnexion
- Ban permanent automatique si fuite
- Message pour rejoindre Discord

### Logging
- Toutes les actions staff sont loggées en base de données
- Notifications Discord pour chaque action importante
- Historique complet des sanctions par joueur

## Support

Pour tout problème ou suggestion, contactez EmyXtrm.

## Licence

Plugin développé spécifiquement pour le serveur Cuboria.
