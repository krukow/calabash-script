(ns uia
  (:require [calabash-script.log :as log]
            [calabash-script.core :as core]
            [cljs.reader :as reader]
            [calabash-script.utils :as utils]))


(defn ^:export query [q]
  (utils/clj->js (core/query (reader/read-string q))))


(defn ^:export tap [q]
  (utils/clj->js (core/tap (reader/read-string q))))
