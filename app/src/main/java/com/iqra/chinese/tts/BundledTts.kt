package com.iqra.chinese.tts

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.iqra.chinese.R

/**
 * Bundled offline TTS using pre-recorded eSpeak-NG audio files.
 * Works on ALL devices with no internet, no TTS engine required.
 * Audio files are stored in res/raw/ as MP3s (~3MB total).
 */
object BundledTts {

    private var player: MediaPlayer? = null
    private var appCtx: Context? = null

    // Map of Chinese text → raw resource ID
    private val AUDIO_MAP: Map<String, Int> = mapOf(
        "你好" to R.raw.w_4f60_597d,
        "谢谢" to R.raw.w_8c22_8c22,
        "再见" to R.raw.w_518d_89c1,
        "是" to R.raw.w_662f,
        "不" to R.raw.w_4e0d,
        "我" to R.raw.w_6211,
        "你" to R.raw.w_4f60,
        "他" to R.raw.w_4ed6,
        "她" to R.raw.w_5979,
        "我们" to R.raw.w_6211_4eec,
        "你们" to R.raw.w_4f60_4eec,
        "他们" to R.raw.w_4ed6_4eec,
        "好" to R.raw.w_597d,
        "大" to R.raw.w_5927,
        "小" to R.raw.w_5c0f,
        "一" to R.raw.w_4e00,
        "二" to R.raw.w_4e8c,
        "三" to R.raw.w_4e09,
        "四" to R.raw.w_56db,
        "五" to R.raw.w_4e94,
        "六" to R.raw.w_516d,
        "七" to R.raw.w_4e03,
        "八" to R.raw.w_516b,
        "九" to R.raw.w_4e5d,
        "十" to R.raw.w_5341,
        "百" to R.raw.w_767e,
        "千" to R.raw.w_5343,
        "人" to R.raw.w_4eba,
        "吃" to R.raw.w_5403,
        "喝" to R.raw.w_559d,
        "看" to R.raw.w_770b,
        "听" to R.raw.w_542c,
        "说" to R.raw.w_8bf4,
        "来" to R.raw.w_6765,
        "去" to R.raw.w_53bb,
        "买" to R.raw.w_4e70,
        "有" to R.raw.w_6709,
        "没有" to R.raw.w_6ca1_6709,
        "喜欢" to R.raw.w_559c_6b22,
        "爱" to R.raw.w_7231,
        "家" to R.raw.w_5bb6,
        "中国" to R.raw.w_4e2d_56fd,
        "北京" to R.raw.w_5317_4eac,
        "上海" to R.raw.w_4e0a_6d77,
        "朋友" to R.raw.w_670b_53cb,
        "老师" to R.raw.w_8001_5e08,
        "学生" to R.raw.w_5b66_751f,
        "书" to R.raw.w_4e66,
        "水" to R.raw.w_6c34,
        "茶" to R.raw.w_8336,
        "咖啡" to R.raw.w_5496_5561,
        "饭" to R.raw.w_996d,
        "今天" to R.raw.w_4eca_5929,
        "明天" to R.raw.w_660e_5929,
        "昨天" to R.raw.w_6628_5929,
        "年" to R.raw.w_5e74,
        "月" to R.raw.w_6708,
        "日" to R.raw.w_65e5,
        "时候" to R.raw.w_65f6_5019,
        "上午" to R.raw.w_4e0a_5348,
        "下午" to R.raw.w_4e0b_5348,
        "晚上" to R.raw.w_665a_4e0a,
        "上" to R.raw.w_4e0a,
        "下" to R.raw.w_4e0b,
        "里" to R.raw.w_91cc,
        "前" to R.raw.w_524d,
        "后" to R.raw.w_540e,
        "左" to R.raw.w_5de6,
        "右" to R.raw.w_53f3,
        "多" to R.raw.w_591a,
        "少" to R.raw.w_5c11,
        "热" to R.raw.w_70ed,
        "冷" to R.raw.w_51b7,
        "新" to R.raw.w_65b0,
        "贵" to R.raw.w_8d35,
        "便宜" to R.raw.w_4fbf_5b9c,
        "快" to R.raw.w_5feb,
        "慢" to R.raw.w_6162,
        "高" to R.raw.w_9ad8,
        "漂亮" to R.raw.w_6f02_4eae,
        "什么" to R.raw.w_4ec0_4e48,
        "谁" to R.raw.w_8c01,
        "哪里" to R.raw.w_54ea_91cc,
        "为什么" to R.raw.w_4e3a_4ec0_4e48,
        "怎么" to R.raw.w_600e_4e48,
        "多少" to R.raw.w_591a_5c11,
        "的" to R.raw.w_7684,
        "了" to R.raw.w_4e86,
        "吗" to R.raw.w_5417,
        "很" to R.raw.w_5f88,
        "也" to R.raw.w_4e5f,
        "都" to R.raw.w_90fd,
        "和" to R.raw.w_548c,
        "但是" to R.raw.w_4f46_662f,
        "因为" to R.raw.w_56e0_4e3a,
        "所以" to R.raw.w_6240_4ee5,
        "现在" to R.raw.w_73b0_5728,
        "这" to R.raw.w_8fd9,
        "那" to R.raw.w_90a3,
        "对" to R.raw.w_5bf9,
        "请" to R.raw.w_8bf7,
        "对不起" to R.raw.w_5bf9_4e0d_8d77,
        "没关系" to R.raw.w_6ca1_5173_7cfb,
        "名字" to R.raw.w_540d_5b57,
        "爸爸" to R.raw.w_7238_7238,
        "妈妈" to R.raw.w_5988_5988,
        "哥哥" to R.raw.w_54e5_54e5,
        "姐姐" to R.raw.w_59d0_59d0,
        "弟弟" to R.raw.w_5f1f_5f1f,
        "妹妹" to R.raw.w_59b9_59b9,
        "儿子" to R.raw.w_513f_5b50,
        "女儿" to R.raw.w_5973_513f,
        "猫" to R.raw.w_732b,
        "狗" to R.raw.w_72d7,
        "鱼" to R.raw.w_9c7c,
        "花" to R.raw.w_82b1,
        "树" to R.raw.w_6811,
        "山" to R.raw.w_5c71,
        "太阳" to R.raw.w_592a_9633,
        "月亮" to R.raw.w_6708_4eae,
        "天气" to R.raw.w_5929_6c14,
        "钱" to R.raw.w_94b1,
        "车" to R.raw.w_8f66,
        "路" to R.raw.w_8def,
        "学校" to R.raw.w_5b66_6821,
        "医院" to R.raw.w_533b_9662,
        "一起" to R.raw.w_4e00_8d77,
        "已经" to R.raw.w_5df2_7ecf,
        "还" to R.raw.w_8fd8,
        "会" to R.raw.w_4f1a,
        "要" to R.raw.w_8981,
        "能" to R.raw.w_80fd,
        "想" to R.raw.w_60f3,
        "知道" to R.raw.w_77e5_9053,
        "觉得" to R.raw.w_89c9_5f97,
        "告诉" to R.raw.w_544a_8bc9,
        "帮" to R.raw.w_5e2e,
        "找" to R.raw.w_627e,
        "开" to R.raw.w_5f00,
        "关" to R.raw.w_5173,
        "问" to R.raw.w_95ee,
        "回" to R.raw.w_56de,
        "进" to R.raw.w_8fdb,
        "出" to R.raw.w_51fa,
        "到" to R.raw.w_5230,
        "从" to R.raw.w_4ece,
        "在" to R.raw.w_5728,
        "对_2" to R.raw.w_5bf9_005f_0032,
        "跟" to R.raw.w_8ddf,
        "比" to R.raw.w_6bd4,
        "最" to R.raw.w_6700,
        "非常" to R.raw.w_975e_5e38,
        "跑步" to R.raw.w_8dd1_6b65,
        "游泳" to R.raw.w_6e38_6cf3,
        "睡觉" to R.raw.w_7761_89c9,
        "起床" to R.raw.w_8d77_5e8a,
        "工作" to R.raw.w_5de5_4f5c,
        "学习" to R.raw.w_5b66_4e60,
        "电话" to R.raw.w_7535_8bdd,
        "手机" to R.raw.w_624b_673a,
        "电视" to R.raw.w_7535_89c6,
        "电脑" to R.raw.w_7535_8111,
        "超市" to R.raw.w_8d85_5e02,
        "公司" to R.raw.w_516c_53f8,
        "高兴" to R.raw.w_9ad8_5174,
        "下雨" to R.raw.w_4e0b_96e8,
        "下雪" to R.raw.w_4e0b_96ea,
        "晴天" to R.raw.w_6674_5929,
        "地铁" to R.raw.w_5730_94c1,
        "出租车" to R.raw.w_51fa_79df_8f66,
        "飞机" to R.raw.w_98de_673a,
        "火车" to R.raw.w_706b_8f66,
        "自行车" to R.raw.w_81ea_884c_8f66,
        "宾馆" to R.raw.w_5bbe_9986,
        "护照" to R.raw.w_62a4_7167,
        "行李" to R.raw.w_884c_674e,
        "衣服" to R.raw.w_8863_670d,
        "帽子" to R.raw.w_5e3d_5b50,
        "眼镜" to R.raw.w_773c_955c,
        "锻炼" to R.raw.w_953b_70bc,
        "身体" to R.raw.w_8eab_4f53,
        "生病" to R.raw.w_751f_75c5,
        "感冒" to R.raw.w_611f_5192,
        "药" to R.raw.w_836f,
        "休息" to R.raw.w_4f11_606f,
        "唱歌" to R.raw.w_5531_6b4c,
        "跳舞" to R.raw.w_8df3_821e,
        "音乐" to R.raw.w_97f3_4e50,
        "运动" to R.raw.w_8fd0_52a8,
        "图书馆" to R.raw.w_56fe_4e66_9986,
        "邮局" to R.raw.w_90ae_5c40,
        "银行" to R.raw.w_94f6_884c,
        "环境" to R.raw.w_73af_5883,
        "机会" to R.raw.w_673a_4f1a,
        "经验" to R.raw.w_7ecf_9a8c,
        "感觉" to R.raw.w_611f_89c9,
        "认为" to R.raw.w_8ba4_4e3a,
        "应该" to R.raw.w_5e94_8be5,
        "需要" to R.raw.w_9700_8981,
        "可能" to R.raw.w_53ef_80fd,
        "问题" to R.raw.w_95ee_9898,
        "解决" to R.raw.w_89e3_51b3,
        "了解" to R.raw.w_4e86_89e3,
        "准备" to R.raw.w_51c6_5907,
        "决定" to R.raw.w_51b3_5b9a,
        "希望" to R.raw.w_5e0c_671b,
        "担心" to R.raw.w_62c5_5fc3,
        "生气" to R.raw.w_751f_6c14,
        "难过" to R.raw.w_96be_8fc7,
        "认识" to R.raw.w_8ba4_8bc6,
        "相信" to R.raw.w_76f8_4fe1,
        "帮助" to R.raw.w_5e2e_52a9,
        "借" to R.raw.w_501f,
        "送" to R.raw.w_9001,
        "打扫" to R.raw.w_6253_626b,
        "整理" to R.raw.w_6574_7406,
        "修理" to R.raw.w_4fee_7406,
        "突然" to R.raw.w_7a81_7136,
        "终于" to R.raw.w_7ec8_4e8e,
        "一般" to R.raw.w_4e00_822c,
        "重要" to R.raw.w_91cd_8981,
        "特别" to R.raw.w_7279_522b,
        "发展" to R.raw.w_53d1_5c55,
        "影响" to R.raw.w_5f71_54cd,
        "表示" to R.raw.w_8868_793a,
        "实现" to R.raw.w_5b9e_73b0,
        "关系" to R.raw.w_5173_7cfb,
        "情况" to R.raw.w_60c5_51b5,
        "方面" to R.raw.w_65b9_9762,
        "重要2" to R.raw.w_91cd_8981_0032,
        "建立" to R.raw.w_5efa_7acb,
        "提高" to R.raw.w_63d0_9ad8,
        "分析" to R.raw.w_5206_6790,
        "研究" to R.raw.w_7814_7a76,
        "总结" to R.raw.w_603b_7ed3,
        "解释" to R.raw.w_89e3_91ca,
        "翻译" to R.raw.w_7ffb_8bd1,
        "交流" to R.raw.w_4ea4_6d41,
        "合作" to R.raw.w_5408_4f5c,
        "竞争" to R.raw.w_7ade_4e89,
        "管理" to R.raw.w_7ba1_7406,
        "创新" to R.raw.w_521b_65b0,
        "改革" to R.raw.w_6539_9769,
        "效率" to R.raw.w_6548_7387,
        "质量" to R.raw.w_8d28_91cf,
        "标准" to R.raw.w_6807_51c6,
        "目标" to R.raw.w_76ee_6807,
        "策略" to R.raw.w_7b56_7565,
        "计划" to R.raw.w_8ba1_5212,
        "执行" to R.raw.w_6267_884c,
        "评价" to R.raw.w_8bc4_4ef7,
        "原则" to R.raw.w_539f_5219,
        "归纳" to R.raw.w_5f52_7eb3,
        "矛盾" to R.raw.w_77db_76fe,
        "本质" to R.raw.w_672c_8d28,
        "客观" to R.raw.w_5ba2_89c2,
        "主观" to R.raw.w_4e3b_89c2,
        "逻辑" to R.raw.w_903b_8f91,
        "抽象" to R.raw.w_62bd_8c61,
        "具体" to R.raw.w_5177_4f53,
        "理论" to R.raw.w_7406_8bba,
        "实践" to R.raw.w_5b9e_8df5,
        "辩证" to R.raw.w_8fa9_8bc1,
        "综合" to R.raw.w_7efc_5408,
        "批判" to R.raw.w_6279_5224,
        "继承" to R.raw.w_7ee7_627f,
        "创造" to R.raw.w_521b_9020,
        "想象" to R.raw.w_60f3_8c61,
        "灵感" to R.raw.w_7075_611f,
        "审美" to R.raw.w_5ba1_7f8e,
        "道德" to R.raw.w_9053_5fb7,
        "价值观" to R.raw.w_4ef7_503c_89c2,
        "辩论" to R.raw.w_8fa9_8bba,
        "假设" to R.raw.w_5047_8bbe,
        "验证" to R.raw.w_9a8c_8bc1,
        "推理" to R.raw.w_63a8_7406,
        "认知" to R.raw.w_8ba4_77e5,
        "感知" to R.raw.w_611f_77e5,
        "意识" to R.raw.w_610f_8bc6,
        "直觉" to R.raw.w_76f4_89c9,
        "智慧" to R.raw.w_667a_6167,
        "启发" to R.raw.w_542f_53d1,
        "蜿蜒" to R.raw.w_873f_8712,
        "肃穆" to R.raw.w_8083_7a46,
        "恬静" to R.raw.w_606c_9759,
        "馥郁" to R.raw.w_99a5_90c1,
        "蹉跎" to R.raw.w_8e49_8dce,
        "踟蹰" to R.raw.w_8e1f_8e70,
        "潸然" to R.raw.w_6f78_7136,
        "惆怅" to R.raw.w_60c6_6005,
        "磅礴" to R.raw.w_78c5_7934,
        "熠熠" to R.raw.w_71a0_71a0,
        "婆娑" to R.raw.w_5a46_5a11,
        "旖旎" to R.raw.w_65d6_65ce,
        "氤氲" to R.raw.w_6c24_6c32,
        "缱绻" to R.raw.w_7f31_7efb,
        "蓬勃" to R.raw.w_84ec_52c3,
        "婉转" to R.raw.w_5a49_8f6c,
        "淋漓" to R.raw.w_6dcb_6f13,
        "酣畅" to R.raw.w_9163_7545,
        "豁然" to R.raw.w_8c41_7136,
        "茅塞顿开" to R.raw.w_8305_585e_987f_5f00,
        "醍醐灌顶" to R.raw.w_918d_9190_704c_9876,
        "悱恻" to R.raw.w_60b1_607b,
        "葳蕤" to R.raw.w_8473_8564,
        "旷世" to R.raw.w_65f7_4e16,
        "绝伦" to R.raw.w_7edd_4f26,
        "晦涩" to R.raw.w_6666_6da9,
        "斐然" to R.raw.w_6590_7136,
        "蹀躞" to R.raw.w_8e40_8e9e,
        "踯躅" to R.raw.w_8e2f_8e85,
        "踌躇" to R.raw.w_8e0c_8e87
    )

