# የ«ሻህ» (Shah) 100% Native Kotlin መተግበሪያ — በGitHub አዲስ Organization በመፍጠር፣ Secret Keys በመሙላት፣ Repository በመፍጠርና በYAML ኮድ ወደ `.aab` እና `.apk` የማምረቻ ሙሉ መመሪያ ማንዋል

ይህ መመሪያ የተዘጋጀውን **`Shah_Native_Kotlin_Android_Project.zip`** ፋይል በመጠቀም በ**GitHub** ላይ አዲስ **Organization** ከመፍጠር ጀምሮ፣ **Repository** በመፍጠር፣ **Secret Keys** በመሙላት፣ ፋይሎቹን **Upload** በማድረግ እና የ**YAML (`.github/workflows/android-release.yml`)** ኮዱን በማስኬድ የተፈረመ (Signed) **`.aab` (ለGoogle Play Console)** እና **`.apk` (በስልክዎ ለመጫንና ለመሞከር)** እንዴት በ100% ጥራት እንደሚያመርቱ በቅደም ተከተል ያብራራል።

---

## ክፍል 1፦ የመተግበሪያው መሰረታዊ መለያዎች (App & Signing Variables)

* **App Name**፦ `ሻህ`
* **Package Name (Application ID)**፦ `eth.culturalsports.shah`
* **Version Code / Version Name**፦ `Version Code: 1` | `Version Name: "1.0.0"`
* **Keystore Signing Variables (በGitHub Secrets የሚሞሉ)**፦
  * `KEY_ALIAS` = `ethioculturalsports`
  * `KEY_PASSWORD` = `ethioculturalsports`
  * `STORE_PASSWORD` = `ethioculturalsports`
* **ኦፊሴላዊ ማህበራዊ ሚዲያና አድራሻ**፦
  * **Email**፦ `eth.culturalsportsfed.1977@gmail.com`
  * **Telegram**፦ `@EthiopianculturalsportFDE` (`https://t.me/EthiopianculturalsportFDE`)
  * **Facebook**፦ `@ECSF` (`https://www.facebook.com/profile.php?id=61558619311072`)
  * **አድራሻ**፦ `📍 አዲስ አበባ ስታዲየም ከቤተዛታ ሆስፒታል ጎን OBS ህንፃ 4ኛ ፎቅ ቢሮ ቁጥር 406`

---

## ክፍል 2፦ በGitHub አዲስ Organization መፍጠር (Step-by-Step)

የፌዴሬሽኑን ወይም የተቋሙን ፕሮጀክቶች በይፋዊ ተቋማዊ ስም ለማስተዳደር በቅድሚያ በGitHub ላይ **Organization** በሚከተለው ቅደም ተከተል ይፍጠሩ፦

