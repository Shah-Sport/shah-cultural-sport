package eth.culturalsports.shah.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import eth.culturalsports.shah.R
import eth.culturalsports.shah.model.AiDifficulty
import eth.culturalsports.shah.model.LanguageOption
import eth.culturalsports.shah.model.QuizQuestion
import eth.culturalsports.shah.model.RuleArticle
import eth.culturalsports.shah.model.UserSettings
import java.util.Locale

class ShahRepository(private val appContext: Context) {

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("shah_native_pro_prefs", Context.MODE_PRIVATE)

    val supportedLanguages: List<LanguageOption> = listOf(
        LanguageOption("am", "አማርኛ (Amharic)", "ኢትዮጵያ (Ethiopia)", "🇪🇹", false),
        LanguageOption("en", "English (Global)", "International & East Africa", "🌍", false),
        LanguageOption("sw", "Kiswahili (Swahili)", "Kenya, Tanzania, Uganda, DRC", "🇰🇪", false),
        LanguageOption("so", "Af-Soomaali (Somali)", "Somalia, Djibouti, Ethiopia", "🇸🇴", false),
        LanguageOption("om", "Afaan Oromoo", "Ethiopia & Kenya", "🇪🇹", false),
        LanguageOption("ti", "ትግርኛ (Tigrinya)", "Eritrea & Ethiopia", "🇪🇷", false),
        LanguageOption("ar", "العربية (Arabic)", "Sudan, Djibouti, Somalia, Eritrea", "🇸🇩", true),
        LanguageOption("fr", "Français (French)", "Djibouti, Burundi, Rwanda, Comores", "🇩🇯", false),
        LanguageOption("rw", "Ikinyarwanda / Kirundi", "Rwanda & Burundi", "🇷🇼", false)
    )

    fun loadSettings(): UserSettings {
        val lang = prefs.getString("lang_code", "am") ?: "am"
        val isDark = prefs.getBoolean("is_dark", true)
        val fontStep = prefs.getInt("font_step", 0).coerceIn(-2, 4)
        val sound = prefs.getBoolean("sound_on", true)
        val vib = prefs.getBoolean("vib_on", true)
        val showNums = prefs.getBoolean("show_nums", true)
        val diff = AiDifficulty.fromCode(prefs.getString("ai_diff", "master") ?: "master")
        val played = prefs.getInt("stats_played", 0)
        val p1W = prefs.getInt("stats_p1_wins", 0)
        val p2W = prefs.getInt("stats_p2_wins", 0)
        return UserSettings(
            languageCode = lang,
            isDarkTheme = isDark,
            fontScaleStep = fontStep,
            soundEnabled = sound,
            vibrationEnabled = vib,
            showStationNumbers = showNums,
            aiDifficulty = diff,
            matchesPlayed = played,
            p1Wins = p1W,
            p2Wins = p2W
        )
    }

    fun saveSettings(settings: UserSettings) {
        prefs.edit()
            .putString("lang_code", settings.languageCode)
            .putBoolean("is_dark", settings.isDarkTheme)
            .putInt("font_step", settings.fontScaleStep)
            .putBoolean("sound_on", settings.soundEnabled)
            .putBoolean("vib_on", settings.vibrationEnabled)
            .putBoolean("show_nums", settings.showStationNumbers)
            .putString("ai_diff", settings.aiDifficulty.code)
            .putInt("stats_played", settings.matchesPlayed)
            .putInt("stats_p1_wins", settings.p1Wins)
            .putInt("stats_p2_wins", settings.p2Wins)
            .apply()
    }

    fun localizedContext(langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(appContext.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return appContext.createConfigurationContext(config)
    }

    fun getString(langCode: String, resId: Int, vars: Map<String, String> = emptyMap()): String {
        val ctx = localizedContext(langCode)
        var text = ctx.resources.getString(resId)
        for ((k, v) in vars) {
            text = text.replace("{$k}", v)
        }
        return text
    }

    fun getOptionLetters(langCode: String): List<String> {
        val ctx = localizedContext(langCode)
        return ctx.resources.getStringArray(R.array.shah_opt_letters).toList()
    }

    fun getRules(langCode: String): List<RuleArticle> {
        val ctx = localizedContext(langCode)
        val res = ctx.resources
        val pkg = appContext.packageName
        val articles = ArrayList<RuleArticle>(6)
        for (i in 1..6) {
            val catId = res.getIdentifier("shah_rule_${i}_category", "string", pkg)
            val titleId = res.getIdentifier("shah_rule_${i}_title", "string", pkg)
            val contentId = res.getIdentifier("shah_rule_${i}_content", "string", pkg)
            if (titleId != 0 && contentId != 0) {
                articles.add(
                    RuleArticle(
                        id = "rule-$i",
                        category = if (catId != 0) res.getString(catId) else "play",
                        title = res.getString(titleId),
                        content = res.getString(contentId)
                    )
                )
            }
        }
        return articles
    }

    fun getQuizQuestions(langCode: String): List<QuizQuestion> {
        val ctx = localizedContext(langCode)
        val res = ctx.resources
        val pkg = appContext.packageName
        val questions = ArrayList<QuizQuestion>(20)
        for (i in 1..20) {
            val qId = res.getIdentifier("shah_quiz_${i}_q", "string", pkg)
            val catId = res.getIdentifier("shah_quiz_${i}_cat", "string", pkg)
            val expId = res.getIdentifier("shah_quiz_${i}_exp", "string", pkg)
            val ansId = res.getIdentifier("shah_quiz_${i}_ans", "integer", pkg)
            val opt0Id = res.getIdentifier("shah_quiz_${i}_opt_0", "string", pkg)
            val opt1Id = res.getIdentifier("shah_quiz_${i}_opt_1", "string", pkg)
            val opt2Id = res.getIdentifier("shah_quiz_${i}_opt_2", "string", pkg)
            val opt3Id = res.getIdentifier("shah_quiz_${i}_opt_3", "string", pkg)

            if (qId != 0) {
                questions.add(
                    QuizQuestion(
                        index = i - 1,
                        question = res.getString(qId),
                        options = listOf(
                            if (opt0Id != 0) res.getString(opt0Id) else "",
                            if (opt1Id != 0) res.getString(opt1Id) else "",
                            if (opt2Id != 0) res.getString(opt2Id) else "",
                            if (opt3Id != 0) res.getString(opt3Id) else ""
                        ),
                        correctAnswer = if (ansId != 0) res.getInteger(ansId) else 0,
                        category = if (catId != 0) res.getString(catId) else "",
                        explanation = if (expId != 0) res.getString(expId) else ""
                    )
                )
            }
        }
        return questions
    }
}
