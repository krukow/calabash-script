(ns calabash-script.tests
  (:require [calabash-script.log :as log]
            [calabash-script.utils :as utils]))


(defn fail
  [reason & kwargs]
  (let [{:keys [screenshot]
         :or   {screenshot "screenshot"}} (apply hash-map kwargs)]
    (when screenshot
      (utils/screenshot screenshot))
    (throw (new js/Error reason))))

(defn fail-if [condition & kwargs]
  (let [{:keys [message screenshot]
         :or   {message "Failed"
                screenshot "screenshot"}} (apply hash-map kwargs)]
    (if condition (fail message screenshot))))

(defn fail-if-not [x & kwargs]
  (apply fail-if (not x) kwargs))

(def ^:dynamic *uia-tests* (atom {}))

(defn define-uia-test
  [doc test]
  (swap! *uia-tests* assoc doc test))

(defn run-all!
  []
  (log/log "Running tests: " (keys @*uia-tests*))
  (doseq [[name test] @*uia-tests*]
    (.logStart js/UIALogger name)
    (try
      (test)
      (swap! *uia-tests* assoc name {:result :pass})
      (.logPass js/UIALogger name)
      (catch js/Object e
        (swap! *uia-tests* assoc name {:result :fail :message (str e)})
        (.logFail js/UIALogger name))))
  (log/log @*uia-tests*))
