(ns calabash-script.core
  (:use [calabash-script.query :only [-query dir-seq valid?]]
        [calabash-script.tests :only [define-uia-test run-all! fail fail-if-not]])

  (:require [calabash-script.log :as log]
            [calabash-script.uia :as uia]
            [calabash-script.convert :as c]
            [calabash-script.utils :as utils]
            [calabash-script.gestures :as g])
  (:require-macros [calabash-script.macros.uia :as m]))

(declare find-first-picker)

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
         screenshot false}}
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

(defn log-tree-query [& args]
  "Logs the tree of the result of a query."
    (when-let [res (seq (apply query-el args))]
      (log/logTree (first res))))

(defn log-tree-mark [mark]
  "Logs the tree of the result of a query for mark."
    (when-let [res (seq (query-el [:view {:marked mark}]))]
      (log/logTree (first res))))


;;;; Helpers ;;;;

(defn wait-for-elements-ready
  [{message :message
    :as wait-opts
    :or {message (str "Timed out waiting for " args)}} & args]
  (wait-for
   (merge wait-opts {:message message})
    #(when-let [els (seq (apply query-el args))]
       (every? (fn [el] (and (.isVisible el) (.isValid el))) els))))

(defn wait-for-mark-ready
  [{message :message
    :as wait-opts
    :or {message (str "Timed out waiting for " mark)}}
   mark]
  (wait-for-elements-ready (merge wait-opts {:message message})
                           [:view {:marked mark}]))


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

(defn double-tap-offset
  [offset]
  (.doubleTap (utils/target) (utils/uia-point offset)))

(defn two-finger-tap
  [& args]
  (apply perform-on-first #(.twoFingerTap %) args))

(defn two-finger-tap-offset
  [offset]
  (.twoFingerTap (utils/target) (utils/uia-point offset)))

(defn flick-offset
  [from to]
  (.flickFromTo (utils/target) (utils/uia-point from) (utils/uia-point to)))

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
                            (clj->js (:hit-point src)) ;already UIA Point
                            (clj->js (:hit-point tgt)) ;already UIA Point
                            duration)))

(defn pan-offset
  "Pan from result of one query to that of another"
  [src-offset tgt-offset & [options]]
  (let [{:keys [duration]
         :or   {duration 0.75}} options]
    (.dragFromToForDuration (utils/target)
                            (utils/uia-point src-offset)
                            (utils/uia-point tgt-offset)
                            duration)))



(defn default-offset
  []
  (let [hp  (.hitpoint (utils/window))]
    {:x (.-x hp) :y (.-y hp)}))

(defn swipe
  [dir query-spec & [options]]
  (let [{:keys [startOffset endOffset duration]
         :or {startOffset {:x 0.5, :y 0.5}
              duration 0.5}
         :as defaults} options
         end-offset (or endOffset (g/drag-inside-offset (keyword dir) startOffset))
         duration (or duration 0.5)]
    (perform-on-first
     #(.dragInsideWithOptions % (clj->js (assoc defaults
                                           :startOffset
                                           (utils/rounded-map startOffset)
                                           :duration
                                           duration
                                           :endOffset
                                           (utils/rounded-map end-offset))))
      query-spec)))

(defn swipe-mark
  [dir mark & args]
  (apply swipe dir [:view {:marked mark}] args))

(defn swipe-offset
  [offset & [options]]
  (let [offset (or offset (default-offset))
        from-offset offset
        to-offset (g/calculate-swipe-to-offset offset options)
        duration (or (:duration options) 0.5)]
    (.dragFromToForDuration (utils/target)
                            (utils/uia-point from-offset)
                            (utils/uia-point to-offset)
                            duration)))

(defn pinch [in-or-out q & [options]] (throw (js/Error.)))

(defn pinch-offset
  [in-or-out offset & [options]]
  (let [offset (or offset (default-offset))
        from-offset offset
        to-offset (g/calculate-pinch-offset in-or-out offset options)
        duration (or (:duration options) 0.5)
        js-from-offset (utils/uia-point from-offset)
        js-to-offset (utils/uia-point to-offset)]
    (case in-or-out
      :in
      (.pinchOpenFromToForDuration (utils/target) js-from-offset js-to-offset duration)

      :out
      (.pinchCloseFromToForDuration (utils/target) js-from-offset js-to-offset duration))))



