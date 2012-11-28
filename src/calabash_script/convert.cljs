(ns calabash-script.convert)

(defn point->map
  [rect]
  (if-let [point (.-origin rect)]
    {:x (.-x point) :y (.-y point)}
    {}
    ))

(defn size->map
  [rect]
  (if-let [size (.-size rect)]
    {:width (.-width size) :height (.-height size)}
    {}))

(defn uia->map
  [uia-el]
  (let [rect (if-let [rect (.rect uia-el)]
               (merge (point->map rect)
                      (size->map rect))
               {})
        hp (if-let [point (.hitpoint uia-el)]
             {:x (.-x point) :y (.-y point)}
             {})]
    {:el uia-el
     :hit-point hp
     :rect rect
     :name (if (.-name uia-el) (.name uia-el) nil)
     :label (if (.-label uia-el) (.label uia-el) nil)}))
