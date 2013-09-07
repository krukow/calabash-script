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
(defn ^:export tapMark [mark] (utils/clj->js (core/tap-mark mark)))

(def ^:export tapOffset (wrap-query-fn core/tap-offset))

(def ^:export pan (wrap-query-fn core/pan))
(def ^:export panOffset (wrap-query-fn core/pan-offset))


(def ^:export swipe (wrap-query-fn core/swipe))
(def ^:export swipeOffset (wrap-query-fn core/swipe-offset))

(def ^:export pinch (wrap-query-fn core/pinch))
(def ^:export pinchOffset (wrap-query-fn core/pinch-offset))

(def ^:export scrollTo (wrap-query-fn core/scroll-to))

(def ^:export elementExists  (wrap-query-fn core/element-exists?))
(def ^:export elementDoesNotExist (wrap-query-fn core/element-does-not-exist?))

(defn ^:export app [] (utils/app))
(defn ^:export window [] (utils/window))
(defn ^:export keyboard [] (utils/keyboard))
(defn ^:export alert [] (core/alert))

(defn ^:export screenshot [name] (utils/screenshot name))

(defn ^:export typeString [string] (core/keyboard-enter-text string))
(defn ^:export enter [] (core/enter))

(defn ^:export setLocation [location] (core/set-location (reader/read-string location)))
(def ^:export deactivate (wrap-query-fn core/deactivate))
