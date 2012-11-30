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
    (.captureScreenWithName (utils/target) (str name (or cnt "")))))

(def ^:dynamic *localizations*
  {:keyboard {:return {:en "return" :da "retur"}}})

(defn localization-for
  "Returns a localized version of the message (string or keyword)"
  [domain msg]
  (let [lang (keyword (or (first (.preferencesValueForKey
                                 (utils/app)
                                 "AppleLanguages"))
                         "en"))
        ]
    (get-in *localizations* [domain msg lang])))

;;https://github.com/ibdknox/jayq/blob/master/src/jayq/util.cljs
(defn clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings."
  [x]
  (cond
    (string? x) x
    (keyword? x) (name x)
    (map? x) (let [obj (js-obj)]
               (doseq [[k v] x]
                 (aset obj (clj->js k) (clj->js v)))
               obj)
    (coll? x) (apply array (map clj->js x))
    :else x))
