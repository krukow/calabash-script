(ns calabash-script.core
  (:use [calabash-script.query :only [-query dir-seq valid?]]
        [calabash-script.tests :only [define-uia-test run-all!]])

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
  (-seq [this] nil)

  js/UIAElement
  (-seq [this]
    (seq (.toArray (.elements this)))))


;;;;;;; Waiting ;;;;;;;;;


(defn sleep
  "Waits for n seconds"
  [n]
  (.delay (utils/target) n))


(defn do-until
  [pred action]
  (loop []
    (let [res (action)]
      (if (pred res)
        res
        (recur)))))

(defn duration-exceeds?
  [start max]
  (let [end (js/Date.)
        dur-sec (/ (- end start) 1000)]
    (>= dur-sec max)))

(defn wait-for
  "Waits for a predicate to be true"
  [{:keys [timeout retry-frequency
           post-timeout message
           screenshot]
    :as options
    :or {timeout 30
         retry-frequency 0.2
         post-timeout 0
         message "Timed out waiting..."
         screenshot true}}
   pred]

  (let [start (js/Date.)
        actions (repeatedly (fn []
                             (let [res (pred)]
                               (when-not res
                                 (sleep retry-frequency))
                               res)))
        results (filter
                   (fn [res]
                     (when (duration-exceeds? start timeout)
                       (tests/fail message (and screenshot "timeout")))
                     res)
                   actions)
        result (first results)]
    (when (> 0 post-timeout)
      (sleep post-timeout))
    result))


;;;;;;; Query ;;;;;;;;
(defn query
  "Perform a query using the calabash-script query language.
   By default every view reachable from the main window is queried.
  "
  ([q] (query q (list (utils/window))))
  ([q coll] (query q coll :descendant))
  ([q coll dir]
     (map c/uia->map (-query q coll dir))))

(defn query-el
  "Performs a query but returns results as a seq of UIA elements."
  [& args]
  (map :el (apply query args)))

(defn log-query [& args]
  "Log the result of a query."
  (log/log (apply query args)))



(defn tap
  "Taps the object which results from running query given by args. Fails if this query gives no results."
  [& args]
  (if-let [res (seq (apply query-el args))]
    (.tap (first res))
    (fail (apply str "No results for query " args))))

(defn pan
  [src-query tgt-query]
  (let [src (first (query src-query))
        tgt (first (query tgt-query))]
    (.dragFromToForDuration (utils/target)
                            (utils/clj->js (:hit-point src))
                            (utils/clj->js (:hit-point tgt))
                            3)))


(defn keyboard-visible?
  []
  (valid? (utils/keyboard)))

(defn keyboard-enter-text
  "Enters a string of characters using the iOS keyboard. Fails if no keyboard is visible. iOS5 only for now (need implementation that doesn't use typeString)."
  [txt]
  (fail-if-not (keyboard-visible?) "Keyboard not visible")
  (.typeString (utils/keyboard) txt))

(defn enter
  "Taps the enter button on keyboard (which must be visible). Note: iOS4.x?"
  []
  (fail-if-not (keyboard-visible?) "Keyboard not visible")
  (.typeString (utils/keyboard) "\n"))


(defn screenshot
  [name]
  (.captureScreenWithName (utils/target) name))


(comment
  (define-uia-test
    "user should be able to log in"
    (fn []
      (tap [:textField {:marked "Name"}])
      (keyboard-enter-text "Karl.Krukow@gmail.com")
      (enter)
      (screenshot "Menu")
      (tap [:button {:marked "Second"}])
      (sleep 0.3)
      (screenshot "Map"))))




(comment
  (define-uia-test
    "I can reorder cells"
    (fn []
      (tap [:view {:marked "Third"}])
      (log-query [:tableCell {:marked "Cell 0"} :button])
      (log-query [:tableCell {:marked "Cell 2"} :button])

      (pan [:tableCell {:marked "Cell 0"} :button]
           [:tableCell {:marked "Cell 2"} :button])
      )))


  (define-uia-test
    "web"
    (fn []
      (log-query [:view])
      (sleep 3)
      ))

(run-all!)
