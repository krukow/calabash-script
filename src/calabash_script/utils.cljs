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

;;https://gist.github.com/1141054
(defn map->obj
  "Convert a clojure map into a JavaScript object"
  [obj]
  (.strobj (into {} (map (fn [[k v]]
                           (let [k (if (keyword? k) (name k) k)
                                 v (if (keyword? v) (name v) v)]
                             (if (map? v)
                               [k (map->obj v)]
                               [k v])))
                         obj))))
