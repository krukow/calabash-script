(ns calabash-script.utils)


(defn target
  []
  (.localTarget js/UIATarget))

(defn app
  []
  (.frontMostApp (target)))

(defn windows
  []
  (seq (.toArray (.windows (app)))))

(defn window
  []
  (let [main (.mainWindow (app))]
    (if (.hitpoint main)
      main
      (first (filter #(.hitpoint %) (windows))))))

(defn keyboard
  []
  (.keyboard (app)))

(defn rounded-map
  [cljmap & keys]
  (let [round-key (fn [cljmap key]
                    (if-let [kv (get cljmap key)]
                      (assoc cljmap key (.round js/Math kv))
                      cljmap))]
    (reduce round-key cljmap (cons :x (cons :y keys)))))

(defn uia-point
  [cljmap & keys]
  (clj->js (apply rounded-map cljmap keys)))

(def ^:dynamic *screenshot-count* (atom 1))

(defn screenshot
  "Take a screenshot with a given name"
  [name]
  (let [cnt @*screenshot-count*]
    (when cnt
      (swap! *screenshot-count* inc))
    (.captureScreenWithName (target) (str name (or cnt "")))))

(def ^:dynamic *localizations*
  {:keyboard {:return {:en "return" :da "retur"}}})

(defn localization-for
  "Returns a localized version of the message (string or keyword)"
  [domain msg]
  (let [lang (keyword (or (first (.preferencesValueForKey
                                 (app)
                                 "AppleLanguages"))
                         "en"))
        ]
    (get-in *localizations* [domain msg lang])))


(defn normalize
  "Normalize a UIAElement by converting UIAElementNil to nil"
  [uia-el]
  (if (instance? js/UIAElementNil uia-el)
    nil
    uia-el))
