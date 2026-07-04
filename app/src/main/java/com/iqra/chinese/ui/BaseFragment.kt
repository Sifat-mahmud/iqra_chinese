package com.iqra.chinese.ui

import androidx.fragment.app.Fragment
import com.iqra.chinese.MainActivity

open class BaseFragment : Fragment() {
    protected val vm get() = (requireActivity() as MainActivity).vm
}
