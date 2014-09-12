(ns calabash-script.utils)


(defn target
  []
  (.localTarget js/UIATarget))

(defn app
  []
  (.frontMostApp (target)))

(defn window
  []
  (.mainWindow (app)))

(defn windows
  []
  (seq (.toArray (.windows (app)))))

(defn keyboard
  []
  (.keyboard (app)))


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