    fun init(context: Context) {
        appCtx = context.applicationContext
    }

    /** Returns true if we have a bundled audio file for this text */
    fun canSpeak(text: String) = AUDIO_MAP.containsKey(text)

    /** Speak using bundled audio. Returns true if audio found and played. */
    fun speak(text: String, slowSpeed: Boolean = false): Boolean {
        val ctx = appCtx ?: return false
        val resId = AUDIO_MAP[text] ?: return false

        try {
            // Safely release any existing player
            val old = player
            player = null
            try { old?.stop(); old?.release() } catch (_: Exception) {}

            val mp = MediaPlayer.create(ctx, resId) ?: run {
                Log.e("BundledTts", "MediaPlayer.create returned null for '$text'")
                return false
            }

            // Apply slow speed if requested (API 23+)
            if (slowSpeed) {
                try { mp.playbackParams = mp.playbackParams.setSpeed(0.65f) }
                catch (_: Exception) { /* ignore on older APIs */ }
            }

            mp.setOnCompletionListener { p ->
                try { p.release() } catch (_: Exception) {}
                if (player === p) player = null
            }
            mp.setOnErrorListener { p, what, extra ->
                Log.e("BundledTts", "MediaPlayer error: what=$what extra=$extra")
                try { p.release() } catch (_: Exception) {}
                if (player === p) player = null
                true
            }

            player = mp
            mp.start()
            Log.i("BundledTts", "Playing bundled audio for: $text")
            return true
        } catch (e: Exception) {
            Log.e("BundledTts", "Playback failed for '$text': ${e.message}")
            return false
        }
    }

    fun stop() {
        try { player?.stop(); player?.release() } catch (_: Exception) {}
        player = null
    }
}