(defn scroll-to
  "Scroll to make an element visible"
  [& args]
  (apply perform-on-first #(.scrollToVisible %) args))

(defn touch-hold
  [duration & args]
  (apply perform-on-first #(.touchAndHold % duration) args))


(defn touch-hold-offset
  [duration offset]
  (.touchAndHold (utils/target) (utils/uia-point offset) duration))

(defn select-picker-values
  [values]
  (log/log "Picker select values" values)
  (if-let [picker (find-first-picker)]
    (let [wheels (.toArray (.wheels picker))]
      (doseq [[index value] values]
        (.selectValue (nth wheels index) value)))
    (throw (js/Error. "No visible picker"))))

(def orientations
  {js/UIA_DEVICE_ORIENTATION_UNKNOWN :unknown
   js/UIA_DEVICE_ORIENTATION_PORTRAIT :portrait
   js/UIA_DEVICE_ORIENTATION_PORTRAIT_UPSIDEDOWN :portrait-upside-down
   js/UIA_DEVICE_ORIENTATION_LANDSCAPELEFT :landscape-left
   js/UIA_DEVICE_ORIENTATION_LANDSCAPERIGHT :landscape-right
   js/UIA_DEVICE_ORIENTATION_FACEUP :faceup
   js/UIA_DEVICE_ORIENTATION_FACEDOWN :facedown})

(def rotatable-orientations
  [js/UIA_DEVICE_ORIENTATION_PORTRAIT
   js/UIA_DEVICE_ORIENTATION_LANDSCAPERIGHT ;portrait rotated clockwise
   js/UIA_DEVICE_ORIENTATION_PORTRAIT_UPSIDEDOWN ;landscaperight rotated clockwise
   js/UIA_DEVICE_ORIENTATION_LANDSCAPELEFT]) ;...

(def clockwise-orientations
  (partition 2 1 (cycle rotatable-orientations)))

;;((:portrait :landscape-right) ;; clockwise of :portrait is :landscape-right
;; (:landscape-right :portrait-upside-down) ;; ..
;; (:portrait-upside-down :landscape-left))


(defn relative-orientation
  [orientation dir]
  (if (some #(= orientation %) rotatable-orientations)
    (let [dir (case dir
                :left  :counter-clockwise
                :right :clockwise
                dir)
          orientations (if (= :clockwise dir)
                         clockwise-orientations
                         (map reverse clockwise-orientations))]
      (second (first (filter #(= orientation (first %)) orientations))))
    (throw (new js/Error (str "Can't rotate orientation: " orientation)))))

(defn orientation
  []
  (get orientations (.deviceOrientation (utils/target))))

(defn rotate
  [dir]
  (.setDeviceOrientation (utils/target)
                         (relative-orientation
                          (.deviceOrientation (utils/target))
                          dir)))

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

(defn element-with-keyboard-focus
  []
  (first (filter #(= 1 (.hasKeyboardFocus %))
                 (concat
                  (query-el [:textField] (utils/windows))
                  (query-el [:textView] (utils/windows))))))

(defn keyboard-enter-text
  "Enters a string of characters using the iOS keyboard. Fails if no keyboard is visible. iOS5+ only."
  [txt & args]
  (fail-if-not (keyboard-visible?) "Keyboard not visible")
  (if-let [tf (element-with-keyboard-focus)]
    (let [tf-serialized (c/uia->map tf)
          reset-to (or (first args) "")
          locate-key-error-reg #"failed to locate key"
          kb (utils/keyboard)
          num-failed-keys (atom 0)]
      (wait-for {:retry-frequency 1
                 :timeout 60
                 :message (str "Unable to type: " txt)}
                (fn []
                  (try
                    (.typeString kb txt)
                    true
                    (catch js/Error err
                      (do
                        (let [err-str (str err)]
                          (log/log ".typeString failed with error: '" err-str "'")
                          (when (re-find locate-key-error-reg err-str)
                            (swap! num-failed-keys inc)
                            (if (> @num-failed-keys 3)
                              (throw err))))
                        (log/log  "Restoring to text: '" reset-to "'")
                        (.setValue tf reset-to)
                        false)))))
      tf-serialized)
    (.typeString (utils/keyboard) txt)))

(defn enter
  "Taps the enter button on keyboard (which must be visible). Note: iOS4.x?"
  []
  (fail-if-not (keyboard-visible?) "Keyboard not visible")
  (.typeString (utils/keyboard) "\n"))

;;

;; Helpers

(defn deactivate
  [duration]
  (let [max-retry 5]
    (loop [i 0]
      (let [val (.deactivateAppForDuration (utils/target) duration)]
        (if (or val (>= i max-retry))
          val
          (recur (inc i)))))))

(defn tap-mark
  [mark]
  (tap [:view {:marked mark}]))

(defn tap-offset
  [offset & [options]]
  (wait-for {:retry-frequency 0.5
             :timeout 20
             :message (str "Unable to tap-offset: " offset)}
            (fn []
              (try
                (.tapWithOptions (utils/target)
                                 (utils/uia-point offset)
                                 (clj->js {:duration (or (:duration options) 0.1)}))
                true
                (catch js/Error err
                  (do
                    (log/log "tap-offset failed" err)
                    false))))))

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
  (.setLocation (utils/target) (clj->js location)))

(defn find-first-picker
  []
  (let [windows (reverse (utils/windows))]
    (first (mapcat #(query-el [:picker] (list %)) windows))))

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
