package com.greenkey.nehaj.organaizercalculator2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.greenkey.nehaj.organaizercalculator2.view.CalculatorActivityGB
import com.greenkey.nehaj.organaizercalculator2.view.CalculatorActivityL
import com.greenkey.nehaj.organaizercalculator2.utils.Utils

class StartActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Utils.hasLollipop()) {
            startActivity(Intent(this, CalculatorActivityL::class.java))
        } else {
            startActivity(Intent(this, CalculatorActivityGB::class.java))
        }
        finish()
    }

}