1. ወደ [GitHub.com](https://github.com) በመግባት በራስጌው ቀኝ ጥግ ላይ ያለውን የ**`+` (Plus)** ምልክት ይጫኑና **"New organization"** የሚለውን ይምረጡ።
2. በሚመጣው የዕቅድ መምረጫ ገጽ ላይ **"Create a free organization"** (`$0 / month forever`) የሚለውን ቁልፍ ይጫኑ።
3. የOrganization ዝርዝር መረጃዎችን ይሙሉ፦
   * **Organization account name**፦ ለምሳሌ `ethiopian-cultural-sports` ወይም `eth-cultural-sports-fed` ብለው ይጻፉ።
   * **Contact email**፦ `eth.culturalsportsfed.1977@gmail.com` ያስገቡ።
   * **This organization belongs to**፦ **"My personal account"** ወይም **"A business or institution"** የሚለውን ይምረጡና የተቋሙን ስም (`Ethiopian Cultural Sports Federation`) ያስገቡ።
4. የማረጋገጫ ሳጥኑን (Verify) አልፈው **"Next"** ➔ **"Complete setup"** የሚለውን ይጫኑ።
5. (አማራጭ) በOrganization Profile Settings ውስጥ የፌዴሬሽኑን ሎጎ (`playstore_assets/federation_logo_1024x1024.png`) እንደ Profile Picture መስቀል ይችላሉ።

---

## ክፍል 3፦ በፈጠሩት Organization ስር አዲስ Repository መፍጠር

1. በፈጠሩት የGitHub Organization ዋና ገጽ ላይ **"Repositories"** ➔ **"New repository"** የሚለውን አረንጓዴ ቁልፍ ይጫኑ።
2. የሚከተሉትን መረጃዎች ይሙሉ፦
   * **Owner**፦ የፈጠሩት የOrganization ስም መመረጡን ያረጋግጡ።
   * **Repository name**፦ `shah-sport-game` (ወይም `shah`) ብለው ይጻፉ።
   * **Description**፦ `የሻህ ባህላዊ ስፖርት ጨዋታ (Shah Cultural Sport Game) - 100% Native Kotlin Android App`
   * **Visibility**፦ **`Public`** ይምረጡ (የGitHub Actions ነፃ የግንባታ ደቂቃዎችን ያለ ገደብ ለመጠቀምና የ`privacy-policy.html` ገጹን በGitHub Pages በነፃ ለማስተናገድ `Public` መሆኑ እጅግ ጠቃሚ ነው፤ የKeystore ሚስጥሮች በSecrets ውስጥ ስለሚቀመጡ በፍጹም ለሰው አይታዩም!)
3. **"Add a README file"** የሚለውን ሳይመርጡ (በZIP ፋይሉ ውስጥ የተሟላ `README.md` ስላለ) በቀጥታ **"Create repository"** የሚለውን ይጫኑ።

---

## ክፍል 4፦ Secret Keys በGitHub ውስጥ መሙላት (100% ደህንነቱ የተጠበቀ አፈራረም)

የKeystore ፓስዎርዶች በኮዱ ውስጥ በቀጥታ (Hardcode) ስላልተጻፉ፣ የYAML ፋይሉ (`.github/workflows/android-release.yml`) አፑን ሲገነባና ሲፈርም (Sign ሲያደርግ) ከGitHub Secrets ውስጥ ያነባቸዋል።

1. በፈጠሩት አዲስ Repository ውስጥ ከላይኛው ማውጫ ላይ ወደ **Settings** ይሂዱ።
2. በግራ በኩል ባለው ዝርዝር ውስጥ **Secrets and variables** ➔ **Actions** የሚለውን ይክፈቱ።
3. **"New repository secret"** የሚለውን አረንጓዴ ቁልፍ እየተጫኑ የሚከተሉትን **3 Secret Keys** አንድ በአንድ ያስገቡ፦

| ተ.ቁ | Name (የSecret ስም) | Secret (የሚሞላው ዋጋ) | ተግባር |
| :---: | :--- | :--- | :--- |
| **1** | `KEY_ALIAS` | `ethioculturalsports` | የKeystore Alias ስም |
| **2** | `KEY_PASSWORD` | `ethioculturalsports` | የKey ሚስጥር ቃል |
| **3** | `STORE_PASSWORD` | `ethioculturalsports` | የKeystore ሚስጥር ቃል |

> **እንዴት ይሰራል?**
> እነዚህን 3 Secrets ካስገቡ በኋላ፣ የYAML ኮዱ ለመጀመሪያ ጊዜ ሲشتغل በ`keytool` አማካኝነት ኦፊሴላዊ `release.keystore` (PKCS12, 2048-bit RSA, 10,000 days validity) በራሱ ፈጥሮ `.aab` እና `.apk` ፋይሎቹን ይፈርማል! በተጨማሪም የተፈጠረውን `release.keystore` በArtifacts ውስጥ ስለሚሰጥዎት ለወደፊት አፕዴቶች አውርደው ማስቀመጥ ይችላሉ።

---

## ክፍል 5፦ የZIP ፋይሉን ወደ Repository Upload ማድረግ

የተሰጠዎትን **`Shah_Native_Kotlin_Android_Project.zip`** በስልክዎ ወይም በኮምፒውተርዎ ከፈቱ (Unzip ካደረጉ) በኋላ በሁለት መንገድ ወደ Repositoryዎ መጫን ይችላሉ፦

### አማራጭ ሀ፦ በGit ትዕዛዝ (በኮምፒውተር ወይም በTermux/Acode ተርሚናል) — *በጣም የሚመከር*
የተፈታው ፎልደር ውስጥ ገብተው የሚከተሉትን ትዕዛዞች ያስኪዱ፦
```bash
git init
git add .
git commit -m "Initial Release: 100% Native Kotlin Shah Cultural Sport Game v1.0.0"
git branch -M main
git remote add origin https://github.com/<YOUR_ORG_NAME>/shah-sport-game.git
git push -u origin main
```

### አማራጭ ለ፦ በቀጥታ በGitHub Web Browser (`Upload files`)
1. በRepositoryዎ ገጽ ላይ **"uploading an existing file"** የሚለውን ይጫኑ።
2. የተፈታውን የZIP ፋይል ይዘቶች (`app`, `gradle`, `.github`, `docs`, `playstore_assets`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, ወዘተ) ጎትተው ያስገቡ (Drag & Drop ወይም Choose your files)።
3. ከታች **"Commit changes"** የሚለውን ይጫኑ።

---

## ክፍል 6፦ በYAML ኮዱ (`.github/workflows/android-release.yml`) ወደ `.aab` እና `.apk` ማምረት

በፕሮጀክቱ ውስጥ `.github/workflows/android-release.yml` የሚል የተሟላ የGitHub Actions YAML ፋይል አብሮ ተካቷል። ፋይሎቹን እንደጫኑ ወዲያውኑ በራሱ መስራት ይጀምራል (ወይም በእጅ ማስጀመር ይችላሉ)፦

1. በRepositoryዎ የላይኛው ማውጫ ላይ **"Actions"** የሚለውን ትር (Tab) ይጫኑ።
2. በግራ በኩል **"Build & Sign Shah Android Release (.aab & .apk)"** የሚለውን ይምረጡ።
3. በቀኝ በኩል **"Run workflow"** ➔ **"Run workflow"** የሚለውን አረንጓዴ ቁልፍ ይጫኑ።
4. የግንባታው ሂደት (Build & Sign) በ**3 እስከ 5 ደቂቃ** ውስጥ ተጠናቆ አረንጓዴ **✅ (Success)** ምልክት ያሳያል።
5. የተጠናቀቀውን Run ሲከፍቱት ከታች በ**Artifacts** ክፍል ውስጥ የሚከተሉትን **3 ዝግጁ ጥቅሎች** ያገኛሉ፦

| የArtifact ስም | በውስጡ የያዘው ፋይል | አገልግሎት |
| :--- | :--- | :--- |
| 📦 **`Shah-GooglePlay-Release-AAB`** | **`app-release.aab`** | በቀጥታ ወደ **Google Play Console** የሚጫን የተፈረመ (Signed) Android App Bundle |
| 📱 **`Shah-Phone-Install-Release-APK`** | **`app-release.apk`** | በቀጥታ **በስልክዎ ላይ ጭነው የሚሞክሩት** የተፈረመ (Signed) APK |
| 🎨 **`Shah-Keystore-And-PlayStore-Assets`** | `release.keystore` + `512x512` Icon + `1024x500` Graphic | ለPlay Console የሚሆኑ ምስሎችና ለወደፊት አፕዴት የሚቀመጥ የKeystore ፋይል |

---

## ክፍል 7፦ የYAML ኮዱ ሙሉ ይዘት (`.github/workflows/android-release.yml`)

በZIP ፋይሉ ውስጥ በ`.github/workflows/android-release.yml` የተቀመጠውና `.aab` እና `.apk` የሚያመርተው ሙሉ የYAML ኮድ የሚከተለው ነው፦

```yaml
name: Build & Sign Shah Android Release (.aab & .apk)

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build-and-sign:
    runs-on: ubuntu-latest

    env:
      KEYSTORE_FILE: release.keystore
      KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
      KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      STORE_PASSWORD: ${{ secrets.STORE_PASSWORD }}
      ADMOB_APP_ID: ${{ secrets.ADMOB_APP_ID }}

    steps:
      - name: 1. Checkout Repository
        uses: actions/checkout@v4

      - name: 2. Set up JDK 17 (Temurin)
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: 3. Set up Android SDK
        uses: android-actions/setup-android@v3

      - name: 4. Setup Gradle 8.9
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.9'

      - name: 5. Prepare Signing Keystore from GitHub Secrets (or Generate Upload Keystore)
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: |
          if [ -z "$KEY_ALIAS" ]; then
            echo "::error::GitHub Secret KEY_ALIAS is not set. Please configure KEY_ALIAS, KEY_PASSWORD, and STORE_PASSWORD in Repository Settings -> Secrets and variables -> Actions."
            exit 1
          fi
          if [ -n "$KEYSTORE_BASE64" ]; then
            echo "Decoding existing release.keystore from KEYSTORE_BASE64 secret..."
            echo "$KEYSTORE_BASE64" | base64 --decode > release.keystore
          else
            echo "Generating production PKCS12 release.keystore using configured GitHub Secrets..."
            keytool -genkeypair -v \
              -keystore release.keystore \
              -storetype PKCS12 \
              -alias "$KEY_ALIAS" \
              -keyalg RSA \
              -keysize 2048 \
              -validity 10000 \
              -storepass "$STORE_PASSWORD" \
              -keypass "$KEY_PASSWORD" \
              -dname "CN=Ethiopian Cultural Sports Federation, OU=Cultural Sports, O=ECSF, L=Addis Ababa, ST=Addis Ababa, C=ET"
          fi

      - name: 6. Build Signed Release Android App Bundle (.aab) for Google Play Console
        run: gradle :app:bundleRelease --no-daemon --stacktrace

      - name: 7. Build Signed Release APK (.apk) for Phone Testing
        run: gradle :app:assembleRelease --no-daemon --stacktrace

      - name: 8. Upload Signed .aab (Google Play Store) Artifact
        uses: actions/upload-artifact@v4
        with:
          name: Shah-GooglePlay-Release-AAB
          path: app/build/outputs/bundle/release/*.aab
          retention-days: 90

      - name: 9. Upload Signed .apk (Phone Installation & Testing) Artifact
        uses: actions/upload-artifact@v4
        with:
          name: Shah-Phone-Install-Release-APK
          path: app/build/outputs/apk/release/*.apk
          retention-days: 90

      - name: 10. Upload Keystore & Play Store Graphics Backup
        uses: actions/upload-artifact@v4
        with:
          name: Shah-Keystore-And-PlayStore-Assets
          path: |
            release.keystore
            playstore_assets/*
          retention-days: 90
```

---

## ክፍል 8፦ የ`privacy-policy.html` ፋይሉን በGitHub Pages ላይ Deploy ማድረግ እና ወደ Play Console መጫን

1. በRepositoryዎ ውስጥ ወደ **Settings** ➔ **Pages** ይሂዱ።
2. በ**Build and deployment** ስር፦
   * **Source**፦ `Deploy from a branch` ይምረጡ።
   * **Branch**፦ `main` እና ፎልደሩን **`/docs`** ይምረጡና **Save** ይጫኑ።
3. ከ1 ደቂቃ በኋላ የሚከተለው የPrivacy Policy ቀጥታ ሊንክ (URL) ይፈጠራል፦
   `https://<YOUR_ORG_NAME>.github.io/shah-sport-game/privacy-policy.html`
4. ወደ [Google Play Console](https://play.google.com/console) ገብተው፦
   * በ**Privacy Policy** መሙያው ላይ ይህንን ሊንክ ያስገቡ።
   * በ**Store Listing** ላይ ከ`playstore_assets/` ውስጥ `ic_launcher_playstore_512x512.png` እና `feature_graphic_1024x500.png` ይጫኑ።
   * በ**Production** ➔ **Create new release** ውስጥ ከGitHub Actions ያወረዱትን **`app-release.aab`** ፋይል ጭነው ለግምገማ ያቅርቡ!
