(ns calabash-script.core
  (:use [calabash-script.query :only [-query dir-seq valid?]])
  (:require [calabash-script.log :as log]
            [calabash-script.uia :as uia]
            [calabash-script.convert :as c]
            [calabash-script.utils :as utils])
  (:require-macros [calabash-script.macros.uia :as m]))

(.setTimeout (utils/target) 0)

(set! *print-fn*
      (fn [& args]
        (log/log (apply str args))))


(extend-protocol ISeqable
  js/UIAKey ;;for some reason UIAKey is not a UIAElement
  (-seq [this]
    nil)

  js/UIAElement
  (-seq [this]
    (seq (.toArray (.elements this)))))

(defn sleep
  [n]
  (.delay (utils/target) n))

(defn query
  ([q] (query q (list (utils/window))))
  ([q coll] (query q coll :descendant))
  ([q coll dir]
     (map c/uia->map (-query q coll dir))))

(defn query-el
  [& args]
  (map :el (apply query args)))

(defn log-query [& args]
  (log/log (apply query args)))


(defn fail [reason]
  (throw (new js/Error reason)))

(defn fail-if [condition msg]
  (if condition
    (fail msg)))

(defn fail-if-not [x msg]
  (fail-if (not x) msg))


(defn tap
  ([& args]
     (if-let [res (seq (apply query args))]
       (.tap (:el (first res)))
       (fail (apply str "No results for query " args)))))


(defn keyboard-visible?
  []
  (valid? (utils/keyboard)))

(defn keyboard-enter-text
  [txt]
  (fail-if-not (keyboard-visible?) "Keyboard not visible")
  (.typeString (utils/keyboard) txt))

(defn enter
  []
  (fail-if-not (keyboard-visible?) "Keyboard not visible")
  (tap [:button {:marked "return"}] (utils/keyboard)))


(defn screenshot
  [name]
  (.captureScreenWithName (utils/target) name))

(def uia-tests (atom {}))

(defn define-uia-test
  [doc test]
  (swap! uia-tests assoc doc test))

(defn run-all!
  []
  (doseq [[name test] @uia-tests]
    (.logStart js/UIALogger name)
    (try
      (test)
      (swap! uia-tests assoc name {:code (str "todo" (comment (str test))) :result :pass})
      (.logPass js/UIALogger name)
      (catch js/Object e
        (swap! uia-tests assoc name {:code (str "todo" (comment test)) :result :fail :message (str e)})
        (.logFail js/UIALogger name))))
  (log/log @uia-tests))


(define-uia-test
  "user should be able to log in"
  (fn []
    (tap [:textField {:marked "Name"}])
    (keyboard-enter-text "Karl.Krukow@gmail.com")
    (enter)
    (screenshot "Menu")
    (tap [:button {:marked "Second"}])
    (sleep 0.3)
    (screenshot "Map")
    ))



(run-all!)
