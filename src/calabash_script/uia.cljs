(ns calabash-script.uia
  (:require-macros [calabash-script.macros.uia :as uia]))

(def classes
  (let [clz-map
        (uia/classes  UIAActionSheet
                      UIAActivityIndicator
                      UIAActivityView
                      UIAAlert
                      UIAApplication
                      UIAButton
                      UIACollectionView
                      UIAEditingMenu
                      UIAElement
                      UIAElementArray
                      UIAHost
                      UIAKey
                      UIAKeyboard
                      UIALink
                      UIALogger
                      UIANavigationBar
                      UIAPageIndicator
                      UIAPicker
                      UIAPickerWheel
                      UIAPopover
                      UIAProgressIndicator
                      UIAScrollView
                      UIASearchBar
                      UIASecureTextField
                      UIASegmentedControl
                      UIASlider
                      UIAStaticText
                      UIAStatusBar
                      UIASwitch
                      UIATabBar
                      UIATableCell
                      UIATableGroup
                      UIATableView
                      UIATarget
                      UIATextField
                      UIATextView
                      UIAToolbar
                      UIAWebView
                      UIAWindow)]
    (assoc clz-map :view js/Object)))
