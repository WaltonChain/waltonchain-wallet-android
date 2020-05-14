package io.horizontalsystems.bankwallet.modules.backup.privatekey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import io.horizontalsystems.bankwallet.R
import kotlinx.android.synthetic.main.fragment_backup_private_key.*

class BackupPrivateKeyFragment : Fragment() {

    val viewModel by activityViewModels<BackupPrivateKeyViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_backup_private_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.wordsLiveData.observe(viewLifecycleOwner, Observer {
            populateWords(it)
        })
    }

    private fun populateWords(words: String) {
        tvPrivateKey.text = words
    }

}
