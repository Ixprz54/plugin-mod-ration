#!/bin/bash

echo "=================================="
echo "  Cuboria Moderation Plugin"
echo "  Build Script"
echo "  Développeur: EmyXtrm"
echo "=================================="
echo ""

# Vérifier que Maven est installé
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven n'est pas installé!"
    exit 1
fi

echo "🔨 Compilation du plugin..."
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Compilation réussie!"
    echo ""
    echo "📦 Le fichier JAR se trouve dans:"
    echo "   target/CuboriaModerationPlugin-1.0.0.jar"
    echo ""
    echo "📋 Prochaines étapes:"
    echo "   1. Copiez le JAR dans le dossier plugins de votre serveur"
    echo "   2. Configurez config.yml (base de données + webhooks Discord)"
    echo "   3. Redémarrez le serveur"
    echo ""
else
    echo ""
    echo "❌ Erreur lors de la compilation"
    exit 1
fi
