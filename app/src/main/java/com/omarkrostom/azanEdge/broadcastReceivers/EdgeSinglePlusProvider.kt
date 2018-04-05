package com.omarkrostom.azanEdge.broadcastReceivers

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.omarkrostom.azanEdge.R
import com.omarkrostom.azanEdge.utils.setPrayerTimes
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailManager
import com.samsung.android.sdk.look.cocktailbar.SlookCocktailProvider

class EdgeSinglePlusProvider : SlookCocktailProvider() {

    override fun onUpdate(context: Context, cocktailManager: SlookCocktailManager, cocktailIds: IntArray?) {
        setMainPlusLayout(context, cocktailManager, cocktailIds)
    }

    /* Enables main layout */
    private fun setMainPlusLayout(context: Context, manager: SlookCocktailManager, cocktailIds: IntArray?) {
        val layoutId = R.layout.layout_main
        val rv = RemoteViews(context.packageName, layoutId)
        rv.setViewVisibility(R.id.ll_main_app, View.VISIBLE)
        updateCocktails(
                cocktailIds,
                manager,
                rv
        )
        setPrayerTimes(context.packageName, cocktailIds!!, context)
    }

    /* Needed to update view */
    private fun updateCocktails(cocktailIds: IntArray?,
                                manager: SlookCocktailManager,
                                rv: RemoteViews) {
        if (cocktailIds != null) {
            for (id in cocktailIds) {
                manager.updateCocktail(id, rv)
            }
        }
    }

}