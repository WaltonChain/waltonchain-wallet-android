package io.horizontalsystems.languageswitcher

import androidx.lifecycle.ViewModel

class LanguageSwitcherPresenter(
        val view: LanguageSwitcherModule.IView,
        val router: LanguageSwitcherModule.IRouter,
        private val interactor: LanguageSwitcherModule.IInteractor)
    : ViewModel(), LanguageSwitcherModule.IViewDelegate {

    private val languages = interactor.availableLanguages

    override fun viewDidLoad() {
        val currentLanguage = interactor.currentLanguage
        val items = languages.map { language ->
            LanguageViewItem(language, interactor.getName(language), interactor.getNativeName(language), currentLanguage == language)
        }

        view.show(items)
    }

    override fun didSelect(position: Int) {
        val selected = languages[position]

        if (selected == interactor.currentLanguage) {
            router.close()
        } else {
            interactor.currentLanguage = selected
            router.reloadAppInterface()
        }
    }
}
