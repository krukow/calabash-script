(ns base)

(defn log [msgs]
  (.logMessage js/UIALogger (apply str msgs)))
