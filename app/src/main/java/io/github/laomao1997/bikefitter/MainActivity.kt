package io.github.laomao1997.bikefitter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * 骑行风格常量 - 休闲
         */
        const val TYPE_RELAX = 0

        /**
         * 骑行风格常量 - 普通
         */
        const val TYPE_NORMAL = 1

        /**
         * 骑行风格常量 - 竞赛
         */
        const val TYPE_RACE = 2
    }

    private lateinit var mEtEggHeight: EditText
    private lateinit var mBtnCalculate: Button
    private lateinit var mRadioGroup: RadioGroup
    private lateinit var mTvSeatHeight: TextView
    private lateinit var mTvStackHeight: TextView
    private lateinit var mTvReachHeight: TextView

    @RidingStyleTypeDef
    private var mRidingStyle = TYPE_NORMAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        mEtEggHeight = findViewById(R.id.et_egg_height)
        mBtnCalculate = findViewById(R.id.btn_calculate)
        mRadioGroup = findViewById(R.id.radio_group)
        mTvSeatHeight = findViewById(R.id.tv_seat_height)
        mTvStackHeight = findViewById(R.id.tv_stack_height)
        mTvReachHeight = findViewById(R.id.tv_reach_height)

        //点击软键盘外部，收起软键盘
        mEtEggHeight.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val manager: InputMethodManager =
                    applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }

        mBtnCalculate.setOnClickListener {
            mEtEggHeight.clearFocus()
            calculateData()
        }

        mRadioGroup.check(R.id.type_normal)
        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.type_relax -> mRidingStyle = TYPE_RELAX
                R.id.type_normal -> mRidingStyle = TYPE_NORMAL
                R.id.type_race -> mRidingStyle = TYPE_RACE
            }
            calculateData()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateData() {
        val eggHeight = getEggHeight()
        val seatHeight = calculateSeatHeight(eggHeight)
        val stack = calculateStack(eggHeight)
        val reach = calculateReach(stack)
        mTvSeatHeight.text = if (seatHeight == 0.0) {
            getString(R.string.double_line)
        } else {
            BigDecimal(seatHeight).setScale(1, BigDecimal.ROUND_HALF_UP).toString()
        }
        mTvStackHeight.text = if (stack == 0.0) {
            getString(R.string.double_line)
        } else {
            BigDecimal(stack).setScale(1, BigDecimal.ROUND_HALF_UP).toString()
        }
        mTvReachHeight.text = if (reach == 0.0) {
            getString(R.string.double_line)
        } else {
            BigDecimal(reach).setScale(1, BigDecimal.ROUND_HALF_UP).toString()
        }
    }

    private fun getEggHeight(): Int {
        val eggHeightText: String = mEtEggHeight.text.toString()
        return try {
            eggHeightText.toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 计算坐垫高度
     */
    private fun calculateSeatHeight(eggHeight: Int): Double {
        if (eggHeight <= 0) return 0.0
        return eggHeight * Constant.SEAT_HEIGHT_RATIO
    }

    /**
     * 计算 STACK 值
     */
    private fun calculateStack(eggHeight: Int): Double {
        if (eggHeight <= 0) return 0.0
        return when (mRidingStyle) {
            TYPE_RELAX -> eggHeight * Constant.STACK_RATIO + 3
            TYPE_NORMAL -> eggHeight * Constant.STACK_RATIO
            TYPE_RACE -> eggHeight * Constant.STACK_RATIO - 2
            else -> 0.0
        }
    }

    /**
     * 计算 REACH 值
     */
    private fun calculateReach(stack: Double): Double {
        if (stack <= 0) return 0.0
        return when (mRidingStyle) {
            TYPE_RELAX -> stack / 1.6
            TYPE_NORMAL -> stack / 1.48
            TYPE_RACE -> stack / 1.36
            else -> 0.0
        }
    }

    /**
     * 骑行风格类型注解
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TYPE_RELAX, TYPE_NORMAL, TYPE_RACE)
    internal annotation class RidingStyleTypeDef
}