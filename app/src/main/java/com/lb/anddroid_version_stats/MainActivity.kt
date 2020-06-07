package com.lb.anddroid_version_stats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var viewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        viewModel.load()
        viewModel.stateLiveData.observe(this, { state: MyViewModel.State? ->
            when (state) {
                null, MyViewModel.State.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    resultView.visibility = View.INVISIBLE
                }
                is MyViewModel.State.Success -> {
                    val versionItems = state.versionItems
                    progressBar.visibility = View.INVISIBLE
                    resultView.visibility = View.VISIBLE
                    val sb = StringBuilder()
                    if (!state.isFromInternet)
                        sb.append("results are not from Internet (some Internet issue)\n")
                    val decimalFormat = DecimalFormat("##.##%")
                    for (versionItem in versionItems) {
                        sb.append("${versionItem.version} - ${versionItem.versionNickName} - API ${versionItem.apiLevel} - ${decimalFormat.format(versionItem.marketSharePercentage)}\n")
                    }
                    textView.text = sb.toString()
                }
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var url: String? = null
        when (item.itemId) {
            R.id.menuItem_all_my_apps -> url = "https://play.google.com/store/apps/developer?id=AndroidDeveloperLB"
            R.id.menuItem_all_my_repositories -> url = "https://github.com/AndroidDeveloperLB"
            R.id.menuItem_current_repository_website -> url = "https://github.com/AndroidDeveloperLB/AnddroidVersionsStats"
        }
        if (url == null)
            return true
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(intent)
        return true
    }
}
