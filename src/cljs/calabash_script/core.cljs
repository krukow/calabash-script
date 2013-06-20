(ns calabash-script.core
  (:use [calabash-script.query :only [-query dir-seq valid?]]
        [calabash-script.tests :only [define-uia-test run-all! fail fail-if-not]])

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
  "Waits for x seconds"
  [x]
  (.delay (utils/target) x))


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
                       (fail message (and screenshot "timeout")))
                     res)
                   actions)
        result (first results)]
    (when (> post-timeout 0)
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




;;;; Helpers ;;;;

(defn perform-on
  [action & args]
  (if-let [res (seq (apply query-el args))]
    (action res)
    (fail (apply str "No results for query " args))))

(defn perform-on-first
  [action & args]
  (apply perform-on (fn [els] (action (first els))) args))


;;;; Gestures ;;;;

(defn tap
  "Taps the object which results from running query given by args. Fails if this query gives no results."
  [& args]
  (apply perform-on-first #(.tap %) args))

(defn double-tap
  [& args]
  (apply perform-on-first #(.doubleTap %) args))

(defn two-finger-tap
  [& args]
  (apply perform-on-first #(.twoFingerTap %) args))

(defn pan
  "Pan from result of one query to that of another"
  [src-query tgt-query & kwargs]
  (let [{:keys [duration]
         :or   {duration 1}} kwargs
         src (first (seq (query src-query)))
         tgt (first (seq (query tgt-query)))]
    (fail-if-not (and src tgt)
                 (apply str "Unable to find results for both of " src-query tgt-query))
    (.dragFromToForDuration (utils/target)
                            (utils/clj->js (:hit-point src))
                            (utils/clj->js (:hit-point tgt))
                            duration)))

(defn scroll-to
  "Scroll to make an element visible"
  [& args]
  (apply perform-on-first #(.scrollToVisible %) args))

(defn touch-hold
  [duration & args]
  (apply perform-on-first #(.touchAndHold % duration) args))


;;; Alerts ;;;

(def ^:dynamic *alert-handlers*
  (atom (list (constantly true)))) ;;default is do nothing

(defn set-alert-handler!
  [handler]
  (aset (utils/target) "onAlert" handler))

(defn add-alert-handler!
  [handler]
  (swap! *alert-handlers* #(cons handler %)))

(set-alert-handler!
 (fn [alert]
   (let [handler-values (map (fn [h] (h alert)) @*alert-handlers*)]
     (first (filter true? handler-values)))))

(defn alert
  []
  (utils/normalize (.alert (utils/app))))

(defn alert-texts
  [])



;;; Keyboard ;;;
(defn keyboard-visible?
  []
  (valid? (utils/keyboard)))

(defn keyboard-enter-text
  "Enters a string of characters using the iOS keyboard. Fails if no keyboard is visible. iOS5+ only for now (need implementation that doesn't use typeString)."
  [txt]
  (fail-if-not (keyboard-visible?) "Keyboard not visible")
  (.typeString (utils/keyboard) txt))

(defn enter
  "Taps the enter button on keyboard (which must be visible). Note: iOS4.x?"
  []
  (fail-if-not (keyboard-visible?) "Keyboard not visible")
  (.typeString (utils/keyboard) "\n"))

;;

;; Helpers

(defn tap-mark
  [mark]
  (tap [:view {:marked mark}]))

(defn element-exists?
  [& args]
  (boolean (seq (apply query args))))

(defn element-does-not-exist?
  [& args]
  (not (apply element-exists? args)))

(defn check-element-exists
  [& args]
  (when-not (apply element-exists? args)
    (fail "Element does not exist: " (apply str args))))

(defn check-element-does-not-exist
  [& args]
  (when (apply element-exists? args)
    (fail "Element does exist: " (apply str args))))

(defn names
  "Returns the :name of all elements matching query"
  [& args]
  (map :name (apply query args)))

(defn set-location [location]
  (.setLocation (utils/target) (utils/clj->js location)))


(comment
  (define-uia-test
    "user should be able to log in"
    (fn []
      (tap [:textField {:marked "Name"}])
      (keyboard-enter-text "Karl.Krukow@gmail.com")
      (enter)
      (utils/screenshot "Menu")
      (tap [:button {:marked "Second"}])
      (sleep 0.3)
      (utils/screenshot "Map"))))



(comment
  (define-uia-test
    "I can reorder cells"
    (fn []
      (tap [:view {:marked "Third"}])
      (log-query [:tableCell {:marked "Cell 0"} :button])
      (log-query [:tableCell {:marked "Cell 2"} :button])

      (pan [:tableCell {:marked "Cell 0"} :button]
           [:tableCell {:marked "Cell 2"} :button])
      ))
)

(comment

  (define-uia-test
    "web scroll"
    (fn []
      (tap [:view {:marked "Fourth"}])
      (scroll-to [:link])
      (utils/screenshot "link")
      (tap [:link])
      (utils/screenshot "link")
      )))


(comment
  (define-uia-test
    "wait"
    (fn []
      (let [cnt (atom 0)]
        (tap [:view {:marked "Third"}])
        (wait-for {:retry-frequency 0.5 :timeout 10 :post-timeout 10
                   }
                  (fn []
                    (log/log "Call..")
                    (if (= @cnt 10)
                      true
                      (do
                        (swap! cnt inc)
                        false))))))))


(comment
  (define-uia-test
    "alert"
    (fn []
      (log/log (alert)))))

;(run-all!)
