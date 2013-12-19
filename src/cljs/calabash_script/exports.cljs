(ns uia
  (:require [calabash-script.log :as log]
            [calabash-script.core :as core]
            [cljs.reader :as reader]
            [calabash-script.utils :as utils]))


(defn wrap-query-fn [qfn]
  (fn [& args]
    (utils/clj->js
     (apply qfn (map reader/read-string args)))))


(defn ^:export queryEl
  [& args]
  (first (apply core/query-el (map reader/read-string args))))

(def ^:export query (wrap-query-fn core/query))
(defn ^:export queryWindows
  [q]
  (utils/clj->js
   (core/query (reader/read-string q) (utils/windows))))

(def ^:export names (wrap-query-fn core/names))

(def ^:export tap (wrap-query-fn core/tap))
(defn ^:export tapMark [mark] (utils/clj->js (core/tap-mark mark)))
(def ^:export tapOffset (wrap-query-fn core/tap-offset))

(def ^:export  doubleTap            (wrap-query-fn core/double-tap))
(def ^:export  doubleTapOffset      (wrap-query-fn core/double-tap-offset))

(def ^:export  twoFingerTap            (wrap-query-fn core/two-finger-tap))
(def ^:export  twoFingerTapOffset      (wrap-query-fn core/two-finger-tap-offset))

(def ^:export  flickOffset             (wrap-query-fn core/flick-offset))

(def ^:export  touchHold            (wrap-query-fn core/touch-hold))
(def ^:export  touchHoldOffset      (wrap-query-fn core/touch-hold-offset))

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


(def  ^:export logQuery (wrap-query-fn core/log-query))
(def  ^:export logTreeQuery (wrap-query-fn core/log-tree-query))
(defn ^:export logTreeMark [mark] (core/log-tree-mark mark))

(def  ^:export waitForElementsReady (wrap-query-fn core/wait-for-elements-ready))
(defn ^:export waitForMarkReady [wait-opts mark]
  (core/wait-for-mark-ready (reader/read-string wait-opts) mark))
