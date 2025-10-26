# 🛡️ Plugin de Modération Cuboria

> Plugin complet de modération pour serveurs Paper 1.21.4 avec interface graphique et intégration Discord

[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![SQLite](https://img.shields.io/badge/SQLite-Intégré-green.svg)](https://www.sqlite.org/)

**Développé par:** EmyXtrm pour Cuboria

---

## ✨ Fonctionnalités Principales

### 🎭 Mode Staff (`/staffco`)
- ✅ Sauvegarde automatique de l'inventaire (SQLite)
- 👻 Vanish automatique (invisible aux joueurs)
- 🛡️ God Mode (invulnérable)
- ✈️ Vol activé
- 🧰 Inventaire d'outils staff personnalisés

### ⚖️ Système de Sanctions (`/sanction`)
- 📋 Interface GUI professionnelle
- 🔧 4 types : Warn, Kick, Mute, Ban
- 📝 Templates personnalisables (`sanctions.yml`)
- 💾 Historique complet en base de données

### 🔔 Système de Convocation
- 🚨 Freeze + Message de convocation
- ⛔ Ban automatique en cas de déconnexion
- 🔄 Toggle Freeze/Unfreeze intégré

### ❄️ Système de Freeze Avancé
- 🧊 Immobilisation totale du joueur
- 🛡️ Invulnérabilité complète
- 🚫 Impossibilité d'attaquer ou interagir
- 💬 Commandes limitées (/msg, /r, /reply)

### 📢 Rapports (`/report`)
- ⏱️ Cooldown anti-spam (1 minute)
- 🔔 Notification temps réel au staff
- 📊 GUI de consultation des rapports
- ✅ Marquage des rapports traités

### 🤖 Intégration Discord
- 📨 Webhooks pour logs et changelog
- 🎨 Embeds colorés selon l'action
- 📝 Logs automatiques complets

---

## 📦 Installation Rapide

1. **Compilez le plugin :**
   ```bash
   mvn clean package
   ```

2. **Récupérez** `target/CuboriaModerationPlugin-1.0.0.jar`

3. **Placez** dans `plugins/` de votre serveur Paper 1.21.4

4. **Démarrez** le serveur

5. **Configurez** `plugins/CuboriaModerationPlugin/config.yml`

**Base de données SQLite créée automatiquement** - Aucune configuration externe requise ! 🎉

---

## ⚙️ Configuration Minimale

### config.yml
```yaml
# Configuration de la base de données (SQLite)
database:
  enabled: true  # SQLite - Aucune config requise !
  # La base de données sera créée automatiquement :
  # plugins/CuboriaModerationPlugin/cuboria_moderation.db

# Configuration Discord
discord:
  webhook-logs: "https://discord.com/api/webhooks/VOTRE_WEBHOOK"
  webhook-changelog: "https://discord.com/api/webhooks/VOTRE_WEBHOOK"

# Configuration du mode Staff
staff-mode:
  auto-vanish: true
  auto-god-mode: true
  save-inventory: true
```

### sanctions.yml - Ajoutez vos propres templates !
```yaml
sanctions:
  ban:
    name: "&4&lBan"
    icon: BARRIER
    templates:
      - reason: "Triche/Cheat"
        duration: -1  # -1 = permanent
        duration-text: "Permanent"
        message: "&4Banni définitivement pour triche."
      - reason: "Comportement grave"
        duration: 2592000  # 30 jours en secondes
        duration-text: "30 jours"
        message: "&cBanni 30 jours pour comportement grave."
```

---

## 🎮 Commandes

| Commande | Description | Permission |
|----------|-------------|------------|
| `/staffco` `/staff` | Activer le mode staff | `cuboria.staff` |
| `/staffdeco` | Désactiver le mode staff | `cuboria.staff` |
| `/sanction <joueur>` | Ouvrir l'interface de sanctions | `cuboria.staff.sanction` |
| `/report <joueur> <raison>` | Signaler un joueur | `cuboria.report` |

---

## 🔐 Permissions

```yaml
cuboria.staff             # Accès mode staff (default: op)
cuboria.staff.sanction    # Système de sanctions (default: op)
cuboria.staff.freeze      # Freeze des joueurs (default: op)
cuboria.staff.vanish      # Mode vanish (default: op)
cuboria.report            # Signaler un joueur (default: true)
```

---

## 🛠️ Compilation

```bash
mvn clean package
```

Le JAR compilé sera dans `target/CuboriaModerationPlugin-1.0.0.jar`

**Prérequis :**
- Maven 3.x
- Java 21 (JDK)

---

## 📖 Documentation Complète

Consultez [PLUGIN_GUIDE.md](PLUGIN_GUIDE.md) pour :
- 📘 Guide d'utilisation détaillé
- 🔧 Configuration avancée
- 🎨 Personnalisation des items staff
- 🗃️ Structure de la base de données SQLite
- 🐛 Troubleshooting

---

## 🎯 Outils Staff (Mode Staff)

| Slot | Outil | Action |
|------|-------|--------|
| 0 | 🔮 Vanish | Clic : Toggle invisibilité |
| 1 | ❄️ Freeze | Clic droit joueur : Freeze/Unfreeze |
| 2 | 🌀 Téléportation | Clic droit joueur : Se téléporter |
| 3 | 📖 Inspection | Clic droit joueur : Voir inventaire + stats |
| 4 | 📋 Rapports | Clic : Voir rapports en attente |

---

## 💡 Points Clés

✅ **SQLite intégré** - Pas de serveur MariaDB/MySQL requis !
✅ **GUI intuitive** - Interface graphique pour toutes les actions
✅ **Ban automatique** - Fuite de convocation = ban permanent
✅ **Freeze invulnérable** - Joueurs freeze complètement protégés
✅ **Discord logs** - Toutes les actions loggées automatiquement
✅ **Templates flexibles** - Ajoutez vos propres raisons de sanction

---

## 🗃️ Base de Données SQLite

### Avantages
- ✅ **Aucune configuration requise** - Fonctionne immédiatement
- ✅ **Pas de serveur externe** - Base locale dans le dossier du plugin
- ✅ **Léger et performant** - Parfait pour Minecraft
- ✅ **Sauvegarde facile** - Copiez simplement le fichier `.db`

### Tables créées automatiquement
- **cuboria_sanctions** - Historique complet des sanctions
- **cuboria_reports** - Signalements des joueurs
- **cuboria_staff_inventories** - Sauvegarde inventaires staff

### Localisation
```
plugins/CuboriaModerationPlugin/cuboria_moderation.db
```

---

## 🏗️ Architecture Technique

### Structure du code
```
fr.cuboria.moderation/
├── commands/           # Commandes (/staffco, /sanction, /report)
├── database/          # Gestionnaire SQLite + Serialization
├── gui/               # Interfaces graphiques (Sanction, Reports, Inspect)
├── listeners/         # Événements (Freeze, Staff Items, GUI clicks)
├── managers/          # Gestionnaires (Staff, Freeze, Sanctions, Discord)
├── models/            # Modèles de données (Sanction, Report)
└── utils/             # Utilitaires (StaffItemBuilder)
```

---

## 🆘 Support & Contribution

- 🐛 **Issues:** [GitHub Issues](https://github.com/EmyXtrm/plugin-mod-ration/issues)
- 💬 **Support:** Contactez EmyXtrm sur Discord
- 🌟 **Stars:** Si le plugin vous plaît, laissez une étoile !

---

## 📋 Prérequis

- **Serveur:** Paper 1.21.4 (Spigot/Bukkit non supportés)
- **Java:** Version 21
- **Espace disque:** ~5 Mo pour le plugin + base SQLite

---

## 🔍 Fonctionnalités Détaillées

### Système de Freeze
Quand un joueur est freeze :
- ❌ **Impossible de bouger** (téléporté si tentative)
- ❌ **Impossible d'interagir** avec des blocs/items
- ❌ **Impossible d'attaquer** d'autres joueurs
- ✅ **Invulnérable** à tous les dégâts
- ✅ **Peut parler** (/msg, /r, /reply autorisés)

### Système de Convocation
1. Staff ouvre `/sanction <joueur>`
2. Clic sur le bouton **Convocation** (slot 26)
3. Joueur freeze + reçoit le message de convocation
4. Si le joueur se déconnecte = **Ban permanent automatique**
5. Staff peut **unfreeze** en recliquant le bouton

### Système de Rapports
- Les joueurs peuvent `/report <joueur> <raison>`
- **Cooldown de 60 secondes** par joueur
- Staff reçoit une **notification instantanée**
- Staff peut voir les rapports via l'item **📋 Rapports**
- Clic sur un rapport = ouvre `/sanction` pour ce joueur
- Shift+Clic = marquer comme traité

---

## 📄 Licence

Ce plugin est développé pour le serveur Cuboria.
Tous droits réservés © 2025 EmyXtrm

---

## 🙏 Remerciements

Merci à la communauté Paper/Bukkit et au serveur Cuboria pour le support !

---

**🎉 Profitez d'un système de modération professionnel et facile à utiliser !**
