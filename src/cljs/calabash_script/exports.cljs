(ns uia
  (:require [calabash-script.log :as log]
            [calabash-script.core :as core]
            [cljs.reader :as reader]
            [calabash-script.utils :as utils]))


(defn wrap-query-fn [qfn]
  (fn [& args]
    (utils/clj->js
      (apply qfn (map reader/read-string args)))))

(def ^:export query (wrap-query-fn core/query))
(def ^:export names (wrap-query-fn core/names))
(def ^:export tap (wrap-query-fn core/tap))
(def ^:export pan (wrap-query-fn core/pan))
(def ^:export scrollTo (wrap-query-fn core/scroll-to))

(defn ^:export elementExists [] (wrap-query-fn core/element-exists?))
(defn ^:export elementDoesNotExist [] (wrap-query-fn core/element-does-not-exist?))

(defn ^:export app [] (utils/app))
(defn ^:export window [] (utils/window))
(defn ^:export keyboard [] (utils/keyboard))
(defn ^:export alert [] (core/alert))

(defn ^:export screenshot [name] (utils/screenshot name))

(defn ^:export typeString [string] (core/keyboard-enter-text string))
(defn ^:export enter [] (core/enter))
(defn ^:export tapMark [mark] (core/tap-mark mark))

(defn ^:export setLocation [location] (core/set-location (reader/read-string location)))
