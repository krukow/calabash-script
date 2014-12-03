(ns calabash-script.gestures)

;; Helper functions for computing coordinates to gestures

(def swipe-deltas
  {:light  {:horizontal {:dx 75 :dy -1}
            :vertical   {:dx -1 :dy 75}}
   :normal {:horizontal {:dx 100 :dy -1}
            :vertical   {:dx -1 :dy 100}}
   :strong  {:horizontal {:dx 150 :dy -1}
             :vertical   {:dx -1 :dy 150}}
   :hard  {:horizontal {:dx 300 :dy -1}
             :vertical   {:dx -1 :dy 300}}})

(defn select-swipe-delta
  [options]
  (if-let [swipe-delta (:swipe-delta options)]
    swipe-delta
    (let [delta-key (or (:force options) :normal)]
      (swipe-deltas delta-key))))

(defn calculate-swipe-to-offset
  [{:keys [x y]} options]
  (let [swipe-delta (select-swipe-delta options)
        base-movement
        (case (:direction options)
          :left
          {:x (- x (get-in swipe-delta [:horizontal :dx]))
           :y (+ y (get-in swipe-delta [:horizontal :dy]))}

          :right
          {:x (+ x (get-in swipe-delta [:horizontal :dx]))
           :y (+ y (get-in swipe-delta [:horizontal :dy]))}

          :up
          {:x (+ x (get-in swipe-delta [:vertical :dx]))
           :y (- y (get-in swipe-delta [:vertical :dy]))}

          :down
          {:x (+ x (get-in swipe-delta [:vertical :dx]))
           :y (+ y (get-in swipe-delta [:vertical :dy]))})]
    (merge-with + base-movement
                  {:x (get options :dx 0)
                   :y (get options :dy 0)})))


(def pinch-delta  {:in  {:x 25 :y -25}
                   :out {:x -25 :y 25}})

(defn calculate-pinch-offset
  [in-or-out offset options]
  (merge-with + offset (get pinch-delta  in-or-out)))

(def drag-inside-delta  {:left  {:x -0.40  :y 0.02}
                         :right {:x 0.40 :y 0.02}
                         :up    {:x 0.02  :y -0.40}
                         :down  {:x 0.02  :y 0.40}})

(defn drag-inside-offset
  [dir start-offset]
  (merge-with + start-offset (get drag-inside-delta dir)))
