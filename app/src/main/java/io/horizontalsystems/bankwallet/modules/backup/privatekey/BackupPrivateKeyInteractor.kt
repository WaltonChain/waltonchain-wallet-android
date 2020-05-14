package io.horizontalsystems.bankwallet.modules.backup.privatekey

import io.horizontalsystems.bankwallet.core.IRandomProvider
import io.horizontalsystems.bankwallet.modules.backup.words.BackupWordsModule
import java.util.HashMap

class BackupPrivateKeyInteractor(words: String) :
        BackupPrivateKeyModule.IInteractor {

    var delegate: BackupPrivateKeyModule.IInteractorDelegate? = null

}
