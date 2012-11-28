(ns calabash-script.ext)




(comment

    js/UIAElementArray
  (-seq [this]
    (prn "Array")
    (prn this)
    (if (zero? (.-length this))
      nil
      (.toArray this))))
