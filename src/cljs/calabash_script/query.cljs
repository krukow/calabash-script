(ns calabash-script.query
  (:require [calabash-script.log :as log]
            [calabash-script.uia :as uia]
            [calabash-script.convert :as c]
            [calabash-script.utils :as utils]))


(def dir? #{:parent :descendant :child})

(defn- desc
  [el acc]
  (if-let [els (seq el)]
    (let [children (mapcat #(desc % []) els)]
      (reduce conj acc (cons el children)))
    (conj acc el)))


(defmulti dir-seq
  "Construct a seq from a direction and an element collection"
  (fn [dir _] dir))


(defmethod dir-seq :parent
  [_ coll]
  (let [no-parent? (fn [el] (or (not (.-parent el))
                               (nil? (.parent el))
                               (instance? js/UIAApplication (.parent el))))
        parents (fn [el acc]
                  (if (no-parent? el)
                    acc
                    (let [p (.parent el)]
                      (recur p (conj acc p)))))]

    (mapcat (fn [x] (parents x [])) coll)))

(defmethod dir-seq :child
  [_ coll]
  (mapcat seq coll))

(defmethod dir-seq :descendant
  [_ coll]
  (mapcat #(desc % []) coll))

(defn valid?
  [el]
  (not (or (nil? el)
           (instance? js/UIAElementNil el))))


(defn filter-by-type
  "Finds elements that are (or inherit from) type"
  [type coll]
  (let [uia-class (uia/classes type)]
    (filter (fn [x]
              (and
               (valid? x)
               (instance? uia-class x)))
            coll)))

(defn matches?
  [sel obj val]
  (cond
   (or (keyword? sel) (string? sel))
   (let [kw-sel (keyword sel)]
     (if (= :marked kw-sel)
       (or (= (.name obj) val)
           (= (.label obj) val)
           (and (.-value obj)
                (= (.value obj) val)))

       (let [res (.withValueForKey obj val (name kw-sel))]
         (valid? res))))
   (vector? sel)
   (case (count sel)
     2  ;;simple predicate
     (let [key (name (first sel))
           rel (name (second sel))]
       (valid? (.withPredicate obj (str key " " rel " " val))))

     nil
     )
   ))


(defn query-map
  [q coll dir]
  (let [filter-fn
          (fn [x]
            (every?
             true?
             (map (fn [[sel val]]
                    (matches? sel x val))
                  q)
             ))]
    (filter filter-fn coll)))



(defprotocol IQuery
  (-query [q coll dir]
    "Perform query q wrt collection and current direction dir"))

(extend-protocol IQuery
  Keyword
  (-query [kw coll dir]
    (filter-by-type kw (dir-seq dir coll)))

  Symbol
  (-query [s coll dir]
    (-query (keyword s) coll dir))

  js/String
  (-query [kw coll dir]
    (filter-by-type (keyword kw) (dir-seq dir coll)))

  PersistentVector
  (-query [q coll dir]
    (first
     (reduce (fn [[coll dir] next]
               (if (dir? next)
                 [coll next]
                 [(-query next coll dir) dir]))
             [coll dir]
             q)))

  ObjMap
  (-query [q coll dir]
    (query-map q coll dir))

  PersistentArrayMap
  (-query [q coll dir]
    (query-map q coll dir))

  PersistentHashMap
  (-query [q coll dir]
    (query-map q coll dir)))
