# Plugin de Modération Cuboria - Guide Complet

**Développé par:** EmyXtrm
**Version:** 1.0.0
**Compatible avec:** Paper 1.21.4
**Base de données:** SQLite (intégrée)

---

## 📋 Table des Matières

1. [Introduction](#introduction)
2. [Fonctionnalités](#fonctionnalités)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Commandes](#commandes)
6. [Permissions](#permissions)
7. [Utilisation](#utilisation)
8. [Intégration Discord](#intégration-discord)
9. [Base de Données](#base-de-données)

---

## 🎯 Introduction

Le **Plugin de Modération Cuboria** est un plugin complet de modération pour serveurs Minecraft Paper 1.21.4. Il offre une interface graphique intuitive pour gérer les sanctions, un système de mode staff avancé avec des outils intégrés, et une intégration Discord pour le logging des actions.

---

## ✨ Fonctionnalités

### Mode Staff (`/staffco`)
- **Sauvegarde automatique** de l'inventaire dans SQLite
- **Vanish automatique** (invisible pour les joueurs non-staff)
- **God Mode automatique** (invulnérable)
- **Vol activé** pour une navigation facile
- **Inventaire d'outils** personnalisés :
  - 🔮 **Vanish** - Activer/Désactiver l'invisibilité
  - ❄️ **Freeze** - Immobiliser un joueur
  - 🌀 **Téléportation** - Se téléporter à un joueur
  - 📖 **Inspection** - Voir l'inventaire et les stats d'un joueur
  - 📋 **Rapports** - Consulter les rapports en attente

### Système de Sanctions (`/sanction <joueur>`)
- **Interface GUI** professionnelle et intuitive
- **4 types de sanctions** :
  - ⚠️ **Warn** - Avertissement
  - 🚪 **Kick** - Expulsion
  - 🔇 **Mute** - Mute temporaire/permanent
  - 🚫 **Ban** - Bannissement temporaire/permanent
- **Templates personnalisables** via `sanctions.yml`
- **Historique des sanctions** stocké en base de données

### Système de Convocation
- **Bouton spécial** dans l'interface `/sanction`
- **Freeze + Message** au joueur convoqué
- **Ban automatique** en cas de déconnexion pendant la convocation
- **Toggle Freeze/Unfreeze** via le même bouton

### Système de Freeze Avancé
- **Invulnérabilité** totale du joueur freeze
- **Impossibilité d'attaquer** ou d'interagir
- **Blocage des mouvements** et téléportations
- **Commandes limitées** (seuls /msg, /r, /reply autorisés)

### Système de Rapports (`/report <joueur> <raison>`)
- **Cooldown anti-spam** (1 minute)
- **Notification en temps réel** au staff
- **GUI de consultation** des rapports
- **Marquage des rapports** comme traités

### Intégration Discord
- **Webhooks** pour logs et changelog
- **Embeds colorés** selon le type d'action
- **Logs automatiques** pour :
  - Activation/Désactivation du mode staff
  - Sanctions appliquées
  - Convocations et fuites
  - Rapports de joueurs

---

## 📦 Installation

1. **Télécharger** le fichier `CuboriaModerationPlugin-1.0.0.jar`
2. **Placer** le JAR dans le dossier `plugins/` de votre serveur Paper 1.21.4
3. **Démarrer** le serveur
4. **Configurer** les fichiers générés dans `plugins/CuboriaModerationPlugin/`

Les fichiers suivants seront créés automatiquement :
```
plugins/CuboriaModerationPlugin/
├── config.yml           # Configuration principale
├── sanctions.yml        # Templates de sanctions
└── cuboria_moderation.db # Base de données SQLite
```

---

## ⚙️ Configuration

### config.yml

```yaml
# Configuration de la base de données (SQLite)
database:
  enabled: true
  # La base de données SQLite sera créée automatiquement

# Configuration Discord
discord:
  # Webhook pour les logs d'actions staff
  webhook-logs: "https://discord.com/api/webhooks/VOTRE_ID/VOTRE_TOKEN"
  # Webhook pour le changelog
  webhook-changelog: "https://discord.com/api/webhooks/VOTRE_ID/VOTRE_TOKEN"
  # Message de convocation
  summon-message: |
    &c&l[!] CONVOCATION STAFF [!]
    &eVous êtes convoqué par un membre du staff.
    &c&lATTENTION: Si vous vous déconnectez, ban permanent!

# Configuration du mode Staff
staff-mode:
  auto-vanish: true
  auto-god-mode: true
  save-inventory: true

# Messages personnalisables
messages:
  prefix: "&8[&6Cuboria&8] &r"
  staff-mode-enabled: "&aMode staff activé! Inventaire sauvegardé."
  staff-mode-disabled: "&cMode staff désactivé! Inventaire restauré."
  player-frozen: "&e{player} &aa été freeze."
  you-are-frozen: "&c&lVous êtes freeze! Impossible de bouger."
  # ... (autres messages)

# Configuration des items Staff (personnalisables)
staff-items:
  vanish:
    slot: 0
    material: ENDER_EYE
    name: "&6&lVanish"
  freeze:
    slot: 1
    material: PACKED_ICE
    name: "&b&lFreeze"
  # ... (autres items)
```

### sanctions.yml

Ce fichier contient tous les **templates de sanctions** :

```yaml
sanctions:
  warn:
    name: "&e&lAvertissement"
    icon: PAPER
    templates:
      - reason: "Langage inapproprié"
        duration: 0
        message: "&cVous avez été averti pour langage inapproprié."
      # ... (autres templates)

  mute:
    name: "&6&lMute"
    icon: IRON_BARS
    templates:
      - reason: "Spam dans le chat"
        duration: 3600  # en secondes (1 heure)
        duration-text: "1 heure"
        message: "&cVous avez été mute pendant 1 heure pour spam."
      # ... (autres templates)

  # ... (kick, ban)
```

**Pour ajouter un nouveau template**, copiez simplement une entrée existante et modifiez les valeurs.

---

## 🎮 Commandes

| Commande | Description | Permission |
|----------|-------------|------------|
| `/staffco` ou `/staff` | Active le mode staff | `cuboria.staff` |
| `/staffdeco` | Désactive le mode staff | `cuboria.staff` |
| `/sanction <joueur>` | Ouvre l'interface de sanctions | `cuboria.staff.sanction` |
| `/report <joueur> <raison>` | Signale un joueur au staff | `cuboria.report` |

---

## 🔐 Permissions

```yaml
cuboria.staff:
  description: Accès aux commandes staff
  default: op

cuboria.staff.sanction:
  description: Accès au système de sanctions
  default: op

cuboria.staff.freeze:
  description: Permet de freeze un joueur
  default: op

cuboria.staff.vanish:
  description: Permet de se rendre invisible
  default: op

cuboria.report:
  description: Permet de signaler un joueur
  default: true  # Tous les joueurs peuvent reporter
```

---

## 📖 Utilisation

### Activation du Mode Staff

1. Exécutez `/staffco` ou `/staff`
2. Votre inventaire actuel est **sauvegardé automatiquement** dans la base de données
3. Vous recevez les **outils staff** dans votre inventaire
4. Vous devenez **invisible** (Vanish) et **invulnérable** (God Mode)
5. Le **vol est activé** pour faciliter vos déplacements

### Outils Staff

#### 🔮 Vanish (Slot 0)
- **Clic gauche/droit** : Toggle Vanish ON/OFF

#### ❄️ Freeze (Slot 1)
- **Clic droit sur un joueur** : Freeze/Unfreeze le joueur
- Le joueur freeze devient invulnérable et ne peut pas bouger ni interagir

#### 🌀 Téléportation (Slot 2)
- **Clic droit sur un joueur** : Se téléporter au joueur

#### 📖 Inspection (Slot 3)
- **Clic droit sur un joueur** : Voir son inventaire, armure et statistiques

#### 📋 Rapports (Slot 4)
- **Clic** : Ouvrir la liste des rapports en attente
- **Clic gauche sur un rapport** : Ouvrir le menu de sanction pour ce joueur
- **Shift+Clic sur un rapport** : Marquer le rapport comme traité

### Application de Sanctions

1. Exécutez `/sanction <joueur>`
2. **Cliquez sur un type de sanction** :
   - ⚠️ Warn
   - 🚪 Kick
   - 🔇 Mute
   - 🚫 Ban
3. **Sélectionnez une raison** dans la liste des templates
4. La sanction est **appliquée automatiquement** et **enregistrée** en base de données
5. Un **log Discord** est envoyé (si configuré)

### Système de Convocation

1. Ouvrez `/sanction <joueur>`
2. **Cliquez sur le bouton "Convocation"** (slot 26, en bas à droite)
3. Le joueur est **freeze** et reçoit le **message de convocation**
4. Si le joueur se **déconnecte**, il est **banni automatiquement**
5. **Cliquez à nouveau** sur le bouton pour **unfreeze** le joueur

### Signalement de Joueurs

Les joueurs peuvent signaler d'autres joueurs :

```
/report <joueur> <raison>
```

Le staff reçoit une **notification en temps réel** et peut consulter les rapports via l'item "Rapports" en mode staff.

---

## 🔗 Intégration Discord

### Configuration des Webhooks

1. Dans Discord, allez dans **Paramètres du Salon** → **Intégrations** → **Webhooks**
2. Créez deux webhooks :
   - Un pour les **logs d'actions staff**
   - Un pour le **changelog** (facultatif)
3. Copiez l'URL du webhook et collez-la dans `config.yml`

### Types de Logs

Le plugin envoie automatiquement des embeds Discord pour :

- ✅ **Mode Staff Activé** (vert)
- ❌ **Mode Staff Désactivé** (rouge)
- ⚠️ **Sanctions Appliquées** (couleur selon le type)
- 🔔 **Convocations** (orange)
- 🚨 **Fuites de Convocation** (rouge)
- 📢 **Nouveaux Rapports** (orange)

---

## 💾 Base de Données

### Structure SQLite

Le plugin utilise **SQLite** pour stocker toutes les données localement dans le fichier `cuboria_moderation.db`.

#### Tables

**cuboria_sanctions**
- Stocke l'historique complet de toutes les sanctions
- Permet de vérifier les bans/mutes actifs
- Utilisé pour afficher l'historique d'un joueur

**cuboria_reports**
- Stocke tous les rapports de joueurs
- Permet de filtrer les rapports traités/non traités

**cuboria_staff_inventories**
- Sauvegarde temporaire des inventaires en mode staff
- Supprimé automatiquement lors du `/staffdeco`

### Avantages de SQLite

✅ **Aucune configuration requise** - Fonctionne immédiatement
✅ **Pas de serveur externe** - Base de données locale
✅ **Léger et performant** - Parfait pour un serveur Minecraft
✅ **Sauvegarde facile** - Copiez simplement le fichier `.db`

---

## 🛠️ Troubleshooting

### Le plugin ne se charge pas
- Vérifiez que vous utilisez **Paper 1.21.4** (pas Spigot, pas Bukkit)
- Consultez les logs dans `logs/latest.log`

### Les sanctions ne s'appliquent pas
- Vérifiez les permissions du staff
- Consultez la console pour les erreurs

### Les webhooks Discord ne fonctionnent pas
- Vérifiez que les URLs sont correctes
- Testez le webhook avec un outil comme Discord Webhook Tester

### L'inventaire n'est pas restauré
- Cela peut arriver en cas de crash serveur
- La base de données conserve une sauvegarde, utilisez `/staffdeco`

---

## 📝 Notes Importantes

⚠️ **Le système de convocation** ban automatiquement les joueurs qui se déconnectent pendant une convocation. Utilisez cette fonction avec précaution.

⚠️ **Les joueurs freeze sont invulnérables** et ne peuvent ni attaquer ni être attaqués. Parfait pour les interrogations staff.

⚠️ **Les templates de sanctions** peuvent être modifiés à chaud dans `sanctions.yml`. Utilisez `/reload confirm` pour recharger (non recommandé en production).

---

## 📞 Support

Pour toute question ou problème, contactez **EmyXtrm** sur le serveur Cuboria.

---

**Plugin développé avec ❤️ pour Cuboria**
