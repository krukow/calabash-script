(ns calabash-script.log)

(defn log [& more]
  (.logMessage js/UIALogger (apply str more)))

(defn logTree
  [el]
  (.logElementTree el))
