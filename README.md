# 📱 Astreintyzer

**Astreintyzer** est une application Android moderne développée en **Jetpack Compose** et **Kotlin**, conçue pour simplifier la gestion et le suivi des périodes d'astreinte et des interventions téléphoniques pour les techniciens.

---

## 🚀 Fonctionnalités principales

- **Gestion des périodes d'astreinte** : Suivi des périodes d'astreinte (par défaut du lundi au lundi sur 7 jours) et affectation par technicien.
- **Détection automatique des appels** : Interception automatique des états d'appels téléphoniques pour détecter la fin des interventions.
- **Notifications intelligentes** : Envoi d'une notification (avec un son personnalisé) 30 secondes après la fin d'un appel pour vous demander si celui-ci a nécessité une intervention d'astreinte.
- **Journalisation des tâches** :
  - Saisie et modification de la nature de l'intervention.
  - Horodatage personnalisable (date et heure).
  - Gestion de la durée de la tâche (par défaut à 5 minutes pour une saisie rapide).
  - Indicateur de déplacement nécessaire et de résolution.
- **Export CSV et Partage par E-mail** : Clôture d'une période d'astreinte avec génération automatique d'un rapport au format CSV partagé par e-mail.
- **Persistance hors ligne (Room)** : Stockage local sécurisé et réactif de tous les utilisateurs, périodes et tâches.
- **Résilience au redémarrage (Boot & Android 15)** : Notification de rappel au démarrage du téléphone pour garantir l'activation de la surveillance.
- **Mode Debug / Simulation** : Options de simulation de notification (immédiate ou différée de 30 secondes) activables depuis le menu des réglages.

---

## 🛠️ Architecture technique

- **UI** : Jetpack Compose & Material 3 (Material You).
- **Architecture** : MVVM (Model-View-ViewModel) avec StateFlow et coroutines Kotlin.
- **Base de données** : Room Database (avec KSP).
- **Communication système** : BroadcastReceivers (`CallReceiver`, `BootReceiver`), AlarmManager et NotificationManager.

---

## 👤 Auteur

- **Thomas Giacomelli**
- 📧 E-mail : [giacomelli.thomas@gmail.com](mailto:giacomelli.thomas@gmail.com)
- 🐙 GitHub : [Thomas-Giacomelli/Astreintyzer](https://github.com/Thomas-Giacomelli/Astreintyzer)

---

## 📄 Licence

Ce projet est sous licence AGPL v3.0.
